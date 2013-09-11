package jp.xii.code.android.pointdetector;

public class DoNotMatchIDandPasswordException extends AuthException {

	private static final long serialVersionUID = 6928715168676940758L;

	public DoNotMatchIDandPasswordException() {
		super("do not match id and password.");
	}

}
