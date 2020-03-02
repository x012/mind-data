package com.angel.indexdb;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.angel.mind.R;

public class TitleEditorActivity extends Activity {
	public final static int REQUEST_EDIT = 0xabcdef;
	private EditText mEditText;
	private long mId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.title_editor);
		mEditText = (EditText) findViewById(R.id.node_label_edit);
		Intent i = getIntent();
		Bundle extras = i.getExtras();
		String title = extras.getString(IndexActivity.TITLE);
		mId = extras.getLong(IndexActivity.ID);
		
		mEditText.setText(title);
		mEditText.setSelection(title.length());
		
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
	
	public void onOkClicked(View v) {
		Intent i = new Intent();
		setResult(Activity.RESULT_OK, i);
		i.putExtra(IndexActivity.TITLE, mEditText.getText().toString());
		i.putExtra(IndexActivity.ID, mId);
		finish();
	}
	
	public void onCancelClicked(View v) {
		setResult(Activity.RESULT_CANCELED);
		finish();
		
	}
	
}
