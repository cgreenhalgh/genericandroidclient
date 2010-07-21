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

import android.content.Context;
import android.location.Location;
import android.util.Log;
import uk.ac.horizon.ug.exploding.client.model.Position;
import uk.ac.horizon.ug.exploding.client.model.Zone;

/** Not really a service, but implements the getZone functionality from
 * the original ZoneService against the current cached state.
 * 
 * @author cmg
 *
 */
public class ZoneService {
	
	private static final String TAG = "ZoneService";
	/** called by Location Utils on position update */
	static public void updateLocation(Context context, Location loc) {
		if (loc==null)
			return;
		double latitude = loc.getLatitude();
		double longitude = loc.getLongitude();
		Zone zone = getZone(context, latitude, longitude);
		Log.d(TAG, "UpdateLocation to "+latitude+","+longitude+", zone="+zone+" ("+(zone!=null ? zone.getName() : "null")+", "+(zone!=null && zone.isSetOrgId() ? zone.getOrgId() : 0));
		BackgroundThread.setLocation(loc, zone!=null ? zone.getName() : null, zone!=null && zone.isSetOrgId() ? zone.getOrgId() : 0);
	}
	
	static public Zone getZone(Context context, double latitude, double longitude){
		ClientState clientState = BackgroundThread.getClientState(context);
		if (clientState==null || clientState.getCache()==null) 
			return null;
		List<Object> zones = clientState.getCache().getFacts(Zone.class.getName());
    	for(Object z : zones)
		{
    		Zone zone = (Zone)z;
    		if (zone.getName()!=null && ("main".equals(zone.getName().toLowerCase()) || zone.getName().startsWith("~")))
    			continue;
    		Position ps [] = zone.getCoordinates();
    		if (polygonContains(ps, latitude, longitude)){
    			return zone;
    		}
		}
    	return null; 
    }
	// fudge
	static final double MAX_GAME_AREA_M = 10000;
	static public boolean outsideGameArea(Context context, double latitude, double longitude) {
		ClientState clientState = BackgroundThread.getClientState(context);
		if (clientState==null || clientState.getCache()==null) 
			return false;
		List<Object> zones = clientState.getCache().getFacts(Zone.class.getName());
		boolean outside = false;
    	for(Object z : zones)
		{
    		Zone zone = (Zone)z;
			Position ps [] = zone.getCoordinates();
			if (zone.getName()!=null && ("main".equals(zone.getName().toLowerCase()) || zone.getName().startsWith("~"))) {
				if (outside) 
					Log.w(TAG,"More than one zone is main or ~... (i.e. game area)");
				if (!polygonContains(ps, latitude, longitude)){
					
					// MAX range check?!
					double distance = LocationUtils.getDistance(latitude, longitude, ps[0].getLatitude(), ps[0].getLongitude());
					if (distance < MAX_GAME_AREA_M)
						outside = true;
					else
						Log.d(TAG,"outsideGameArea ignoring game area "+zone.getName()+": distance="+distance);
				}	
    		}
    		else {
				if (polygonContains(ps, latitude, longitude))
					return true;
    		}

		}
    	return outside; 
	}
	/**
     * Checks if the Polygon contains a point.
     * @see "http://alienryderflex.com/polygon/"
     * @param x Point horizontal pos.
     * @param y Point vertical pos.
     * @return Point is in Poly flag.
     */
 
	static public boolean polygonContains(Position ps[], double latitude, double longitude) {

		boolean oddTransitions = false;
		for( int i = 0, j = ps.length-1; i < ps.length; j = i++ ) {

			if( ( ps[ i ].getLatitude() < latitude && ps[ j ].getLatitude() >= latitude ) || ( ps[ j ].getLatitude() < latitude && ps[ i ].getLatitude() >= latitude ) ) {
				if( ps[ i ].getLongitude() + ( latitude - ps[ i ].getLatitude() ) / ( ps[ j ].getLatitude() - ps[ i ].getLatitude() ) * ( ps[ j ].getLongitude() - ps[ i ].getLongitude() ) < longitude ) {
					oddTransitions = !oddTransitions;          
				}
			}
		}

		return oddTransitions;
	}
}
