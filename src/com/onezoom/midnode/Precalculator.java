package com.onezoom.midnode;

public interface Precalculator {
	void preCalcWholeTree(MidNode tree);
	
	void preCalcWholeTree(MidNode tree, Float angle);
	
	void preCalcOneInteriorNode(InteriorNode interiorNode);

	void preCalcOneLeafNode(LeafNode leafNode);

}
