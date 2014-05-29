package com.onezoom.midnode;

import com.onezoom.CanvasActivity;

public abstract class MidNode implements Comparable<MidNode>{
	//initializer contains different file information for different species.
	//Therefore, it needs to be destroy and create after quit and start the corresponding activity.
	public static Precalculator precalculator = new Precalculator();
	public static Visualizer visualizer = new Visualizer();
	public static PositionCalculator positionCalculator = new PositionCalculator();
	
	public PositionData positionData = new PositionData();
	public TraitsData traitsCalculator = new TraitsData();	
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
	private static CanvasActivity client;
	
	public MidNode getParent() { return parent; }
	
	public static CanvasActivity getClient() {
		return client;
	}
	
	public static void setClient(CanvasActivity _client) {
		client = _client;
	}
	/**
	 * Loading tree from file to memory starting from *interior0
	 * @return
	 */
	public static MidNode startLoadingTree() {
		client.getInitializer().setDuringInitialization(true);
		return client.getInitializer().createMidNode("0");
	}
	
	/**
	 * re-calculate the tree. 
	 * 
	 * This function recalculate from the root the tree rather than re-anchored node.
	 * 
	 * After re-calculation, re-anchor the tree to a new node. (most possibly the root of the tree
	 * as this function is always called using the root node).
	 */
	public void recalculate() {
		positionCalculator.recalculate(this, PositionData.xp, PositionData.yp, PositionData.ws);
		MidNode.positionCalculator.reanchor(this);
	}
	
	/**
	 * re-calculate the tree
	 * 
	 * This function recalculate the tree from a re-anchored node.
	 * More detail can be seen in positionCalculator.java.
	 * 
	 * Set dynamic false to prevent dynamically loading chunks from file.
	 * Then re-anchor the tree and set dynamic true to 
	 * calculate new position and load new file at the same time.
	 */
	public void recalculateDynamic() {
		PositionCalculator.setDynamic(true);
		recalculateDynamic(PositionData.xp, PositionData.yp, PositionData.ws);

		if (PositionData.ws > 100 || PositionData.ws < 0.01) {
			System.out.println("reanchor called");
			positionCalculator.reanchor(this);
		}
		PositionCalculator.setDynamic(false);
		recalculateDynamic(PositionData.xp, PositionData.yp, PositionData.ws);
	}

	/**
	 * re-calculate the tree. The global variable PositionData.xp, PositionData.yp and PositionData.ws
	 * should be the position of re-anchored node.
	 * @param xp
	 * @param yp
	 * @param ws
	 */
	public void recalculateDynamic(float xp, float yp, float ws) {		
		client.getInitializer().setDuringInitialization(false);
		positionCalculator.recalculateDynamic(xp, yp, ws, this);
	}
	
	/**
	 * Used to decide which file to be loaded first.
	 */
	@Override
	public int compareTo(MidNode another) {
		return this.positionData.compareTo(another.positionData);
	}
}
