package jp.xii.code.android.pointdetector;

public class UserPlace {
	
	public int id;
	public int user_id;
	public String point_name;
	public float latitude;
	public float longitude;
	public int got_at;
	
	public String toString() {
		return "ID:" + id + "/ユーザーID:" + user_id + "/地点名:" + point_name
				+ "/緯度:" + latitude + "/経度:" + longitude + "/取得日時スタンプ:" + got_at;
	}
	
	public int getID() {
		return id;
	}
	public int getUserID() {
		return user_id;
	}
	public String getPointName() {
		return point_name;
	}
	public float getLatitude() {
		return latitude;
	}
	public float getLongitude() {
		return longitude;
	}
	public int getGotat() {
		return got_at;
	}
	
}
