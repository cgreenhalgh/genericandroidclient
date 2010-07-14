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

import uk.ac.horizon.ug.exploding.client.Client.QueuedMessage;
import uk.ac.horizon.ug.exploding.client.model.Member;
import uk.ac.horizon.ug.exploding.client.model.Position;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

/**
 * @author cmg
 *
 */
public class CreateMemberActivity extends Activity implements ClientMessageListener {

	private static final String TAG = "CreateMember";

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();    
    	inflater.inflate(R.menu.create_member_menu, menu);    
    	return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.create_member_menu_create:
			createMember();
			return true;
		}
		return super.onContextItemSelected(item);
	}
	private static enum DialogId {
		CREATING_MEMBER
	}
	private Client cache;
	private QueuedMessage createMemberMessage;
	@Override
	protected Dialog onCreateDialog(int id) {
		if (id==DialogId.CREATING_MEMBER.ordinal()) {
			ProgressDialog creatingPd = new ProgressDialog(this);
			creatingPd.setCancelable(true);
			creatingPd.setMessage("Creating member...");
			creatingPd.setOnCancelListener(new OnCancelListener() {
				@Override
				public void onCancel(DialogInterface dialog) {
					if (cache!=null && createMemberMessage!=null)
						cache.cancelMessage(createMemberMessage, true);
				}
			});
			return creatingPd;
		}
		return super.onCreateDialog(id);
	}

	/**
	 * 
	 */
	private void createMember() {
		try {
			Member member = new Member();
			EditText et = (EditText)findViewById(R.id.create_member_action_edit_text);
			member.setAction(Integer.parseInt(et.getText().toString()));
			et = (EditText)findViewById(R.id.create_member_health_edit_text);
			member.setHealth(Integer.parseInt(et.getText().toString()));
			et = (EditText)findViewById(R.id.create_member_wealth_edit_text);
			member.setWealth(Integer.parseInt(et.getText().toString()));
			et = (EditText)findViewById(R.id.create_member_brains_edit_text);
			member.setBrains(Integer.parseInt(et.getText().toString()));
			et = (EditText)findViewById(R.id.create_member_name_edit_text);
			member.setName(et.getText().toString());
			ClientState clientState = BackgroundThread.getClientState(this);
			Position pos = new Position();
			Location loc = clientState.getLastLocation();
			pos.setLatitude(loc.getLatitude());
			pos.setLongitude(loc.getLongitude());
			pos.setElevation(loc.getAltitude());
			member.setPosition(pos);
			member.setZone(clientState.getZoneOrgID());
			cache = clientState.getCache();
			
			// Note: this is (now) an async action
			createMemberMessage = cache.queueMessage(cache.addFactMessage(member), this);
			Log.i(TAG,"Creating member: "+member);
			
			showDialog(DialogId.CREATING_MEMBER.ordinal());
		} 
		catch (Exception e) {
			Toast.makeText(this, "Sorry: "+e, Toast.LENGTH_LONG).show();
			Log.e(TAG, "Creating member", e);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_member);
	}

	@Override
	public void onMessageResponse(MessageStatusType status,
			String errorMessage, Object value) {
		Log.d(TAG,"onMessageResponse: status="+status+", error="+errorMessage+", value="+value);
		dismissDialog(DialogId.CREATING_MEMBER.ordinal());

		if (status==MessageStatusType.OK)
			this.finish();
		else
			Toast.makeText(this, "Sorry: "+errorMessage, Toast.LENGTH_LONG).show();
		// tidy up
		createMemberMessage = null;
		cache = null;
	}

}
