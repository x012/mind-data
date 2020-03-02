package com.angel.graphview.components;

import com.angel.graphdatabase.GraphDatabase;

public abstract class Repulsor {
	protected static final float SPACE_DIST_SQ = 250 * 250;

	protected static final float SPACE_HARDNESS = 200;

	public abstract void pushApart(GraphDatabase graphManager);

}
