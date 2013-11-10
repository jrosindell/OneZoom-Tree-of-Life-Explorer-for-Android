package com.onezoom.midnode;

public class LeafNode extends MidNode {

	public LeafNode(MidNode pNode, String data, int childIndex) {
		this.childIndex = childIndex;
		parent = pNode;
		traitsCaculator.initLeafNode(data);
		precalculator.preCalcOneLeafNode(this);
	}

}
