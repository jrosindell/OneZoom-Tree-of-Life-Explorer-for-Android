package com.onezoom.midnode;

public interface TraitsCaculator{

	void initLeafNode(String data);
	void initInteriorNode(String data);
	void setColor(MidNode midNode);
	void concalc(MidNode midNode);
	int getColor();
	float getLengthbr();
	int getRichness();
	String getCname();
	String getName1();
	String getName2();
	String getRedlist();
	String getPopstab();
}
