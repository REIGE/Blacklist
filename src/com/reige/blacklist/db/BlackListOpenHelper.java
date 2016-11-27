package com.reige.blacklist.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BlackListOpenHelper extends SQLiteOpenHelper {

	public BlackListOpenHelper(Context context) {
		super(context, "blacklist.db", null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		//创建数据库中表
		db.execSQL("create table blacklist " +
				"(_id integer primary key autoincrement , phone varchar(20), mode varchar(5));");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}



}
