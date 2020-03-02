package com.angel.graphview.components;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Animated View that uses a Handler to schedule frame draws
 * 
 */
public abstract class HandlerAnimatedView extends View {

	private static final int FPS = 30;

	private static final int SEC = 1000;

	// Schedules framedraws at regular intervals
	private final Handler mHandler = new Handler();

	private static final boolean PERF_MEASURE = false;

	private static final String TAG = HandlerAnimatedView.class.getSimpleName();

	private final Runnable mFrameDrawRunnable = new Runnable() {
		public long timeSinceLastExec;
		private long lastTime;

		public void run() {
			if (PERF_MEASURE) {
				long now = System.currentTimeMillis();
				timeSinceLastExec = now - lastTime;
				lastTime = now;
				Log.d(TAG, "frame time (ms): " + timeSinceLastExec);
			}

			mHandler.removeCallbacks(this);
			mHandler.postDelayed(this, SEC / FPS);
			tick();
			invalidate();
		}
	};

	public HandlerAnimatedView(Context context) {
		super(context);
	}

	protected abstract void tick();

	public HandlerAnimatedView(Context context, AttributeSet attr) {
		super(context, attr);
	}

	public HandlerAnimatedView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	protected void onVisibilityChanged(View changedView, int visibility) {
		super.onVisibilityChanged(changedView, visibility);
		if (isInEditMode())
			return;
		if (visibility == VISIBLE) {
			startAnimation();
		} else {
			stopAnimation();
		}
	}

	public void startAnimation() {
		mHandler.postDelayed(mFrameDrawRunnable, SEC / FPS);
	}

	public void stopAnimation() {
		mHandler.removeCallbacks(mFrameDrawRunnable);
	}

}
