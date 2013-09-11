package jp.xii.code.android.pointdetector;


//カメラプレビュー表示クラス

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class CameraView extends SurfaceView implements SurfaceHolder.Callback {
	 
 private Activity mOwner;    // オーナーアクティビティ(画面の回転処理などに使う)
 private Camera mCamera; // カメラ
 private SurfaceHolder mHolder;  // サーフェスホルダー
  
 public CameraView(Context context) {
     super(context);
      
     this.mHolder = this.getHolder();
     this.mHolder.addCallback(this);
     this.mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
 }
  
 public CameraView(Context context, AttributeSet attrs, int defStyle) {
     super(context, attrs, defStyle);

     this.mHolder = this.getHolder();
     this.mHolder.addCallback(this);
     this.mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

 }

 public CameraView(Context context, AttributeSet attrs) {
     super(context, attrs);
      
     this.mHolder = this.getHolder();
     this.mHolder.addCallback(this);
     this.mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

 }

 public void setOwner(Activity activity) {
     this.mOwner = activity;
 }
  
 @Override
 public void surfaceCreated(SurfaceHolder holder) {
     try {
         // カメラを開く
         this.mCamera = Camera.open();
          
         // プレビューディスプレイの設定
         this.mCamera.setPreviewDisplay(this.mHolder);
          
     } catch (Exception ep) {
         ep.printStackTrace();
          
         // 失敗したときはカメラを解放する
         this.mCamera.release();
         this.mCamera = null;
     }
 }
  
 @Override
 public void surfaceDestroyed(SurfaceHolder holder) {
      
     // プレビューを停止
     this.mCamera.stopPreview();
      
     // カメラを解放
     this.mCamera.release();
     this.mCamera = null;
 }

 @Override
 public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
      
     // 何かする前に一度プレビューを停止する
     this.mCamera.stopPreview();
      
     // カメラパラメータを取り出す
     Camera.Parameters params = this.mCamera.getParameters();

     // ベストなプレビューサイズを探す
     // 
     // 端末のサポートしているプレビューサイズを取り出す
     List<Size> listSize = params.getSupportedPreviewSizes();
      
     // 一番つ劣化が少ない画像のサイズ
     Size bestPrevSize = this.getBestPreviewSize(listSize, width, height);
      
     // プレビューサイズを設定する
     params.setPreviewSize(bestPrevSize.width, bestPrevSize.height);
      
     // 端末のサポートしてる画像サイズを取り出す
     listSize = params.getSupportedPictureSizes();
      
     // ベストな画像サイズを調べる
     Size bestPictureSize = this.getBestPreviewSize(listSize, width, height);
      
     // 画像サイズを設定する
     params.setPictureSize(bestPictureSize.width, bestPictureSize.height);

     // カメラの回転角度をセットする
     CameraView.setCameraDisplayOrientation(this.mOwner, 0, this.mCamera);
      
     // オートフォーカスの設定
     params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
      
     // パラメーターの更新
     this.mCamera.setParameters(params);
      
     // プレビューを再開
     this.mCamera.startPreview();
      
     // プレビューが再開したらオートフォーカスの設定(プレビュー中じゃないときにオートフォーカス設定すると落ちる)
     //this.mCamera.autoFocus(null);
 }
  
 private final float ASPECT_TOLERANCE = 0.05f;
  
 private Size getBestPreviewSize(List<Size> listPreviewSize, int w, int h) {
      
     // プレビューサイズリストがなかったときは何もしない
     if (listPreviewSize == null) {
         return null;
     }
      
     // 端末が立った状態の場合はWとHを入れ替える
     if (w < h) {
         int tmp = w;
         w = h;
         h = tmp;
     }
      
     float bestRatio = (float)w / h; // この比率に近いものをリストから探す
     float minHeightDiff = Float.MAX_VALUE;  // 一番高さに差がないもの
     int bestHeight = h; // プレビュー画面にベストな高さ
     float currRatio = 0;    // 今見ているもののアスペクト比
     Size bestSize = null;
      
     // 近いサイズのものを探す
     for (Size curr : listPreviewSize) {
          
         // 今見ているもののアスペクト比
         currRatio = (float)curr.width / curr.height;
          
         // 許容範囲を超えちゃってるやつは無視
         if (ASPECT_TOLERANCE < Math.abs(currRatio - bestRatio)) {
             continue;
         }
          
         // 前に見たやつより高さの差が少ない
         if (Math.abs(curr.height - bestHeight) < minHeightDiff) {
              
             // 一番いいサイズの更新
             bestSize = curr;
              
             // 今のところこれが一番差が少ない
             minHeightDiff = Math.abs(curr.height - bestHeight);
         }
     }
      
     // 理想的なものが見つからなかった場合、しょうがないので画面に入るようなやつを探しなおす
     if (bestSize == null) {
          
         // でっかい値をいれとく（未使用です）
         minHeightDiff = Float.MAX_VALUE;

         // 今度は画面に入りそうなものを探す
         for (Size curr : listPreviewSize) {
              
             // 今見ているもののアスペクト比
             currRatio = (float)curr.width / curr.height;
              
             // 前に見たやつより高さの差が少ない
             if (Math.abs(curr.height - bestHeight) < minHeightDiff) {
                  
                 // 一番いいサイズの更新
                 bestSize = curr;
                  
                 // 今のところこれが一番差が少ない
                 minHeightDiff = Math.abs(curr.height - bestHeight);
             }
         }
     }
      
     return bestSize;
 }
  
  
 public static void setCameraDisplayOrientation(
         Activity activity, int cameraId, android.hardware.Camera camera) {
      
     // 向きを設定
     camera.setDisplayOrientation(CameraView.getCameraDisplayOrientation(activity));
 }
  
 public static int getCameraDisplayOrientation(Activity activity) {
      
     // ディスプレイの回転角を取り出す
     int rot = activity.getWindowManager().getDefaultDisplay().getRotation();
      
     // 回転のデグリー角
     int degree = 0;
      
     // 取り出した角度から実際の角度への変換
     switch (rot) {
     case Surface.ROTATION_0:    degree = 0;     break;
     case Surface.ROTATION_90:   degree = 90;    break;
     case Surface.ROTATION_180:  degree = 180;   break;
     case Surface.ROTATION_270:  degree = 270;   break;
     }
      
     // 背面カメラだけの処理になるけど、画像を回転させて縦持ちに対応
     return (90 + 360 - degree) % 360;
 }

}
