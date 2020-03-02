package com.angel.graphview;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

import com.angel.activity.Shared;

public class Node {

	// Center x,y
	public float cx;
	public float cy;

	// Velocity x,y
	public float vx;
	public float vy;
	static double phase;

	private static final int COLOR_BLACK = 0xFF000000;

	private static final int COLOR_NORMAL = COLOR_BLACK;// 0xBB101010;
	private static final int DARK_RED = 0xFF880000;

	private static final int COLOR_HIGHLIGHT = 0xFF4D0066;
	// private static final int DARK_GREEN = 0xFF226600;

	// private static final int PURPLE = 0xBB5E2080;

	static final Paint PAINT_NODE = new Paint();
	static final TextPaint PAINT_TXT = new TextPaint();
	static final Paint PAINT_SELECT = new Paint();
	static final Paint PAINT_CACHE = new Paint(Paint.FILTER_BITMAP_FLAG);

	static final float PADDING = 4;

	// private static final String TAG = "Node";
	private static final Paint GLOW_PAINT = new Paint();

	static {
		PAINT_TXT.setColor(Color.LTGRAY);
		PAINT_TXT.setAntiAlias(GraphView.ANTI_ALIAS);
		PAINT_TXT.setSubpixelText(true);

		PAINT_NODE.setAntiAlias(GraphView.ANTI_ALIAS);
		PAINT_NODE.setColor(COLOR_NORMAL);

		PAINT_SELECT.setAntiAlias(GraphView.ANTI_ALIAS);
		PAINT_SELECT.setColor(Color.WHITE);
		PAINT_SELECT.setStyle(Paint.Style.STROKE);
		PAINT_SELECT.setStrokeWidth(3);

		// GLOW_PAINT.setColor(0xFF080808);
		GLOW_PAINT.setColor(0x08FFFFFF);

		GLOW_PAINT.setAntiAlias(GraphView.ANTI_ALIAS);

	}

	private StaticLayout layout;
	public String label;
	private Object key;
	private Bitmap cache[];

	boolean highlighted;
	private int mColor;
	private static final RectF sRect = new RectF();
	
	static Random sRnd = new Random();

	private void draw(Canvas c, float cx, float cy) {
		float w = getWidth();
		float h = getHeight();

		// Set highlight
		if (highlighted) {
			// PAINT_NODE.setColor(COLOR_HIGHLIGHT);
			PAINT_NODE.setColor(mColor);
			
			// float sin = FloatMath.sin((float) phase / 5) * (float) .05
			//		+ (float) 1;
			// c.drawCircle(cx, cy, 100 * sin, GLOW_PAINT);
			// c.drawCircle(cx, cy, 80 * sin, GLOW_PAINT);
			// c.drawCircle(cx, cy, 60 * sin, GLOW_PAINT);
		}

		sRect.set(cx - w, cy - h, cx + w, cy + h);
		c.drawRoundRect(sRect, 3, 3, PAINT_NODE);

		c.save();
		c.translate(cx - w + PADDING, cy - h + PADDING);
		layout.draw(c);
		c.restore();

		// restore lowlight :]
		if (highlighted) {
			PAINT_NODE.setColor(COLOR_NORMAL);
			PAINT_TXT.setColor(Color.LTGRAY);

		}
	}

	public void createCache() {
		//
		// cache = new Bitmap[2];
		// float h = getHeight();
		// float w = getWidth();
		// Bitmap bm = Bitmap.createBitmap((int) (w * 2 + 2), (int) (h * 2 + 2),
		// Bitmap.Config.ARGB_8888);
		//
		// Canvas c = new Canvas(bm);
		// draw(c, w + 1, h + 1);
		// cache[0] = bm;
		// // -----------------------------
		// bm = Bitmap.createBitmap((int) (w + 2), (int) (h + 2),
		// Bitmap.Config.ARGB_8888);
		// bm.setDensity(80);
		//
		// c = new Canvas(bm);
		// Paint p = new Paint(Paint.FILTER_BITMAP_FLAG);
		// Rect src = new Rect(1, 1, (int) (w * 2 + 1), (int) (h * 2 + 1));
		// Rect dst = new Rect(1, 1, (int) w + 1, (int) h + 1);
		// c.drawBitmap(cache[0], src, dst, p);
		// cache[1] = bm;
		//
	}

	public void clearCache() {
		cache = null;
	}

	public float getHeight() {
		// return 10;
		return layout.getHeight() / 2 + PADDING;

	}

	public float getWidth() {
		// return TXT_PAINT.measureText(label.toString()) / 2 + 4;
		return layout.getWidth() / 2 + PADDING;
	}

	public Object getKey() {
		return key;
	}

	public void setLabel(String lbl) {

		label = lbl;
		float measured = PAINT_TXT.measureText(label);
		int width = (int) Math.min(measured, 100) + 1;
		Alignment align = Layout.Alignment.ALIGN_CENTER;
		float spacingmult = 1;
		float spacingadd = 0;
		boolean includepad = false;

		layout = new StaticLayout(lbl, PAINT_TXT, width, align, spacingmult,
				spacingadd, includepad);
		mColor = hashToColor(lbl.hashCode());
	}

	public void setKey(Object key) {
		this.key = key;
	}

	public void paint(Canvas c) {
		// Log.i(TAG, "zoom " + zoom);
		if (cache != null) {
			float zoom = Shared.getZoom(c);
			float w = getWidth();
			float h = getHeight();
			if (zoom > 0.66) {
				c.drawBitmap(cache[0], cx - w - 1, cy - h - 1, PAINT_CACHE);
			} else {
				c.drawBitmap(cache[1], cx - w - 2, cy - h - 2, PAINT_CACHE);
			}
		} else {
			draw(c, cx, cy);
		}
	}

	public Node() {
	}

	public byte[] encode() {
		byte[] data = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos;
		dos = new DataOutputStream(baos);
		try {
			// The crux
			dos.writeUTF(getLabel());
			dos.writeFloat(cx);
			dos.writeFloat(cy);
			dos.writeFloat(vx);
			dos.writeFloat(vy);
			dos.writeBoolean(highlighted);

			data = baos.toByteArray();
			dos.close();
			baos.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return data;
	}

	public void decode(byte[] data) {
		ByteArrayInputStream bais = new ByteArrayInputStream(data);
		DataInputStream dis = new DataInputStream(bais);
		try {
			// The crux
			setLabel(dis.readUTF());
			cx = dis.readFloat();
			cy = dis.readFloat();
			vx = dis.readFloat();
			vy = dis.readFloat();

			highlighted = dis.readBoolean();

			createCache();
			dis.close();
			bais.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/** Sets selector glow animation phase to the next... */
	public static void staticTick() {
		//phase += Math.PI / 8;
	}

	public static void setGlobalTypeface(Typeface tf) {
		PAINT_TXT.setTypeface(tf);

	}

	public String getLabel() {
		return label;
	}
	
	private int hashToColor(int hash) {
		sRnd.setSeed(hash);
		final float[] hsv = new float[3];
		hsv[0] = sRnd.nextFloat() * 360; // hue
		Log.d("Node.hashToColor", "hue: "+ hsv[0]);
		hsv[1] = 1f; // sat
		hsv[2] = .35f; // bright (value)
		return Color.HSVToColor(hsv);

	}
}
