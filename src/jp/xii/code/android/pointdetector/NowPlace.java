package jp.xii.code.android.pointdetector;

import android.location.LocationListener;
import android.location.LocationManager;

public class NowPlace {
	
	LocationManager lm = null;
	public double lat = 0;
	public double lon = 0;
	
	OverlaySurfaceView sv;
	
	// コンストラクタ
	public NowPlace (LocationManager locationManager) {
		if (lm == null) {
			lm = locationManager;
		}
	}
	
	public LocationManager getLocationManager () {
		return lm;
	}
	
	public void startLocationListener(LocationListener listener) {
    	if (lm != null) {
        	lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 10, listener);		
        	lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 100, 10, listener);
    	}
	}
	
	public void destoryLocationListener(LocationListener listener) {
    	if (lm != null) {
        	lm.removeUpdates(listener);	
    	}
	}

}
