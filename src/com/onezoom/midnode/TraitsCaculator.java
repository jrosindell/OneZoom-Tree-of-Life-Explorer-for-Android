package com.onezoom.midnode;

public interface TraitsCaculator{

	void initLeafNode(String data);
	void initInteriorNode(String data);
	void setColor(MidNode midNode);
	void concalc(MidNode midNode);
	int getColor();
	float getLengthbr();
	int getRichness();
	void setLengthbr(float lengthbr);
	void setRichness(int richness);
	String getCname();
	String getName1();
	String getName2();
	String getRedlist();
	String getPopstab();
	void setCname(String cname);
	void setName1(String name1);
	void setName2(String name2);
	void setRedlist(String redlist);
	void setPopstab(String popstab);
}
