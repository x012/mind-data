package com.angel.indexdb;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.angel.activity.MindDataActivity;
import com.angel.activity.Shared;
import com.angel.mind.R;

public class IndexActivity extends ListActivity {

	private static final int DELETE_ID = Menu.FIRST + 1;
	private static final int EDIT_ID = Menu.FIRST + 2;

	private static final String TAG = "IndexActivity";
	public static final String DB_NAME = "dbName";
	public static final String TITLE = "title";
	public static final String ID = "id";
	IndexDbAdapter mIndexDbAdaper;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_index);
		init();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.index_activity, menu);
		return true;
	}

	private void init() {
		registerForContextMenu(getListView());
		mIndexDbAdaper = new IndexDbAdapter(this);
		mIndexDbAdaper.open();
		// insertTest();
		fillData();

	}

	private void fillData() {
		Cursor cursor = mIndexDbAdaper.fetchData();
		startManagingCursor(cursor);

		String[] from = new String[] { IndexDbAdapter.KEY_TITLE };
		int[] to = new int[] { R.id.label };

		// Now create an array adapter and set it to display using our row
		SimpleCursorAdapter notes = new SimpleCursorAdapter(this,
				R.layout.chooser_row, cursor, from, to);
		setListAdapter(notes);
	}

	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_new:
			mIndexDbAdaper.createRow(Shared.createRandomString());
			fillData();
			return true;
		}
		return super.onMenuItemSelected(featureId, item);
	}

	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, DELETE_ID, 0, R.string.menu_delete);
		menu.add(0, EDIT_ID, 0, R.string.menu_edit_title);

	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();

		switch (item.getItemId()) {
		case DELETE_ID:
			mIndexDbAdaper.deleteRow(info.id);
			deleteDatabase(getDbName(info.id));
			fillData();
			return true;
		case EDIT_ID:
			
			// Fetch title, put it in extras
			Intent i = new Intent(this, TitleEditorActivity.class);
			String title = mIndexDbAdaper.getTitleAt(info.id);
			i.putExtra(TITLE, title);
			
			// Also put id in extras
			i.putExtra(ID, info.id);
			
			// Start the activity
			int requestCode = TitleEditorActivity.REQUEST_EDIT;
			startActivityForResult(i, requestCode);
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case TitleEditorActivity.REQUEST_EDIT:
			switch (resultCode) {
			case Activity.RESULT_OK:
				Bundle extras = data.getExtras();
				String title = extras.getString(TITLE);
				long id = extras.getLong(ID);
				CharSequence text = "Edited text: " + title;
				int duration = Toast.LENGTH_SHORT;
				Toast toast = Toast.makeText(this, text, duration);
				toast.show();
				mIndexDbAdaper.updateRow(id, title);
				break;
			case Activity.RESULT_CANCELED:
			}
		}
		// TODO edit title returned in data's extras... DONE
	}

	protected void onDestroy() {
		super.onDestroy();
		if (mIndexDbAdaper != null) {
			mIndexDbAdaper.close();
		}
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, MindDataActivity.class);
		i.putExtra(DB_NAME, getDbName(id));
		Log.d(TAG, l.getItemAtPosition(position).toString());
		startActivity(i);
	}

	private static String getDbName(long id) {
		return "graphdata" + id + ".db";
	}

}
