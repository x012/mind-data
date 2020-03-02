package com.angel.graphview.components;

import com.angel.activity.Shared;
import com.angel.graphdatabase.GraphDatabase;
import com.angel.graphview.Node;

public class FastRepulsor extends Repulsor {
	public void pushApart(GraphDatabase graphManager) {

		// SPACE OUT VERSION 2.0
		// This was most time consuming method
		// Now its optimized, Yayyy!!
		// Performace Increased by 326% (50 nodes)

		// still slow 5% - 10% effectiveness (in range 50 nodes)

		// eff measure
		// int allstat = 0;
		// int inrange = 0;

		Node[] nodesCache = graphManager.getNodesCache();

		for (Node n1 : nodesCache) {
			for (Node n2 : nodesCache) {
				if (n1 == n2) {
					break;
				}
				float vmodx, vmody;
				// Edge.calcLinePts(pts, n1, n2);
				float dx = n1.cx - n2.cx;
				float dy = n1.cy - n2.cy;
				// float dx = pts[0];
				// float dy = pts[1];

				// float sizeMod = 30 / (n1.getWidth() + n2.getWidth());
				// Log.d(TAG, "Mod:---- " + sizeMod);
				// dx *= sizeMod;
				// dy *= sizeMod;

				float distSq = dx * dx + dy * dy;

				// allstat++;
				if (distSq < SPACE_DIST_SQ) {
					// inrange++;
					if (distSq < 1) {

						// Too close, so just shake 'em
						vmodx = (Shared.RND.nextFloat() - 0.5f) * 10;
						vmody = (Shared.RND.nextFloat() - 0.5f) * 10;
					} else {

						// In range, push 'em apart
						float mod = (SPACE_DIST_SQ - distSq) / SPACE_DIST_SQ;
						vmodx = (dx * mod) / distSq * SPACE_HARDNESS;
						vmody = (dy * mod) / distSq * SPACE_HARDNESS;
					}
					n1.vx += vmodx;
					n1.vy += vmody;
					n2.vx -= vmodx;
					n2.vy -= vmody;
				}
			}
		}
		// eff measure
		// Log.d(TAG, "effectiveness: " + (float) inrange / allstat + " n: "
		// + nodesCache.length);

	}

}
