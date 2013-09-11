package jp.xii.code.android.pointdetector.test;

import jp.xii.code.android.pointdetector.SyncData;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class SyncDataTest extends ActivityInstrumentationTestCase2<SyncData> {
	
	SyncData activity;
	Context context;
	SharedPreferences pref;
	
	EditText userName;
	EditText passWord;
	Button loginButton;
	Button placeList;
	CheckBox GPSSetting;
	
	public SyncDataTest() {
		super(SyncData.class);
	}
	
	// セットアップ(onCreateみたいなもの?)
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		activity = getActivity();
		context = activity.getApplication().getApplicationContext();
		
		// デバッグモードを有効化
		activity.setDebugMode(true);
		
		// 設定項目読み出し用
		pref = PreferenceManager.getDefaultSharedPreferences(context);
		
		// ログインフォーム
		userName = (EditText)activity.findViewById(
				jp.xii.code.android.pointdetector.R.id.editText_User);
		passWord = (EditText)activity.findViewById(
				jp.xii.code.android.pointdetector.R.id.editText_Password);
		loginButton = (Button)activity.findViewById(
				jp.xii.code.android.pointdetector.R.id.get_button);
		placeList = (Button)activity.findViewById(
				jp.xii.code.android.pointdetector.R.id.button_placelist);
		GPSSetting = (CheckBox)activity.findViewById(
				jp.xii.code.android.pointdetector.R.id.checkBox_stopgps);
		
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testログインフォームでユーザ名とパスワードが空() throws Exception {
		
		// case 1
		// ユーザー名とパスワードが空
		activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	
        		userName.setText("");
        		passWord.setText("");
        		
                loginButton.performClick();
                
                assertEquals(activity.NOUSERNAME, activity.getStatusReason());
                
            }
        });
		this.getInstrumentation().waitForIdleSync();
		
	}
	
	public void testログインフォームでユーザー名のみ空() throws Exception {
		
		// case 2
		// ユーザー名のみ空
		activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	
        		userName.setText("");
        		passWord.setText("hogehoge");
        		
                loginButton.performClick();
                
                assertEquals(activity.NOUSERNAME, activity.getStatusReason());
                
            }
        });
		this.getInstrumentation().waitForIdleSync();
		
	}
	
	public void testログインフォームでパスワードのみ空() throws Exception {
		
		// case 3
		// パスワードのみ空
		activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	
        		userName.setText("hogehoge");
        		passWord.setText("");
        		
                loginButton.performClick();
                
                assertEquals(activity.NOPASSWORD, activity.getStatusReason());
                
            }
        });
		this.getInstrumentation().waitForIdleSync();
		
	}
	
	public void testログインフォームで認証失敗() throws Exception {
		
		// case 4
		// 認証失敗
		activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	
        		userName.setText("hogehoge");
        		passWord.setText("fugofugo");
        		
                loginButton.performClick();
                
                assertEquals(activity.NOTMATCHNAMEPASS, activity.getStatusReason());
                
            }
        });
		this.getInstrumentation().waitForIdleSync();
		
	}
	
	public void testログインフォームで認証成功() throws Exception {
		
		// case 5
		// 認証成功
		activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	
        		userName.setText("testuser");
        		passWord.setText("test1234");
        		
                loginButton.performClick();
                
                assertEquals(activity.SUCCESSLOGIN, activity.getStatusReason());
                
            }
        });
		this.getInstrumentation().waitForIdleSync();
		
	}
	
}
