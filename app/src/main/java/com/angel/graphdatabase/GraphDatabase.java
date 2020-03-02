package com.angel.graphdatabase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Matrix;
import android.util.Log;

import com.angel.graphview.Edge;
import com.angel.graphview.Node;

/**
 * <h1>Responsibilities:</h1> <br>
 * <ul>
 * <li>store data arrays</li>
 * <li>link edges to nodes</li>
 * <li>load data from db</li>
 * <li>write data to db</li>
 * <li>update cache</li>
 * <li>supply graph specific utilities</li>
 * </ul>
 */
public class GraphDatabase {

	// Original txt Modified
	private static final String EDGES_TABLE = "edges";
	private static final String NODES_TABLE = "nodes";
	private static final String META_DATA_TABLE = "meta_data";

	private static final String DATA = "data";
	private static final String _ID = "_id";
	private static final String TO_ID = "to_id";
	private static final String FROM_ID = "from_id";
	public static final int DATABASE_VERSION = 3;
	private static final String TAG = "GraphDatabase";
	private SQLiteDatabase mDb;
	private SQLiteOpenHelper mSQLiteOpenHelper;

	private final ArrayList<Node> mNodes = new ArrayList<Node>();

	private final ArrayList<Edge> mEdges = new ArrayList<Edge>();

	private HashMap<Object, Node> mKeyToNode = new HashMap<Object, Node>();

	private Node[] mNodesCache;
	private boolean graphInitalized = false;

	public void removeEdge(Edge e) {
		mEdges.remove(e);
		deleteEdge(e);

	}

	public Edge connect(Node from, Node to, int edgeType) {
		Edge e = new Edge(from, to);
		// e.fromKey = from.getKey();
		// e.toKey = to.getKey();
		e.setType(edgeType);
		createEdgeInDb(e);
		mEdges.add(e);
		return e;

	}

	public void removeNode(Node node) {

		mNodes.remove(node);

		// remove edges connected to selected node
		Iterator<Edge> i = mEdges.iterator();
		while (i.hasNext()) {
			Edge e = i.next();
			if (e.from == node || e.to == node) {
				i.remove();
				deleteEdge(e);
			}
		}

		Object key = node.getKey();
		long id = (Long) key;
		mDb.delete(NODES_TABLE, _ID + "=" + id, null);
		mKeyToNode.remove(key);
		updateNodesCache();
	}

	public Edge findEdge(Node from, Node to) {
		Edge result = null;
		for (Edge e : mEdges) {
			if (e.from != from) {
				continue;
			} else if (e.to != to) {
				continue;
			}

			// if we are here we found something
			result = e;
		}
		return result;
	}

	/**
	 * Add Node to data structures
	 */
	private Node putNodeToDataStruct(Node n) {

		mKeyToNode.put(n.getKey(), n);
		mNodes.add(n);
		return n;
	}

	/**
	 * Create node in db, set key
	 */
	public Node addNode(Node n) {

		// Create it in db
		ContentValues values = new ContentValues();
		values.put(DATA, n.encode());

		Long key = mDb.insert(NODES_TABLE, null, values);

		// Set the returned key
		n.setKey(key);

		putNodeToDataStruct(n);
		updateNodesCache();
		return n;
	}

	/**
	 * Update node in db
	 */
	public void updateNode(Node n) {

		// Update it in db
		ContentValues values = new ContentValues();
		values.put(DATA, n.encode());
		String whereClause = _ID + "=" + n.getKey();
		String[] whereArgs = null;
		long numRows = mDb.update(NODES_TABLE, values, whereClause, whereArgs);
		Log.i(TAG, "num rows affected: " + numRows);
		// Log.d(TAG,"wC: " + whereClause);
	}

	public void updateAllNodesInDb() {
		for (Node n : mNodes) {
			updateNode(n);
		}
	}

	public void loadGraph() {

		if (graphInitalized)
			return;

		// Load nodes
		Node[] ns = loadNodes();
		for (Node n : ns) {
			// float rw = Common.RND.nextFloat() * 300;
			// float rh = Common.RND.nextFloat() * 400;
			// n.cx = rw;
			// n.cy = rh;

			// (+mNodes +keyToNode)
			putNodeToDataStruct(n);
		}
		updateNodesCache();

		// Load Edges
		Edge[] es = loadEdges();
		for (Edge e : es) {
			mEdges.add(e);
		}

		graphInitalized = true;
	}

	public final Node[] getNodesCache() {
		return mNodesCache;
	}

	public ArrayList<Edge> getEdges() {
		return mEdges;
	}

	public ArrayList<Node> getNodes() {
		return mNodes;
	}

	private void updateNodesCache() {
		mNodesCache = new Node[mNodes.size()];
		mNodes.toArray(mNodesCache);
	}

	public GraphDatabase(Context context, String dbName) {
		mSQLiteOpenHelper = new GraphDbOpenHelper(context, dbName);
	}

	public void open() {
		try {
			mDb = mSQLiteOpenHelper.getWritableDatabase();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public void close() {

		// try {
		// mDb.close();
		// } catch (SQLException e) {
		// e.printStackTrace();
		// }
		mSQLiteOpenHelper.close();
	}

	public void saveAll() {
		updateAllNodesInDb();

	}

	// Load nodes into the nodes HashMap
	private Node[] loadNodes() {

		// fetch all nodes
		Cursor cur = mDb.query(NODES_TABLE, new String[] { _ID, DATA }, null,
				null, null, null, null);
		int idIndx = cur.getColumnIndex(_ID);
		int dataIndx = cur.getColumnIndex(DATA);

		Node[] ns = new Node[cur.getCount()];
		int i = 0;

		cur.moveToFirst();
		while (!cur.isAfterLast()) {
			long id = cur.getLong(idIndx);
			byte[] data = cur.getBlob(dataIndx);

			Node n;
			n = new Node();
			n.setKey(id);
			n.decode(data);

			// keyToNode.put(key, n);
			ns[i++] = n;
			cur.moveToNext();
		}

		return ns;
	}

	// Load edges
	private Edge[] loadEdges() {

		// fetch all edges
		Cursor cur = mDb.query(EDGES_TABLE, new String[] { _ID, FROM_ID, TO_ID,
				DATA }, null, null, null, null, null);

		int idIndx = cur.getColumnIndex(_ID);
		int fromIdIndx = cur.getColumnIndex(FROM_ID);
		int toIdIndx = cur.getColumnIndex(TO_ID);
		int dataIndx = cur.getColumnIndex(DATA);

		Edge[] edges = new Edge[cur.getCount()];
		int i = 0;

		cur.moveToFirst();
		while (!cur.isAfterLast()) {
			long id = cur.getLong(idIndx);
			Long fromKey = cur.getLong(fromIdIndx);
			Long toKey = cur.getLong(toIdIndx);
			byte[] data = cur.getBlob(dataIndx);
			Log.d(TAG, "data: " + Arrays.toString(data));

			// assume that nodes are already loaded
			Node fromNode = mKeyToNode.get(fromKey);
			Node toNode = mKeyToNode.get(toKey);

			// Defense
			if (fromNode == null || toNode == null) {
				throw new RuntimeException("fromNode or toNode is null");
			}

			Edge e = new Edge(fromNode, toNode);

			// TODO This does not have any useful purpuse
			e.edgeKey = id;

			e.decode(data);

			edges[i++] = e;

			cur.moveToNext();
		}

		return edges;
	}

	private void deleteEdge(Edge e) {
		Object key = e.getKey();
		long id = (Long) key;
		mDb.delete(EDGES_TABLE, _ID + "=" + id, null);
	}

	public Node findNearest(float x, float y) {

		Node nearest = null;
		float minDistSq = Float.MAX_VALUE;
		final ArrayList<Node> nodes = getNodes();
		// final Node[nodes = graphManager.getNodes();

		for (Node n : nodes) {
			float distX = x - n.cx;
			float distY = y - n.cy;
			float distSq = distX * distX + distY * distY;
			if (distSq < minDistSq) {
				nearest = n;
				minDistSq = distSq;
			}
		}
		return nearest;
	}

	private final void createEdgeInDb(Edge e) {
		ContentValues values = new ContentValues();

		values.put(FROM_ID, (Long) e.from.getKey());
		values.put(TO_ID, (Long) e.to.getKey());
		values.put(DATA, e.encode());

		e.edgeKey = mDb.insert(EDGES_TABLE, null, values);
	}

	public void saveMatrix(Matrix m) {
		float[] matrixValues = new float[9];
		ContentValues values = new ContentValues();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		for (float val : matrixValues) {
			try {
				dos.writeFloat(val);
			} catch (IOException unlikely) {
				unlikely.printStackTrace();
			}
		}
		values.put(DATA, baos.toByteArray());
		mDb.insert(META_DATA_TABLE, null, values);
	}

	public boolean loadMatrix(Matrix m) {
		if (graphInitalized)
			return false;

		Cursor cursor = mDb.query(META_DATA_TABLE, new String[] { DATA }, null,
				null, null, null, null);
		if (cursor.moveToFirst()) {
			byte[] data = cursor.getBlob(0);
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			DataInputStream dis = new DataInputStream(bais);
			float[] matrixValues = new float[9];
			for (int i = 0; i < 9; i++) {
				try {
					matrixValues[i] = dis.readFloat();
				} catch (IOException unlikely) {
					unlikely.printStackTrace();
				}
			}
			m.setValues(matrixValues);
			return true;
		}
		return false;
	}

}
