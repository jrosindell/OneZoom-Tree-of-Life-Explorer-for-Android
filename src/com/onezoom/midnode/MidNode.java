package com.onezoom.midnode;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.util.Log;

import com.onezoom.midnode.displayBinary.BinaryFactory;

public abstract class MidNode {
	private static Factory factory = new BinaryFactory();
	protected static Precalculator precalculator = factory.createPrecalculator();
	protected static Visualizer visualizer = factory.createVisualizer();
	protected static PositionCalculator positionCalculator = factory.createPositionCalculator();
	
	//stores position information like bezier, xvar, etc...
	public PositionData positionData = factory.createPositionData(); 
	//stores metadata and methods for retrieving them
	protected TraitsCaculator traitsCaculator = factory.createTraitsCaculator();

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
		PositionData.setScreenSize();
	}
	
	public void drawElement(Canvas canvas) {
		visualizer.drawElement(canvas, this);
	}
	
	public void recalculate(float xp, float yp, float ws) {
		positionCalculator.recalculate(this, xp, yp, ws);
	}
	
	public void moveScreenPosition(int xp, int yp, float ws) {
		PositionData.setScreenPosition(xp, yp, ws);
	}
	
	public String toString() {
		String returnString = traitsCaculator.toString();
		if (this.child1 != null) returnString += this.child1.toString();
		if (this.child2 != null) returnString += this.child2.toString();
		return returnString;
	}
	
	public MidNode getParent() { return parent; }
}
