package com.angel.activity;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

public class EditTextWithBackEvent extends EditText {
	
	public interface EditTextImeBackListener {
	    public abstract void onImeBack(EditTextWithBackEvent ctrl, String text);
	}

    private EditTextImeBackListener mOnImeBack;

    public EditTextWithBackEvent(Context context) {
        super(context);
    }

    public EditTextWithBackEvent(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EditTextWithBackEvent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            if (mOnImeBack != null) mOnImeBack.onImeBack(this, this.getText().toString());
        }
        return super.dispatchKeyEvent(event);
    }

    public void setOnEditTextImeBackListener(EditTextImeBackListener listener) {
        mOnImeBack = listener;
    }

}

