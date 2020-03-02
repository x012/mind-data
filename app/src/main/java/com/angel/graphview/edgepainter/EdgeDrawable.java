package com.angel.graphview.edgepainter;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public class EdgeDrawable extends Drawable {
	
	EdgePainter mEdgePainter;
	float mDensity;
	boolean mInverted;
	
	public EdgeDrawable(EdgePainter painter, float density, boolean inverted) {
		mEdgePainter = painter;
		mDensity = density;
		mInverted = inverted;
	}

	public void draw(Canvas c) {
		Rect bounds = getBounds();
		float sourceX;
		float sourceY;
		float targetX;
		float targetY;
		float headDirection;
		if (!mInverted) {
			sourceX = bounds.left + 8 * mDensity;
			sourceY = bounds.bottom - 8 * mDensity;
			targetX = bounds.right - 10 * mDensity;
			targetY = bounds.top + 10 * mDensity;
			headDirection = 45;
		} else {
			targetX = bounds.left + 10 * mDensity;
			targetY = bounds.bottom - 10 * mDensity;
			sourceX = bounds.right - 8 * mDensity;
			sourceY = bounds.top + 8 * mDensity;
			headDirection = 45 + 180;
		}
		
		
		//TODO
		// calc arrowhead direction
		//grad = diffY / diffX;
		//double atan = Math.atan(grad);
		//float headDirection = (float) (atan / Math.PI) * 180 + 90 * Math.signum(diffX);

		float scale = mDensity * 1.5f;
		c.scale(scale, scale);
		
		mEdgePainter.render(c, sourceX / scale, sourceY / scale, targetX / scale, targetY / scale, headDirection);

		}

	public int getOpacity() {
		return android.graphics.PixelFormat.TRANSLUCENT;
	}

	public void setAlpha(int alpha) {
		throw new UnsupportedOperationException();
	}

	public void setColorFilter(ColorFilter cf) {
		throw new UnsupportedOperationException();
	}

	public int getIntrinsicWidth() {
		return (int) (48 * mDensity);
	}

	@Override
	public int getIntrinsicHeight() {
		return (int) (48 * mDensity);
	}
	
	

}
