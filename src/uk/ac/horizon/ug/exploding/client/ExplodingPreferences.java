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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

/**
 * @author cmg
 *
 */
public class ExplodingPreferences extends PreferenceActivity {

	/**
	 * 
	 */
	public ExplodingPreferences() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		// force initialisation of device ID
		getDeviceId(this);
	}
	/** get default device id (imei) */
	public static String getDefaultDeviceId(Context context) {
		TelephonyManager mTelephonyMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = mTelephonyMgr.getDeviceId(); // Requires READ_PHONE_STATE  
		return imei;
	}
	public static final String CLIENT_ID = "clientId";
	/** get device id */
	public static String getDeviceId(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		if (!preferences.contains(CLIENT_ID) || preferences.getString(CLIENT_ID, "").length()==0) {
			preferences.edit().putString(CLIENT_ID, getDefaultDeviceId(context)).commit();
		}
		return preferences.getString(CLIENT_ID, null);
	}
	public static final String PLAYER_NAME = "playerName";
	/** get device id */
	public static String getPlayerName(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getString(PLAYER_NAME, "");
	}
}
