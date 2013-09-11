package jp.xii.code.android.pointdetector.test;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import jp.xii.code.android.pointdetector.PointDetector;
import android.content.Context;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.test.ActivityInstrumentationTestCase2;

public class PointDetectorTest extends
		ActivityInstrumentationTestCase2<PointDetector> {
	
	private PointDetector activity;
	private Context context;
	private SharedPreferences pref;
	private String userName, loginHash;
	private int SPID;
	private String SPName;
	private float SPLat, SPLng;
	private boolean oneTime;
	private Map<String, Object> illegalPreferences =
			new HashMap<String, Object>();
	
	// コンストラクタ(superは必ず実行する必要あり)
	public PointDetectorTest() {
		super(PointDetector.class);
	}
	
	// セットアップ(onCreateみたいなもの?)
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		activity = getActivity();
		context = activity.getApplication().getApplicationContext();
		
		// デバッグモードに設定
		activity.setDebugMode(true);
		
		// 設定項目読み出し用
		pref = PreferenceManager.getDefaultSharedPreferences(context);
		
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	// 設定値を全て読み出し
	private boolean readAllPreferences() throws Exception {
		
		boolean res = false;
		int c = 0;
		
		Map<String, ?> prefMap = pref.getAll();
		
		for(Entry<String, ?> entry : prefMap.entrySet()){
			String key = entry.getKey();
			Object value = entry.getValue();
			
			if (key == "user_name") {
				userName = (String)value;
				c++;
			} else if (key == "set_place_id") {
				SPID = (Integer)value;
				c++;
			} else if (key == "set_place_name") {
				SPName = (String)value;
				c++;
			} else if (key == "set_place_latitude") {
				SPLat = (Float)value;
				c++;
			} else if (key == "set_place_longitude") {
				SPLng = (Float)value;
				c++;
			} else if (key == "login_hash") {
				loginHash = (String)value;
				c++;
			} else if (key == "one_time") {
				oneTime = (Boolean)value;
				c++;
			} else {
				illegalPreferences.put(key, value);
			}
			
		}
		
		if (c == 7) {
			res = true;
		}
		
		return res;
		
	}	
	
	// 不正ローカル値を読み取り、文字列にして返却
	private String concatIllegalPreferences() throws Exception {
		
		String ret = " ";
		for(Entry<String, ?> entry : illegalPreferences.entrySet()) {
			String key = "[".concat(entry.getKey());
			String value = "/".concat((String)entry.getValue() + "] ");
			ret.concat(key.concat(value) + " ");
		}
		return ret;
		
	}
	
	public void testローカル値格納確認() throws Exception {
		
		// ログイン中かどうかを確認
		boolean flagLogin = activity.isFlagLogin();
		
		boolean readPrefRes = false;
		
		if (flagLogin) { // ログイン中
			
			readPrefRes = readAllPreferences();
			if (illegalPreferences.size() > 0) {
				fail("[ログイン中]ローカル値に不正な値が混入しています".concat(concatIllegalPreferences()));
			}
			
			assertTrue("[ログイン中]全てのローカル値を読めません", readPrefRes);
			
		} else { // 未ログイン
			
			try {
				
				readPrefRes = readAllPreferences();
				if (illegalPreferences.size() > 0) {
					fail("[未ログイン]ローカル値に不正な値が混入しています ".concat(concatIllegalPreferences()));
				}
				
				assertTrue("[未ログイン]全てのローカル値を読めません", readPrefRes);
				
			} catch (Exception e) {
				
				// 未ログインで値格納されていない👉一度もログインしたこと無い👉正常
				
			}
			
		}
		
	}
	
}
