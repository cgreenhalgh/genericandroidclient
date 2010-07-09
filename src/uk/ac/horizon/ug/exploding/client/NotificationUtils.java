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

import uk.ac.horizon.ug.exploding.client.model.Message;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * @author cmg
 *
 */
public class NotificationUtils {

	private static final String TAG = "NotificationUtils";

	/**
	 * @param gameMapActivity
	 * @param message
	 */
	public static void postMessage(Context context,
			Message message) {
		
		Log.d(TAG, "Handle message "+message);
		// TODO
		Toast.makeText(context, "["+message.getType()+"] "+message.getYear()+" "+message.getTitle()+": "+message.getDescription(), Toast.LENGTH_LONG).show();
	}

}
