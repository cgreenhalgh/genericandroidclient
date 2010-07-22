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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import uk.ac.horizon.ug.exploding.client.logging.LoggingActivity;

/**
 * @author cmg
 *
 */
public class ClapperboardActivity extends LoggingActivity implements LocationListener {

	private static final String TAG = "Clapperboard";
	private Handler mHandler = new Handler();
	
	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.exploding.client.logging.LoggingActivity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.clapperboard);
	}

	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.exploding.client.logging.LoggingActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		update();
		LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates("gps", 0, 0, this);
	}
	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.exploding.client.logging.LoggingActivity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		mHandler.removeCallbacks(myUpdateTask);
		LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
		locationManager.removeUpdates(this);
	}
	private Runnable myUpdateTask = new Runnable() {
		public void run() {
			update();
		}
	};
	private void update() {
		try {
			TextView tv = (TextView)findViewById(R.id.clapperboard_device_id_text_view);
			String deviceId = ExplodingPreferences.getDefaultDeviceId(this);
			tv.setText(deviceId);
			tv = (TextView)findViewById(R.id.clapperboard_player_name_text_view);
			String playerName = ExplodingPreferences.getPlayerName(this);
			tv.setText(playerName);
			String clientId = ExplodingPreferences.getDeviceId(this);
			tv = (TextView)findViewById(R.id.clapperboard_client_id_text_view);
			tv.setText(clientId);
			Date now = new Date();//System.currentTimeMillis();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			String date = dateFormat.format(now);
			tv = (TextView)findViewById(R.id.clapperboard_device_date_text_view);
			tv.setText(date);
			SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ssZ");
			String time = timeFormat.format(now);
			tv = (TextView)findViewById(R.id.clapperboard_device_time_text_view);
			tv.setText(time);
			LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
			Location loc = locationManager.getLastKnownLocation("gps");
			tv = (TextView)findViewById(R.id.clapperboard_location_text_view);
			DecimalFormat latFormat = new DecimalFormat("0.000000");
			tv.setText(loc==null ? "Unknown" : latFormat.format(loc.getLatitude())+","+latFormat.format(loc.getLongitude()));
		}
		catch (Exception e) {
			Log.e(TAG, "Updating clapperboard", e);
		}
		// next second...
		mHandler.postDelayed(myUpdateTask, 1000-(System.currentTimeMillis() % 1000));
	}

	@Override
	public void onLocationChanged(Location location) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
}
