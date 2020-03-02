package com.angel.graphview.components;

import java.util.ArrayList;

import android.util.FloatMath;

import com.angel.graphdatabase.GraphDatabase;
import com.angel.graphview.Edge;
import com.angel.graphview.Node;


public class VisibleEdgeRelaxer extends EdgeRelaxer {

	public void relaxEdges(GraphDatabase graphDatabase) {
		final ArrayList<Edge> edges = graphDatabase.getEdges();

		for (Edge e : edges) {

			// Calculate velocity modification
			Node n0 = e.from;
			Node n1 = e.to;

			float dx, dy;

			// Should optimize I guess... maybe later
			if (e.realDistX != 0 || e.realDistY != 0) {
				dx = e.realDistX;
				dy = e.realDistY;
			} else {
				dx = n1.cx - n0.cx;
				dy = n1.cy - n0.cy;
			}

			float sqlen = dx * dx + dy * dy;
			float len = FloatMath.sqrt(sqlen);

			// Increase edge length based on node size
			// float sizeMod = vn0.getWidth() + vn1.getWidth() - 100;
			// sizeMod = Math.max(0, sizeMod);

			// Prevent division by zero
			len = (len == 0) ? .0001f : len;

			float f = ((TARGET_LENGTH) - len) / (len * EDGE_SOFTNESS);
			float vmodx = f * dx;
			float vmody = f * dy;

			// Apply the modification in the velocity
			n0.vx -= vmodx;
			n0.vy -= vmody;
			n1.vx += vmodx;
			n1.vy += vmody;

		}
	}

}
