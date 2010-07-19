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
import uk.ac.horizon.ug.exploding.client.model.Game;
import uk.ac.horizon.ug.exploding.client.model.Zone;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

/**
 * @author cmg
 *
 */
public class GameStatusActivity extends LoggingActivity implements ClientStateListener {

	private static final String TAG = "Ga,eStatus";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_status);
		BackgroundThread.addClientStateListener(this, this, Game.class.getName());
		// initialise
		clientStateChanged(BackgroundThread.getClientState(this));
	}

	@Override
	public void clientStateChanged(final ClientState clientState) {
		Game game = null;
		List<Object> zones = null;
		String zoneNames = null;
		if (clientState!=null && clientState.getCache()!=null) {
		// get players...
			List<Object> facts = clientState.getCache().getFacts(Game.class.getName());
			Log.d(TAG,"Found "+facts.size()+" Game objects in cache");
			if (facts.size()>0)
				game = (Game)facts.get(0);
			zones = clientState.getCache().getFacts(Zone.class.getName());
			StringBuilder sb = new StringBuilder();
			sb.append(zones.size()+" zones: ");
			for (Object z : zones) {
				Zone zone = (Zone)z;
				sb.append(zone.getName()+" ("+zone.getID()+"/"+zone.getOrgId()+") ");
			}
			zoneNames = sb.toString();
		}
		TextView tv;
		tv = (TextView)findViewById(R.id.game_status_id_text_view);
		tv.setText(game==null ? "-" : ""+game.getID());
		tv = (TextView)findViewById(R.id.game_status_name_text_view);
		tv.setText(game==null ? "-" : ""+game.getName());
		tv = (TextView)findViewById(R.id.game_status_state_text_view);
		tv.setText(game==null ? "-" : ""+game.getState());
		tv = (TextView)findViewById(R.id.game_status_year_text_view);
		tv.setText(game==null ? "-" : ""+game.getYear());
		tv = (TextView)findViewById(R.id.game_status_zones_text_view);
		tv.setText(zoneNames==null ? "-" : ""+zoneNames);

	
	}
	
}
