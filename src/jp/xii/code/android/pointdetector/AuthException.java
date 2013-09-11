package jp.xii.code.android.pointdetector;

public class AuthException extends Exception {

	private static final long serialVersionUID = -1521352932333750587L;

	public AuthException() {
		super("failed to synchronize server");
	}
	
	public AuthException (String message) {
		super(message);
	}
}
