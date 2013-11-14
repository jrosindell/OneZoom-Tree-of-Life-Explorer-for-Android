package com.onezoom.midnode;

public class LeafNode extends MidNode {

	public LeafNode(String[] line) {
			assert line.length == 7;
			this.index = Integer.parseInt(line[0]);
			this.parentIndex = Integer.parseInt(line[1]);
			this.child1Index = -1;
			this.child2Index = -1;
			this.traitsCaculator.setName1(line[2]);
			this.traitsCaculator.setName2(line[3]);
			this.traitsCaculator.setCname(line[4]);
			this.traitsCaculator.setRichness(1);
			this.traitsCaculator.setLengthbr(0f);
			this.traitsCaculator.setRedlist(line[5]);
			this.traitsCaculator.setPopstab(line[6]);
	}
	
	public LeafNode(MidNode pNode, String data, int childIndex) {
		this.childIndex = childIndex;
		parent = pNode;
		traitsCaculator.initLeafNode(data);
		precalculator.preCalcOneLeafNode(this);
	}

}
