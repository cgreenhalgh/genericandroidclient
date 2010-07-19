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

import org.json.JSONStringer;

import uk.ac.horizon.ug.exploding.client.logging.LoggingUtils;
import android.content.res.Configuration;
import android.util.Log;

/**
 * @author cmg
 *
 */
public class Application extends android.app.Application {

	public static final String LOGTYPE_APPLICATION = "Application";
	private static final String TAG = "HorizonApplication";

	private void log(String method) {
		log(method, null, null);
	}
	private void log(String method, String extraKey, Object extraValue) {
		try {
			JSONStringer js = new JSONStringer();
			js.object();
			js.key("method");
			js.value(method);
			if (extraKey!=null) {
				js.key(extraKey);
				js.value(extraValue);
			}
			js.endObject();
			LoggingUtils.log(LOGTYPE_APPLICATION, js.toString());
		}
		catch (Exception e) {
			Log.e(TAG,"log("+method+","+extraKey+","+extraValue+")", e);
		}
	}

	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		log("method", "onConfigurationChanged", newConfig.toString());
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onCreate() {		
		LoggingUtils.init(this);
		log("onCreate");
		super.onCreate();
	}

	@Override
	public void onLowMemory() {
		log("onLowMemory");
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		log("onTerminate");
		super.onTerminate();
	}

}
