package com.angel.graphview.components;

import com.angel.graphdatabase.GraphDatabase;

public abstract class EdgeRelaxer {

	public static final float EDGE_SOFTNESS = 20;

	public static final float TARGET_LENGTH = 35;

	public abstract void relaxEdges(GraphDatabase graphDatabase);
}
