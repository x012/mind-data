package com.angel.graphview.components;

import android.graphics.Matrix;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;

/**
 * <h1>Responsibilities:</h1> <br>
 * <ul>
 * <li>handle gestures</li>
 * <li>handle camera transformations</li>
 * 
 * </ul>
 */
public class GestureAndTransformationHandler {

	public static class GestureData {
		public boolean drag;
		public boolean fling;
		public int maxPtrs;
		public boolean pinch;
		public int pointerId0;
		public int pointerId1;
		public final float[] pointsEnd = new float[4];
		public final float[] pointsLazy = new float[4];
		public final float[] pointsPreDrag = new float[4];
		public final float[] pointsPrevLazy = new float[4];
		public final float[] pointsStart = new float[4];

	}

	public interface OnGestureListener {
		public void onTap(float absX, float absY);
	}

	private static final float DRAG_THRESHOLD_SQ = 7 * 7;
	private static final float MOMENTUM_DIEOUT = .25f;

	static {
		// fill up Matrix pool
		for (int i = 0; i < 10; i++) {
			MatrixPool.recycle(new Matrix());
		}
	}

	private final GestureData mGestureData = new GestureData();

	private Matrix mMatrix = new Matrix();

	private OnGestureListener mOnGestureListener;

	private Matrix mOrigMatrix = new Matrix();
	
	private float[] mPts = new float[2];
	
	private boolean rotationAllowed = false;

	public GestureAndTransformationHandler(OnGestureListener listener) {
		mOnGestureListener = listener;

	}

	public final void convertScreenCoordsToAbsolute(final float[] pts) {
		final Matrix inverse = MatrixPool.get();
		mMatrix.invert(inverse);
		inverse.mapPoints(pts);
		MatrixPool.recycle(inverse);
	}

	public GestureData getGestureData() {
		return mGestureData;
	}

	public Matrix getScreenTransformMatrix() {
		return mMatrix;
	}

	public boolean onTouchEvent(final MotionEvent event) {

		// called about 50-60 times per sec during finger movement
		// mostly without history data, although history size sometimes can be
		// 10
		// when laggy I guess... (Called from main thread)

		// need to differentiate drags from node select reqs

		int action = event.getActionMasked();
		int actIndex = event.getActionIndex();
		int pCount = event.getPointerCount();

		float[] ps = mGestureData.pointsStart;
		float[] pe = mGestureData.pointsEnd;
		float[] pl = mGestureData.pointsLazy;
		float[] ppl = mGestureData.pointsPrevLazy;
		float[] ppd = mGestureData.pointsPreDrag;

		// For logging...
		// String msg = actIndex + ", id: " + event.findPointerIndex(actIndex);

		if (action == MotionEvent.ACTION_DOWN) { // ////////////////////////////////////
			// Gesture has just started

			// Log action
			// Log.d(TAG, "Action Down, index: " + msg);

			// Init maxPtrs, which is 1 at this point
			mGestureData.maxPtrs = 1;

			// Store initial finger id
			mGestureData.pointerId0 = event.getPointerId(actIndex);

			// Store INITIAL starting point for drag for distance measure
			int i0 = event.findPointerIndex(mGestureData.pointerId0);
			ppd[0] = event.getX(i0);
			ppd[1] = event.getY(i0);
			mGestureData.drag = false;

		} else if (action == MotionEvent.ACTION_MOVE) { // /////////////////////////////
			// In between movement action, nothing special...
			int i0 = event.findPointerIndex(mGestureData.pointerId0);

			if (mGestureData.pinch) {

				// Save current finger positions
				// int i0 = event.findPointerIndex(gData.pointerId0);
				int i1 = event.findPointerIndex(mGestureData.pointerId1);
				pe[0] = event.getX(i0);
				pe[1] = event.getY(i0);
				pe[2] = event.getX(i1);
				pe[3] = event.getY(i1);

			} else if (mGestureData.maxPtrs == 1) {

				// Check for drag threshold hit
				ppd[2] = event.getX(i0);
				ppd[3] = event.getY(i0);

				if (!mGestureData.drag) {
					float distX = ppd[2] - ppd[0];
					float distY = ppd[3] - ppd[1];
					float distSq = distX * distX + distY * distY;
					if (distSq > DRAG_THRESHOLD_SQ) {

						// Drag just started
						// Log.d(TAG, "Drag started " + distSq);
						mGestureData.drag = true;
						mGestureData.fling = false;
						ps[0] = event.getX(i0);
						ps[1] = event.getY(i0);

						// Init lazy points to start points
						System.arraycopy(ps, 0, pl, 0, 2);

						// Init prev lazy points to start points
						System.arraycopy(ps, 0, ppl, 0, 2);

						// Init end point
						System.arraycopy(ps, 0, pe, 0, 2);

						// Save the current matrix,
						// so we can use it as a base while dragging
						mOrigMatrix.set(mMatrix);
					}
				}

				if (mGestureData.drag) {

					// Dragging
					pe[0] = event.getX(i0);
					pe[1] = event.getY(i0);

				}

			}
		} else if (action == MotionEvent.ACTION_POINTER_DOWN) { // /////////////////////
			// New finger has been added to the screen :)

			// Log action
			// Log.d(TAG, "Action Pointer Down, index: " + msg);

			// Update if more fingers are used for this gesture
			mGestureData.maxPtrs = Math.max(pCount, mGestureData.maxPtrs);

			mGestureData.drag = false;

			// Two fingers?
			if (pCount == 2) {
				// Two finger transform just started

				// Store the secondary finger id
				mGestureData.pointerId1 = event.getPointerId(actIndex);

				// Fling ended, pinch started
				mGestureData.fling = false;
				mGestureData.pinch = true;

				// Store starting finger points for this pinch gesture
				int i0 = event.findPointerIndex(mGestureData.pointerId0);
				int i1 = event.findPointerIndex(mGestureData.pointerId1);
				ps[0] = event.getX(i0);
				ps[1] = event.getY(i0);
				ps[2] = event.getX(i1);
				ps[3] = event.getY(i1);

				// Init lazy points to start points
				System.arraycopy(ps, 0, pl, 0, 4);

				// Init prev lazy points to start points
				System.arraycopy(ps, 0, ppl, 0, 4);

				// Init end point
				System.arraycopy(ps, 0, pe, 0, 4);

				// Save the current matrix,
				// so we can use it as a base while pinching
				mOrigMatrix.set(mMatrix);

			}
		} else if (action == MotionEvent.ACTION_POINTER_UP) { // ///////////////////////
			// A finger just got up

			// Log.d(TAG, "Action Pointer Up, index: " + msg);

			if (pCount == 2) {

				// pinch ended, fling started
				mGestureData.pinch = false;
				mGestureData.fling = true;

				// set final (last in this pinch) coords
				int i0 = event.findPointerIndex(mGestureData.pointerId0);
				int i1 = event.findPointerIndex(mGestureData.pointerId1);
				pe[0] = event.getX(i0);
				pe[1] = event.getY(i0);
				pe[2] = event.getX(i1);
				pe[3] = event.getY(i1);

				// If the initial finger is up,
				// than make the secondary initial
				if (event.findPointerIndex(mGestureData.pointerId0) == actIndex) {
					mGestureData.pointerId0 = mGestureData.pointerId1;
				}

			}

		} else if (action == MotionEvent.ACTION_UP) { // ///////////////////////////////
			// Gesture has just ended all fingers are up

			// Log.d(TAG, "Action Up, index: " + msg);

			if (mGestureData.maxPtrs == 1 && !mGestureData.drag) {

				// Tap occured
				mPts[0] = event.getX();
				mPts[1] = event.getY();
				convertScreenCoordsToAbsolute(mPts);
				mOnGestureListener.onTap(mPts[0], mPts[1]);

			} else if (mGestureData.maxPtrs == 1) {

				// It was a drag
				mGestureData.drag = false;
				mGestureData.fling = true;

				// Get the last coords
				int i0 = event.findPointerIndex(mGestureData.pointerId0);
				if (i0 != -1) {

					pe[0] = event.getX();
					pe[1] = event.getY();
					// Log.d(TAG, "last coords got");

				}
			}

		} // ///////////////////////////////END///////////////////////////////////

		return true;
	}

	public void onViewSizeChanged(int w, int h, int oldw, int oldh) {
		if (oldw == 0 && oldh == 0) {
			// set zero point to the center of the view
			mOrigMatrix.postTranslate(w / 2, h / 2);
		} else {
			// preserve center
			float ocx = oldw / 2;
			float ocy = oldh / 2;
			float cx = w / 2;
			float cy = h / 2;
			mMatrix.postTranslate(cx - ocx, cy - ocy);

		}
	}

	public void transformationTick() {
		final float[] pl = mGestureData.pointsLazy;
		final float[] ppl = mGestureData.pointsPrevLazy;

		final float[] pe = mGestureData.pointsEnd;
		final float[] ps = mGestureData.pointsStart;
		if (mGestureData.fling) {

			// Fling
			if (mGestureData.maxPtrs == 2) {

				// Two fingers (pinch) fling
				for (int i = 0; i < 4; i++) {
					ppl[i] = ppl[i] * (1 - MOMENTUM_DIEOUT) + pl[i]
							* MOMENTUM_DIEOUT;
				}

				// New stuff----------------surlodas---------------------
				// for (int i = 0; i < 4; i+=2) {
				// float xdif = ppl[i] - pl[i];
				// float ydif = ppl[i+1] - pl[i+1];
				// float dist = FloatMath.sqrt(xdif * xdif + ydif * ydif);
				// if (dist > 0.01) {
				// ppl[i] -= xdif / dist * 0.01;
				// ppl[i + 1] -= ydif / dist * 0.01;
				//
				// } else {
				// ppl[i] = pl[i];
				// ppl[i + 1] = pl[i + 1];
				// }
				//
				// }

				final Matrix momentum = MatrixPool.get();

				if (rotationAllowed) {
					momentum.setPolyToPoly(ppl, 0, pl, 0, 2);

				} else {
					// calculate no-rotation transform
					float cxS = (ppl[0] + ppl[2]) / 2;
					float cyS = (ppl[1] + ppl[3]) / 2;
					float dxS = ppl[2] - ppl[0];
					float dyS = ppl[1] - ppl[3];
					float rangeSsq = dxS * dxS + dyS * dyS;

					float cxE = (pl[0] + pl[2]) / 2;
					float cyE = (pl[1] + pl[3]) / 2;
					float dxE = pl[2] - pl[0];
					float dyE = pl[1] - pl[3];
					float rangeEsq = dxE * dxE + dyE * dyE;

					// New way (no-rot.)
					float scale = (float) FloatMath.sqrt(rangeEsq / rangeSsq);
					momentum.postScale(scale, scale, cxS, cyS);
					momentum.postTranslate(cxE - cxS, cyE - cyS);
				}

				mMatrix.postConcat(momentum);

				MatrixPool.recycle(momentum);

			} else if (mGestureData.maxPtrs == 1) {

				// One finger (drag) fling
				for (int i = 0; i < 2; i++) {
					ppl[i] = ppl[i] * (1 - MOMENTUM_DIEOUT) + pl[i]
							* MOMENTUM_DIEOUT;
				}

				Matrix momentumMatrix = MatrixPool.get();
				momentumMatrix.setPolyToPoly(ppl, 0, pl, 0, 1);
				mMatrix.postConcat(momentumMatrix);
				MatrixPool.recycle(momentumMatrix);

			}

		} else {

			// pinch/drag
			if (mGestureData.maxPtrs == 2) {

				// Pinch

				// Apply the two finger transformation to our matrix
				System.arraycopy(pl, 0, ppl, 0, 4);
				for (int i = 0; i < 4; i++) {
					pl[i] = pl[i] * .3f + pe[i] * .7f;
				}

				Matrix trans = MatrixPool.get();

				if (rotationAllowed) {

					// Old way rotation that allows rotation
					trans.setPolyToPoly(ps, 0, pl, 0, 2);
				} else {

					// calculate no-rotation transform
					float cxS = (ps[0] + ps[2]) / 2;
					float cyS = (ps[1] + ps[3]) / 2;
					float dxS = ps[2] - ps[0];
					float dyS = ps[1] - ps[3];
					float rangeSsq = dxS * dxS + dyS * dyS;

					float cxE = (pl[0] + pl[2]) / 2;
					float cyE = (pl[1] + pl[3]) / 2;
					float dxE = pl[2] - pl[0];
					float dyE = pl[1] - pl[3];
					float rangeEsq = dxE * dxE + dyE * dyE;

					// New way (no-rot.)
					float scale = FloatMath.sqrt(rangeEsq / rangeSsq);
					trans.postScale(scale, scale, cxS, cyS);
					trans.postTranslate(cxE - cxS, cyE - cyS);
				}

				mMatrix.set(mOrigMatrix);
				mMatrix.postConcat(trans);
				MatrixPool.recycle(trans);

			} else {

				// Drag
				// Apply the one finger transformation to our matrix
				System.arraycopy(pl, 0, ppl, 0, 2);
				for (int i = 0; i < 2; i++) {
					pl[i] = pl[i] * .3f + pe[i] * .7f;
				}

				final Matrix other = MatrixPool.get();
				other.setPolyToPoly(ps, 0, pl, 0, 1);
				mMatrix.set(mOrigMatrix);
				mMatrix.postConcat(other);
				MatrixPool.recycle(other);
			}
		}
	}

	public Matrix getOrigMatrix() {
		return mOrigMatrix;
	}

	public void setOrigMatrix(Matrix mOrigMatrix) {
		this.mOrigMatrix = mOrigMatrix;
	}

	public void scale(float value) {
		mOrigMatrix.postScale(value, value);
		mMatrix.postScale(value, value);
		
		
	}


}

class MatrixPool {
	private static int pointer = 0;
	private static final Matrix[] store = new Matrix[10];
	private static final String TAG = "MatrixPool";
	static {
		Log.d(TAG, "pointer = 0");
		Log.d(TAG, "pointer++ " + pointer++);
		pointer = 0;
		Log.d(TAG, "pointer = 0");
		Log.d(TAG, "++pointer " + ++pointer);
		pointer = 0;

	}

	public static final Matrix get() {
		// Log.d(TAG, "get()");
		// Log.d(TAG, "pointer = " + (pointer - 1));
		return store[--pointer];
	}

	public static final void recycle(Matrix m) {
		// Log.d(TAG, "recycle()");
		// Log.d(TAG, "pointer = " + pointer);
		m.reset();
		store[pointer++] = m;

	}

}
