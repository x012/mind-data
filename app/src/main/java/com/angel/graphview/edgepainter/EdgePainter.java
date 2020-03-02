package com.angel.graphview.edgepainter;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;

public class EdgePainter {
	public int id = -1;
	
	protected static final int ANDRO_BLUE = 0xFF0099CC;
	protected static final int ANDRO_RED = 0xFFFF4444;
	protected static final int ANDRO_GREEN = 0xFF669900;//0xFF99cc00;
	protected static final int ANDRO_NEUTAL = 0xFF909090;
	

	
	protected static final Path ARROW_HEAD = new Path();
	protected static final Path X_HEAD = new Path();

	static {
   		// Construct a wedge-shaped path
		ARROW_HEAD.moveTo(0, -3);
		ARROW_HEAD.lineTo(-3.2f, 4);
		ARROW_HEAD.lineTo(0, 2);
		ARROW_HEAD.lineTo(3.2f, 4);
		ARROW_HEAD.close();
		Matrix m = new Matrix();
		m.postTranslate(0, 1.5f);
		m.postScale(1.2f, 1.2f);
		ARROW_HEAD.transform(m);
    
		// Construct a X-shaped path
		Matrix r90 = new Matrix();
		r90.postRotate(-90);
		X_HEAD.moveTo(1, 0);
		
		for (int i = 0; i < 4; i++) {
			X_HEAD.lineTo(1, 0);
			X_HEAD.lineTo(4, 3);
			X_HEAD.lineTo(3, 4);
			
			X_HEAD.transform(r90);
		}
    
		X_HEAD.close();
    
		X_HEAD.transform(m);
    
	}


	public static EdgePainter plain = new Plain();
	public static EdgePainter greenV = new GreenV();
	public static EdgePainter redV = new RedV();
	public static EdgePainter greenX = new GreenX();
	public static EdgePainter redX = new RedX();
	
	public static EdgePainter[] edgeTypes = {plain, greenV, redV, greenX, redX};
	

	public void render(Canvas c, float sourceX, float sourceY, float targetX,
			float targetY, float headDirection) {
		// Double head test
		// c.restore();
		// c.save();
		// c.translate(sourceX, sourceY);
		// c.rotate(deg + 180);
		// c.drawPath(X_HEAD, EDGE_PAINT);

	}

}
