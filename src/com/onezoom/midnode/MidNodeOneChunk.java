package com.onezoom.midnode;

import java.util.LinkedList;

import com.onezoom.CanvasActivity;
import com.onezoom.midnode.displayBinary.BinaryInitializer;

public class MidNodeOneChunk {
	MidNode chunkRoot;
	MidNodeOneChunk parentChunk;
	LinkedList<MidNodeOneChunk> childrenChunk = new LinkedList<MidNodeOneChunk>();
	LinkedList<MidNode> nonInitChunksParent = new LinkedList<MidNode>();
	static Initializer initializer = new BinaryInitializer();
	
	
	public MidNodeOneChunk() {

	}

	public MidNodeOneChunk(MidNode rootNode) {
		chunkRoot = rootNode;
		parentChunk = null;
	}

	public MidNode getroot() {
		return chunkRoot;
	}

	public static MidNodeOneChunk createTree(CanvasActivity canvasActivity,String fileNumber) {
		return initializer.createTreeChunk(fileNumber);
	}

	public void addChildren(MidNodeOneChunk tempChunk) {
		childrenChunk.add(tempChunk);
		tempChunk.parentChunk = this;
	}

	public void preCalculateWholeTree() {
		chunkRoot.preCalculateWholeTree();
	}

	public void recalculate(float xp, float yp, float ws) {
		chunkRoot.recalculateDynamic(xp, yp, ws);
	}

	public void init() {
		chunkRoot.init();
	}
	
	public void recalculate() {
		recalculate(PositionData.xp, PositionData.yp, PositionData.ws);
	}
//	public void recalculate() {
////		chunkRoot.recalculate();
//		recalculateOneChunk();
//		
//		for (int i = 0; i < nonInitChunksParent.size(); i++) {
//			if (nonInitChunksParent.get(i).positionData.dvar)
//				createOneChunk(nonInitChunksParent.get(i));
//		}
//		
//		for (int i = 0; i < childrenChunk.size(); i++) {
//			if (childrenChunk.get(i).shouldBeDrawn())
//				childrenChunk.get(i).recalculate();
//		}
//	}
//
//	private void createOneChunk(MidNode midNode) {
//		
//	}
//
//	private boolean shouldBeDrawn() {
//		return getroot().parent.positionData.dvar;
//	}
//
//	private void recalculateOneChunk() {
//		
//	}
}
