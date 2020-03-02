package com.angel.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.angel.activity.EditTextWithBackEvent.EditTextImeBackListener;
import com.angel.graphdatabase.GraphDatabase;
import com.angel.graphview.GraphView;
import com.angel.graphview.GraphView.Command;
import com.angel.graphview.edgepainter.EdgeDrawable;
import com.angel.graphview.edgepainter.EdgePainter;
import com.angel.indexdb.IndexActivity;
import com.angel.mind.R;

public class MindDataActivity extends Activity implements GraphView.Callbacks {

	private enum EditMode {
		EDIT, NEW, NONE
	}

	private static final String TAG = MindDataActivity.class.getSimpleName();

	private EditMode mEditMode = EditMode.NONE;

	private EditTextWithBackEvent mEditText;

	private boolean mMenuShown = true;

	private GraphView mGraphView;

	private RelativeLayout mRootLayout;

	private PopupWindow mTestPopupWindow;

	private LinearLayout mTextbar;

	private View mToolbar;

	/**
	 * Called when the activity is first created.<br>
	 */
	@Override
	public void onCreate(Bundle inState) {
		super.onCreate(inState);
		setContentView(R.layout.mind_data_view);
		mRootLayout = (RelativeLayout) findViewById(R.id.main_layout);

		// Obtain view references
		mEditText = (EditTextWithBackEvent) findViewById(R.id.node_label_edit);
		mEditText.setOnEditTextImeBackListener(new EditTextImeBackListener() {
			public void onImeBack(EditTextWithBackEvent ctrl, String text) {
				stopEdit();
			}
		});
		mToolbar = findViewById(R.id.scroll_view);
		mTextbar = (LinearLayout) findViewById(R.id.toolbar_text);

		// Restore stuff from saved state
		if (inState != null) {
			mMenuShown = inState.getBoolean("menuShown");
			mEditMode = EditMode.values()[inState.getInt("editMode")];
			setTextFieldText(inState.getString("textField"));

			if (!mMenuShown) {
				mTextbar.setVisibility(View.INVISIBLE);
				mToolbar.setVisibility(View.INVISIBLE);
			}
		}

		if (mEditMode == EditMode.NONE) {
			mTextbar.setVisibility(View.INVISIBLE);
		} else {
	
		}
		restorePreviousGraphViewInstanceIfAny();
		mRootLayout.addView(mGraphView);
		mTextbar.bringToFront();
		mToolbar.bringToFront();
		

		// Setup long clicks for the arrow button
		View arrowButton = findViewById(R.id.button_arrow);
		arrowButton.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				mGraphView.postCommand(Command.INVERT_EDGE);
				updateArrowImage(v);
				Vibrator vibr = (Vibrator) getSystemService(VIBRATOR_SERVICE);
				vibr.vibrate(25);

				return true;
			}
		});

		initPopupWindow();
		updateArrowImage(findViewById(R.id.button_arrow));
		

	}

	public void onActionClick(View v) {
		int viewId = v.getId();
		switch (viewId) {
		case R.id.button_new:
			mEditMode = EditMode.NEW;
			mEditText.setText("");
			startEdit();
			break;
		case R.id.button_edit:
			if (mGraphView.isNodeSelected()) {
				mEditMode = EditMode.EDIT;
				setTextFieldText(mGraphView.getSelectedText());
				startEdit();
			}
			break;
		case R.id.button_delete:
			mGraphView.postCommand(GraphView.Command.DELETE_NODE);
			break;
		case R.id.button_connect:
			mGraphView.postCommand(GraphView.Command.CONNECT);
			break;
		case R.id.button_highlight:
			mGraphView.postCommand(GraphView.Command.HIGHLIGHT);
			break;
		case R.id.button_arrow:
			mGraphView.postCommand(GraphView.Command.CHANGE_EDGE);
			updateArrowImage(v);

			// showPopupWindow(v);

			// v.setBackgroundDrawable(ed);

			break;
		case R.id.button_done:
			switch (mEditMode) {
			case NEW:
				String labelText = mEditText.getText().toString();
				mGraphView.setLabelInput(labelText);
				mGraphView.postCommand(GraphView.Command.ADD_NODE);
				stopEdit();
				break;
			case EDIT:
				labelText = mEditText.getText().toString();
				mGraphView.setLabelInput(labelText);
				mGraphView.postCommand(GraphView.Command.EDIT_NODE);
				stopEdit();
				break;
			}

			break;
		case R.id.button_cancel:
			stopEdit();
			break;
		}

	}

	private void updateArrowImage(View v) {
		EdgePainter ep = EdgePainter.edgeTypes[mGraphView.getSelectedEdgeType()];
		EdgeDrawable ed = new EdgeDrawable(ep, getResources()
				.getDisplayMetrics().density, mGraphView.isEdgeInverted());
		ImageButton ib = (ImageButton) v;

		ib.setImageDrawable(ed);
	}

	public boolean onPrepareOptionsMenu(Menu menu) {
		mMenuShown = !mMenuShown;
		if (mMenuShown) {
			showToolbar();
		} else {
			hideToolbar();
		}
		return false;
	}

	@Override
	public Object onRetainNonConfigurationInstance() {
		return mGraphView;
	}

	private void startEdit() {
		// mGraphView.stopAnimation();
		showTextbar();
		showSoftkeyboard();
	}

	private void stopEdit() {
		mEditMode = EditMode.NONE;
		hideTextbar();
		hideSoftkeyboard();
		// mGraphView.startAnimation();
	}

	private void showTextbar() {
		Animation anim;
		anim = AnimationUtils.loadAnimation(this, R.anim.drop_down_in);
		mTextbar.startAnimation(anim);
		mTextbar.setVisibility(View.VISIBLE);
	}

	private void showToolbar() {
		Animation anim;
		anim = AnimationUtils.loadAnimation(this, R.anim.pop_up_in);
		mToolbar.startAnimation(anim);
		mToolbar.setVisibility(View.VISIBLE);
	}

	private void hideTextbar() {
		Animation anim;
		anim = AnimationUtils.loadAnimation(this, R.anim.drop_down_out);
		anim.setAnimationListener(new AnimationListener() {
			public void onAnimationEnd(Animation animation) {
				mTextbar.setVisibility(View.INVISIBLE);
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
			}
		});
		mTextbar.startAnimation(anim);
	}

	private void hideToolbar() {
		Animation anim;
		anim = AnimationUtils.loadAnimation(this, R.anim.pop_up_out);
		anim.setAnimationListener(new AnimationListener() {
			public void onAnimationEnd(Animation animation) {
				mToolbar.setVisibility(View.INVISIBLE);
			}

			public void onAnimationRepeat(Animation animation) {
			}

			public void onAnimationStart(Animation animation) {
			}
		});
		mToolbar.startAnimation(anim);
	}

	private void initPopupWindow() {
		View contentView = LayoutInflater.from(this).inflate(
				R.layout.chooser_row, null);
		mTestPopupWindow = new PopupWindow(this);
		mTestPopupWindow.setTouchInterceptor(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					mTestPopupWindow.dismiss();

					return true;
				}

				return false;
			}
		});
		// mTestPopupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
		// mTestPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		mTestPopupWindow.setWidth(100);
		mTestPopupWindow.setHeight(150);
		mTestPopupWindow.setTouchable(true);
		mTestPopupWindow.setFocusable(true);
		mTestPopupWindow.setOutsideTouchable(true);
		mTestPopupWindow.setContentView(contentView);

	}

	private void restorePreviousGraphViewInstanceIfAny() {
		Object data = getLastNonConfigurationInstance();
		if (data == null) {

			// Create graphView since its not loaded yet

			mGraphView = new GraphView(this);

			// Get dbname passed with the Intent that started this Activity
			// Hardcore defensive programming ftw!
			Bundle extras = getIntent().getExtras();
			if (extras == null) {
				throw new NullPointerException("No extras passed in the Intent");
			}

			String dbName = extras.getString(IndexActivity.DB_NAME);
			if (dbName == null) {
				throw new NullPointerException(
						"No dbName passed in the Intent extras");
			}
			GraphDatabase graphDatabase = new GraphDatabase(this, dbName);
			mGraphView.setGraphDatabase(graphDatabase);

		} else {

			// Restore GraphView
			// probably after an orientation change

			GraphView old = (GraphView) data;
			mGraphView = old;

			// Why? Because the db got closed onDestroy() -> relase()!...
			// ...and this method opens it again.
			mGraphView.setGraphDatabase(mGraphView.getGraphDatabase());

			Log.i(TAG, "Data Transported!");
		}
	}

	@SuppressWarnings("unused")
	private void showPopupWindow(View anchor) {
		// int gravity = Gravity.TOP | Gravity.LEFT;
		// mTestPopupWindow.showAtLocation(parent, gravity, 0, 0);
		mTestPopupWindow.showAsDropDown(anchor); // anchor
	}

	private void hideSoftkeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mEditText.getApplicationWindowToken(), 0);
	}

	private void showSoftkeyboard() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(mEditText, 0);
	}

	protected void onPause() {
		super.onPause();
		mGraphView.setCallbacks(null);
		mGraphView.stopAnimation();
		mGraphView.saveAll();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mGraphView.setCallbacks(this);
		mGraphView.startAnimation();

	}

	protected void onDestroy() {
		super.onDestroy();

		mGraphView.relase();
		mRootLayout.removeView(mGraphView);
	}

	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean("menuShown", mMenuShown);
		outState.putInt("editMode", mEditMode.ordinal());
		outState.putString("textField", mEditText.getText().toString());

	}

	@Override
	public void onSelectedNodeChanged() {
		if (mEditMode == EditMode.EDIT) {
			if (mGraphView.isNodeSelected()) {
				setTextFieldText(mGraphView.getSelectedText());
			} else {
				stopEdit();
			}
		}

	}

	private void setTextFieldText(String text) {
		mEditText.setText(text);
		mEditText.setSelection(text.length());
	}

}
