package com.angel.activity;

import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.util.FloatMath;

public class Shared {
	private static final String[] SYL = { "tá", "dé", "ba", "ra", "ló", "ku",
			"ma", "se", "pó", "ci", "sa", "bi", "ha", "sza", "bü", "dös",
			"pun", "ki", "ter", "kes", "pás", "li", "szo", "fi", "pi", "ros",
			"al", "ku", "sa", "nyó", "fó", "der", "só", "ger", "gõ", "rom",
			"ka", "a"

	};

	public static final Random RND = new Random();

	private static float[] mValues = new float[9];

	public static final String createRandomString() {
		
		// attach some random syllables
		StringBuilder word = new StringBuilder();
		int numsyl = RND.nextInt(3) + 2;
		for(int i = 0; i < numsyl; i++) {
			word.append(SYL[RND.nextInt(SYL.length)]);
		}
		
		// capitalize 1st letter
		char first = word.charAt(0);
		first = Character.toUpperCase(first);
		word.setCharAt(0, first);
		
		return word.toString();
	}

	public static final float getZoom(Canvas c) {
		Matrix m = c.getMatrix();
		return getZoom(m);
	}

	public static float getZoom(Matrix m) {
		float[] values = mValues;
		m.getValues(values);
		float xscale = values[Matrix.MSCALE_X];
		float xskew = values[Matrix.MSKEW_X];
		float zoom = FloatMath.sqrt(xscale * xscale + xskew * xskew);
		return zoom;
	}
	
	public static void unRotate(Matrix m) {
		float[] values = new float[9];
		m.getValues(values);
		float xscale = values[Matrix.MSCALE_X];
		float xskew = values[Matrix.MSKEW_X];
		float zoom = FloatMath.sqrt(xscale * xscale + xskew * xskew);
		values[Matrix.MSKEW_X] = 0;
		values[Matrix.MSKEW_Y] = 0;
		values[Matrix.MSCALE_X] = zoom;
		values[Matrix.MSCALE_Y] = zoom;
		m.setValues(values);
		
	}
	
	
	

}
