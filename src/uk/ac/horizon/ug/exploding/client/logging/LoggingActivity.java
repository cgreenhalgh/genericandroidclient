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
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * @author cmg
 *
 */
public class LoggingActivity extends Activity {

	protected ActivityLogger activityLogger = new ActivityLogger(this); 
	
	protected void log(String method) {
		activityLogger.log(method, null, null);
	}
	protected void log(String method, String extraKey, Object extraValue) {
		activityLogger.log(method, extraKey, extraValue);
	}
	
	@Override
	public void closeContextMenu() {
		activityLogger.logCloseContextMenu();
		super.closeContextMenu();
	}

	@Override
	public void closeOptionsMenu() {
		activityLogger.logCloseOptionsMenu();
		super.closeOptionsMenu();
	}

	@Override
	public void finish() {
		activityLogger.logFinish();
		super.finish();
	}

	@Override
	public void onBackPressed() {

		activityLogger.logOnBackPressed();
		super.onBackPressed();
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		activityLogger.logOnContextItemSelected(item);
		return super.onContextItemSelected(item);
	}

	@Override
	public void onContextMenuClosed(Menu menu) {
		activityLogger.logOnContextMenuClosed(menu);
		super.onContextMenuClosed(menu);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		activityLogger.logOnCreate(this, savedInstanceState);
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		activityLogger.logOnDestroy();
		super.onDestroy();
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		activityLogger.logOnMenuOpened(featureId, menu);
		return super.onMenuOpened(featureId, menu);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		activityLogger.logOnNewIntent(intent);
		super.onNewIntent(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		activityLogger.logOnOptionsItemSelected(item);
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		activityLogger.logOnOptionsMenuClosed(menu);
		super.onOptionsMenuClosed(menu);
	}

	@Override
	protected void onPause() {
		activityLogger.logOnPause();
		super.onPause();
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		activityLogger.logOnPrepareDialog(id, dialog);
		super.onPrepareDialog(id, dialog);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		activityLogger.logOnPrepareOptionsMenu(menu);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	protected void onRestart() {
		activityLogger.logOnRestart();
		super.onRestart();
	}

	@Override
	protected void onResume() {
		activityLogger.logOnResume();
		super.onResume();
	}

	@Override
	protected void onStart() {
		activityLogger.logOnStart();
		super.onStart();
	}

	@Override
	protected void onStop() {
		activityLogger.logOnStop();
		super.onStop();
	}

	@Override
	public void startActivity(Intent intent) {
		activityLogger.logStartActivity(intent);
		super.startActivity(intent);
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		activityLogger.logStartActivityForResult(intent, requestCode);
		super.startActivityForResult(intent, requestCode);
	}

	@Override
	public void startActivityFromChild(Activity child, Intent intent,
			int requestCode) {
		activityLogger.logStartActivityFromChild(child, intent, requestCode);
		super.startActivityFromChild(child, intent, requestCode);
	}

	@Override
	public boolean startActivityIfNeeded(Intent intent, int requestCode) {
		activityLogger.logStartActivityIfNeeded(intent, requestCode);
		return super.startActivityIfNeeded(intent, requestCode);
	}

	
}
