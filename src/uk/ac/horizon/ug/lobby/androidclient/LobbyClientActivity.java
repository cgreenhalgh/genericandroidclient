/**
 * 
 */
package uk.ac.horizon.ug.lobby.androidclient;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import uk.ac.horizon.ug.exploding.client.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

/**
 * @author cmg
 *
 */
public class LobbyClientActivity extends Activity {
	static final String TAG = "Lobbyclient";
	
	private WebView mWebView;
	private Handler mHandler = new Handler(); 
	   
	static final int DIALOG_ERROR = 1;
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.lobbyclient);
//        Button button = (Button)findViewById(R.id.go_to_default);
//        button.setOnClickListener(this);
        mWebView = (WebView) findViewById(R.id.lobbyclientWebview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSavePassword(false);          
        webSettings.setSaveFormData(false);         
        webSettings.setJavaScriptEnabled(true);       
        webSettings.setSupportZoom(false);      
        mWebView.setWebChromeClient(new MyWebChromeClient());
        mWebView.setWebViewClient(new MyWebViewClient());

        mWebView.addJavascriptInterface(new LobbyclientJavaScriptInterface(), "lobbyclient");    

        Log.i(TAG,"Load lobbyHtmlUrl "+getString(R.string.lobbyHtmlUrl));
        mWebView.loadUrl(getString(R.string.lobbyHtmlUrl));	
    }

	// JS interface for localStorage - minimal
	class LocalStorageJavaScriptInterface {
		// TODO persist
		HashMap<String,String> values = new HashMap<String,String>();
		
		public synchronized String getItem(String key) {
			Log.i(TAG,"getItem("+key+")");
			return values.get(key);
		}
		public synchronized void setItem(String key, String value) {
			Log.i(TAG,"setItem("+key+","+value+")");
			values.put(key, value);
		}
		
	}
	
	// JS interface, as lobbyclient.game
	class GameJavaScriptInterface {
		private String indexJson;
		private String queryUrl;
		public synchronized String getIndexJson() {
			if (indexJson==null) {
				// ensure string " are escaped!
				indexJson = getString(R.string.lobbyIndexJson);
			}
			Log.i(TAG,"return indexJson "+indexJson);
			return indexJson;
		}
		public synchronized String getQueryUrl() {
			if (queryUrl==null)
				queryUrl = getString(R.string.lobbyQueryUrl);
			Log.i(TAG,"return queryUrl "+queryUrl);
			return queryUrl;
		}
	}
	// JS interface, as lobbyclient
	class LobbyclientJavaScriptInterface {
		private GameJavaScriptInterface game;
		public LobbyclientJavaScriptInterface() {}
		private LocalStorageJavaScriptInterface localStorage;
		public synchronized GameJavaScriptInterface getGame() {
			if (game==null)
				game = new GameJavaScriptInterface();
			return game;
		}
		public synchronized LocalStorageJavaScriptInterface getLocalStorage() {
			if (localStorage==null)
				localStorage = new LocalStorageJavaScriptInterface();
			return localStorage;
		}
	}	
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateDialog(int)
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_ERROR:
		{
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Unable to start Lobby client");
			builder.setCancelable(true);
			AlertDialog alert = builder.create();
			return alert;
		}
		default:
			return null;
		}
	}
	
	
	/**       
	 * Provides a hook for calling "alert" from javascript. Useful for       
	 * debugging your javascript.       
	 */    
	final class MyWebChromeClient extends WebChromeClient {
		@Override          
		public boolean onJsAlert(WebView view, String url, String message, JsResult result) {   
			Log.d(TAG, message);     
			AlertDialog.Builder builder = new AlertDialog.Builder(LobbyClientActivity.this);
			builder.setMessage(message);
			builder.setCancelable(true);
			AlertDialog alert = builder.create();
			alert.show();
			//Toast.makeText(LobbyClientActivity.this, message, Toast.LENGTH_LONG).show();
			result.confirm();     
			return true;     
		}

		/* (non-Javadoc)
		 * @see android.webkit.WebChromeClient#onConsoleMessage(java.lang.String, int, java.lang.String)
		 */
		@Override
		public void onConsoleMessage(String message, int lineNumber,
				String sourceID) {
			// TODO Auto-generated method stub
			super.onConsoleMessage(message, lineNumber, sourceID);
			Log.d(TAG,"ConsoleMessage: "+message+" ("+lineNumber+", "+sourceID+")");
			AlertDialog.Builder builder = new AlertDialog.Builder(LobbyClientActivity.this);
			builder.setMessage("Sorry - there is a problem with the client ("+message+" at "+sourceID+" line "+lineNumber+")");
			builder.setCancelable(true);
			AlertDialog alert = builder.create();
			alert.show();
		}
		
	}  
	
	class MyWebViewClient extends WebViewClient {

		/* (non-Javadoc)
		 * @see android.webkit.WebViewClient#shouldOverrideUrlLoading(android.webkit.WebView, java.lang.String)
		 */
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// TODO Auto-generated method stub
			Log.i(TAG,"shouldOverrideUrlLoading("+url+")");
			return super.shouldOverrideUrlLoading(view, url);
		}

		/* (non-Javadoc)
		 * @see android.webkit.WebViewClient#onReceivedError(android.webkit.WebView, int, java.lang.String, java.lang.String)
		 */
		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			// TODO Auto-generated method stub
			super.onReceivedError(view, errorCode, description, failingUrl);
			Log.d(TAG, "receivedError("+errorCode+","+description+","+failingUrl+")");
		}
		
		
	}
}
