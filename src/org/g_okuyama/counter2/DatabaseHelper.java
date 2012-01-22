package org.g_okuyama.counter2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	//DatabaseHelper helper = null;
	private static final int DB_VERSION = 1;
	
	//SQL命令(タイトル(テキストメモ)、カウンタ、時刻を作成)
	private static final String CREATE_TABLE_SQL = 
		  "create table counter"
		  + "(rowid integer primary key autoincrement,"
		  + "title text not null,"
		  + "count text not null,"
		  + "date text not null,"
		  + "place text not null,"
		  + "timecount text not null,"
		  + "button text not null)";
	
	DatabaseHelper(Context context){
		super(context, "countdb", null, DB_VERSION);
	}
	
	public void onCreate(SQLiteDatabase db){
		db.execSQL(CREATE_TABLE_SQL);
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
		//何もしない
	}
}
