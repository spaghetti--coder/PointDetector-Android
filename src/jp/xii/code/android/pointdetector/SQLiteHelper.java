package jp.xii.code.android.pointdetector;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLiteHelper extends SQLiteOpenHelper {
	
	static private String DB_NAME = "targetplace.db";
	static private int DB_VERSION = 1;
	
	public SQLiteHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS targetpoints("
				+ "_id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,"
				+ "point_name TEXT, latitude REAL, longitude REAL)");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
