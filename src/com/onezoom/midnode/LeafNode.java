package com.onezoom.midnode;

public class LeafNode extends MidNode {

	public LeafNode(MidNode pNode, String data) {
		parent = pNode;
		traitsCaculator.initLeafNode(data);
	}

}
