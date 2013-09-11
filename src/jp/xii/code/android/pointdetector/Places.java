package jp.xii.code.android.pointdetector;

import android.content.Context;
import android.preference.PreferenceManager;

// 地点情報保持クラス(ついでにtwitter投稿フラグを持たせる)

public class Places {
	
    // JR札幌駅の緯度経度
    private final static double[] JRSAPPOROSTA = {43.068621, 141.350806};
    // 地下鉄大通駅の緯度経度
    private final static double[] ODORISTA = {43.060477, 141.354529};
    // 菊水駅の緯度経度
    private final static double[] KIKUSUISTA = {43.057212, 141.372832};
    // 東札幌駅の緯度経度
    private final static double[] HIGASHISAPPOROSTA = {43.051646, 141.384607};
    
    // イーアス札幌Bタウンの緯度経度
    private final static double[] IIASBTOWN = {43.055429,141.384602};
    
    // 産業振興会館
    private final static double[] ICC = {43.0559302,141.3858478};
    
    // 本社
    private final static double[] HEADOFFICE = {43.070606,141.350516};
    
    // 自宅
    private final static double[] MYHOME = {43.0543541, 141.3755309};
	
    private Context myContext;
    
    // コンストラクタ
    public Places (Context context) {
    	myContext = context;
    }
    public Places () {
    	
    }
    
    public double[] getSettingPlace (){
    	
    	double[] ret = {0, 0};
    	
    	if (place("jrsapporosta")) {
    		ret = JRSAPPOROSTA;
    	} else if (place("odorista")) {
    		ret = ODORISTA;
    	} else if (place("kikusista")) {
    		ret = KIKUSUISTA;
    	} else if (place("higashisapporosta")) {
    		ret = HIGASHISAPPOROSTA;
    	} else if (place("iiasbtown")) {
    		ret = IIASBTOWN;
    	} else if (place("icc")) {
    		ret = ICC;
    	} else if (place("headoffice")) {
    		ret = HEADOFFICE;
    	} else if (place("myhome")) {
    		ret = MYHOME;
    	}
    	
    	return ret;
    	
    }
    
    // 設定値呼び出しメソッド
    private boolean place(String str) {
    	return PreferenceManager.getDefaultSharedPreferences(myContext).getString("set_place", "").equals(str);
    }
    
    // プリファレンスマネージャーに保存されている緯度・経度を引っ張ってくるメソッド
    public double[] getTargetPlace() {
    	double lat = (double)PreferenceManager.getDefaultSharedPreferences(myContext).getFloat("set_place_latitude", 0);
    	double lon = (double)PreferenceManager.getDefaultSharedPreferences(myContext).getFloat("set_place_longitude", 0);
    	double[] ret = {lat, lon};
    	return ret;
    }
    
    // プリファレンスマネージャーに保存されている地点名を引っ張ってくるメソッド
    public String getTargetPlaceName() {
    	return PreferenceManager.getDefaultSharedPreferences(myContext).getString("set_place_name",	"-");
    }
}
