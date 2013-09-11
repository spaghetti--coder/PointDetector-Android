package jp.xii.code.android.pointdetector;

//カメラプレビューにOverlayするSurfaceViewを格納したクラス
//fps表示

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class OverlaySurfaceView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
 
 // 緯度経度からの2点間距離計算関連
 // 現在位置
 private static double latitude = 0;
 private static double longitude = 0;
 private double[] nowPlace = {0, 0};
 
 private Places place;
 
 // **********************************
 // 設定
 // **********************************
 
 // 表示する距離
 private final static float SETDISTANCE = 3.0f;
 
 // 画像表示時間(long型、ミリ秒で指定)
 private final static long SHOWIMAGETIME = 30000l;
 
 // **********************************
 // 設定ここまで
 // **********************************
 
 private boolean flag = false;
	private boolean flag_image = false;
 
	// 描画関連
 private int mScrWidth;  // 画面の幅
 private int mScrHeight; // 画面の高さ
 private SurfaceHolder mHolder;  // サーフェスホルダー
 
 private Thread mThreadMove; // 定期的に更新するためのスレッド
 
 private Context myContext;
 private FPSManager fpsManager = new FPSManager(10);
 
 //画像読み込み
 private Resources res = this.getContext().getResources();
 private Bitmap found = BitmapFactory.decodeResource(res, R.drawable.found);
 
 public long foundtime = 0l;
 public long elapsedtime = 0l;
 
 // コンストラクタ
 public OverlaySurfaceView(Context context) {
     super(context);
     myContext = context;
     
     // サーフェスホルダーを取り出す
     this.mHolder = this.getHolder();
     this.mHolder.setFormat(PixelFormat.TRANSPARENT);
      
     // コールバック関数を登録する
     this.mHolder.addCallback(this);
     this.setZOrderMediaOverlay(true);
 }
 
 public OverlaySurfaceView(Context context, AttributeSet attrs, int defStyle) {
     super(context, attrs, defStyle);
     myContext = context;

     // サーフェスホルダーを取り出す
     this.mHolder = this.getHolder();
     this.mHolder.setFormat(PixelFormat.TRANSPARENT);
      
     // コールバック関数を登録する
     this.mHolder.addCallback(this);
     this.setZOrderMediaOverlay(true);
 }
 
 public OverlaySurfaceView(Context context, AttributeSet attrs) {
     super(context, attrs);
     myContext = context;

     // サーフェイホルダーを取り出す
     this.mHolder = this.getHolder();
     this.mHolder.setFormat(PixelFormat.TRANSPARENT);
      
     // コールバック関数を登録する
     this.mHolder.addCallback(this);
 }

	// サーフェス変更時(画面回転時等)
 @Override
 public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
     this.mScrWidth = width;
     this.mScrHeight = height;
 }
 
 // サーフェスが作成されたとき
 @Override
 public void surfaceCreated(SurfaceHolder holder) {
      
     // 更新用スレッドの開始
     this.mThreadMove = new Thread(this);
     this.mThreadMove.start();
      
 }
 
 // 更新用スレッド
 public void start() {
     
     // 更新用スレッドの開始
     this.mThreadMove = new Thread(this);
     this.mThreadMove.start();
              
 }
 
 // サーフェス破壊時(アプリ終了時)
 @Override
 public void surfaceDestroyed(SurfaceHolder holder) {
 	// スレッド終了
     this.mThreadMove = null;
 }
 
 public void setPlace (double lat, double lon) {
 	latitude = lat;
 	longitude = lon;
 	if (!flag) {
 		flag = true;
 	}
 }
 
 @Override
 public void run() {
 	
 	// 距離計算関連
 	Coords dist;
 	double distance = -1.0f; // 現在位置情報取得前は-1にしておく
 	String d = "";
 	
 	// キャンバス
     Canvas canvas;
     
     // fps
     float fps = 0.0f;
     long ft = fpsManager.getFoundTime();
     long et = fpsManager.getElapsedTimefromFoundTime();
     
     // 発見時の画像表示位置を調整
     // 画像のwidth,height
     int found_width = found.getWidth();
     int found_height = found.getHeight();
     
     // 画像の表示位置を調整(中央に配置)
     int px = (mScrWidth - found_width) / 2;
     int py = (mScrHeight - found_height) / 2;
     
     // fpsを表示する文字
     Paint paint = new Paint();
     paint.setColor(Color.BLUE);
     paint.setAntiAlias(true);
     paint.setTextSize(60);
     
     // tweet関連フラグ
     Tweet tweet = new Tweet(myContext);
     String tweetmsg = "";
     
     // 設定マネージャ
     SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(myContext);
     
     // スレッド存続中は無限ループ
     while (this.mThreadMove != null) {
         canvas = null;
         
         fpsManager.calcFPS(); // fps計算メソッド
         fps = Math.round(fpsManager.getFPS() * 10.0f) / 10.0f; // fpsは小数第1位までにする
         
         try {
         	
             synchronized (this.mHolder) {
                 canvas = this.mHolder.lockCanvas();
                 
                 if (canvas == null)
                     continue;
                 
                 // 背景をクリア
                 canvas.drawColor(0, Mode.CLEAR);
                 
             	// 設定ポイントを取得
             	place = new Places(myContext);
             	double[] setPlace = place.getTargetPlace();
             	String targetPlaceName = place.getTargetPlaceName();
             	
             	// 設定地点取得不可(=未ログイン)の場合
             	if ((setPlace[0] == 0 && setPlace[1] == 0) || targetPlaceName == "-") { 
             		
             		// ツイートメッセージをセット
                 	tweetmsg = "ログインしてください (" + fps + "fps)";
                 	
                 	// ログインを促すメッセージ
                     paint.setColor(Color.BLUE);
                     canvas.drawText("ログインしてください", 10, 60, paint);
                     canvas.drawText(fps + "fps", 10, 130, paint);
                     
                 // ログインしている場合
             	} else {
             		
                     // GPSの2点間距離計算
                     nowPlace[0] = latitude;
                     nowPlace[1] = longitude;
                     if (nowPlace[0] == 0) {
                     	distance = -1;
                     	d = "search";
                     } else {
                         dist = new Coords(nowPlace, setPlace);
                         distance = dist.getDistance();
                         if (distance > 1000.0f) {
                         	d = String.format("%.2fkm", (distance / 1000.0f));
                         } else {
                         	d = String.format("%.1fm", distance);
                         }
                     }
                     
                     // 検索中(距離は初期設定の-1m)
                     if (d == "search") {
                         canvas.drawText("検索中...", 10, 60, paint);
                         canvas.drawText(fps + "fps", 10, 130, paint);
                     	tweetmsg = "｢" + targetPlaceName + "｣を検索中 (" + fps + "fps)";
                     
                     // 現在位置を特定てきた場合
                     } else {
                     	
                     	tweetmsg = "｢" + targetPlaceName + "｣まで" + d  + " (" + fps + "fps)";
                     	
                     	// 発見した場合
                     	if (distance <= SETDISTANCE) {
                     		
                     		paint.setColor(Color.RED);
                             canvas.drawText("｢" + targetPlaceName + "｣まで" + d, 10, 60, paint);
                             canvas.drawText(fps + "fps", 10, 130, paint);
                             
                             // 3秒間画像を表示
                             fpsManager.setElapsedTimefromFoundTime(SystemClock.uptimeMillis());
                             et = fpsManager.getElapsedTimefromFoundTime();
                             if ((et - ft) < SHOWIMAGETIME) {
                                 canvas.drawBitmap(found, px, py, paint);
                             	if (!flag_image) {
                             		flag_image = true;
                             	}
                             } else {
                             	// 表示終了後、初期化しておく
                             	if (flag_image) {
                             		flag_image = false;
                             	}
                             }
                             
                             // twitterアプリには1回だけ投げる
                             if (!tweet.isCalledTwitter() && (et - ft) > SHOWIMAGETIME * 3) {
                             	tweet.tweetMsg(tweetmsg);
                             	tweet.setCalledTwitter(true);
                             	
                             	// ついでに、このときの時間を取得しておく
                                 fpsManager.setFoundTime(SystemClock.uptimeMillis());
                                 ft = fpsManager.getFoundTime();
                                 
                             }
                             
                     	// 未発見の場合
                     	} else {
                     		
                             paint.setColor(Color.BLUE);
                             canvas.drawText("｢" + targetPlaceName + "｣まで" + d, 10, 60, paint);
                             canvas.drawText(fps + "fps", 10, 130, paint);
                     		
                             // twitterアプリへのインテントのフラグを解除
                             if (tweet.isCalledTwitter()) tweet.setCalledTwitter(false);
                             
                             // 発見時間等の初期化
                             fpsManager.setFoundTime(0l);
                             ft = 0l;
                             fpsManager.setElapsedTimefromFoundTime(0l);
                             et = 0l;
                             
                     	}
                         
                     }
             		
             	}
                 
                 // ツイートするメッセージを設定しておく
                 Editor e = p.edit();
                 e.putString("tweet_cache", tweetmsg);
                 e.commit();
                 
             }
             
         } finally {
         	
             // キャンバスの解放し忘れに注意
             if (canvas != null) {
                 this.mHolder.unlockCanvasAndPost(canvas);
             }
             
         }
         
     }
     
 }
 
 // システムへ描画指示を行う
 public void onDraw() {
//     Canvas canvas = this.mHolder.lockCanvas();
//     this.draw(canvas);
//     this.mHolder.unlockCanvasAndPost(canvas);
 }
 
 // SurfaceView内に描画するもの
 public void draw(Canvas canvas) {
 	
 }
 
}
