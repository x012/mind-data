package com.angel.graphdatabase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GraphDbOpenHelper extends SQLiteOpenHelper{
	
	private static final int DATABASE_VERSION = 2;

	public GraphDbOpenHelper(Context context, String name) {
		super(context, name, null, DATABASE_VERSION);
	}

	// Method is called during creation of the database
	public void onCreate(SQLiteDatabase database) {
		NodesTable.onCreate(database);
		EdgesTable.onCreate(database);
		MetaDataTable.onCreate(database);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		NodesTable.onUpgrade(db, oldVersion, newVersion);
		EdgesTable.onUpgrade(db, oldVersion, newVersion);
		MetaDataTable.onUpgrade(db, oldVersion, newVersion);
		
	}
}

class NodesTable {
	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table nodes "
			+ "(_id integer primary key autoincrement, " + "data blob);";

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion,
			int newVersion) {
		Log.w(NodesTable.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS nodes");
		onCreate(db);
	}
}

class EdgesTable {
	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table edges "
			+ "(_id integer primary key autoincrement, " + "from_id integer, "
			+ "to_id integer, data blob);";

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion,
			int newVersion) {
		Log.w(EdgesTable.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS edges");
		onCreate(db);
	}
}

class MetaDataTable {
	// Database creation SQL statement
	private static final String DATABASE_CREATE = "create table meta_data "
			+ "(data blob);";

	public static void onCreate(SQLiteDatabase db) {
		db.execSQL(DATABASE_CREATE);
	}

	public static void onUpgrade(SQLiteDatabase db, int oldVersion,
			int newVersion) {
		Log.w(EdgesTable.class.getName(), "Upgrading database from version "
				+ oldVersion + " to " + newVersion
				+ ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS meta_data");
		onCreate(db);
	}
}