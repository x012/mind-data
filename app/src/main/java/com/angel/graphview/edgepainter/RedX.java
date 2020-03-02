package com.angel.graphview.edgepainter;

import com.angel.graphview.GraphView;

import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;

public class RedX extends EdgePainter {
	int id = 0;
	private static final Paint EDGE_PAINT = new Paint();
	static {
		EDGE_PAINT.setAntiAlias(GraphView.ANTI_ALIAS);
		//EDGE_PAINT.setColor(Color.WHITE);
		EDGE_PAINT.setColor(ANDRO_RED);
		
		//EDGE_PAINT.setAlpha(0x40);
		EDGE_PAINT.setStrokeWidth(1.5f);float[] ivals = new float[] {4,4};
		EDGE_PAINT.setPathEffect(new DashPathEffect(ivals, 0));
	}
	
	public void render(Canvas c, float sourceX, float sourceY, float targetX,
			float targetY, float deg) {
     	c.drawLine(sourceX, sourceY, targetX, targetY, EDGE_PAINT);

     	c.save();
		c.translate(targetX, targetY);
		c.rotate(deg);
		
		c.drawPath(X_HEAD, EDGE_PAINT);

		c.restore();


	}

}
