package jp.xii.code.android.pointdetector;

/* Androidで特定GPSのポイントに3m以内に近づくとカメラプレビューの
 * 上に適当な画像を表示し、そのタイミングでtwitterにGPSポイントまでの
 * 距離とfpsを投稿するアプリです。
 * フレームレートも常時表示する様にし、20fps以上の動作をするアプリを
 * 作成して下さい。 */

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.RelativeLayout;

public class PointDetector extends Activity implements LocationListener {
    
    // ********************************************
    // ここまで
    // ********************************************
    
	// 送信URL
	private final static String SERVER = "http://133.242.188.66/";
	
	// 設定マネージャ
	private SharedPreferences pref;
	
    // レイアウト関連(幅(高さ)100%)
    private final static int MP = LayoutParams.MATCH_PARENT;
    
    private Context context;
    
    // 位置取得関連
	public LocationManager locationManager;
    public double[] now = {0,0};
	
    public NowPlace place;
    public OverlaySurfaceView sv;
    
    private FPSManager fpsManager = new FPSManager(); // 発見時間呼び出し用
    private Tweet tweet = new Tweet(this);
    
    private boolean flag_login = false;
	private boolean flag_logincheck = false;
    
    private String userName = "";
    
    private boolean debugMode = false;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        
        // コンテキスト
        context = this;
        
        // 設定マネージャ
        pref = PreferenceManager.getDefaultSharedPreferences(context);
        
        // ロケーションマネージャ
    	locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    	place = new NowPlace(locationManager);
    	
    	if (!debugMode) {
    		// カメラビューを取り出す
    		final CameraView cameraView = new CameraView(this);
        	cameraView.setZOrderMediaOverlay(false);
        	cameraView.setZOrderOnTop(false);
        	cameraView.setOwner(this);
        	this.setContentView(cameraView, new LayoutParams(MP, MP));
    	}
    	
        // カメラビューにかぶせるコントローラービュー
        RelativeLayout controlOverlayView = (RelativeLayout)this.getLayoutInflater().inflate(R.layout.activity_point_detector, null);
        this.addContentView(controlOverlayView, new LayoutParams(MP, MP));    
        
    }
	
	// sub menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.point_detector, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
    	switch (item.getItemId()) {
    	case R.id.item1:
    		tweet.tweetMsg(PreferenceManager.getDefaultSharedPreferences(this).getString("tweet_cache", ""));
    		break;
    	case R.id.item2:
    		startSyncActivity(); // 事実上の設定画面
    		break;
    	}
    	return true;
    }
    // end
	
    public void startSyncActivity () {
    	
    	Intent intent = new Intent();
    	intent.setClassName(this.getPackageName(), this.getPackageName() + ".SyncData");
    	intent.putExtra("flag_login", flag_login);
    	startActivity(intent);
    	
    }
    
    public void startApplyForm () {
    	
    	Uri uri = Uri.parse("http://www15052ui.sakura.ne.jp/apply/");
    	Intent intent = new Intent(Intent.ACTION_VIEW,uri);
    	startActivity(intent);
    	
    }
    
    public void onResume() {
		super.onResume();
    	place.startLocationListener(this);
    	tweet.setCalledTwitter(false);
    	fpsManager.setFoundTime(0l);
    	fpsManager.setElapsedTimefromFoundTime(0l);
		flag_logincheck = false;
    }
    
    public void onPause() {
		super.onPause();
    	place.destoryLocationListener(this);
    	tweet.setCalledTwitter(false);
    	fpsManager.setFoundTime(0l);
    	fpsManager.setElapsedTimefromFoundTime(0l);
		flag_logincheck = false;
    }
    
    public double[] getLocation() {
    	return now;
    }
    
    // 緯度・経度を取得したとき
	@Override
	public void onLocationChanged (Location location) {
		
		now[0] = location.getLatitude();
		now[1] = location.getLongitude();
		long timestamp = System.currentTimeMillis() / 1000L; // 取得時間(Unix time)
		
//		android.util.Log.d("LocationChanged", "got location [" +  timestamp + "] / Latitude:" + now[0] + " / Longitude:" + now[1]);
		
		// 緯度・経度情報を継承
		sv = new OverlaySurfaceView(this);
		sv.setPlace(now[0], now[1]);
		
		// ログインしているのであれば、ユーザーが取得した緯度・経度のデータを、サーバーサイドに送信する
		userName = pref.getString("user_name", "-");
//		android.util.Log.i("LocationChanged", "UserName:" + userName);
		
		if (userName != "-") {
			
//			android.util.Log.i("LocationChanged", "Logged in.");
			
			// ログインハッシュ
			String loginHash = pref.getString("login_hash", "");
			
			// ログインハッシュに何か値が入っているのであれば、位置情報を送信
			if (loginHash != null) {
        		
				//　随時、非同期通信(AsyncTask)で送信
				new MyTask(context, false).execute(new ServerParams(SERVER, userName, now[0], now[1], timestamp, loginHash));
				
			}
			
		// 未ログインの場合は、ログインを促す
		} else {
			
//			android.util.Log.i("LocationChanged", "Not Login.");
			
			if (!flag_logincheck) {
				
				new AlertDialog.Builder(this)
					.setTitle(R.string.dialog_login_title)
					.setMessage(R.string.dialog_login_message)
					.setPositiveButton(R.string.dialog_login_positive, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							
							startSyncActivity();
							
						}
					})
					.setNeutralButton(R.string.dialog_login_newtral, new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							startApplyForm();
							
						}
						
					})
					.show();
				
				flag_logincheck = true;
				
			}
			
		}
		
		// 一度情報を取得したらGPSを切る設定の場合はそうする
		if (pref.getBoolean("one_time", true)) {
			try {
				Thread.sleep(3000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			place.destoryLocationListener(this);
		}
	}
	
	@Override
	public void onProviderDisabled(String provider) {
	}
	
	@Override
	public void onProviderEnabled(String provider) {
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	
    public boolean isFlagLogin() {
		return flag_login;
	}
	public void setFlagLogin(boolean flag_login) {
		this.flag_login = flag_login;
	}
    
    public boolean isDebugMode() {
		return debugMode;
	}

	public void setDebugMode(boolean debugMode) {
		this.debugMode = debugMode;
	}
	
}
