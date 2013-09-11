package jp.xii.code.android.pointdetector;

public class ServerParams {
	
	private final static int MODE_NONE = -1;
	private final static int MODE_AUTH = 0;
	private final static int MODE_SETTARGET = 1;
	private final static int MODE_SENDLOCATION = 2;
	
	private int _mode;
	private String _server;
	private String _username;
	private String _password;
	private int _pointid;
	private String _hash;
	private double _latitude;
	private double _longitude;
	private long _timestamp;
	
	public ServerParams() {
		setMode(MODE_NONE);
	}
	
	// ユーザー名とパスワードで認証
	public ServerParams (String server, String username, String password) {
		this.setMode(MODE_AUTH);
		this.setServer(server);
		this.setUserName(username);
		this.setPassword(password);
	}
	
	// 目標地点設定モード
	public ServerParams (String server, String username, int pointid, String hash) {
		this.setMode(MODE_SETTARGET);
		this.setServer(server);
		this.setUserName(username);
		this.setPointID(pointid);
		this.setHash(hash);
	}
	
	// ユーザー地点報告モード
	public ServerParams (String server, String username, double latitude, double longitude, long timestamp, String hash) {
		this.setMode(MODE_SENDLOCATION);
		this.setServer(server);
		this.setUserName(username);
		this.setLatitude(latitude);
		this.setLongitude(longitude);
		this.setTimestamp(timestamp);
		this.setHash(hash);
	}
	
	public int getMode() {
		return _mode;
	}

	public void setMode(int mode) {
		this._mode = mode;
	}

	public String getServer() {
		return _server;
	}

	public void setServer(String server) {
		this._server = server;
	}

	public String getUserName() {
		return _username;
	}

	public void setUserName(String username) {
		this._username = username;
	}

	public String getPassword() {
		return _password;
	}

	public void setPassword(String password) {
		this._password = password;
	}

	public int getPointID() {
		return _pointid;
	}

	public void setPointID(int pointid) {
		this._pointid = pointid;
	}

	public String getHash() {
		return _hash;
	}

	public void setHash(String hash) {
		this._hash = hash;
	}

	public double getLatitude() {
		return _latitude;
	}

	public void setLatitude(double _latitude) {
		this._latitude = _latitude;
	}

	public double getLongitude() {
		return _longitude;
	}

	public void setLongitude(double _longitude) {
		this._longitude = _longitude;
	}

	public long getTimestamp() {
		return _timestamp;
	}

	public void setTimestamp(long _timestamp) {
		this._timestamp = _timestamp;
	}
	
}
