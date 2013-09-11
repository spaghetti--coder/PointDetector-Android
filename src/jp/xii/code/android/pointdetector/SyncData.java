package jp.xii.code.android.pointdetector;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

public class SyncData extends Activity {
	
	// JSON取得データを格納
	List<TargetPlace> targetPlace = new ArrayList<TargetPlace>();
	
	// ログイン先URL
	private static final String KEY_URL_BASE = "http://133.242.188.66/";
	
	// ユーザー名一時保管
	private String user_name = "";
	
	private Context context;
	
	private boolean flag_login = false;
	
    PlaceList listInfo = null;
    Integer pointID[] = null;
    CharSequence pointName[] = null;
    Float[] pointLatitude = null;
    Float[] pointLongitude = null;
    
    private boolean debugMode = false;
	
    // エラー定数
    public final int NOERROR = 0;
    public final int NOUSERNAME = -1;
    public final int NOPASSWORD = -2;
    public final int NOTMATCHNAMEPASS = -3;
    public final int PROTOCOLERROR = -4;
    public final int MALFORMEDURL = -5;
    public final int FILENOTFOUND = -6;
    public final int EOFERROR = -7;
    public final int IOERROR = -8;
    public final int GENERALERROR = -100;
    public final int UNKNOWNERROR = -200;
    
    // 成功定数
    public final int SUCCESSLOGIN = 1;
    
    // ステータス情報格納
    private int StatusReason = NOERROR;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE); // タイトルバー非表示
        
        context = (Context)this;
        
		setContentView(R.layout.activity_sync_data);
		
		// インテントから、ログイン中かどうかのフラグを受取り
		Intent intent = getIntent();
		if (intent != null) {
			flag_login = intent.getBooleanExtra("flag_login", false);
		}
		
		// リンクを反映
		TextView textlink = (TextView) findViewById(R.id.create_new_account);
		textlink.setMovementMethod(LinkMovementMethod.getInstance());
		
		// ログイン中であれば、ログイン中である旨の文字を表示する
		if (flag_login) {
			TextView loginmsg = (TextView) findViewById(R.id.result_text_view);
			loginmsg.setText(R.string.login_already);
		}
		
		// 設定マネージャから、ユーザー名・パスワード・地点名を呼び出し
        final SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        // ユーザー名
        String userName = p.getString("user_name", "-");
        if (userName != "-") {
        	EditText user = (EditText)findViewById(R.id.editText_User);
        	user.setText(userName);
        	user_name = userName;
        }
        
		// 地点名
        final String currentPlaceName = p.getString("set_place_name", "");
    	TextView pointinfo = (TextView)findViewById(R.id.result_placelist);
        if (userName != "-" && currentPlaceName.isEmpty()) {
        	pointinfo.setTextColor(Color.RED);
        	pointinfo.setText(R.string.notselect_location);
        } else if (currentPlaceName.length() != 0) {
        	pointinfo.setTextColor(Color.WHITE);
        	pointinfo.setText(R.string.current_target_place);
        	pointinfo.setText(pointinfo.getText() + currentPlaceName);
        }
        
		// ログインボタン
        findViewById(R.id.get_button).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	
        		// ログインフォーム
        		EditText user = (EditText)findViewById(R.id.editText_User);
        		EditText password = (EditText)findViewById(R.id.editText_Password);
            	TextView result_login = (TextView)findViewById(R.id.result_text_view);
    	    	TextView result_target = (TextView)findViewById(R.id.result_placelist);
        		
            	// バリデーション
            	if (user.getText().length() == 0) {
            		
            		StatusReason = NOUSERNAME;
            		if (isDebugMode()) return;
            		
            		new AlertDialog.Builder(context)
            		.setTitle(R.string.dialog_error)
            		.setMessage(R.string.login_empty_username)
            		.setNegativeButton(R.string.dialog_button_abort,
            				new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									// 何もしない
									
								}
            			
            		})
            		.show();
            		
            		result_login.setTextColor(Color.RED);
                    result_login.setText(R.string.login_empty_username);
                    
            	} else if (password.getText().length() == 0) {
            		
            		StatusReason = NOPASSWORD;
            		if (isDebugMode()) return;
            		
            		new AlertDialog.Builder(context)
            		.setTitle(R.string.dialog_error)
            		.setMessage(R.string.login_empty_password)
            		.setNegativeButton(R.string.dialog_button_abort,
            				new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									// 何もしない
									
								}
            			
            		})
            		.show();
            		
            		result_login.setTextColor(Color.RED);
                    result_login.setText(R.string.login_empty_password);
                    
            	} else {
            		
            		result_login.setTextColor(Color.WHITE);
            		result_login.setText(""); // 空文字にする
            		result_target.setTextColor(Color.WHITE);
            		result_target.setText(""); // 空文字にする
            		
            		// ユーザー名保管
            		user_name = user.getText().toString();
            		
            		try {
            			
            			// 通常モード
            			if (!isDebugMode()) {
                			
                    		// 非同期通信へ渡す(AsyncTask)
                			MyTask task = new MyTask(SyncData.this);
                			task.execute(new ServerParams(
                    				KEY_URL_BASE, user_name, password.getText().toString()));
                			
                		// AsyncTaskの戻り値を使うと、進捗状況がまともに表示されなくなる(使用しない方が良い)
                		// デバッグ時のみ使用(メモリリークの原因になるダイアログを生成しない)
            			} else {
                			
                    		// 非同期通信へ渡す(AsyncTask)
                			MyTask task = new MyTask(SyncData.this, false);
                			task.execute(new ServerParams(
                    				KEY_URL_BASE, user_name, password.getText().toString()));
                			
            				Message result = task.get();
            				if (result.getResult()) {
            					
            					StatusReason = SUCCESSLOGIN;
            					
            				} else {
            					
            					String msg = result.getMessage();
            					
            					if (msg == "ユーザー名またはパスワードが間違っています") {
            						StatusReason = NOTMATCHNAMEPASS;
            					} else if (msg == "プロトコルエラーが発生しました") {
            						StatusReason = PROTOCOLERROR;
            					} else if (msg == "URIの形式が不正です") {
            						StatusReason = MALFORMEDURL;
            					} else if (msg == "ログイン結果を取得できませんでした(404)") {
            						StatusReason = FILENOTFOUND;
            					} else if (msg == "ログイン結果を取得できませんでした(EOF)") {
            						StatusReason = EOFERROR;
            					} else if (msg == "ログイン結果を取得できませんでした(IO)") {
            						StatusReason = IOERROR;
            					} else if (msg == "何らかのエラーが発生いたしました") {
            						StatusReason = GENERALERROR;
            					} else {
            						StatusReason = UNKNOWNERROR;
            					}
            					
            				}
            			}
            	        
            		} catch (Exception e) {
            			
            			e.printStackTrace();
            			
            		}
            		
            	}
            	
            }
            
        });
        
        // 目標地点選択ボタン
		findViewById(R.id.button_placelist).setOnClickListener(new OnClickListener() {
			
			// 地点一覧をSQLiteから取得して返却するメソッド
			private void getTargetPoint () {
				
				// SQLiteを読み込んで、CharSequnseに表示するitemを追加
				SQLiteHelper helper = new SQLiteHelper(context);
				SQLiteDatabase dbr = helper.getReadableDatabase(); // 一旦読み込みで開く
				
				// 全データ読み込み
				Cursor c = dbr.rawQuery(
						"SELECT * FROM targetpoints ORDER BY _id DESC", null);
				
				boolean hasRecord = c.moveToFirst();
				int i = 0;
				Integer[] resultID = new Integer[c.getCount()];
				CharSequence[] resultPointName = new String[c.getCount()]; // 地点名(表示ラベル)
				Float[] resultLatitude = new Float[c.getCount()];
				Float[] resultLongitude = new Float[c.getCount()];
				while (hasRecord) {
					resultID[i] = c.getInt(0); // ID
					resultPointName[i] = c.getString(1); // 地点名
					resultLatitude[i] = c.getFloat(2); // 緯度
					resultLongitude[i] = c.getFloat(3); // 経度
					hasRecord = c.moveToNext();
					i++;
				}
				c.close();
				dbr.close();
		        
				pointID = resultID;
				pointName = resultPointName;
				pointLatitude = resultLatitude;
				pointLongitude = resultLongitude;
				
			}
			
			@Override
			public void onClick(View view) {
				
        		// 未ログイン/ハッシュ無しの際はエラーダイアログ出して終了
    	        if (user_name == "" || PreferenceManager.getDefaultSharedPreferences(context).getString("login_hash", "") == "") {
    	        	
    	        	new AlertDialog.Builder(SyncData.this)
    	        	.setTitle(R.string.dialog_error)
    	        	.setMessage(R.string.dialog_notlogin)
    	        	.setNegativeButton(R.string.dialog_button_abort, new DialogInterface.OnClickListener() {
    	        		
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							// nothing to do
							
						}
    	        		
    	        	})
    	        	.show();
    	        	
    	        // ログインしている場合
    	        } else {
    	        	
    				try {
    	        		
    					getTargetPoint(); // 地点一覧を更新する
    					
    			        new AlertDialog.Builder(SyncData.this)
    			        .setTitle(R.string.select_target_place)
    			        .setItems(pointName,
    			            new DialogInterface.OnClickListener(){
    			        	
    			        	public void onClick(DialogInterface dialog, int which) {
    			        		
    			        		// 設定マネージャ
    			    	        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
    			    	        // 地点更新部のテキスト
    			    	        TextView v = (TextView)findViewById(R.id.result_placelist);
//			        			// とりあえず更新中にしておく
//			        			v.setTextColor(Color.WHITE);
//			        			v.setText(R.string.setting_place);
			        			
			            		// 非同期通信へ渡す(AsyncTask)
			        			
			        			try {
			        				
    			            		MyTask task = new MyTask(SyncData.this);
    			            		task.execute(new ServerParams(
    			            				KEY_URL_BASE, user_name, pointID[which], p.getString("login_hash", "")));
			        				
    			            		Message result = task.get();
    			            		
    			            		// サーバー側に正常に反映された場合
    			            		if (result.getResult()) {
    			            			
            			        		// アンドロイド側に保存
            			    			Editor e = p.edit();
            			    			e.putInt("set_place_id", pointID[which]);
            			    			e.putString("set_place_name", pointName[which].toString());
            			    			e.putFloat("set_place_latitude", pointLatitude[which]);
            			    			e.putFloat("set_place_longitude", pointLongitude[which]);
            			    	        e.commit();
            			    	        
        			        			v.setTextColor(Color.WHITE);
//        			        			v.setText(result.getMessage());
            			    	        v.setText(context.getResources().getString(R.string.current_target_place)
            			    	        		+ pointName[which]);
        			        			
    			            		} else {
            			    	        
        			        			v.setTextColor(Color.RED);
        			        			v.setText(result.getMessage());
    			            			
    			            		}
    			            		
			        			} catch (Exception e) {
			        				
			        				e.printStackTrace();
			        				
			        			}
			        			
    			        	}
    			        	
    			        })
    			        .show();
    					
    				} catch (Exception e) {
    					
    					e.printStackTrace();
    					
    				}
    	        	
    	        }
				
			}
			
		});
		
		// 最初の測位でGPSをオフにするチェックボックス
		final CheckBox checkbox = (CheckBox)findViewById(R.id.checkBox_stopgps);
		Boolean onetime = p.getBoolean("one_time", true);
		
		if (onetime) {
			
			// チェックボックスに最初からチェックを入れておく
			checkbox.setChecked(true);
			
		} else {
			
			// チェックボックスのチェックを最初から外しておく
			checkbox.setChecked(false);
			
		}
		
		// チェックボックスクリック時の動作(設定マネージャへの書き込み)
		checkbox.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				
				// タップした際にチェックが入る(こっちが先)→isChecked()がtrueになる→one_timeもtrueにする
				if (checkbox.isChecked()) {
					
					Editor e = p.edit();
					e.putBoolean("one_time", true);
					e.commit();
					
				// タップした際にチェックが外れる(こっちが先)→isChecked()がfalseになる→one_timeもfalseにする
				} else {
					
					Editor e = p.edit();
					e.putBoolean("one_time", false);
					e.commit();
					
				}
				
			}
			
		});
		
		// 元のアプリに戻る(この設定アプリを終了)
		findViewById(R.id.button_back_application).setOnClickListener(new OnClickListener () {

			@Override
			public void onClick(View arg0) {
				
				finish();
				
			}
			
		});
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.sync_data, menu);
		return true;
	}
    
	public void resetAllPreferences () {
		
        SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(context);
        Editor e = p.edit();
        e.clear().commit();
        
	}
	
	public int getStatusReason() {
		return StatusReason;
	}

	public void setStatusReason(int statusReason) {
		StatusReason = statusReason;
	}
	
	public boolean isDebugMode() {
		return debugMode;
	}
	
	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}
	
}
