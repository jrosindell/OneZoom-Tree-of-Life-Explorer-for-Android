package com.onezoom.midnode;


import junit.framework.Assert;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import com.onezoom.CanvasActivity;
import com.onezoom.midnode.displayBinary.BinaryFactory;
import com.onezoom.midnode.displayBinary.BinaryInitializer;

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
	
	public static MidNode createNode (MidNode pNode, String data, boolean buildOneNode, int childIndex) {
//		assert pNode != null;
		Assert.assertNotNull(pNode);
//		assert (childIndex == 0) || (childIndex == 1) || (childIndex == 2);
		Assert.assertEquals((childIndex == 0) || (childIndex == 1) || (childIndex == 2), true);
		if (data.charAt(0) == '(')
			return new InteriorNode(pNode, data, buildOneNode, childIndex);
		else
			return new LeafNode(pNode, data, childIndex);
	}
	
	public static MidNode createNode(String fileIndex) {
		return initializer.createMidNode(fileIndex);
	}

	public static void setScreenSize(int left, int bottom, int width, int height) {
		PositionData.setScreenSize(left, bottom, width, height);
	}
	
	public static void setContext(Context context) {
		BinaryInitializer.setContext(context);
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

	public void recalculateDynamic() {
		recalculateDynamic(PositionData.xp, PositionData.yp, PositionData.ws);
	}
	
	public void recalculateDynamic(float xp, float yp, float ws) {
		positionCalculator.recalculateDynamic(xp, yp, ws, this);
		this.init();
	}
}
