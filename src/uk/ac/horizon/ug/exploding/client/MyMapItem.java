package uk.ac.horizon.ug.exploding.client;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

/** just a test for now */
class MyMapItem extends OverlayItem {

	public MyMapItem(GeoPoint point, String title, String snippet) {
		super(point, title, snippet);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Drawable getMarker(int stateBitset) {
		// TODO Auto-generated method stub
		//Log.d(TAG,"getmarker("+stateBitset+")="+this.mMarker);
		return this.mMarker;
		//return super.getMarker(stateBitset);
	}
	
}