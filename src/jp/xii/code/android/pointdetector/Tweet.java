package jp.xii.code.android.pointdetector;

import java.net.URLEncoder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

// インテントでtwitterアプリにツイートするプログラム

public class Tweet {
    
	private Context myContext;
    
    private boolean _isCalledTwitter = false;
	
	// コンストラクタ
	public Tweet (Context context) {
		myContext = context;
	}
	
	public void tweetMsg (String string) {
		
		android.util.Log.v("tweetMsg", string);
		
		try {
			
			// 暗黙的インテント
			
			// 文字列をエンコードする
			String encodeStr = URLEncoder.encode(string, "utf-8");
			
			Uri uri = Uri.parse("http://twitter.com/intent/tweet?text=" + encodeStr);
			
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			myContext.startActivity(Intent.createChooser(intent, "twitterで共有する"));
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
    // twitter呼び出したかどうかのメソッド
    public boolean isCalledTwitter () {
    	return _isCalledTwitter;
    }
    // セッター
    public void setCalledTwitter (boolean b) {
    	_isCalledTwitter = b;
    }
    
}
