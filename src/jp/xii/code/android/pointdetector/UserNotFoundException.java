package jp.xii.code.android.pointdetector;

public class UserNotFoundException extends AuthException {
	
	private static final long serialVersionUID = -4215470496913327529L;

	public UserNotFoundException () {
		super("User Not Found");
	}
	
}
