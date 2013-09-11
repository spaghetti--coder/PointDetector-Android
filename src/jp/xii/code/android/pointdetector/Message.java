package jp.xii.code.android.pointdetector;

public class Message {
	private boolean _result;
	private String _message;
	
	public Message() {
		
	}
	
	public Message(boolean result, String message) {
		this.setResult(result);
		this.setMessage(message);
	}
	
	public String toString () {
		return "result:" + Boolean.toString(this.getResult()) + "/message:" + this.getMessage();
	}
	
	public boolean getResult() {
		return _result;
	}
	public void setResult(boolean result) {
		_result = result;
	}
	
	public String getMessage() {
		return _message;
	}
	public void setMessage(String message) {
		_message = message;
	}
	
}
