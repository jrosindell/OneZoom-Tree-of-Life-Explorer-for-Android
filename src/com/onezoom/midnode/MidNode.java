package com.onezoom.midnode;

import android.graphics.Canvas;
import android.util.Log;

import com.onezoom.midnode.displayBinary.BinaryFactory;

public abstract class MidNode{
	public static int countDrawElement = 0;
	private static Factory factory = new BinaryFactory();
	protected static Precalculator precalculator = factory.createPrecalculator();
	protected static Visualizer visualizer = factory.createVisualizer();
	public static PositionCalculator positionCalculator = factory.createPositionCalculator();
	
	//stores position information like bezier, xvar, etc...
	public PositionData positionData = factory.createPositionData(); 
	//stores metadata and methods for retrieving them
	public TraitsCaculator traitsCaculator = factory.createTraitsCaculator();

	public MidNode child1;
	public MidNode child2;
	public int childIndex;
	protected MidNode parent = null;
	
	public static MidNode createNode (MidNode pNode, String data, boolean buildOneNode, int childIndex) {
		assert pNode != null;
		assert (childIndex == 0) || (childIndex == 1) || (childIndex == 2);
		
		if (data.charAt(0) == '(')
			return new InteriorNode(pNode, data, buildOneNode, childIndex);
		else
			return new LeafNode(pNode, data, childIndex);
	}
	
	public static void setScreenSize(int left, int bottom, int width, int height) {
		PositionData.setScreenSize(left, bottom, width, height);
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
	
	public MidNode getParent() { return parent; }
}
