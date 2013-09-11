package jp.xii.code.android.pointdetector;

//MIT License
//cf. http://yamadarake.jp/trdi/report000001.html
//ヒュベニの公式を用いた、緯度・経度2点間の距離を算出するメソッド(一部改編してメソッド化)

public class Coords {
	
	// *****************************
	// 設定
	// *****************************
	
	// 現在地点からポイントまでの距離について、フラグを立てる距離
	public static final float SETDISTANCE = 3.0f;
	
	// *****************************
	// ここまで
	// *****************************
	
	public static final double BESSEL_A = 6377397.155;
	public static final double BESSEL_E2 = 0.00667436061028297;
	public static final double BESSEL_MNUM = 6334832.10663254;

	public static final double GRS80_A = 6378137.000;
	public static final double GRS80_E2 = 0.00669438002301188;
	public static final double GRS80_MNUM = 6335439.32708317;
	
	public static final double WGS84_A = 6378137.000;
	public static final double WGS84_E2 = 0.00669437999019758;
	public static final double WGS84_MNUM = 6335439.32729246;
	
	public static final int BESSEL = 0;
	public static final int GRS80 = 1;
	public static final int WGS84 = 2;
	
	public double d = 0; // memorize distance
	
	// コンストラクタ
	public Coords (final double[] from, final double[] to) {
		d = calcDistHubeny(from[0], from[1], to[0], to[1]);
	}
	
	// 距離返却メソッド
	public double distBase() {
		return d;
	}
	
	public double getDistance () {
		return d;
	}
	
	// 小数第1位まで表示(double→floatへ変換後、小数第1位で四捨五入)
	public float distanceofTwoPoints () {
		return (Math.round((float)d * 10.0f) / 10.0f);
	}
	// 外部用メソッドここまで
	
	// 以下からは算出用メソッド
	// 度をラジアンに変換
	public static double deg2rad(double deg){
		return deg * Math.PI / 180.0;
	}
	
	// ヒュベニの公式を使って2点間の距離を計算
	public static double calcDistHubeny(double lat1, double lng1,
	                                    double lat2, double lng2,
	                                    double a, double e2, double mnum){
		double my = deg2rad((lat1 + lat2) / 2.0);
		double dy = deg2rad(lat1 - lat2);
		double dx = deg2rad(lng1 - lng2);

		double sin = Math.sin(my);
		double w = Math.sqrt(1.0 - e2 * sin * sin);
		double m = mnum / (w * w * w);
		double n = a / w;
		
		double dym = dy * m;
		double dxncos = dx * n * Math.cos(my);
		
		return Math.sqrt(dym * dym + dxncos * dxncos);
	}
	
	public static double calcDistHubeny (double lat1, double lng1,
	                                    double lat2, double lng2) {
		return calcDistHubeny(lat1, lng1, lat2, lng2,
	                          GRS80_A, GRS80_E2, GRS80_MNUM);
	}
	
	public static double calcDistHubery (double lat1, double lng1,
                                  double lat2, double lng2, int type) {
		switch(type){
		case BESSEL:
			return calcDistHubeny(lat1, lng1, lat2, lng2,
	                              BESSEL_A, BESSEL_E2, BESSEL_MNUM);
		case WGS84:
	        return calcDistHubeny(lat1, lng1, lat2, lng2,
	                              WGS84_A, WGS84_E2, WGS84_MNUM);
		default:
	        return calcDistHubeny(lat1, lng1, lat2, lng2,
	                              GRS80_A, GRS80_E2, GRS80_MNUM);
		}
	}
	  
}
