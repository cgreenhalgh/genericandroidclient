package uk.ac.horizon.ug.exploding.client;

import uk.ac.horizon.ug.exploding.client.model.Member;
import uk.ac.horizon.ug.exploding.client.model.Position;

import java.util.List;

import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;

/** just a test for now */
public class MyMapOverlay extends ItemizedOverlay<MyMapItem> implements ClientStateListener {
	private static final String TAG = "MyMapOverlay";
	private static final int MILLION = 1000000;
	private Drawable defaultMarker;
	private List<Object> members;
	
	public MyMapOverlay(Drawable defaultMarker, ClientState clientState) {
		super(defaultMarker);
		boundCenter(defaultMarker);
		clientStateChanged(clientState);
	}
	@Override
	protected MyMapItem createItem(int i) {
		Log.d(TAG,"CreateItem("+i+"), drawable="+defaultMarker);
		Member member = (Member)members.get(i);
		Position pos = member.getPosition();
		if (pos==null) {
			Log.e(TAG,"Member "+member.getID()+" has null position");
			pos = new Position();
		}
		// TODO sensible name?
		MyMapItem item = new MyMapItem(new GeoPoint((int)(pos.getLatitude()*MILLION),(int)(pos.getLongitude()*MILLION)), member.getPlayerID(), null);
		//item.setMarker(defaultMarker);
		return item;
	}

	@Override
	public int size() {
		if (members==null)
			return 0;
		return members.size();
	}
	/* (non-Javadoc)
	 * @see uk.ac.horizon.ug.exploding.client.ClientStateListener#clientStateChanged(uk.ac.horizon.ug.exploding.client.ClientState)
	 */
	@Override
	public synchronized void clientStateChanged(final ClientState clientState) {
		if (clientState==null  || clientState.getCache()==null) 
			members = null;
		else {
			members = clientState.getCache().getFacts(Member.class.getName());
		}
		Log.d(TAG,"Members changed: "+size()+" found");
		populate();		
	}
	
}