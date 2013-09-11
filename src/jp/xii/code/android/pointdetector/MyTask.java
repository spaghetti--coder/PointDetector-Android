package jp.xii.code.android.pointdetector;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;
import android.preference.PreferenceManager;

public class MyTask extends AsyncTask<ServerParams, Integer, Message> implements OnCancelListener {
	
	private final static int MODE_NONE = -1;
	private final static int MODE_AUTH = 0;
	private final static int MODE_SETTARGET = 1;
	private final static int MODE_SENDLOCATION = 2;
	
	private boolean need_dialog = true;
	
	private final static String dialogTitle = "処理中…";
	private ProgressDialog pDialog = null;
	private Context _context = null;
	
	private ServerParams sparams = null;
	
	private SharedPreferences p = null;
	private Editor e = null;
	
	private SQLiteHelper helper = null;
	private SQLiteDatabase dbr = null;
	private SQLiteDatabase dbw = null;
	private Cursor c = null;
    
    private Handler handler = new Handler();
    
	public MyTask (Context context) {
		_context = context;
	}
	public MyTask (Context context, boolean dialog) {
		_context = context;
		need_dialog = dialog;
	}
	
	// AsynkTask読み込み前に実行されるメソッド
	@Override
	protected void onPreExecute() {
		if (need_dialog) {
			// プログレスダイアログを表示
			pDialog = new ProgressDialog(_context);
			pDialog.setTitle(dialogTitle);
		    pDialog.setMessage("準備しています");
		    pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		    pDialog.setCancelable(true);
		    pDialog.setOnCancelListener(this);
		    pDialog.setMax(100);
		    pDialog.setProgress(0);
		    pDialog.show();
		}
	}
	
	@Override
	protected Message doInBackground(ServerParams... parameters) {
		// パラメータを内部変数に引き渡す
		sparams = parameters[0];
		
		InputStream is = null;
		
		// set default messages
		Message msg = new Message();
		
		UserInfo uinfo = null;
		
		// ユーザー認証であれば
		if (sparams.getMode() == MODE_AUTH) {
			
			String server = sparams.getServer();
			String user_name = sparams.getUserName();
			String password = sparams.getPassword();
			
			try {
				
				/* -----------
				 * ユーザー情報取得
				 * ----------- */
				
			    setDialogMessage("ログインデータの送信準備中");
			    publishProgress(10);
		        
			    is = getRawData(server + "appli/login", user_name, password);
			    
			    // 準備できたらプログレスを25まで進行
			    publishProgress(20);
			    
		        uinfo = getUserInfo(is);
		        
			    // 準備できたらプログレスを30まで進行
			    publishProgress(30);
			    
				msg.setResult(uinfo.getResult());
				msg.setMessage(uinfo.getMessage());
			    
			    if (msg.getResult()) { // ログイン成功の場合
					
					/* -----------
					 * ログインデータ保存
					 * ----------- */
			    	
				    // 準備できたらプログレスを40まで進行
				    publishProgress(40);
				    setDialogMessage("ログイン成功(ログインデータ保存中)");
			    	
			    	// AndroidアプリのPreferencesに保存
			        // 設定マネージャ
			        p = PreferenceManager.getDefaultSharedPreferences(_context);
					e = p.edit();
					e.putString("user_name", sparams.getUserName()); // ユーザー名(非同期通信に渡されたパラメータを使用)
					e.putInt("user_id", uinfo.getUserID()); // ユーザーID(非同期通信で取得したもの)
					e.putInt("set_place_id", uinfo.getSetPoint()); // 設定ポイントのID(非同期通信で取得したもの)
					e.putString("login_hash", uinfo.getLoginHash()); // ログインハッシュ(非同期通信で取得したもの)
			        e.commit();
			        
					/* -----------
					 * 設定地点情報の
					 * 更新確認
					 * ----------- */
			    	
				    // 保存終了したらプログレスを45まで進行
				    publishProgress(45);
				    setDialogMessage("設定地点情報の更新を確認します");
				    
					// JSON取得URLのベース
					String urlBase = server + "target/location/";
					// JSON取得データを格納
					List<TargetPlace> tPlace = new ArrayList<TargetPlace>(); // 目標地点
					
				    publishProgress(50);
				    setDialogMessage("設定地点情報を取得中");
					
					// 目標地点の個数を取得
					URL urlTPLength = new URL(urlBase + "length");
			        InputStreamReader isrTPLength = new InputStreamReader( urlTPLength.openStream() );
			        
				    publishProgress(55);
				    setDialogMessage("設定地点情報を解析中");
				    
			        JsonReader jsrTPLength = new JsonReader( isrTPLength );
			        Gson mygsonTPLength = new Gson();
			        Length lobj = new Length();
			        lobj = mygsonTPLength.fromJson( jsrTPLength, lobj.getClass() );
			        int len = lobj.getLength();
					
			        // for文内で進行させるプログレス数
			        int tmp_progress = 25 / len;
			        
			        for (int i = 1; i <= len; i++) {
			        	
						URL url = new URL(urlBase + "id/" + i);
						
				        InputStreamReader isr = new InputStreamReader( url.openStream() );
				        JsonReader jsr = new JsonReader( isr );
				        Gson mygson = new Gson();
				        TargetPlace tpobj = new TargetPlace();
				        tpobj = mygson.fromJson( jsr, tpobj.getClass() );
				        tPlace.add(tpobj); // リストに格納
				        
				        publishProgress(55 + (tmp_progress * i));
				        
			        }
				    
			        // プログレス数を強制的に85に補正
				    publishProgress(80);
				    setDialogMessage("設定地点情報の照会中");
				    
					/* -----------
					 * 設定地点情報の
					 * DB読み書き
					 * ----------- */
				    
					helper = new SQLiteHelper(_context);
					dbr = helper.getReadableDatabase(); // 一旦読み込みで開く
					
					// とりあえず全データを読み込み
					c = dbr.rawQuery(
							"SELECT * FROM targetpoints ORDER BY _id DESC", null);
					
					boolean hasRecord = c.moveToFirst();
					int i = 0;
					Integer[] resultID = new Integer[c.getCount()];
					while (hasRecord) {
						resultID[i] = c.getInt(0); // IDのみ取得
						hasRecord = c.moveToNext();
						i++;
					}
					c.close();
					dbr.close();
					
				    publishProgress(85);
				    
					// 書き込み
					dbw = helper.getWritableDatabase();
					dbw.beginTransaction();
					
					boolean flag = false;
					boolean flag_updated = false;
					int j = 0;
				    
					for (i = 0; i < tPlace.size(); i++) {
						
						// 読み込んだデータ
						int _id = tPlace.get(i).getID(); // ID
						String point_name = tPlace.get(i).getPointName(); // 地点名
						float latitude = tPlace.get(i).getLatitude(); // 緯度
						float longitude = tPlace.get(i).getLongitude(); // 経度
						
						// データの重複を確認
						if (resultID.length != 0) {
							for (j = 0; j <= resultID.length; j++) {
								
								// IDが重複している(既にDB内にデータが存在する)場合は、フラグを立ててループを抜ける
								if (_id == resultID[j]) {
									flag = true;
									break;
								}
								
							}
						}
						
						// データが重複していなければ、新規データとみなして書き込み
						if (!flag) {
							
						    publishProgress(90);
						    setDialogMessage("新規目標地点データの書き込み中");
						    
							dbw.execSQL("INSERT INTO targetpoints(point_name, latitude, longitude) values(?, ?, ?)", 
									new Object[] { point_name, latitude, longitude });
							
							flag_updated = true;
							
						}
						
						flag = false; // フラグの初期化
						
					}
					
					dbw.setTransactionSuccessful();
					dbw.endTransaction();
					
					// サーバー側に設定されている地点情報を、Androidアプリ側にも反映
					
					dbr = helper.getReadableDatabase(); // 再度読み込みで開く
					
					// 設定ポイントのIDから、引っ張りたい情報を参照
					c = dbr.rawQuery(
							"SELECT * FROM targetpoints WHERE _id = " + uinfo.getSetPoint(), null);
					
					hasRecord = c.moveToFirst();
					
					int settingID = c.getInt(0);
					String settingPlaceName = c.getString(1);
					float settingLatitude = c.getFloat(2);
					float settingLongitude = c.getFloat(3);
					
					c.close();
					dbr.close();
					
					// 取得したポイント名や緯度経度情報を、プリファレンスマネージャに書き込み
	    			e = p.edit();
	    			e.putInt("set_place_id", settingID);
	    			e.putString("set_place_name", settingPlaceName);
	    			e.putFloat("set_place_latitude", settingLatitude);
	    			e.putFloat("set_place_longitude", settingLongitude);
	    	        e.commit();
					
					msg.setResult(true);
					
					if (flag_updated) {
						
					    publishProgress(100);
					    setDialogMessage("ログイン・目標地点更新完了");
					    msg.setMessage("正常にログインし、目標地点情報の更新に成功しました。");
						
					} else {
						
					    publishProgress(100);
					    setDialogMessage("ログイン完了");
					    msg.setMessage("正常にログインできました。");
						
					}
					
			    } else { // ログイン失敗の場合
			    	
				    // 準備できたらプログレスを40まで進行
				    publishProgress(40);
				    setDialogMessage("ログイン失敗");
			    	
				    throw new AuthException();
			    	
			    }
			    
			} catch (ClientProtocolException e) {
				
				setDialogMessage("ログイン結果取得失敗");
				msg.setResult(false);
				msg.setMessage("プロトコルエラーが発生しました");
				android.util.Log.w("MyTask", "Occurred ClientProtocolException [Error of HTTPClient]");
				e.printStackTrace();
				
			} catch (MalformedURLException e) {
				
				setDialogMessage("ログイン結果取得失敗");
				msg.setResult(false);
				msg.setMessage("URIの形式が不正です");
				android.util.Log.w("MyTask", "Occurred MalformedURLException [Invalid URI]");
				e.printStackTrace();
				
			} catch (FileNotFoundException e) {
				
				setDialogMessage("ログイン結果取得失敗");
				msg.setResult(false);
				msg.setMessage("ログイン結果を取得できませんでした(404)");
				android.util.Log.w("MyTask", "Occurred FileNotFoundException [404 File Not Found]");
				e.printStackTrace();
				
			} catch (EOFException e) {
				
				setDialogMessage("ログイン結果取得失敗");
				msg.setResult(false);
				msg.setMessage("ログイン結果を取得できませんでした(EOF)");
				android.util.Log.w("MyTask", "Occurred EOFException [File Error]");
				e.printStackTrace();
				
			} catch (IOException e) {
				
				setDialogMessage("ログイン結果取得失敗");
				msg.setResult(false);
				msg.setMessage("ログイン結果を取得できませんでした(IO)");
				android.util.Log.w("MyTask", "Occurred IOException [I/O Error]");
				e.printStackTrace();
				
			} catch (AuthException e) {
				
				setDialogMessage("ログイン失敗");
				msg.setResult(false);
				msg.setMessage("ユーザー名またはパスワードが間違っています");
				android.util.Log.i("MyTask", "Incorrect pairs of username and password [Auth Error]");
				e.printStackTrace();
				
			} catch (Exception e) {
				
				setDialogMessage("ログイン失敗");
				msg.setResult(false);
				msg.setMessage("何らかのエラーが発生いたしました");
				android.util.Log.w("MyTask", "Occurred Exception [Fatal Error]");
				e.printStackTrace();
				
			}
			
		// 地点設定モードであれば
		} else if (sparams.getMode() == MODE_SETTARGET) {
			
			String server = sparams.getServer();
			String user_name = sparams.getUserName();
			int pointid = sparams.getPointID();
			String hash = sparams.getHash();
			
			if (user_name.equals("") || user_name == null
					|| pointid <= 0 || server.equals("")
					|| hash.equals("") || hash == null) {
				
				setDialogMessage("目標地点更新失敗");
				msg.setResult(false);
				msg.setMessage("ログインしてください");
				
			} else {
				
				// JSON取得URLのベース
				String urlBase = server + "report/target/" + user_name + "/" + pointid + "/" + hash;
				
				try {
					
		    		// 目標地点の個数を取得
		    		URL urlsetTP = new URL(urlBase);
				    publishProgress(15);
		    		is = urlsetTP.openStream();
				    publishProgress(30);
		    		String raw =  inputStreamToString(is);
				    publishProgress(50);
//		    		android.util.Log.i("MyTask", "rawdata:" + raw);
//		            InputStreamReader isrsetTP = new InputStreamReader( is );
//		            JsonReader jsrsetTP = new JsonReader( isrsetTP );
//		            Gson mygsonsetTP = new Gson();
//					Message ret = new Message();
//		        	ret = mygsonsetTP.fromJson( jsrsetTP, ret.getClass() );
		        	
		    		Pattern p = Pattern.compile("\"result\":(true|false),\"message\":\"(.*)\"");
		    		Matcher m = p.matcher(raw);
				    publishProgress(70);
		    		if (m.find()) {
			    		msg.setResult(Boolean.valueOf(m.group(1)));
			    		msg.setMessage(m.group(2));
		    		} else {
			    		throw new IOException();
		    		}
				    publishProgress(80);
		    		
//					android.util.Log.i("MyTask", "[result]" + msg.toString());
			        
				} catch (MalformedURLException e) {
					
					setDialogMessage("目標地点更新失敗");
					msg.setResult(false);
					msg.setMessage("URIの形式が不正です");
					android.util.Log.w("MyTask", "Occurred MalformedURLException [Invalid URI]");
					e.printStackTrace();
					
				} catch (FileNotFoundException e) {
					
					setDialogMessage("目標地点更新失敗");
					msg.setResult(false);
					msg.setMessage("更新結果を取得できませんでした");
					android.util.Log.w("MyTask", "Occurred FileNotFoundException [404 File Not Found]");
					e.printStackTrace();
					
				} catch (EOFException e) {
					
					setDialogMessage("目標地点更新失敗");
					msg.setResult(false);
					msg.setMessage("更新結果を取得できませんでした");
					android.util.Log.w("MyTask", "Occurred EOFException [File Error]");
					e.printStackTrace();
					
//				} catch (JsonIOException e) {
//					
//					setDialogMessage("目標地点更新失敗");
//					msg.setResult(false);
//					msg.setMessage("更新結果を処理できませんでした");
//					android.util.Log.w("MyTask", "Occurred IOException [GSON I/O Error]");
//					e.printStackTrace();
//					
//				} catch (JsonSyntaxException e) {
//					
//					setDialogMessage("目標地点更新失敗");
//					msg.setResult(false);
//					msg.setMessage("更新結果を処理できませんでした");
//					android.util.Log.w("MyTask", "Occurred IOException [GSON I/O Error]");
//					e.printStackTrace();
					
				} catch (IOException e) {
					
					setDialogMessage("目標地点更新失敗");
					msg.setResult(false);
					msg.setMessage("更新結果を処理できませんでした");
					android.util.Log.w("MyTask", "Occurred IOException [I/O Error]");
					e.printStackTrace();
					
				} catch (Exception e) {
					
					setDialogMessage("目標地点更新失敗");
					msg.setResult(false);
					msg.setMessage("何らかのエラーが発生いたしました");
					android.util.Log.w("MyTask", "Occurred Exception [Fatal Error]");
					e.printStackTrace();
					
				}
				
			}
			
		// 地点送信モードであれば
		} else if (sparams.getMode() == MODE_SENDLOCATION) {
			
			String server = sparams.getServer();
			String user_name = sparams.getUserName();
			double latitude = sparams.getLatitude();
			double longitude = sparams.getLongitude();
			long timestamp = sparams.getTimestamp();
			String hash = sparams.getHash();
			
			try {
				
				// 送信URLの準備
				String seturl = server + "report/location/" +  user_name + "/" + latitude + "," + longitude
						+ "/" + timestamp + "/" + hash;
				URL url = new URL(seturl);
				
				// ユーザー情報送信
				
				// 送信結果を受取り
				is = url.openStream();
	    		String raw =  inputStreamToString(is);
	    		Pattern p = Pattern.compile("\"result\":(true|false),\"message\":\"(.*)\"");
	    		Matcher m = p.matcher(raw);
	    		if (m.find()) {
		    		msg.setResult(Boolean.valueOf(m.group(1)));
		    		msg.setMessage(m.group(2));
	    		} else {
		    		throw new IOException();
	    		}
				
			} catch (MalformedURLException e) {
				
				msg.setResult(false);
				msg.setMessage("URIの形式が不正です");
				android.util.Log.w("MyTask", "Occurred MalformedURLException [Invalid URI]");
				e.printStackTrace();
				
			} catch (FileNotFoundException e) {
				
				msg.setResult(false);
				msg.setMessage("更新結果を取得できませんでした");
				android.util.Log.w("MyTask", "Occurred FileNotFoundException [404 File Not Found]");
				e.printStackTrace();
				
			} catch (EOFException e) {
				
				msg.setResult(false);
				msg.setMessage("更新結果を取得できませんでした");
				android.util.Log.w("MyTask", "Occurred EOFException [File Error]");
				e.printStackTrace();
				
			} catch (IOException e) {
				
				msg.setResult(false);
				msg.setMessage("更新結果を処理できませんでした");
				android.util.Log.w("MyTask", "Occurred IOException [I/O Error]");
				e.printStackTrace();
				
			} catch (Exception e) {
				
				e.printStackTrace();
				
			}
			
		}
		
		// プログレスを100にする
		if (need_dialog || pDialog != null) {
			publishProgress(100);
		}
		
		return msg;
		
	}
	
	@Override
    protected void onProgressUpdate(Integer... progress) {
		if (need_dialog || pDialog != null) {
			pDialog.setProgress(progress[0]);
		}
    }
	
	// ダイアログのメッセージを更新
	// (CalledFromWrongThreadExceptionr対策で、ハンドラ—を使用)
	private void setDialogMessage (final String message) {
		handler.post(new Runnable() {

			@Override
			public void run() {
				if (need_dialog || pDialog != null) {
					pDialog.setMessage(message);
				}
			}
			
		});
	}
	
//	private void setDialogMessage (final int message_id) { // strings.xmlから文字を参照する場合は、こっちを使用
//		handler.post(new Runnable() {
//
//			@Override
//			public void run() {
//				if (need_dialog || pDialog != null) {
//					pDialog.setMessage(_context.getResources().getString(message_id));
//				}
//			}
//			
//		});
//	}
	
	@Override
    protected void onPostExecute(Message result) {
		
		if (!need_dialog) { // ダイアログ不要時は、実行結果をログに吐き出して終了
			if (result == null) {
				android.util.Log.i("MyTask", "[result]null");
			} else {
				android.util.Log.i("MyTask", "[result]" + result.toString());
			}
			return;
		}
		
		// ダイアログ終了
		if (pDialog != null) {
			pDialog.dismiss();
		}
		
        String dTitle = "処理失敗";
        
        if (result == null) {
        	android.util.Log.i("MyTask", "[result]null");
        	result = new Message(false, "正常に結果を取得できませんでした。");
        	
        } else if (result.getResult()) {
        	dTitle = "処理成功";
        	android.util.Log.i("MyTask", "[result]" + result.toString());
        } else {
        	android.util.Log.i("MyTask", "[result]" + result.toString());
        }
        
        // 地点更新モードの場合、空文字=正常成功、same_value=同一値なので、メッセージ修正
        if (sparams.getMode() == MODE_SETTARGET) {
        	
        	if (result.getResult()) {
            	
            	if (result.getMessage().equals("same_value")) {
                    result.setMessage("既に同じ地点が選択されています。");
            	} else {
                    result.setMessage("目標地点を変更しました。");
            	}
            	
        	} else {
        		
        		if (result.getMessage().equals("") || result.getMessage() == null) {
                    result.setMessage("目標地点の変更に失敗しました。");
        		}
        		
        	}
        	
        }
        
        // 別のダイアログでメッセージを表示して終了
        new AlertDialog.Builder(_context)
        	.setTitle(dTitle)
        	.setMessage(result.getMessage())
        	.setPositiveButton(R.string.dialog_button_abort,
        			new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// none
							
						}
					})
			.show();
        
    }
	
	@Override
	protected void onCancelled() {
		
		try {
			
			// ダイヤログが開きっぱなしの場合は閉じる
			if (pDialog != null) {
				pDialog.dismiss();
			}
			
			// プリファレンスをいったんコミット
			if (e != null) {
				e.commit();
			}
			
			// データベースを一通り終わらせる
			if (helper != null) {
				
				if (c != null) c.close();
				if (dbr != null) dbr.close();
				if (dbw != null) {
					dbw.setTransactionSuccessful();
					dbw.endTransaction();
				}
				
			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	@Override
	public void onCancel(DialogInterface arg0) {
		
		try {
			
			// ダイヤログが開きっぱなしの場合は閉じる
			if (pDialog != null) {
				pDialog.dismiss();
			}
			
			// プリファレンスをいったんコミット
			if (e != null) {
				e.commit();
			}
			
			// データベースを一通り終わらせる
			if (helper != null) {
				
				if (c != null) c.close();
				if (dbr != null) dbr.close();
				if (dbw != null) {
					dbw.setTransactionSuccessful();
					dbw.endTransaction();
				}
				
			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
	}
	
	private InputStream getRawData (String url, String setuser, String setpassword)
			throws MalformedURLException, FileNotFoundException, EOFException {
		
        // HTTPクライアントを準備
	    HttpClient httpclient = new DefaultHttpClient();
	    HttpPost httppost = new HttpPost(url);
	    HttpResponse response = null;
	    String tmp = null;
	    
	    try {
	    	
	        // POSTするデータを定義
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("username", setuser));
	        nameValuePairs.add(new BasicNameValuePair("password", setpassword));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        
	        // POSTリクエストを実行する
	        response = httpclient.execute(httppost);
	        HttpEntity entity = response.getEntity();
	        tmp = EntityUtils.toString(entity);
	        
	    } catch (ClientProtocolException e) {
	    	
	    	e.printStackTrace();
	    	
	    } catch (IOException e) {
	    	
	    	e.printStackTrace();
	    	
	    }
	    
	    // IOStreamで返却
	    return new ByteArrayInputStream(tmp.getBytes());
	    
	}
	
	private UserInfo getUserInfo(InputStream is) throws IOException {
		
		// JSON取得データを格納
		UserInfo uinfo = new UserInfo(); // ユーザー情報
		
		// ユーザー情報取得
        InputStreamReader isrUserInfo = new InputStreamReader( is );
        JsonReader jsrUserInfo = new JsonReader( isrUserInfo );
        Gson mygsonUserInfo = new Gson();
        try {
            uinfo = mygsonUserInfo.fromJson( jsrUserInfo, uinfo.getClass() );
        } catch (Exception e) {
        	uinfo = null;
        	e.printStackTrace();
        }
        
		return uinfo;
		
	}
	
	public static String inputStreamToString(InputStream in) throws IOException{
        
	    BufferedReader reader = 
	        new BufferedReader(new InputStreamReader(in, "UTF-8"/* 文字コード指定 */));
	    StringBuffer buf = new StringBuffer();
	    String str;
	    while ((str = reader.readLine()) != null) {
	            buf.append(str);
	            buf.append("¥n");
	    }
	    return buf.toString();
	}
	
}
