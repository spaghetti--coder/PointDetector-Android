package jp.xii.code.android.pointdetector;

public class TargetPlace {
	
	public int id;
	public String point_name;
	public float latitude;
	public float longitude;
	
	public String toString()
	{
		return "ID:" + id + "/地点名:" + point_name + "/緯度:" + latitude + "/経度:" + longitude;
	}
	
	public int getID()
	{
		return id;
	}
	
	public String getPointName()
	{
		return point_name;
	}
	
	public float getLatitude()
	{
		return latitude;
	}
	
	public float getLongitude()
	{
		return longitude;
	}
}
