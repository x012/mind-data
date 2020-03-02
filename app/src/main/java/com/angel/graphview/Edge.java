package com.angel.graphview;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import android.graphics.Canvas;

import com.angel.graphview.edgepainter.EdgePainter;

public class Edge {
	
	EdgePainter mEdgePainter = EdgePainter.plain;
	
        
	public Node from, to;
    
	/** This does not have any useful purpuse yet*/
	public Object edgeKey;
    
	/** Real distance cache (x component), for edge relaxing optimization*/
	public transient float realDistX;
    
	/** Real distance cache (y component), for edge relaxing optimization*/
	public transient float realDistY;
	
	// This is the only variable storing edge type now
	// TODO Think about implementing more edge types
	// ideas:
	// EdgeStyle mEdgeStyle
	// EdgeStyle -*> Attributes...*
	//
	// NodeStyle extends Style
	// Style -> Attributes...*
	
	/** Defines appearance
	 *  Currently the only edge visual style prop*/
	int edgeType = 0;

	public Object getKey() {
		return edgeKey;
	}


	public void paint(Canvas c) {
		float sourceX = from.cx;
		float sourceY = from.cy;
		float targetX = to.cx;
		float targetY = to.cy;
    
		float diffX = targetX - sourceX;
		float diffY = targetY - sourceY;
    
		float grad = Math.abs(diffY / diffX);
    
		// Calculate line staring point (from)
		float width = from.getWidth() + 3;
		float height = from.getHeight() + 3;
		float gradNode = height / width;
    
		if (gradNode > grad) {
			if (diffX > 0) {
				// jobb
				sourceX += width;
				sourceY += width / diffX * diffY;
			} else {
				// bal
				sourceX -= width;
				sourceY -= width / diffX * diffY;
			}
    
		} else {
			if (diffY > 0) {
				// lent
				sourceY += height;
				sourceX += height / diffY * diffX;
			} else {
				// fent
				sourceY -= height;
				sourceX -= height / diffY * diffX;
			}
		}
    
		// Calculate line stopping point (to)
		width = to.getWidth() + 3;
		height = to.getHeight() + 3;
		gradNode = height / width;
    
		if (gradNode > grad) {
			if (diffX < 0) {
				// jobb
				targetX += width;
				targetY += width / diffX * diffY;
			} else {
				// bal
				targetX -= width;
				targetY -= width / diffX * diffY;
			}
    
		} else {
			if (diffY < 0) {
				// lent
				targetY += height;
				targetX += height / diffY * diffX;
			} else {
				// fent
				targetY -= height;
				targetX -= height / diffY * diffX;
			}
		}
		
		// optimization:
		// store calculated distance
		realDistX = targetX - sourceX;
		realDistY = targetY - sourceY;
    
    
		// calc arrowhead direction
		grad = diffY / diffX;
		double atan = Math.atan(grad);
		float headDirection = (float) (atan / Math.PI) * 180 + 90 * Math.signum(diffX);
		
		// canvas, sx, sy, tx, ty, deg
		mEdgePainter.render(c, sourceX, sourceY, targetX, targetY, headDirection);
		
		
	}

	public Edge(Node from, Node to) {
		this.from = from;
		this.to = to;
	}

	/** A better dist calc for spacing / edge relaxing. 
	 * Its unused now because of these calculations cached in
	 * realDistX and realDistY*/
	public static void calcLinePts(float pts[], Node f, Node t) {
		float fx = f.cx;
		float fy = f.cy;
		float tx = t.cx;
		float ty = t.cy;

		float dx = tx - fx;
		float dy = ty - fy;

		float grad = Math.abs(dy / dx);

		// Calculate line staring point (from)
		float width = f.getWidth();
		float height = f.getHeight();
		float gradNode = height / width;

		if (gradNode > grad) {
			if (dx > 0) {
				// jobb
				fx += width;
				fy += width / dx * dy;
			} else {
				// bal
				fx -= width;
				fy -= width / dx * dy;
			}

		} else {
			if (dy > 0) {
				// lent
				fy += height;
				fx += height / dy * dx;
			} else {
				// fent
				fy -= height;
				fx -= height / dy * dx;
			}
		}

		// Calculate line stopping point (to)
		width = t.getWidth() + 3;
		height = t.getHeight() + 3;
		gradNode = height / width;

		if (gradNode > grad) {
			if (dx < 0) {
				// jobb
				tx += width;
				ty += width / dx * dy;
			} else {
				// bal
				tx -= width;
				ty -= width / dx * dy;
			}

		} else {
			if (dy < 0) {
				// lent
				ty += height;
				tx += height / dy * dx;
			} else {
				// fent
				ty -= height;
				tx -= height / dy * dx;
			}
		}

		// pts[0] = fx;
		// pts[1] = fy;
		// pts[2] = tx;
		// pts[3] = ty;
		// float ldx = Math.copySign(tx - fx, dx);
		// float ldy = Math.copySign(ty - fy, dy);
		float ldx = tx - fx;
		float ldy = ty - fy;

		// if inverted
		if ((ldx * dx) < 0) {
			ldx = -dx;
			ldy = -dy;
		}

		pts[0] = -ldx;
		pts[1] = -ldy;

	}
	
	public void decode(byte[] data) {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		DataInputStream dis = new DataInputStream(bais);
		try {
			// The crux
			setType(dis.readInt());

			dis.close();
			bais.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public byte[] encode() {
		byte[] data = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos;
		dos = new DataOutputStream(baos);
		try {
			// The crux
			dos.writeInt(edgeType);

			data = baos.toByteArray();
			dos.close();
			baos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	public void setType(int type) {
		edgeType = type;
		mEdgePainter = EdgePainter.edgeTypes[type];
	}
	

}
