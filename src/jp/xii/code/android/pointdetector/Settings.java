package jp.xii.code.android.pointdetector;

import java.util.List;

public class Settings {
	
	private final static int MODE_NONE = -1;
	private final static int MODE_UI = 0;
	private final static int MODE_M = 1;
	
	public int mode;
	
	public UserInfo userinfo;
	public List<TargetPlace> targetplace;
	public Message message;
	
	public Settings () {
		mode = MODE_NONE;
	}
	
	public Settings (UserInfo uinfo, List<TargetPlace> tplace) {
		mode = MODE_UI;
		userinfo = uinfo;
		targetplace = tplace;
	}
	public Settings (Message msg) {
		mode = MODE_M;
		message = msg;
	}
	
	public UserInfo getUserInfo () {
		return userinfo;
	}
	
	public List<TargetPlace> getTargetPlace () {
		return targetplace;
	}
	
	public Message getMessage() {
		return message;
	}
	
	public int getMode() {
		return mode;
	}
	
}
