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
package uk.ac.horizon.ug.exploding.client.logging;

import org.json.JSONStringer;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * @author cmg
 *
 */
public class ActivityLogger {

	public ActivityLogger(Activity activity) {
		activityClass = activity.getClass();
		activityHashCode = activity.hashCode();
	}
	
	public static final String LOGTYPE_ACTIVITY = "Activity";
	private static final String TAG = "ActivityLogger";
	private Class activityClass;
	private int activityHashCode;
	
	public void log(String method) {
		log(method, null, null);
	}
	public void log(String method, String extraKey, Object extraValue) {
		try {
			JSONStringer js = new JSONStringer();
			js.object();
			js.key("method");
			js.value(method);
			js.key("class");
			js.value(activityClass.getName());
			js.key("hashCode");
			js.value(activityHashCode);
			if (extraKey!=null) {
				js.key(extraKey);
				js.value(extraValue);
			}
			js.endObject();
			LoggingUtils.log(LOGTYPE_ACTIVITY, js.toString());
		}
		catch (Exception e) {
			Log.e(TAG,"log("+method+","+extraKey+","+extraValue+")", e);
		}
	}
	
	public void logCloseContextMenu() {
		log("closeContextMenu");
	}

	public void logCloseOptionsMenu() {
		log("closeOptionsMenu");
	}

	public void logFinish() {
		log("finish");
	}

	public void logOnBackPressed() {
		log("onBackPressed");
	}

	public void logOnContextItemSelected(MenuItem item) {
		log("onContextItemSelected", "item", item.getTitle().toString());
	}

	public void logOnContextMenuClosed(Menu menu) {
		log("onContextMenuClosed");
	}

	public void logOnCreate(Context context, Bundle savedInstanceState) {
		LoggingUtils.init(context);
		log("onCreate");
	}

	public void logOnDestroy() {
		log("onDestroy");
	}

	public void logOnMenuOpened(int featureId, Menu menu) {
		log("onMenuOpened","featureId", featureId);
	}

	public void logOnNewIntent(Intent intent) {
		log("onNewIntent", "intent", intent.toString());
	}

	public void logOnOptionsItemSelected(MenuItem item) {
		log("onOptionsItemSelected", "item", item.getTitle().toString());
	}

	public void logOnOptionsMenuClosed(Menu menu) {
		log("onOptionsMenuClosed");
	}

	public void logOnPause() {
		log("onPause");
	}

	public void logOnPrepareDialog(int id, Dialog dialog) {
		log("onPrepareDialog", "id", id);
	}

	public void logOnPrepareOptionsMenu(Menu menu) {
		log("onPrepareOptionsMenu");
	}

	public void logOnRestart() {
		log("onRestart");
	}

	public void logOnResume() {
		log("onResume");
	}

	public void logOnStart() {
		log("onStart");
	}

	public void logOnStop() {
		log("onStop");
	}

	public void logStartActivity(Intent intent) {
		log("startActivity", "intent", intent.toString());
	}

	public void logStartActivityForResult(Intent intent, int requestCode) {
		log("startActivityForResult", "intent", intent.toString());
	}

	public void logStartActivityFromChild(Activity child, Intent intent,
			int requestCode) {
		log("startActivityFromChild", "intent", intent.toString());
	}

	public void logStartActivityIfNeeded(Intent intent, int requestCode) {
		log("startActivityIfNeeded", "intent", intent.toString());
	}

	
}
