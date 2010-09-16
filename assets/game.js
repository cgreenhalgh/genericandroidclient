// NB requires jQuery & jQuery-json
// also requires ../resources/common.js (timeToString)

// run-time context dependencies - for web/browser-based vs lobby client vs app client deployment:
//
// - for browser-based usage, URL parameter queryUrl must be specified; 
//   used to load GameTemplate information (header) and subsequent game instance/factory queries.
//   Browser-based usage on Android CANNOT open the marketplace client (via URL),
//   this can only be done via an Intent. Also CANNOT check if a particular client is installed 
//   (requires native code), or directly open a client application (requires an Intent). 
//   Possible fallback is to open a URL with a custom MIME type which the client is registered
//   to handle from the Browser. But error handling is then not good.
//   Custom URL scheme handling works on Android from browser without attempting to load URL.
//   (If there the 'client' is pure HTML then opening it in a browser window is fine.)
//   iOS seems to support iTunes http: and itms: URLs to at least get to the app store.
//   iOS also supports custom URL schemes linked to apps; not sure about MIME types, yet.
//
//   So best bet for browser-based is to open a URL with a custom scheme registered to the 
//   client App.
//
// - for lobby client usage, there should also be index view/interaction prior to this.
//   A custom javascript bridge could give access to open the marketplace client,
//   check for installed application(s), send custom intents to applications.
//   Alternatively the custom WebView can intercept the openURL requests and handle them
//   appropriately (I assume).
//
// - for app client (embedded) usage, the client is implicitly installed (although version
//   could be checked via native helper).
//   The game template info and icon could/should be a local asset.
//   Query URL will still be remote, so will need to be separate from local info.
//   Hmm. But the same native client (install) might support different games.
//   Re-badge and separate distribution [yes for now]? or integrated game template lobby function?? [no]
//   
//   Does the lobby client just RESERVE a slot, and pass the /lobby/ information to the 
//   game app which then requests to PLAY [no], or does the lobby PLAY and pass the /game/ information
//   to the game app [yes]? [Could be either, but the latter keeps all lobby interaction in the generic
//   lobby client code base.]
//
//   The lobby client can open a custom URL to signal game client handover the same as the 
//   browser-only version.
//
// So... what we are looking for from the embedded browser context is:
//   lobbyclient - defined in embedded context
//     .game - defined in game-specific embedded context
//         .indexJson - JSON-encoded GameIndex object for Game (typically from local resource)
//                      [browser version gets from server queryUrl]
//         .queryUrl - queryUrl to use for searching
//                      [browser version gets from URL parameter]
//         .clientName - optional (for client type query)
//         .appId - for client type query/check
//                      [browser version doesn't know]
//         .appMajorVersion - for client type query/check
//         .appMinorVersion - for client type query/check

// start here...
$.ajaxSetup({cache:false,async:true,timeout:30000});

function currentTimeMillis() {
	// it seems like Date.getTime() might be in the local timezone (which stinks)
	var d = new Date();
	return d.getTime()+d.getTimezoneOffset() * 60000;
}

function get_lobbyclient() {
	try {
		return lobbyclient;
	}
	catch (err) {}
	return undefined;
}

// get game if dedicated lobbyclient
function get_game() {
	if (get_lobbyclient()==undefined)
		return undefined;
	if (lobbyclient.getGame()==null || lobbyclient.getGame()==undefined)
		return undefined;
	return lobbyclient.getGame();
}

// <include>Math.uuid.js (v1.4)
// http://www.broofa.com
// mailto:robert@broofa.com
// Copyright (c) 2010 Robert Kieffer
// Dual licensed under the MIT and GPL licenses.
// A more performant, but slightly bulkier, RFC4122v4 solution.  We boost performance
// by minimizing calls to random()
(function() {
	  // Private array of chars to use
	  var CHARS = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split(''); 
	Math.uuidFast = function() {
	  var chars = CHARS, uuid = new Array(36), rnd=0, r;
	  for (var i = 0; i < 36; i++) {
	    if (i==8 || i==13 ||  i==18 || i==23) {
	      uuid[i] = '-';
	    } else if (i==14) {
	      uuid[i] = '4';
	    } else {
	      if (rnd <= 0x02) rnd = 0x2000000 + (Math.random()*0x1000000)|0;
	      r = rnd & 0xf;
	      rnd = rnd >> 4;
	      uuid[i] = chars[(i == 19) ? (r & 0x3) | 0x8 : r];
	    }
	  }
	  return uuid.join('');
	};
})();
// </include>

var queryUrl = null;
var gameIndex = null;

function uiTimeToString(time) {
	return prettyTimeToLocalString(time);//new Date(time).toString();
}

//date to cookie format time:  Thu, 2 Aug 2001 20:47:11 UTC [~]
var dayOfWeek = ['Sun','Mon','Tue','Wed','Thu','Fri','Sat'];
var month = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
function timeToCookie(time) {
	if (time==0)
		return "Unspecified";
	var date = new Date(time);
	// TZ?
	var str = dayOfWeek[date.getUTCDay()]+', '+date.getUTCDate()+' '+
	month[date.getUTCMonth()]+' '+date.getUTCFullYear()+' '+
	format(date.getUTCHours(),2)+':'+format(date.getUTCMinutes(),2)+':'+format(date.getUTCSeconds(),2)+' UTC';
	return str;
}


function get_cookie_value(name) {
	var cookie = document.cookie;
	if (cookie!=undefined && cookie!=null) {
		var res = new RegExp( name+'=([^;]*)' ).exec(cookie);
		if (res!=null)
			return res[1];
	}
	return undefined;
}

function test_storage() {
	// test WebStorage support - http://www.w3.org/TR/webstorage/
	// works on: Android 2.1
	// fails on: IE7
	try {
		if (window.localStorage==undefined || window.localStorage==null)
			alert('localStorage undefined');
		else {
			var data = window.localStorage.getItem('lobbyService');
			if (data=='some data')
				alert('Hooray - WebStorage worked (already set)');
			else {
				window.localStorage.setItem('lobbyService','some data');
				data = window.localStorage.getItem('lobbyService');
				if (data=='some data')
					alert('Hooray - WebStorage worked');
				else
					alert('Boo - got back '+data+' from WebStorage');
			}
		}
	}
	catch (err) {
		alert('Error trying WebStorage: '+$.toJSON(err));
	}
	// test cookie storage
	try {
		var value = get_cookie_value('lobby.game.js');
		if (value=='some data') 
			alert('cookie already set: '+value);
		else {
			document.cookie = 'lobby.game.js=some data; expires='+timeToCookie(currentTimeMillis()+1000*60*60*24*365)+'; path=/browser/';
			value = get_cookie_value('lobby.game.js');
			if (value=='some data')
				alert('Hooray - cookie worked');
			else
				alert('Boo - got back '+data+' from cookie');
		}		
	}
	catch (err) {
		alert('Cookie storage error: '+$.toJSON(err));
	}
	
	// test Gears DB
//	try {
//		var db = google.gears.factory.create('beta.database');
//		db.open('lobbyService');
//		db.execute('create table if not exists localStorage' +
//		' (VARCHAR(100) key, Text value)');
//		db.execute('insert into localStorage values (?, ?)', ['lobbyService','some data']);
//		var rs = db.execute('select value from localStorage where key = ?', ['lobbyService']);
//		if (rs.isValidRow()) {
//			var data = rs.field(0);
//			if (data=='some data')
//				alert('Hooray - Gears Database worked');
//			else
//				alert('Boo - got back '+data+' from Gears Database');
//		}
//		else
//			alert('Boo - got nothing back from Gears Database');
//		rs.close();	         
//	}
//	catch (err) {
//		alert('Error trying GoogleGears Database: '+$.toJSON(err));
//	}

}

// fallback to non-persistent
var persistent_cache = {};
var persistence_type = undefined;
var key_prefix = 'game.js.';
var myLocalStorage;

function init_persistence() {
	if (window.localStorage!=undefined) {
		// W3C WebStorage
		myLocalStorage = window.localStorage;
		persistence_type = 'WebStorage';
	}
	else if (get_lobbyclient()!=undefined) {
		if (lobbyclient.getLocalStorage()!=undefined && lobbyclient.getLocalStorage()!=null) {
			myLocalStorage = lobbyclient.getLocalStorage();
			persistence_type = 'Lobbyclient';
		}
	}
	else {
		var cookies_ok = false;
		try {
			// 1 year
			// TODO fix: illegal format for expires: Fri Sep 16 2011 10:29:03 GMT+0000 (GMT)
			// -> Thu, 2 Aug 2001 20:47:11 UTC [~]
			document.cookie = key_prefix+'test=ok; expires='+timeToCookie(currentTimeMillis()+1000*60*60*24*365)+'; path=/browser/';
			cookies_ok = true;
		} catch (err) {
		}
		if (cookies_ok) {
			// try cookies
			myLocalStorage = {};
			myLocalStorage.getItem = function(key) {
				var val = get_cookie_value(key_prefix+key);
				if (val==null || val==undefined)
					return val;
				return decodeURIComponent(val);
			}
			myLocalStorage.setItem = function(key,value) {
				// 1 year
				// TODO fix: illegal format for expires: Fri Sep 16 2011 10:29:03 GMT+0000 (GMT)
				// -> Thu, 2 Aug 2001 20:47:11 UTC [~]
				document.cookie = key_prefix+key+'='+encodeURIComponent(value)+'; expires='+timeToCookie(currentTimeMillis()+1000*60*60*24*365)+'; path=/browser/';
				return;
			}
			persistence_type = 'Cookies';
		}
		else {
			// fallback to transient
			myLocalStorage = {};
			myLocalStorage.getItem = function(key) {
				return persistent_cache[key];
			}
			myLocalStorage.setItem = function(key, value) {
				persistent_cache[key] = value;
			}
		}
	}
}


// get a persistent value 
function set_persistent_string(key, value) {
	myLocalStorage.setItem(key, value);
}

function get_persistent_string(key) {
	return myLocalStorage.getItem(key);
}

// get client ID - hopefully persistent; make if unknown.
function get_client_id() {
	var client_id_key = 'clientId';
	var client_id = get_persistent_string(client_id_key);
	if (client_id==undefined || client_id==null) {
		client_id = Math.uuidFast();
		set_persistent_string(client_id_key, client_id);
		alert('Creating new client ID ('+client_id+') - will not have access to previous games');
	}
	return client_id;
}

//http://www.netlobo.com/url_query_string_javascript.html
function gup( name ) {  
	name = name.replace(/[\[]/,"\\\[").replace(/[\]]/,"\\\]");  
	var regexS = "[\\?&]"+name+"=([^&#]*)";  
	var regex = new RegExp( regexS ); 
	var results = regex.exec( window.location.href ); 
	if( results == null )   
		return "";  
	else    
		return results[1];
}
function update_game_index(index) {
	var table = $('#game');
	gameIndex = index;
	$('tr',table).remove();
	if (index==null) {
		alert('Sorry - Index is null');
		return;
	}
	var image = '';
	if (index.imageUrl!=undefined) {
		image = '<img src="'+index.imageUrl+'" alt="image"/>';
	}
	table.append('<tr><td width="64px">'+image+'</td><td>'+
			'<h2><a href="'+index.link+'">'+index.title+'</a></h2>'+
			'<p>'+index.description+'</p>'+
	'</td></tr>');
}

function error_game_index(msg) {
	gameIndex = null;
	var table = $('#game');
	$('tr',table).remove();
	table.append('<tr><td><input class="queryOption" type="button" value="Try again" onclick="load_game_index()"/></td></tr>');
	alert('Sorry, there was a problem getting game information ('+msg+')');
}

function load_game_index() {
	// embedded game-specific version?
	if (get_game()!=undefined) {
		try {
			// the Java string doesn't work with parseJSON ?! :-(
			var index = $.parseJSON(String(get_game().getIndexJson()));
			//alert('index='+$.toJSON(index)+' from '+get_game().getIndexJson());
			update_game_index(index);
			return;
		}
		catch (err) {
			error_game_index(err.message); //$.toJSON(err)+' with '+get_game().getIndexJson());
			return;
		}
	}
	var table = $('#game');
	$('tr',table).remove();
	table.append('<tr><td>Loading...</td></tr>');
	var data = {version:1,maxResults:0};
	try {
		$.ajax({url: queryUrl, 
			type: 'POST',
			contentType: 'application/json',
			processData: false,
			data: $.toJSON(data),
			dataType: 'json',
			success: function success(data, status) {
			update_game_index(data);
		},
		error: function error(req, status) {
			error_game_index(req.status);
//			alert('Error doing query ('+req.status+': '+req.statusText+')');
		}
		});
	} catch (err) {
		error_game_index(err.message);//$.toJSON(err));
//		alert('Error attempting query on '+item.queryUrl+': '+err);
	}
}

function append_instance_table_item(table, item, ix) {
	var image = '';
	if (item.imageUrl!=undefined) {
		image = '<img src="'+item.imageUrl+'" alt="image"/>';
	}
	var title = '<h3>'+item.title+'</h3>';
	if (ix!=undefined && item.joinUrl!=undefined) 
		title = '<h3><input class="queryOption" type="button" value="Play: '+item.title+'" onclick="join_game('+ix+')"/>'+/*item.title+*/'</h3>';
	if (ix!=undefined && item.newInstanceUrl!=undefined) 
		title = '<h3><input class="queryOption" type="button" value="Create: '+item.title+'" onclick="create_game('+ix+')"/>'+/*item.title+*/'</h3>';
	var subtitle = '';
	if (item.subtitle!=undefined) 
		subtitle = '<h4>'+item.subtitle+'<h4>';
	var times = '';
	if (item.startTime!=undefined) {
		times = '<p>'+uiTimeToString(item.startTime)+'-'+uiTimeToString(item.endTime)+'</p>';
	}
	if (item.locationName!=undefined) {
		times = times+'<p>At '+item.locationName+'</p>';	    			
	} else if (item.radiusMetres!=undefined && item.radiusMetres!=0) {
		times = times+'<p>At '+item.latitudeE6+','+item.longitudeE6+'</p>';
	}
	if (item.firstStartTime!=undefined) {
		times = times+'<p>Next: '+uiTimeToString(item.firstStartTime)+'</p>';
	}
	if (item.startTimeCron!=undefined) {
		// hope firstStartTime shown
		times = times+'<p>Every: '+item.startTimeCron+' until '+uiTimeToString(item.maxTime)+'</p>';
	}
	var clients = '';
	for (var ci=0; item.clientTemplates!=undefined && ci<item.clientTemplates.length; ci++) {
		var ct = item.clientTemplates[ci];
		clients = clients+'<p>'+ct.title;
		//+'<br>('+ct.clientType+' '+ct.minMajorVersion+'.'+ct.minMinorVersion+'.'+ct.minUpdateVersion+')';
		if (ct.locationSpecific)
			clients = clients+' (Located!)';
		clients = clients+'</p>';
	}
	table.append('<tr><td>'+
			title+subtitle+
			times+
			clients+
			'</td></tr>');							
}
var instanceIndex = null;
function update_instance_index(index) {
	//alert('got: '+$.toJSON(index));
	var table = $('#list');
	instanceIndex = index;
	$('tr',table).remove();
	// items only
	if (index.items!=undefined && index.items!=null) {
		table.append('<tr><td>Received '+index.items.length+' response(s):</td></tr>');
		for (var i=0; i<index.items.length; i++) {
			var item = index.items[i];
			append_instance_table_item(table, item, i);
		}
	} 
	else 
		table.append('<tr><td>Did not find any matching games</td></tr>');
}

var coords = null;
var os = null;
var searching = false;

function update_search_disabled() {
	// should search be disabled?
	var disabled = true;
	if (queryUrl!=null && os!=null && !searching) {
		// location
		if (coords!=null)
			disabled = false;
		else if ($('select[name=rangeOption]').attr('value')=='7')
			// NB the select value is updated on android before change, but the 
			// selected on the option is not
			disabled = false;
	}
	$('input[name=search]').attr('disabled',disabled);
}

function get_location() {
	var tr = $('#locationStatus');
	tr.html('checking location API...');
	// Try W3C Geolocation  
	if(navigator.geolocation) {  
		tr.html('checking location (W3C geolocation)...');
		navigator.geolocation.getCurrentPosition(function(position) {  
			// success  
			tr.html('Latitude: ' + position.coords.latitude + '<br/>Longitude: ' + position.coords.longitude);  
			coords = position.coords;
			update_search_disabled();
		}, function(position_error) {  
			// failure  
			tr.html('Sorry - there was a problem ('+position_error+')');
		}, {  
			// options  
			enableHighAccuracy: true 
		});  
	} else {  
		tr.html('Sorry - no location API');
	}  		
}

function check_os_name(prefix) {
	var all = new RegExp( prefix+'[^;\\)]*' );
	var res = all.exec( navigator.appVersion );
	if (res!=null) {
		var v2 = new RegExp('(([ ]*[^ 0-9][^ ]*)*)[ ]*(\\d+)[.](\\d+)(.*)').exec(res[0]);
		if (v2!=null)
			return [v2[1],v2[3],v2[4],v2[5]];
		var v1 = new RegExp('(([ ]*[^ 0-9][^ ]*)*)[ ]*(\\d+)(.*)').exec(res[0]);
		if (v1!=null)
			return [v1[1],v2[3],v2[4],''];
		return [res[0],'','',''];
	}
	return null;
}
function get_os_name() {
	// e.g. ....; Windows NT 5.1; ...
	// e.g. ...; Android 2.1; ...
	// Win Mac X11 Linux apparently are good substrings
	var os;
	os = check_os_name('Android');
	if (os==null)
		os = check_os_name('Win');
	if (os==null)
		os = check_os_name('Mac');
	// make sure Linux is checked after Android!
	if (os==null)
		os = check_os_name('Linux');
	if (os==null)
		os = check_os_name('X11');
	return os;
}

function show_list() {
	$('#query_div').hide('fast');
	$('#list_div').show('fast');
	$('#create_div').hide('fast');
	$('#join_div').hide('fast');
}
function show_query() {
	$('#list_div').hide('fast');
	$('#query_div').show('fast');
	$('#create_div').hide('fast');
	$('#join_div').hide('fast');
}
function show_create() {
	$('#list_div').hide('fast');
	$('#query_div').hide('fast');
	$('#create_div').show('fast');
	$('#join_div').hide('fast');
}
function show_join() {
	$('#list_div').hide('fast');
	$('#query_div').hide('fast');
	$('#create_div').hide('fast');
	$('#join_div').show('fast');
}


// on load
$(document).ready(function() {

	//var str = '{"test":"hello","foo":1}';
	//var obj = $.parseJSON(str);
	//alert('JSON: '+str+' -> '+obj+' -> '+$.toJSON(obj));
	
	show_query();
	
	// limit width 
	var width = $(window).width();
	// allow a 'bit' for scroll bars
	if (width>20) {
		//alert('width='+width);
		$('#content').width(width-20);
		$('#content').css('max-width',width-20);
		// on join, response goes too long
	}
	
	// test WebStorage...
	init_persistence();
	//alert('persistnce_type='+persistence_type+', localStorage='+localStorage+', myLocalStorage='+myLocalStorage+', game='+get_game());
	//test_storage();

	$('#clientId').html('Client ID: checking...');
	$('#clientId').html('Client ID: '+get_client_id()+' ('+persistence_type+')');


	//alert('width='+$('body').innerWidth()+',height='+$('body').innerHeight());
	//var width = $('body').innerWidth();
	//if (width > 
	// embedded game-specific version?
	if (get_game()!=undefined) {
		queryUrl = get_game().getQueryUrl();
	}
	else {
		// browser-base
		queryUrl = decodeURIComponent(gup('queryUrl'));
	}
	if (queryUrl==null || queryUrl=='') {
		$('#query_div').hide('fast');
		alert('Sorry - no queryUrl');
		return;
	}

	load_game_index();

	// can we get location?
	get_location();

	// check client?
	var client = $('#client');
	os = get_os_name();
	if (os==null)
		client.html('Unknown operating system');
	else
		client.html('Client: '+os[0]+' '+os[1]+' '+os[2]+' '+os[3]);			
});

// Query options
function get_time_constraint() {
	//var timeOptions = ['Now','Today','Tomorrow','Within a week', 'Any Time'];
	var timeConstraint = {};
	var includeStarted = $('input[name=includeStarted]').attr('checked');
	timeConstraint.includeStarted = includeStarted!=null && includeStarted!='';

	var now = currentTimeMillis();
	var timeOptions = ['Now','Today','Tomorrow','Within a week','Any Time'];
	// stop at 2am? Local time
	var midnight = new Date(now);
	midnight.setHours(0); midnight.setMinutes(0); midnight.setSeconds(0); midnight.setMilliseconds(0);
	var timeOfDay = now-midnight;
	var timeOptionMin = [now, now, now-timeOfDay+24*60*60*1000, now, now];
	var timeOptionMax = [now+5*60*1000, now-timeOfDay+24*60*60*1000, now-timeOfDay+2*24*60*60*1000, 
	                     now-timeOfDay+7*24*60*60*1000, 0];
	var timeOptionIx = Number($('select[name=timeOption] > option[selected]').attr('value'));
	timeConstraint.minTime = timeOptionMin[timeOptionIx];
	if (timeOptionMax[timeOptionIx]!=0) {
		timeConstraint.maxTime = timeOptionMax[timeOptionIx];
	}				        	
	return timeConstraint;
}
function get_location_constraint() {	
	var rangeOptions = ['Here!', 'Up to 1.5 km', 'Up to 5 km', 'Up to 15 km', 'Up to 50 km', 'Up to 150 km', 'Up to 500 km', 'Anywhere'];
	var rangeOptionMetres = [0, 1500, 5000, 15000, 50000, 150000, 500000, -1];
	var rangeOptionIx = Number($('select[name=rangeOption] > option[selected]').attr('value'));
	var locationConstraint = {};
	if (rangeOptionIx<7 && coords!=null) {
		// not anywhere
		locationConstraint = {
				type:'CIRCLE',
				radiusMetres:rangeOptionMetres[rangeOptionIx]
		};
		locationConstraint.latitudeE6 = Math.round(coords.latitude*1000000);
		locationConstraint.longitudeE6 = Math.round(coords.longitude*1000000);	    		    	
	}
	return locationConstraint;	    	
}

// do search 
function do_query() {
	var query = {version:1};
	query.clientId = get_client_id();
	if (os!=null && $('input[name=includeClient]').attr('checked')==true) {
		query.clientType = os[0];
		if (os[1].length>0)
			query.majorVersion = Number(os[1]);
		if (os[2].length>0)
			query.minorVersion = Number(os[2]);
	} 
	if (coords!=null) {
		query.latitudeE6 = Math.round(coords.latitude*1000000);
		query.longitudeE6 = Math.round(coords.longitude*1000000);
	}
	query.timeConstraint = get_time_constraint();
	query.locationConstraint = get_location_constraint();
	// TODO clientId
	query.deviceId = get_client_id(); // unauthenticated!
	// TODO

	var table = $('#list');
	table.empty();
	table.append('<tr><td>Searching...</td></tr>');
	searching = true;
	show_list();
	update_search_disabled();

	var data = $.toJSON(query);
	try {
		$.ajax({url: queryUrl, 
			type: 'POST',
			contentType: 'application/json',
			processData: false,
			data: data,
			dataType: 'json',
			success: function success(data, status) {
			searching = false;
			update_search_disabled();
			update_instance_index(data);
		},
		error: function error(req, status) {
			searching = false;
			update_search_disabled();
			var table = $('#list');
			table.empty();
			table.append('<tr><td>Error doing search ('+req.status+': '+req.statusText+')</td></tr>');
			alert('Sorry - there was a problem searching');
		}
		});
	} catch (err) {
		searching = false;
		update_search_disabled();
		var table = $('#list');
		table.empty();
		table.append('<tr><td>Error doing search ('+err+')</td></tr>');
		alert('Sorry - there was a problem searching');
	}
}

var instanceItem = null;
// check client for current instanceItem
//function check_client() {
//	if (instanceItem!=null && instanceItem!=undefined) {
//		if (instanceItem.clientTemplates!=undefined && instanceItem.clientTemplates.length>0 && instanceItem.clientTemplates[0].applicationLaunchId!=undefined) {
//			var app = instanceItem.clientTemplates[0].applicationMarketId;
//			// won't work on android
//			window.open(app,'client_check','');
//		}
//	}
//}
function getGameClientTemplate() {
	if (instanceItem==undefined || instanceItem==null)
		return undefined;
	if (instanceItem.clientTemplates!=undefined) {
		if (instanceItem.clientTemplates.length>0) {
			if (instanceItem.clientTemplates[0].applicationLaunchId!=undefined) {
				return instanceItem.clientTemplates[0];
			}		
		}
	}
	return undefined;
}

// join specific game
function join_game(ix) {
	instanceItem = instanceIndex.items[ix];
	if (instanceItem!=null && instanceItem!=undefined) {
		var table = $('#join');
		$('tr',table).remove();
		append_instance_table_item(table, instanceItem);
		var disableCheckClient = true;
		var clientUrl = undefined;
		// don't show the check for embedded, game specific.
		// don't show the check if there are no client templates.
		if (get_game()==undefined && getGameClientTemplate()!=undefined) {
			clientUrl = getGameClientTemplate().appMarketUrl;
			if (clientUrl!=undefined && clientUrl!=null)
				disableCheckClient = false;
		}
		table.append('<tr><td><input class="queryOption" type="button" name="do_join" value="Join Game" onclick="do_join()"/>'+
				(disableCheckClient ? '' : '<a href="'+clientUrl+'" target="client_check">Check Client</a>')+
				'</td></tr>');
		show_join();
	}
}
// create (request) new instance for specific game (factory)
function create_game(ix) {
	instanceItem = instanceIndex.items[ix];
	if (instanceItem!=null && instanceItem!=undefined) {
		var table = $('#create');
		$('tr',table).remove();
		append_instance_table_item(table, instanceItem);
		table.append('<tr><td><input class="queryOption" type="button" name="do_create" value="Create Game" onclick="do_create()"/></td></tr>');
		show_create();
	}
}

// create join game request
function get_game_join_request(type) {
	var request = {version:1,type:type};
	//request.seqNo 
	//clientId, deviceId, nickname, deviceId, gameSlotId, type(RESERVE, RELEASE, PLAY, NEW_INSTANCE),
	//clientType, clientTitle, majorVersion, minorVersion,
	// latitudeE6, longitudeE6, newInstanceStartTime, newInstanceVisibility
	request.time = currentTimeMillis();
	request.deviceId = get_client_id(); // unauthenticated!
	if (os!=null) {
		request.clientType = os[0];
		if (os[1].length>0)
			request.majorVersion = Number(os[1]);
		if (os[2].length>0)
			request.minorVersion = Number(os[2]);
	} 
	if (coords!=null) {
		request.latitudeE6 = Math.round(coords.latitude*1000000);
		request.longitudeE6 = Math.round(coords.longitude*1000000);
	}
	return request;
}

// confirm create
function do_create() {
	$('input[name=do_create]').attr('disabled', true);
	var table = $('#create');
	$('tr',table).remove();
	table.append('<tr><td>Sending create game request...</td></tr>');
	var request = get_game_join_request('NEW_INSTANCE');
	// really, we need this!
	if (instanceItem.firstStartTime!=undefined)
		request.newInstanceStartTime = instanceItem.firstStartTime;
	else {
		var msg = 'Sorry - no start time provided';
		table.append('<tr><td>'+msg+'</td></tr>');
		alert(msg);
		return;
	}
	try {
		$.ajax({url: instanceItem.newInstanceUrl, 
    		type: 'POST',
    		contentType: 'application/json',
    		processData: false,
    		data: $.toJSON(request),
    		dataType: 'json',
    		success: function success(data, status) {
				$('input[name=do_join]').attr('disabled', false);
				table.append('<tr><td>'+data.message+'</td></tr>');
				if (data.status=='OK') {
					// re-query! (for now)
					show_query();
					do_query();
					alert('Game created!');
				}
			},
			error: function error(req, status) {
				$('input[name=do_join]').attr('disabled', false);
				var msg = 'Sorry - there was a problem ('+req.status+': '+status+')';
				table.append('<tr><td>'+msg+'</td></tr>');
				alert(msg);
			}
		});
	} catch (err) {
		$('input[name=do_join]').attr('disabled', false);
		var msg = 'Sorry - there was a problem ('+e.name+': '+e.message+')';
		table.append('<tr><td>'+msg+'</td></tr>');
		alert(msg);
	}
}
function addParameter(url, name, value) {
	if (url.indexOf('?')<0)
		url = url+'?';
	else
		url = url+'&';
	url = url+encodeURIComponent(name)+'='+encodeURIComponent(value);
	return url;
}
// play!!
function handle_play_ok(response) {
	try {
		// we need response.playUrl and response.playData ...
		// playData is server-specific, e.g. (exploding places) conversationId, gameId, gameStatus
		// these should all be added to the application Launch Url, which is in the 
		// instanceIndex clientTemplates or in the lobbyclient environment.
		var appLaunchUrl = undefined;
		if (get_game()!=undefined)
			appLaunchUrl = get_game().getAppLaunchUrl();
		else {
			var client = getGameClientTemplate();
			if (client!=undefined) 
				appLaunchUrl = client.appLaunchUrl;
		}
		if (appLaunchUrl==undefined) {
			alert('Sorry - there is not enough information to start the client');
			return;
		}
		if (response.playUrl!=undefined) {
			appLaunchUrl = addParameter(appLaunchUrl, "playUrl", response.playUrl);
		}
		if (response.playData!=undefined) {
			for (var key in response.playData) {
				appLaunchUrl = addParameter(appLaunchUrl, key, response.playData[key]);
			}
		}
		try {
			//alert('try to open '+appLaunchUrl);
			// if http/https assume browser-based and use a new window
			// window.open doesn't seem to be picked up by my WebView at present :-(
			if (get_lobbyclient()!=undefined)
				get_lobbyclient().open(appLaunchUrl);
			else
				window.open(appLaunchUrl,'game_client');
		}
		catch(err) {
			alert('Sorry - could not start game ('+err.message+')');
		}
	} catch (err) {
		alert('Error: '+$.toJSON(err));
	}
}

//confirm join
function do_join() {
	$('input[name=do_join]').attr('disabled', true);
	var table = $('#join');
	$('tr',table).remove();
	table.append('<tr><td>Requesting to join game...</td></tr>');
	var request = get_game_join_request('PLAY');
	try {
		$.ajax({url: instanceItem.joinUrl, 
    		type: 'POST',
    		contentType: 'application/json',
    		processData: false,
    		data: $.toJSON(request),
    		dataType: 'json',
    		success: function success(data, status) {
				$('input[name=do_join]').attr('disabled', false);
				var msg = data.message==undefined ? data.status : data.message;
				table.append('<tr><td>'+msg+'</td></tr>');
				if (data.status=='TRY_LATER') 
					;// no op
				else if (data.status=='OK') {
					handle_play_ok(data);
					table.append('<tr><td>The client should be started!</td></tr>');
				}
				else {
					// some kind of error
					alert(msg);
				}					
			},
			error: function error(req, status) {
				$('input[name=do_join]').attr('disabled', false);
				var msg = 'Sorry - there was a problem ('+req.status+': '+status+')';
				table.append('<tr><td>'+msg+'</td></tr>');
				alert(msg);
			}
		});
	} catch (err) {
		$('input[name=do_join]').attr('disabled', false);
		var msg = 'Sorry - there was a problem ('+e.name+': '+e.message+')';
		table.append('<tr><td>'+msg+'</td></tr>');
		alert(msg);
	}
}
