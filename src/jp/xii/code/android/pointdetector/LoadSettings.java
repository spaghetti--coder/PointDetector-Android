package jp.xii.code.android.pointdetector;

import java.util.List;

public class LoadSettings {
	public UserInfo userinfo;
	public List<TargetPlace> targetplace;
	public boolean result;
	public String message;
	
	public LoadSettings (UserInfo uinfo, List<TargetPlace> tplace) {
		userinfo = uinfo;
		targetplace = tplace;
	}
	public LoadSettings (Boolean res, String msg) {
		result = res;
		message = msg;
	}
	
	public UserInfo getUserInfo () {
		return userinfo;
	}
	
	public List<TargetPlace> getTargetPlace () {
		return targetplace;
	}
	
	public boolean getResult() {
		return result;
	}
	
	public String getMessage() {
		return message;
	}
	
}
