/**
 * Copyright 2010 The University of Nottingham
 * 
 * This file is part of genericandroidclient.
 *
 *  genericandroidclient is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  genericandroidclient is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with genericandroidclient.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package uk.ac.horizon.ug.exploding.client;

import uk.ac.horizon.ug.exploding.client.logging.LoggingActivity;
import uk.ac.horizon.ug.exploding.client.logging.LoggingUtils;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

/**
 * @author cmg
 *
 */
public class DebugHomeActivity extends LoggingActivity implements ClientStateListener  {
	private static final String TAG = "DebugActivity";
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (BackgroundThread.getHandler()==null)
        	BackgroundThread.setHandler(handler);
        setContentView(R.layout.debug);
        BackgroundThread.addClientStateListener(this, this, ClientState.Part.STATUS.flag());
        
        //AudioUtils.addSoundFile(this, R.raw.buzzing, new SoundAttributes(1.0f, 1.0f, true, 1.0f));
        //AudioUtils.play(R.raw.buzzing);
    }
    /** create menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();    
    	inflater.inflate(R.menu.debug_menu, menu);    
    	return true;
    }
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.debug_menu_preferences:
		{
			Intent intent = new Intent();
			intent.setClass(this, ExplodingPreferences.class);
			startActivity(intent);
			return true;
		}
		case R.id.debug_menu_gps:
		{
			Intent intent = new Intent();
			intent.setClass(this, GpsStatusActivity.class);
			startActivity(intent);
			return true;
		}			
		case R.id.debug_menu_player_status:
		{
			Intent intent = new Intent();
			intent.setClass(this, PlayerStatusActivity.class);
			startActivity(intent);
			return true;
		}			
		case R.id.debug_menu_game_status:
		{
			Intent intent = new Intent();
			intent.setClass(this, GameStatusActivity.class);
			startActivity(intent);
			return true;
		}			
		default:
			return super.onOptionsItemSelected(item);			
		}
	}
	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.exploding.client.ClientStateListener#clientStateChanged(uk.ac.horizon.ug.exploding.client.ClientState)
	 */
	@Override
	public void clientStateChanged(ClientState clientState) {
	Log.d(TAG, "clientStateChanged: "+clientState);
		//if (clientState.getClientStatus()!=ClientStatus.LOGGING_IN && clientState.getClientStatus()!=ClientStatus.GETTING_STATE)
		//Toast.makeText(this, "State: "+clientState.getClientStatus(), Toast.LENGTH_SHORT).show();
	
		if (clientState.isStatusChanged())
			updateDialogs(clientState);

	}
	/** update visible dialogs */
	private void updateDialogs(ClientState clientState) {
		TextView logFileTextView = (TextView)findViewById(R.id.debug_log_file_text_view);
		logFileTextView.setText(LoggingUtils.getLogFile()!=null ? LoggingUtils.getLogFile().toString() : "Not logging (internal error)");
		// update status
		TextView statusTextView = (TextView)findViewById(R.id.debug_status_text_view);
		statusTextView.setText(clientState.getClientStatus().name());
		// update status
		TextView gameStatusTextView = (TextView)findViewById(R.id.debug_game_status_text_view);
		gameStatusTextView.setText(clientState.getGameStatus().name());
		// update login status
		TextView loginStatusTextView = (TextView)findViewById(R.id.debug_login_status_text_view);
		loginStatusTextView.setText(clientState.getLoginStatus().name());
		// update login message
		TextView loginMessageTextView = (TextView)findViewById(R.id.debug_login_message_text_view);
		loginMessageTextView.setText(clientState.getLoginMessage());
	}
	// Need handler for callbacks to the UI thread    
	final Handler handler = new Handler();    
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		ClientState clientState = BackgroundThread.getClientState(this);
		updateDialogs(clientState);
		Log.d(TAG, "onResume(), clientState="+clientState);
		TextView urlTextView = (TextView)findViewById(R.id.debug_server_url_text_view);
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		urlTextView.setText(preferences.getString("serverUrl", "(not set)"));
	}
}
