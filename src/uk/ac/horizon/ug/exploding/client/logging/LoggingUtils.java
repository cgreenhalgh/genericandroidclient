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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONStringer;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * @author cmg
 *
 */
public class LoggingUtils {
	
	/** header type */
	public static final String LOGTYPE_HEADER = "LogHeader-v1";
	public static final String LOGTYPE_UNCAUGHT_EXCEPTION = "UncaughtException";
	
	/** determines directory to log to (no /s) */
	private static String applicationDirName;
	/** standard top-level directory */
	private static final String LOG_ROOT_DIR = "/sdcard/";
	private static final String TAG = "LoggingUtils";
	/** output stream */
	private static BufferedWriter logWriter;
	/** log file */
	private static File logFile;
	/** log to android Log aswell */
	private static boolean logToAndroid = false;
	/** init */
	public static synchronized void init(Context context) {
		String packageName = context.getApplicationInfo().packageName;
		if (applicationDirName!=null) {
			if (applicationDirName.equals(packageName))
				// no-op
				return;
			// close and re-initialise?
			Log.w(TAG,"Re-initialise logging with different package name: "+packageName+" vs "+applicationDirName);
			// TODO Log
			return;
		}
		applicationDirName = packageName;
		String fileName = "log_"+System.currentTimeMillis()+".json";
		File dir = new File(LOG_ROOT_DIR, applicationDirName);
		if (dir.mkdir())
			Log.i(TAG,"Created log directory "+dir);
		if (!dir.exists()) {
			Log.e(TAG,"Log directory does not exist: "+dir+" - cannot log");
			return;
		}
		if (!dir.isDirectory()) {
			Log.e(TAG,"Log directory is not a directory: "+dir+" - cannot log");
			return;
		}
		File file = new File(LOG_ROOT_DIR+applicationDirName, fileName);
		if (file.exists())
			Log.w(TAG,"Appending to existing log: "+file);
		try {
			logWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), Charset.forName("UTF-8")));
			Log.i(TAG,"Logging to "+file);
			logFile = file;
		}
		catch (Exception e) {
			Log.e(TAG,"Opening log file "+file+" - cannot log", e);
			logWriter = null;
		}
		logHeader(context);
		
		// dump exceptions!
		final UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
			
			@Override
			public void uncaughtException(Thread thread, Throwable ex) {
				// TODO Auto-generated method stub
				try {
					JSONStringer js = new JSONStringer();
					js.object();
					js.key("thread");
					js.value(thread.getName());
					js.key("exception");
					js.value(ex.toString());
					js.key("stackTrace");
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					ex.printStackTrace(pw);
					js.value(sw.getBuffer().toString());
					js.endObject();
					log(LOGTYPE_UNCAUGHT_EXCEPTION, js.toString());				
				}
				catch (Exception e) {
					Log.e(TAG,"Logging uncaught exception", e);
				}
				// cascade
				if (handler!=null)
					handler.uncaughtException(thread, ex);
			}
		});
	}
	/** get log file (or null) */
	public static synchronized File getLogFile() {
		return logFile;
	}
	/**
	 * @param context
	 */
	private static void logHeader(Context context) {
		TelephonyManager mTelephonyMgr = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = mTelephonyMgr.getDeviceId(); // Requires READ_PHONE_STATE  

		try {
			JSONStringer js = new JSONStringer();
			js.object();
			js.key("imei");
			js.value(imei);
			js.key("time");
			js.value(System.currentTimeMillis());
			js.key("package");
			js.value(context.getApplicationInfo().packageName);
			js.key("application");
			js.value(context.getApplicationInfo().name);
			js.key("os.name");
			js.value(System.getProperty("os.name"));
			js.key("os.arch");
			js.value(System.getProperty("os.arch"));
			js.key("os.version");
			js.value(System.getProperty("os.version"));
			
            PackageManager pm = context.getPackageManager(); 
            try { 
            	PackageInfo pi; 
            	// Version 
            	pi = pm.getPackageInfo(context.getPackageName(), 0); 
            	js.key("versionName");
            	js.value(pi.versionName); 
            	js.key("versionCode");
            	js.value(pi.versionCode); 
            } catch (NameNotFoundException e) { 
            	Log.e(TAG,"Checking package info", e);
            } 
        	js.key("phoneModel");
        	js.value(android.os.Build.MODEL);
        	js.key("androidVersion");
        	js.value(android.os.Build.VERSION.RELEASE); 
			
			js.endObject();
			log(LOGTYPE_HEADER, js.toString());
		}
		catch (Exception e) {
			Log.e(TAG,"Creating log data (header)", e);
		}
	}
	/**
	 * @param typeHeader
	 * @param string
	 */
	public static void log(String type, String data) {
		log(System.currentTimeMillis(), type, data);
	}
	/**
	 * @param currentTimeMillis
	 * @param type
	 * @param data
	 */
	public synchronized static void log(long time, String type, String data) {
		// TODO Auto-generated method stub
		if (logToAndroid || logWriter==null)
			Log.d(TAG,"log("+time+","+type+","+data+")");

		if (logWriter==null) 
			return;

		try {
			logWriter.write(new Long(time).toString());
			logWriter.write(":");
			logWriter.write(type);
			logWriter.write(":");
			logWriter.write(data);
			logWriter.write("\n");
			logWriter.flush();
		} catch (IOException e) {
			Log.e(TAG,"log("+time+","+type+","+data+")", e);
		}
	}
}
