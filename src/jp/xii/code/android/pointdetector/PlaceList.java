package jp.xii.code.android.pointdetector;

public class PlaceList {
	public Integer ID[];
	public CharSequence PointName[];
	public Float Latitude[];
	public Float Longitude[];
	
	public PlaceList (Integer id[], CharSequence pointname[], Float latitude[], Float longitude[]) {
		ID = id;
		PointName = pointname;
		Latitude = latitude;
		Longitude = longitude;
	}
	
	public Integer[] getID() {
		return ID;
	}
	
	public CharSequence[] getPointName() {
		return PointName;
	}
	
	public Float[] getLatitude() {
		return Latitude;
	}
	
	public Float[] getLongitude() {
		return Longitude;
	}
	
}
