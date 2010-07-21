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

import java.util.Iterator;
import java.util.List;

import org.json.JSONStringer;

import uk.ac.horizon.ug.exploding.client.logging.LoggingUtils;

import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.location.GpsStatus.Listener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

/**
 * @author cmg
 *
 */
public class LocationUtils {
	private static String TAG = "LocationUtils";
	private static transient boolean locationRequired = false;
	private static LocationThread locationThread;
	private static String PROVIDERS[] = new String [] { "gps" };
	private static int MAX_CURRENT_LOCATION_AGE_MS = 30000;
	private static long lastCheck = 0;
	private static int MIN_CHECK_INTERVAL = 1000;
	
	public static final String LOGTYPE_LOCATION = "LOCATION";// as for Aether's notebook
	public static final String LOGTYPE_GPS_STATUS = "GpsStatus";// as for Aether's notebook

	/** distance in metres */
	public static double getDistance(double lat1, double lon1, double lat2, double lon2) {
		Location l1 = getLocation(lat1, lon1);
		Location l2 = getLocation(lat2, lon2);
		return l1.distanceTo(l2);
	}
	public static Location getLocation(double lat, double lon) {
		Location l2 = new Location("gps");
		l2.setLatitude(lat);
		l2.setLongitude(lon);
		l2.setAltitude(0);
		return l2;
	}

	public static synchronized void updateRequired(Context context, boolean req) {
		Log.d(TAG,"updateRequired("+req+")");
		if (locationCallback==null) {
			locationCallback = new LocationCallback(context);			
		}
		if (locationThread==null) {
			locationThread = new LocationThread(context);
			locationThread.start();
		}
		if (locationRequired!=req || lastCheck==0) {
			locationRequired = req;
			long elapsed = System.currentTimeMillis()-lastCheck;
			if (elapsed < MIN_CHECK_INTERVAL) {
				lastCheck = lastCheck+MIN_CHECK_INTERVAL;
				locationThread.checkDelayed(MIN_CHECK_INTERVAL-elapsed);
			}
			else {
				locationThread.check();
				lastCheck = System.currentTimeMillis();
			}
		}
	}
	public static boolean locationProviderEnabled(Context context) {
		LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		for (int pi=0; pi<PROVIDERS.length; pi++) {
			String provider = PROVIDERS[pi];
			if (!locationManager.isProviderEnabled(provider)) 
				return false;
		}
		return true;
	}
	public static String getLocationProviderError(Context context) {
		LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		for (int pi=0; pi<PROVIDERS.length; pi++) {
			String provider = PROVIDERS[pi];
			if (!locationManager.isProviderEnabled(provider)) 
				return "Please enable location provider \""+provider+"\"";
		}
		return null;		
	}
	public static Location getCurrentLocation(Context context) {
		LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		for (int pi=0; pi<PROVIDERS.length; pi++) {
			String provider = PROVIDERS[pi];
			if (!locationManager.isProviderEnabled(provider)) {
				Log.e(TAG,"Required location provider "+provider+" disabled (getCurrentLocation)");				
			}
			else {
				Location loc = locationManager.getLastKnownLocation(provider);
				if (loc!=null) {
					long age = System.currentTimeMillis()-loc.getTime();
					if (age > MAX_CURRENT_LOCATION_AGE_MS) {
						Log.w(TAG, "Location provider "+provider+" last location is too old ("+age+" ms)");
					}
					else {
						// TODO accuracy requirement?
						return loc;
					}
				}
			}
		}
		return null;		
	}
	static class LocationThread extends Thread {
		private Context context;
		private Handler handler;
		private boolean locating = false;
		LocationThread(Context context) {
			this.context = context;			
		}
		public void checkDelayed(long l) {
			waitForHandler();
			Runnable r = new Runnable() {
				public void run() {
					boolean required = locationRequired;
					Log.d(TAG,"Checking in thread ("+required+" vs "+locating+")");
					if (required && !locating) 
						registerOnThread(context, locationCallback, /*locationCallback*/null);
					else if (!required && locating)
						unregisterOnThread(context, locationCallback, /*locationCallback*/null);
					locating = required;
					// dont rush?!
				}
			};
			if (l<=0)
				handler.post(r);
			else {
				Log.d(TAG,"Cleck delayed by "+l);
				handler.postDelayed(r, l);
			}
		}
		public void run() {
			Looper.prepare();
			handler = new Handler();
			Looper.loop();
			Log.i(TAG,"LocationThread terminated");
		}
		private void waitForHandler() {
			try {
				while (handler==null) {
					sleep(10);
				}
			} catch (InterruptedException ie) { /*ignore?*/ }
		}
		void check() {
			checkDelayed(0);
		}
	}
	public static void registerOnThread(Context context, LocationListener locationCallback, Listener listener) {
		LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		List<String> providers = locationManager.getAllProviders();
		Log.i(TAG,"Found "+providers.size()+" location providers");
		for (String provider : providers) {
			if (locationManager.isProviderEnabled(provider)) {
				Log.i(TAG,"Provider "+provider+" enabled");
			}
			else {
				Log.i(TAG,"Provider "+provider+" disabled");	
			}
		}
		for (int pi=0; pi<PROVIDERS.length; pi++) {
			String provider = PROVIDERS[pi];
			if (locationManager.isProviderEnabled(provider)) {
				Log.i(TAG,"Registering with provider "+provider);
				Location loc = locationManager.getLastKnownLocation(provider);
				if (loc!=null) {
					Log.i(TAG,"Last Location, provider="+loc.getProvider()+", lat="+loc.getLatitude()+", long="+loc.getLongitude()+", bearing="+(loc.hasBearing() ? ""+loc.getBearing() : "NA")+", speed="+(loc.hasSpeed() ? ""+loc.getSpeed() : "NA")+", accuracy="+(loc.hasAccuracy() ? ""+loc.getAccuracy() : "NA")+", alt="+(loc.hasAltitude() ? ""+loc.getAltitude() : "NA"));
					
					ZoneService.updateLocation(context, loc);
					
				}
				//if (!"passive".equals(provider))
				if (locationCallback!=null)
					locationManager.requestLocationUpdates(provider, 0/*minTime*/, 0/*minDistance*/, locationCallback);
			}
			else
				Log.e(TAG,"Required provider "+provider+" not enabled!");
		}
		if (listener!=null)
			locationManager.addGpsStatusListener(listener);
	}
	public static void unregisterOnThread(Context context, LocationListener locationCallback, Listener listener) {
		Log.i(TAG,"Unregister for location events");
		LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		if (locationCallback!=null)
			locationManager.removeUpdates(locationCallback);
		if (listener!=null)
			locationManager.removeGpsStatusListener(listener);
	}
	private static LocationCallback locationCallback;
	static class LocationCallback implements LocationListener, GpsStatus.Listener {
		private Context context;
		LocationCallback(Context context) {
			this.context = context;
		}
		@Override
		public void onLocationChanged(Location loc) {
			Log.i(TAG,"Location provider="+loc.getProvider()+", lat="+loc.getLatitude()+", long="+loc.getLongitude()+", bearing="+(loc.hasBearing() ? ""+loc.getBearing() : "NA")+", speed="+(loc.hasSpeed() ? ""+loc.getSpeed() : "NA")+", accuracy="+(loc.hasAccuracy() ? ""+loc.getAccuracy() : "NA")+", alt="+(loc.hasAltitude() ? ""+loc.getAltitude() : "NA"));
			try {
				JSONStringer js = new JSONStringer();
				// as er Aether's notebook
				js.object();
				js.key("accuracy");
				if(loc.hasAccuracy())
					js.value(loc.getAccuracy());
				else
					js.value(null);
				
				js.key("altitude");
				if(loc.hasAltitude())
					js.value(loc.getAltitude());
				else
					js.value(null);
				
				js.key("bearing");
				if(loc.hasBearing())
					js.value(loc.getBearing());
				else
					js.value(null);
				
				js.key("latitude");
				js.value(loc.getLatitude());
				
				js.key("longitude");
				js.value(loc.getLongitude());
				
				js.key("provider");
				js.value(loc.getProvider());
				
				js.key("speed");
				if(loc.hasSpeed())
					js.value(loc.getSpeed());
				else
					js.value(null);
				
				Bundle extras = loc.getExtras();
				js.key("extras");
				if(extras != null)
					js.value(extras.toString());
				else
					js.value(null);

				js.endObject();
				LoggingUtils.log(LOGTYPE_LOCATION, js.toString());
			}
			catch (Exception e) {
				Log.e(TAG,"Logging "+loc);
			}
			ZoneService.updateLocation(context, loc);
		}

		@Override
		public void onProviderDisabled(String provider) {
			Log.i(TAG,"Provider "+provider+" disabled");
		}

		@Override
		public void onProviderEnabled(String provider) {
			Log.i(TAG, "Provider "+provider+" enabled");
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			Log.i(TAG, "Provider "+provider+" status now "+status+" ("+(status==LocationProvider.AVAILABLE ? "AVAILABLE" : status==LocationProvider.OUT_OF_SERVICE ? "OUT_OF_SERVICE" : status==LocationProvider.TEMPORARILY_UNAVAILABLE ? "TEMPORARILY_UNAVAILABLE" : "unknown")+", extras="+extras);
		}

		@Override
		public void onGpsStatusChanged(int event) {
			LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
			GpsStatus gpsStatus = locationManager.getGpsStatus(null);
			// TODO Auto-generated method stub
			Log.i(TAG,"GpsStatus "+event+" ("+(event==GpsStatus.GPS_EVENT_FIRST_FIX ? "First fix" : event==GpsStatus.GPS_EVENT_SATELLITE_STATUS ? "Satelite status" : event==GpsStatus.GPS_EVENT_STARTED ? "started" : event==GpsStatus.GPS_EVENT_STOPPED ? "stopped" : "unknown"));
			Log.i(TAG,"GpsStatus timeToFirstFix="+gpsStatus.getTimeToFirstFix());
			Iterator<GpsSatellite> sats = gpsStatus.getSatellites().iterator();
			while(sats.hasNext()) {
				GpsSatellite sat = sats.next();
				Log.i(TAG,"GpsSatellite snr="+sat.getSnr()+", used="+sat.usedInFix()+", azimuth="+sat.getAzimuth()+", elevation="+sat.getElevation()+", almanac="+sat.hasAlmanac()+", ephemeris="+sat.hasEphemeris()+", prn="+sat.getPrn());
			}
		}
	
	}
	
}
