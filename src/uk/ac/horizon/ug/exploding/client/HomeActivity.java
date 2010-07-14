/**
 * Copyright 2010 The University of Nottingham
 * 
 * This file is part of GenericAndroidClient.
 *
 *  GenericAndroidClient is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  GenericAndroidClient is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with GenericAndroidClient.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package uk.ac.horizon.ug.exploding.client;

import java.util.List;

import uk.ac.horizon.ug.exploding.client.AudioUtils.SoundAttributes;
import uk.ac.horizon.ug.exploding.client.model.Player;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/** activity launched from Home.
 * Note: should be singleTask in manifest, i.e. only one should ever exist as task root.
 * Also manages background thread shared by other Activities. 
 * 
 * @author cmg
 *
 */
public class HomeActivity extends Activity implements ClientStateListener {
	private static final String TAG = "HomeActivity";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BackgroundThread.setHandler(handler);
        setContentView(R.layout.main);
        BackgroundThread.addClientStateListener(this, this, ClientState.Part.STATUS.flag());
        showPlayerNameDialog = true;
        //AudioUtils.addSoundFile(this, R.raw.buzzing, new SoundAttributes(1.0f, 1.0f, true, 1.0f));
        //AudioUtils.play(R.raw.buzzing);
    }
    
    @Override
	protected void onNewIntent(Intent intent) {
    	if (intent.getAction().equals(Intent.ACTION_MAIN) && 
    			intent.getCategories().contains(Intent.CATEGORY_LAUNCHER) && 
    			(intent.getFlags() & Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY)==0) {
    		// prompt for new game on re-launch
    		showPlayerNameDialog = true;
    		Log.d(TAG,"onNewIntent(MAIN,LAUNCHER): "+intent);
    	}
    	else
    		Log.d(TAG,"onNewIntent: "+intent);
		super.onNewIntent(intent);
	}

	/** create menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();    
    	inflater.inflate(R.menu.main_menu, menu);    
    	return true;
    }
    private boolean enableRetry = false;
    private boolean enablePlay = false;
    private boolean playerDialogActive = false;
	/* (non-Javadoc)
	 * @see android.app.Activity#onPrepareOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.main_menu_retry).setEnabled(enableRetry);

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean standaloneMode = preferences.getBoolean("standaloneMode", false);
		
		menu.findItem(R.id.main_menu_play).setEnabled(standaloneMode || enablePlay);
		return super.onPrepareOptionsMenu(menu);
	}
	private void play() {
		Intent intent = new Intent();
		intent.setClass(this, GameMapActivity.class);
		startActivity(intent);
	}
	private void playIfReady() {
		if (playerDialogActive==true || (connectingPd!=null && connectingPd.isShowing()) || (this.gettingStatePd!=null && gettingStatePd.isShowing())) {
			Log.d(TAG,"Not ready to play due to dialog(s)");
			return;
		}
		ClientState clientState = BackgroundThread.getClientState(this);
		if (clientState!=null && 
				(clientState.getClientStatus()==ClientStatus.IDLE || 
						clientState.getClientStatus()==ClientStatus.POLLING || 
						clientState.getClientStatus()==ClientStatus.PAUSED))
			play();
		else
			Log.d(TAG,"playIfReady won't play");
	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// Now automatic ?!
		case R.id.main_menu_play:
			play();
			return true;
		case R.id.main_menu_retry: {
			ClientState clientState = BackgroundThread.getClientState(this);
			if (clientState!=null && clientState.getClientStatus()==ClientStatus.CONFIGURING) {
				showDialog(DialogId.NEW_GAME.ordinal());
				playerDialogActive = true;
			}
			else
				BackgroundThread.retry(this);
			return true;
		}
		// Now in Debug
//		case R.id.main_menu_preferences:
//		{
//			Intent intent = new Intent();
//			intent.setClass(this, ExplodingPreferences.class);
//			startActivity(intent);
//			return true;
//		}
//		case R.id.main_menu_gps:
//		{
//			Intent intent = new Intent();
//			intent.setClass(this, GpsStatusActivity.class);
//			startActivity(intent);
//			return true;
//		}			
//		case R.id.main_menu_player_status:
//		{
//			Intent intent = new Intent();
//			intent.setClass(this, PlayerStatusActivity.class);
//			startActivity(intent);
//			return true;
//		}			
//		case R.id.main_menu_game_status:
//		{
//			Intent intent = new Intent();
//			intent.setClass(this, GameStatusActivity.class);
//			startActivity(intent);
//			return true;
//		}			
		default:
			return super.onOptionsItemSelected(item);			
		}
	}
	private static enum DialogId {
		CONNECTING, GETTING_STATE, NEW_GAME, PLAYER_NAME
	}
	private ProgressDialog connectingPd;
	private ProgressDialog gettingStatePd;
    /* (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id==DialogId.CONNECTING.ordinal()) {
			connectingPd = new ProgressDialog(this);
			connectingPd.setCancelable(true);
			connectingPd.setMessage("Connecting...");
			connectingPd.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					BackgroundThread.cancel(HomeActivity.this);
				}
			});
			return connectingPd;
		}
		if (id==DialogId.GETTING_STATE.ordinal()) {
			gettingStatePd = new ProgressDialog(this);
			gettingStatePd.setCancelable(true);
			gettingStatePd.setMessage("Getting Information...");
			gettingStatePd.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					BackgroundThread.cancel(HomeActivity.this);
				}
			});
			return gettingStatePd;
		}
		if (id==DialogId.NEW_GAME.ordinal()) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Do you want to join a new game?")
				.setCancelable(false)
				.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						//MyActivity.this.finish();
						dialog.cancel();
						showDialog(DialogId.PLAYER_NAME.ordinal());
						playerDialogActive = true;
					}       
				})       
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {                
						dialog.cancel();           
						playerDialogActive = false;
						// TODO: carry on?
						playIfReady();
					}     
				});
			return builder.create();
		}
		if (id==DialogId.PLAYER_NAME.ordinal()) {
			final Dialog d = new Dialog(this);
			d.setContentView(R.layout.player_name_dialog);
			d.setCancelable(true);
			d.setTitle("Player Name:");
			d.setOnCancelListener(new OnCancelListener()  {				
				@Override
				public void onCancel(DialogInterface dialog) {
					dismissDialog(DialogId.PLAYER_NAME.ordinal());
					// TODO carry on?
					//BackgroundThread.cancel(HomeActivity.this);
					playerDialogActive = false;
					playIfReady();
				}
			});
			Button ok = (Button)d.findViewById(R.id.player_name_dialog_ok);
			ok.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					EditText et = (EditText)d.findViewById(R.id.player_name_dialog_edit_text);
					BackgroundThread.setPlayerName(et.getText().toString());
					Log.d(TAG,"Set player name to "+et.getText().toString());
					dismissDialog(DialogId.PLAYER_NAME.ordinal());
					playerDialogActive = false;
					BackgroundThread.restart(HomeActivity.this);
				}
			});
			return d;
		}
		// TODO Auto-generated method stub
		return super.onCreateDialog(id);
	}
	
	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		if (id==DialogId.PLAYER_NAME.ordinal()) {
			EditText et = (EditText)dialog.findViewById(R.id.player_name_dialog_edit_text);
			String playerName = BackgroundThread.getPlayerName();
			if (playerName==null || playerName.length()==0) {
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
				playerName = preferences.getString("defaultPlayerName", "");
			}
			ClientState clientState = BackgroundThread.getClientState(this);
			if (clientState!=null && clientState.getCache()!=null) {
				// get players...
				List<Object> facts = clientState.getCache().getFacts(Player.class.getName());
				//Log.d(TAG,"Found "+facts.size()+" Player objects in cache");
				if (facts.size()>0) {
					Player player = (Player)facts.get(0);
					if (player.getName()!=null)
						playerName = player.getName();
				}
			}
			et.setText(playerName);
		}
		super.onPrepareDialog(id, dialog);
	}
	// Need handler for callbacks to the UI thread    
	final Handler handler = new Handler();    
	// Create runnable for posting    

	/** NB this is not the GUI thread */
	@Override
	public void clientStateChanged(final ClientState clientState) {
		Log.d(TAG, "clientStateChanged: "+clientState);
		// TODO Auto-generated method stub
		//if (clientState.getClientStatus()!=ClientStatus.LOGGING_IN && clientState.getClientStatus()!=ClientStatus.GETTING_STATE)
		//Toast.makeText(this, "State: "+clientState.getClientStatus(), Toast.LENGTH_SHORT).show();
	
		if (clientState.isStatusChanged()) {
			updateDialogs(clientState);
			// shouldn't be needed on state change - dialog(s) should do it ?!
//			if (hasFocus)
//				playIfReady();
		}
	}

	/** update visible dialogs */
	private void updateDialogs(ClientState clientState) {
		switch (clientState.getClientStatus()) {
		case ERROR_GETTING_STATE:
		case ERROR_DOING_LOGIN:
		case ERROR_IN_SERVER_URL:
		case CANCELLED_BY_USER:
		case ERROR_AFTER_STATE:
		case CONFIGURING:
			enableRetry = true;
			enablePlay = false;
			break;
		case POLLING:
		case IDLE:
		case PAUSED:
			enablePlay = true;
			enableRetry = false;
			break;
		default:
			enableRetry = false;
			enablePlay = false;
			break;
		}

		if (clientState.getClientStatus()==ClientStatus.LOGGING_IN) 
			showDialog(DialogId.CONNECTING.ordinal());
		else if (connectingPd!=null && connectingPd.isShowing())
			dismissDialog(DialogId.CONNECTING.ordinal());
		if (clientState.getClientStatus()==ClientStatus.GETTING_STATE) 
			showDialog(DialogId.GETTING_STATE.ordinal());
		else if (gettingStatePd!=null && gettingStatePd.isShowing()) {
			dismissDialog(DialogId.GETTING_STATE.ordinal());
			if (clientState.getClientStatus()==ClientStatus.PAUSED || 
					clientState.getClientStatus()==ClientStatus.POLLING|| 
					clientState.getClientStatus()==ClientStatus.IDLE)
				play();
		}

		// Now in debug
//		// update status
		TextView statusTextView = (TextView)findViewById(R.id.main_status_text_view);
		statusTextView.setText(getClientStatusText(clientState.getClientStatus()));
//		// update status
//		TextView gameStatusTextView = (TextView)findViewById(R.id.main_game_status_text_view);
//		gameStatusTextView.setText(clientState.getGameStatus().name());
//		// update login status
//		TextView loginStatusTextView = (TextView)findViewById(R.id.main_login_status_text_view);
//		loginStatusTextView.setText(clientState.getLoginStatus().name());
//		// update login message
//		TextView loginMessageTextView = (TextView)findViewById(R.id.main_login_message_text_view);
//		loginMessageTextView.setText(clientState.getLoginMessage());
	}
	/**
	 * @param clientStatus
	 * @return
	 */
	private String getClientStatusText(ClientStatus clientStatus) {
		switch(clientStatus) {
		case CANCELLED_BY_USER:
			return "Last attempt cancelled by the user - please try again";
		case CONFIGURING:
			return "Waiting for initial information from user...";
		case ERROR_AFTER_STATE:
			return "There has been an error talking to the server but it may be temporary";
		case ERROR_DOING_LOGIN:
			return "There was an error joining the game - please try again";
		case ERROR_GETTING_STATE:
			return "There was an error getting information about the game - please try again";
		case ERROR_IN_SERVER_URL:
			return "There was an error in the configuration of this client (the server URL) - please seek assistance";
		case GETTING_STATE:
			return "Getting information about the game from the server...";
		case IDLE:
			return "Everything seems to be working";
		case LOGGING_IN:
			return "Joining the game...";
		case NEW:
			return "Starting up...";
		case PAUSED:
			return "Paused - should resume automatically";
		case POLLING:
			return "Checking for new information from the server...";
		case STOPPED:
			return "The game has ended.";
		}		
		return clientStatus==null ? "null" : clientStatus.toString();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		hasFocus = false;
		Log.d(TAG, "onPause()");
		AudioUtils.autoPause();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		Log.d(TAG, "onRestart()");
	}

    private boolean showPlayerNameDialog = false;
    private boolean hasFocus = false;
	/* (non-Javadoc)
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		hasFocus = true;

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		boolean shutdownClient = preferences.getBoolean("shutdownClient", false);
		if (shutdownClient) {
			BackgroundThread.shutdown(this);
			preferences.edit().putBoolean("shutdownClient", false).commit();
		}

		ClientState clientState = BackgroundThread.getClientState(this);

		// this might start it playing...
		updateDialogs(clientState);
		Log.d(TAG, "onResume(), clientState="+clientState);
		// now in debug
//		TextView urlTextView = (TextView)findViewById(R.id.main_server_url_text_view);
		// preferences edited by PreferencesActivity
		boolean restartClient = preferences.getBoolean("restartClient", false);
		if (restartClient) {
			showPlayerNameDialog = true;
			preferences.edit().putBoolean("restartClient", false).commit();
		}
		if (showPlayerNameDialog) {
			showPlayerNameDialog = false;
			
			if ((connectingPd==null || !connectingPd.isShowing()) && (gettingStatePd==null || !gettingStatePd.isShowing())) {
				showDialog(DialogId.NEW_GAME.ordinal());
				playerDialogActive = true;
			}
		}
		else
			playIfReady();
	}
//	private void checkRestart() {
//		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
//		boolean restartClient = preferences.getBoolean("restartClient", false);
//		if (restartClient) {
//			BackgroundThread.restart(this);
//			preferences.edit().putBoolean("restartClient", false).commit();
//		}
//		// now in debug
////		urlTextView.setText(preferences.getString("serverUrl", "(not set)"));
//		
//		//AudioUtils.autoResume();
//		// TEST
//        //AudioUtils.play(R.raw.buzzing);
//	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		Log.d(TAG, "onStart()");
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		Log.d(TAG, "onStop()");
	}
    
}