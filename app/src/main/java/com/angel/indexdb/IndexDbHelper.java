package com.angel.indexdb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class IndexDbHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "index.db";

	private static final int DATABASE_VERSION = 1;

	public IndexDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Method is called during creation of the database
	public void onCreate(SQLiteDatabase database) {
		IndexTable.onCreate(database);
	}

	// Method is called during an upgrade of the database,
	// e.g. if you increase the database version
	public void onUpgrade(SQLiteDatabase database, int oldVersion,
			int newVersion) {
		IndexTable.onUpgrade(database, oldVersion, newVersion);
	}
}
