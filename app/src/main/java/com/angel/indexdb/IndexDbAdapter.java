package com.angel.indexdb;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class IndexDbAdapter {

	public static final String KEY_TITLE = "title";
	public static final String KEY_DB_NAME = "name";
	public static final String KEY_ID = "_id";
	private static final String TAG = "IndexDb";

	Context context;
	IndexDbHelper dbHelper;
	SQLiteDatabase db;

	public IndexDbAdapter(Context context) {
		this.context = context;
	}

	public Cursor fetchData() {
		String[] columns = new String[] { KEY_ID, KEY_TITLE };
		Cursor c = db.query(IndexTable.NAME, columns, null, null, null, null,
				null);
		Log.d(TAG, c.toString());

		return c;
	}

	public Cursor getRowAt(long id) {
		String[] columns = new String[] { KEY_ID, KEY_TITLE };
		Cursor c = db.query(IndexTable.NAME, columns, KEY_ID + "=" + id, null,
				null, null, null);
		if (c != null)
			if (c.moveToFirst())
				return c;
		return null;
	}

	public String getTitleAt(long id) {
		Cursor c = getRowAt(id);
		String title = null;
		if (c != null) {
			int title_index = c.getColumnIndex(IndexDbAdapter.KEY_TITLE);
			title = c.getString(title_index);
		}
		return title;
	}

	public long createRow(String title) {
		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, title);

		return db.insert(IndexTable.NAME, null, values);
	}

	public void open() throws SQLException {
		dbHelper = new IndexDbHelper(context);
		db = dbHelper.getWritableDatabase();

	}

	public void close() {
		dbHelper.close();

	}

	public void deleteRow(long id) {
		db.delete(IndexTable.NAME, KEY_ID + " = " + id, null);
	}

	public boolean updateRow(long id, String title) {
		ContentValues values = new ContentValues();
		values.put(KEY_TITLE, title);
		return db.update(IndexTable.NAME, values, KEY_ID + "=" + id, null) > 0;
	}

}
