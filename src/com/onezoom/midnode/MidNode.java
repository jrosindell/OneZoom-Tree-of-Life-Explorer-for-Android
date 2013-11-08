package com.onezoom.midnode;

import android.graphics.Canvas;

import com.onezoom.midnode.displayBinary.BinaryFactory;

public abstract class MidNode {
	private static Factory factory = new BinaryFactory();
	protected static Precalculator precalculator = factory.createPrecalculator();
	protected static Visualizer visualizer = factory.createVisualizer();
	protected static PositionCalculator positionCalculator = factory.createPositionCalculator();
	
	//stores position information like bezier, xvar, etc...
	protected PositionData positionData = factory.createPositionData(); 
	//stores metadata and methods for retrieving them
	protected TraitsCaculator traitsCaculator = factory.createTraitsCaculator();

	protected MidNode[] children = new MidNode[2];
	protected MidNode parent = null;
	
	public static MidNode createNode (MidNode pNode, String data, boolean buildOneNode) {
		assert pNode != null;
		if (data.charAt(0) == '(')
			return new InteriorNode(pNode, data, buildOneNode);
		else
			return new LeafNode(pNode, data);
	}
	
	public static void setScreenSize(int left, int bottom, int width, int height) {
		PositionData.setScreenSize();
	}
	
	public void drawElement(Canvas canvas) {
		visualizer.drawElement(canvas, this);
	}
	
	public void recalculate() {
		positionCalculator.recalculate();
	}
	
	public void moveScreenPosition(int xp, int yp, float ws) {
		PositionData.setScreenPosition(xp, yp, ws);
	}
}
