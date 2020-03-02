package com.angel.indexdb;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class IndexTable {
	public static final String NAME = "index_table";
	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table " 
			+ NAME + " " 
			+ "(_id integer primary key autoincrement, "
			+ "title" 
			+ " text not null);";

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		Log.w(IndexTable.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		database.execSQL("DROP TABLE IF EXISTS todo");
		onCreate(database);
	}
}
