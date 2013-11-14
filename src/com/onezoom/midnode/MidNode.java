package com.onezoom.midnode;


import org.apache.http.conn.ClientConnectionManager;

import android.graphics.Canvas;
import android.util.Log;
import com.onezoom.CanvasActivity;
import com.onezoom.midnode.displayBinary.BinaryFactory;

public abstract class MidNode{
	public static int countDrawElement = 0;
	private static Factory factory = new BinaryFactory();
	public static Initializer initializer = factory.createInitializer();
	public static Precalculator precalculator = factory.createPrecalculator();
	protected static Visualizer visualizer = factory.createVisualizer();
	public static PositionCalculator positionCalculator = factory.createPositionCalculator();
	
	//stores position information like bezier, xvar, etc...
	public PositionData positionData = factory.createPositionData(); 
	//stores metadata and methods for retrieving them
	public TraitsCaculator traitsCaculator = factory.createTraitsCaculator();
	

	public MidNode child1;
	public MidNode child2;
	//childIndex tells which children this node belongs to
	public int childIndex;
	public int child1Index;
	public int child2Index;
	public int parentIndex;
	public int index;
	public MidNode parent = null;
	public static CanvasActivity client;
	public static String selected;
	
	public static MidNode createNode (MidNode pNode, String data, boolean buildOneNode, int childIndex) {
		assert pNode != null;
		assert (childIndex == 0) || (childIndex == 1) || (childIndex == 2);
		
		if (data.charAt(0) == '(')
			return new InteriorNode(pNode, data, buildOneNode, childIndex);
		else
			return new LeafNode(pNode, data, childIndex);
	}
	
	public static MidNode createNode(CanvasActivity canvasActivity,
			String selectedGroup, String fileIndex) {
		client = canvasActivity;
		selected = selectedGroup;
		return initializer.createMidNode(canvasActivity, selectedGroup, fileIndex);
	}

	public static void setScreenSize(int left, int bottom, int width, int height) {
		PositionData.setScreenSize(left, bottom, width, height);
	}
	
	public void preCalculateWholeTree() {
		this.precalculator.preCalcWholeTree(this);
	}
	
	public void drawElement(Canvas canvas) {
		countDrawElement = 0;
		visualizer.drawTree(canvas, this);
		Log.d("debug", "zzzzzzzzzzzzzz" + countDrawElement);
	}
	
	public void recalculate(float xp, float yp, float ws) {
		PositionData.setScreenPosition(xp, yp, ws);
		positionCalculator.recalculate(this, xp, yp, ws);
	}
	
	public void recalculate() {
		positionCalculator.recalculate(this, positionData.xp, positionData.yp, positionData.ws);
	}
	
	public void init() {
		traitsCaculator.concalc(this);
		traitsCaculator.setColor(this);
		positionCalculator.calculateBoundingBox(this);
	}
	
	public String toString() {
		String returnString = positionData.toString();
		if (this.child1 != null) returnString += this.child1.toString();
		if (this.child2 != null) returnString += this.child2.toString();
		return returnString;
	}
	
	public void outputInitElement() {
		Log.d("debug", "total element: " + Integer.toString(elementAmount()));
	}
	
	public int elementAmount() {
		int re = 1;
		if (this.child1 != null)
			re += this.child1.elementAmount();
		if (this.child2 != null)
			re += this.child2.elementAmount();
		return re;
	}
	
	public MidNode getParent() { return parent; }
}
