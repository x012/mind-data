package com.angel.graphview.components;

import java.util.ArrayList;

import com.angel.activity.Shared;
import com.angel.graphdatabase.GraphDatabase;
import com.angel.graphview.Node;

public class NormalRepulsor extends Repulsor {

	public void pushApart(GraphDatabase graphManager) {

		// The most time consuming method
		// Any optimalizations count

		final ArrayList<Node> nodes = graphManager.getNodes();

		for (Node n1 : nodes) {

			for (Node n2 : nodes) {
				if (n1 == n2) {
					break;
				}
				float dx, dy;
				float difX = n1.cx - n2.cx;
				float difY = n1.cy - n2.cy;
				float distSq = difX * difX + difY * difY;
				if (distSq < 1) {

					// Too close, so just shake 'em
					dx = Shared.RND.nextFloat() - 0.5f;
					dy = Shared.RND.nextFloat() - 0.5f;
					n1.vx += dx;
					n1.vy += dy;
					n2.vx -= dx;
					n2.vy -= dy;

				} else if (distSq < SPACE_DIST_SQ) {

					// In range, push 'em apart
					float mod = (SPACE_DIST_SQ - distSq) / SPACE_DIST_SQ;
					dx = (difX * mod) / distSq * SPACE_HARDNESS;
					dy = (difY * mod) / distSq * SPACE_HARDNESS;
					n1.vx += dx;
					n1.vy += dy;
					n2.vx -= dx;
					n2.vy -= dy;
				}

			}
		}
	}

}
