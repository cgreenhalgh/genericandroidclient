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

import uk.ac.horizon.ug.exploding.client.logging.LoggingActivity;
import uk.ac.horizon.ug.exploding.client.model.Player;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

/**
 * @author cmg
 *
 */
public class PlayerStatusActivity extends LoggingActivity implements ClientStateListener {

	private static final String TAG = "PlayerStatus";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player_status);
		BackgroundThread.addClientStateListener(this, this, Player.class.getName());
		// initialise
		clientStateChanged(BackgroundThread.getClientState(this));
	}

	@Override
	public void clientStateChanged(final ClientState clientState) {
		Player player = null;
		if (clientState!=null && clientState.getCache()!=null) {
		// get players...
			List<Object> facts = clientState.getCache().getFacts(Player.class.getName());
			Log.d(TAG,"Found "+facts.size()+" Player objects in cache");
			if (facts.size()>0)
				player = (Player)facts.get(0);
		}
		TextView tv;
		tv = (TextView)findViewById(R.id.player_status_can_author_text_view);
		tv.setText(player==null ? "-" : ""+player.getCanAuthor());
		tv = (TextView)findViewById(R.id.player_status_id_text_view);
		tv.setText(player==null ? "-" : ""+player.getID());
		tv = (TextView)findViewById(R.id.player_status_name_text_view);
		tv.setText(player==null ? "-" : ""+player.getName());
		tv = (TextView)findViewById(R.id.player_status_new_member_quota_text_view);
		tv.setText(player==null ? "-" : ""+player.getNewMemberQuota());
		tv = (TextView)findViewById(R.id.player_status_points_text_view);
		tv.setText(player==null ? "-" : ""+player.getPoints());
	}
	
}
