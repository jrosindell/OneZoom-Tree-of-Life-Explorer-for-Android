package com.onezoom.midnode;

import android.graphics.Canvas;
import android.util.Log;

import com.onezoom.CanvasActivity;
import com.onezoom.midnode.displayBinary.BinaryInitializer;
import com.onezoom.midnode.displayBinary.BinaryPositionCalculator;
import com.onezoom.midnode.displayBinary.BinaryPrecalculator;
import com.onezoom.midnode.displayBinary.BinaryTraitsCalculator;
import com.onezoom.midnode.displayBinary.BinaryVisualizer;

public abstract class MidNode implements Comparable<MidNode>{
	public static BinaryInitializer initializer = new BinaryInitializer();
	public static BinaryPrecalculator precalculator = new BinaryPrecalculator();
	protected static BinaryVisualizer visualizer = new BinaryVisualizer();
	public static BinaryPositionCalculator positionCalculator = new BinaryPositionCalculator();
	
	public PositionData positionData = new PositionData();
	public BinaryTraitsCalculator traitsCalculator = new BinaryTraitsCalculator();	
	public MidNode child1;
	public MidNode child2;
	//childIndex tells which children this node belongs to
	public int childIndex;
	public int child1Index;
	public int child2Index;
	public int parentIndex;
	public int fileIndex;
	public int index;
	public MidNode parent = null;
	
	public static MidNode createNode(String fileIndex) {
		initializer.setDynamic(false);
		return initializer.createMidNode(fileIndex);
	}

	public static void setScreenSize(int left, int bottom, int width, int height) {
		PositionData.setScreenSize(left, bottom, width, height);
	}
	
	public static void setContext(CanvasActivity context) {
		BinaryInitializer.setContext(context);
	}
	
	public void preCalculateWholeTree() {
		MidNode.precalculator.preCalcWholeTree(this);
	}
	
	public void drawElement(Canvas canvas) {
		visualizer.drawTree(canvas, this);
	}
	
	public void recalculate(float xp, float yp, float ws) {
		positionCalculator.recalculate(this, xp, yp, ws);
	}
	
	public void recalculate() {
		positionCalculator.recalculate(this, PositionData.xp, PositionData.yp, PositionData.ws);
		MidNode.positionCalculator.reanchor(this);
	}
	
	public MidNode getParent() { return parent; }

	public void recalculateDynamic() {
		BinaryPositionCalculator.setDynamic(false);
		recalculateDynamic(PositionData.xp, PositionData.yp, PositionData.ws);
		if ((PositionData.ws > 100 || PositionData.ws < 0.01)
				&& !BinaryInitializer.canvasActivity.getTreeView().isDuringInteraction()) {
			positionCalculator.reanchor(this);
		}
		BinaryPositionCalculator.setDynamic(true);
		recalculateDynamic(PositionData.xp, PositionData.yp, PositionData.ws);
	}

	public void recalculateDynamic(float xp, float yp, float ws) {		
		initializer.setDynamic(true);
		positionCalculator.recalculateDynamic(xp, yp, ws, this);
	}
	
	@Override
	public int compareTo(MidNode another) {
		return this.positionData.compareTo(another.positionData);
	}


	public boolean testLink(float mouseX, float mouseY) {
		return LinkHandler.testLink(this, mouseX, mouseY);
	}

	

	public String wikilink() {
		return LinkHandler.wikiLink();
	}
}
