package com.angel.graphview;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;

import com.angel.activity.Shared;
import com.angel.graphdatabase.GraphDatabase;
import com.angel.graphview.components.EdgeRelaxer;
import com.angel.graphview.components.FastRepulsor;
import com.angel.graphview.components.GestureAndTransformationHandler;
import com.angel.graphview.components.HandlerAnimatedView;
import com.angel.graphview.components.Repulsor;
import com.angel.graphview.components.VisibleEdgeRelaxer;
import com.angel.graphview.components.GestureAndTransformationHandler.GestureData;
import com.angel.graphview.edgepainter.EdgePainter;

/**
 * <h1>Responsibilities:</h1> <br>
 * <ul>
 * <li>render graphics</li>
 * <li>calculate physics</li>
 * 
 * </ul>
 */
public class GraphView extends HandlerAnimatedView implements
		GestureAndTransformationHandler.OnGestureListener {

	public enum Command {
		ADD_NODE, CHANGE_EDGE, CONNECT, DELETE_NODE, EDIT_NODE, HIGHLIGHT, TOGGLE_FREEZE, INVERT_EDGE
	}

	public interface Callbacks {
		void onSelectedNodeChanged();

	}

	public static final boolean ANTI_ALIAS = true;

	private static final float AIR_FRICTION_COEFF = .8f;

	private static final int BG_COLOR;

	private static final double FRICTION = 0.05; // pixels / frametime ^ 2

	// Circle radius in which no centralizing gravity applied (squared, px)
	private static final float NO_GRAVITY_CIRCLE_2 = 1000;

	private static final String TAG = "NodesView";

	private Callbacks mCallbacks;

	static {
		final float[] hsv = new float[3];
		hsv[0] = Shared.RND.nextFloat() * 360; // hue
		hsv[1] = 0f; // sat
		hsv[2] = .25f; // bright (value)
		BG_COLOR = Color.HSVToColor(hsv);
	}

	private boolean connectionModeOn;

	// Manages spring forces between connected nodes
	private EdgeRelaxer mEdgeRelaxer = new VisibleEdgeRelaxer();

	// Manages gestures and camera movement
	private GestureAndTransformationHandler mGestureAndTransformationHandler = new GestureAndTransformationHandler(
			this);

	private GraphDatabase mGraphDatabase;

	private boolean mGraphFrozen;

	// Helper matrix for drawing
	private Matrix mHelperMatrix = new Matrix();

	private String mLabelInput;

	private Paint mPointerCirclePaint;

	// Manages how nodes pushed apart
	private Repulsor mRepulsor = new FastRepulsor();

	// The current selected Node, null if no selection
	private Node mSelected;

	private int mSelectedEdgeType;

	private boolean mInvertedEdge = false;

	public GraphView(Context context) {
		super(context);
		init(context);
	}

	public GraphView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public GraphView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public int getSelectedEdgeType() {
		return mSelectedEdgeType;
	}

	public void onTap(float absX, float absY) {

		final boolean connecting = connectionModeOn;
		final Node selected = getSelected();
		final GraphDatabase graphDatabase = getGraphDatabase();
		final int edgeType = mSelectedEdgeType;

		// Tapping!! -----------------------
		// Calculate mapped points then select nearest node
		if (connecting) {
			if (selected == null) {

				// If none selected select one now
				// Keep connecting true and wait until next tap

				toggleNearestSelection(absX, absY);
			} else {

				// Find nearest
				Node nearest = graphDatabase.findNearest(absX, absY);

				// no connection to self
				if (selected != nearest) {

					// check for existing connection
					Edge existing = graphDatabase.findEdge(selected, nearest);
					if (existing == null) {

						// reverse check
						existing = graphDatabase.findEdge(nearest, selected);
					}

					if (existing == null) {

						// Now connect the bitches!
						graphDatabase.connect(selected, nearest, edgeType);
					} else {

						// delete existing
						graphDatabase.removeEdge(existing);
					}

					// Finished connecting
					// connecting = false;
					// selected = nearest;
					setSelected(nearest);

				} else {

					// Just an extra self conn review
					// connecting = false;
					// selected = null;
					setSelected(null);
				}
			}
		} else {

			// Connect mode off, just select nearest
			toggleNearestSelection(absX, absY);
		}
	}

	public boolean onTouchEvent(final MotionEvent event) {
		return mGestureAndTransformationHandler.onTouchEvent(event);
	}

	public void postCommand(Command command) {
		switch (command) {
		case CONNECT:
			connectionModeOn = !connectionModeOn;
			break;
		case HIGHLIGHT:
			if (mSelected != null) {
				mSelected.highlighted = !mSelected.highlighted;
			}
			break;
		case CHANGE_EDGE:
			mSelectedEdgeType = (mSelectedEdgeType + 1)
					% EdgePainter.edgeTypes.length;
			break;
		case DELETE_NODE:
			Node sel = mSelected;
			if (sel != null) {
				mGraphDatabase.removeNode(sel);
				setSelected(mGraphDatabase.findNearest(sel.cx, sel.cy));
			}
			break;
		case ADD_NODE:
			commandAddNode(mLabelInput);
			mLabelInput = null;
			break;
		case TOGGLE_FREEZE:
			mGraphFrozen = !mGraphFrozen;
			break;
		case EDIT_NODE:
			if (mSelected != null)
				mSelected.setLabel(mLabelInput);
			break;
		case INVERT_EDGE:
			mInvertedEdge = !mInvertedEdge;
			break;
		}
	}

	public void relase() {
		stopAnimation();
		mGraphDatabase.close();
	}

	public void setGraphDatabase(GraphDatabase gdb) {
		mGraphDatabase = gdb;
		mGraphDatabase.open();
	}

	public void setLabelInput(String text) {
		mLabelInput = text.trim();
	}

	public void setCallbacks(Callbacks callbacks) {
		mCallbacks = callbacks;
	}

	public GraphDatabase getGraphDatabase() {
		return mGraphDatabase;
	}

	public String getSelectedText() {
		if (mSelected == null) {
			return null;
		} else {
			return mSelected.getLabel();
		}
	}

	public boolean isNodeSelected() {
		return mSelected != null;
	}

	public void saveAll() {
		mGraphDatabase.saveAll();
//		mGraphDatabase.saveMatrix(mGestureAndTransformationHandler
//				.getOrigMatrix());

	}

	private void applyFriction() {
		final ArrayList<Node> nodes = mGraphDatabase.getNodes();
		for (Node n : nodes) {
			float abs = FloatMath.sqrt(n.vx * n.vx + n.vy * n.vy);
			if (abs > FRICTION) {
				n.vx -= n.vx / abs * FRICTION;
				n.vy -= n.vy / abs * FRICTION;
				n.vx *= AIR_FRICTION_COEFF;
				n.vy *= AIR_FRICTION_COEFF;

			} else {
				n.vx = 0;
				n.vy = 0;
			}

		}
	}

	private void centralize() {
		final ArrayList<Node> nodes = mGraphDatabase.getNodes();
		float gravity = 1;
		for (Node n : nodes) {

			// normalize & calculate
			float dist2 = FloatMath.sqrt(n.cx * n.cx + n.cy * n.cy);
			if (dist2 > NO_GRAVITY_CIRCLE_2) {
				n.vx -= n.cx / dist2 * gravity;
				n.vy -= n.cy / dist2 * gravity;
			}

		}
	}

	private void commandAddNode(String labelTxt) {
		Node n = new Node();

		if (labelTxt.equals("")) {
			String randomString = Shared.createRandomString();
			n.setLabel(randomString);
		} else {
			n.setLabel(labelTxt);
		}

		mGraphDatabase.addNode(n);

		n.createCache();

		if (mSelected != null) {
			float ang = Shared.RND.nextFloat() * (float) Math.PI * 2;

			n.cx = mSelected.cx + FloatMath.cos(ang)
					* EdgeRelaxer.TARGET_LENGTH * 2;
			n.cy = mSelected.cy + FloatMath.sin(ang)
					* EdgeRelaxer.TARGET_LENGTH * 2;

			if (!mInvertedEdge) {
				mGraphDatabase.connect(mSelected, n, mSelectedEdgeType);
			} else {
				mGraphDatabase.connect(n, mSelected, mSelectedEdgeType);

			}

		} else {

			float[] pts = new float[2];
			getScreenCenterAbsoluteCoords(pts);
			n.cx = pts[0];
			n.cy = pts[1];
		}

	}

	private void drawPointers(Canvas c, GestureData gestureData) {

		Paint p = mPointerCirclePaint;
		switch (gestureData.maxPtrs) {
		case 2:
			c.drawCircle(gestureData.pointsLazy[0], gestureData.pointsLazy[1],
					50, p);
			c.drawCircle(gestureData.pointsLazy[2], gestureData.pointsLazy[3],
					50, p);
			break;
		case 1:
			c.drawCircle(gestureData.pointsLazy[0], gestureData.pointsLazy[1],
					50, p);
		}

	}

	private void getScreenCenterAbsoluteCoords(final float[] outPts) {
		outPts[0] = getWidth() / 2;
		outPts[1] = getHeight() / 2;

		mGestureAndTransformationHandler.convertScreenCoordsToAbsolute(outPts);
	}

	private Node getSelected() {
		return mSelected;
	}

	// ---$---$--- Action Methods ---$---$---

	private void init(Context context) {
		if (isInEditMode())
			return;
		float density = getContext().getResources().getDisplayMetrics().density;
		mGestureAndTransformationHandler.scale(density);
		mPointerCirclePaint = new Paint();
		mPointerCirclePaint.setStyle(Paint.Style.STROKE);
		mPointerCirclePaint.setStrokeWidth(2);
		mPointerCirclePaint.setAntiAlias(GraphView.ANTI_ALIAS);
		mPointerCirclePaint.setColor(0xC0FFFFFF);
		// Node.setGlobalTypeface(Typeface.createFromAsset(context.getAssets(),
		// "diablo.ttf"));

	}

	private void moveNodes() {
		final ArrayList<Node> nodes = mGraphDatabase.getNodes();
		for (Node n : nodes) {
			n.cx += n.vx;
			n.cy += n.vy;
		}
	}

	private void setSelected(Node selected) {
		if (mSelected != selected) {
			// previously selected:
			if (mSelected != null)
				mSelected.createCache();

			// now selected:
			if (selected != null)
				selected.clearCache();

			mSelected = selected;

			if (mCallbacks != null) {
				mCallbacks.onSelectedNodeChanged();
			}
		}

	}

	private void toggleNearestSelection(float x, float y) {

		Node selected = getSelected();
		GraphDatabase graphDatabase = getGraphDatabase();

		// ... and deselect if trying to select the same
		Node nearest = graphDatabase.findNearest(x, y);
		if (nearest != selected) {
			// selected = nearest;
			setSelected(nearest);
		} else {
			// selected = null;
			setSelected(null);
		}

	}

	protected void onDraw(Canvas c) {

		if (isInEditMode()) {
			c.drawColor(0xff303070);
			return;
		}

		c.drawColor(BG_COLOR);

		final ArrayList<Edge> edges = mGraphDatabase.getEdges();
		final ArrayList<Node> nodes = mGraphDatabase.getNodes();

		c.save();

		c.getMatrix(mHelperMatrix);

		mHelperMatrix.preConcat(mGestureAndTransformationHandler
				.getScreenTransformMatrix());
		c.setMatrix(mHelperMatrix);

		// draw the edges
		for (Edge e : edges) {
			e.paint(c);
		}

		Node.staticTick();
		// draw the nodes
		for (Node n : nodes) {
			n.paint(c);
		}

		if (mSelected != null) {
			float radius = (float) (Math.sin(Node.phase) + 1.5) * 5;

			if (connectionModeOn) {
				Node.PAINT_NODE.setShadowLayer(radius, 0, 0, 0xFFBBFF00);

			} else {
				Node.PAINT_NODE.setShadowLayer(radius, 0, 0, Color.WHITE);

			}

			// TODO remove double painting
			mSelected.paint(c);

			// restore
			Node.PAINT_NODE.clearShadowLayer();
		}

		c.restore();

		GestureData gestureData = mGestureAndTransformationHandler
				.getGestureData();
		if (gestureData.pinch || gestureData.drag) {
			drawPointers(c, gestureData);
		}

	}

	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (isInEditMode())
			return;
//		Matrix m = new Matrix();
//		if (mGraphDatabase.loadMatrix(m)) {
//			mGestureAndTransformationHandler.getOrigMatrix().set(m);
//			mGestureAndTransformationHandler.getScreenTransformMatrix().set(m);
//		}

		mGestureAndTransformationHandler.onViewSizeChanged(w, h, oldw, oldh);

		// Load if havent already
		mGraphDatabase.loadGraph();
	}

	protected void tick() {
		if (!mGraphFrozen) {
			mEdgeRelaxer.relaxEdges(mGraphDatabase);
			mRepulsor.pushApart(mGraphDatabase);
			// shakeIt();
			centralize();
			applyFriction();
			moveNodes();
		}
		mGestureAndTransformationHandler.transformationTick();

	}

	public boolean isEdgeInverted() {
		return mInvertedEdge;

	}

}
