package com.onezoom.midnode;

public interface TraitsCaculator{

	void initLeafNode(String data);
	void initInteriorNode(String data);
	void setColor(MidNode midNode);
	void concalc(MidNode midNode);
	int getColor();
}
