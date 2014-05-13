package com.onezoom.midnode;


public class TraitsData{
	public static float timelim = -1;
	private float lengthbr;
	private String name1, name2, cname;
	private String[] signName;
	private String popstab, redlist;
	private int richness_val;
	private int color;


	public float getLengthbr() {
		return lengthbr;
	}

	
	public int getRichness() {
		return richness_val;
	}

	
	public String getCname() {
		return cname;
	}

	
	public String getName1() {
		return name1;
	}

	
	public String getName2() {
		return name2;
	}

	
	public String getRedlist() {
		return redlist;
	}

	
	public String getPopstab() {
		return popstab;
	}

	
	public void setCname(String cname) {
		this.cname = cname;
	}

	
	public void setName1(String name1) {
		this.name1 = name1;
	}

	
	public void setName2(String name2) {
		this.name2 = name2;
	}

	
	public void setRedlist(String redlist) {
		this.redlist = redlist;
	}

	
	public void setPopstab(String popstab) {
		this.popstab = popstab;
	}

	
	public void setLengthbr(float lengthbr) {
		this.lengthbr = lengthbr;
	}

	
	public void setRichness(int richness) {
		this.richness_val = richness;
	}
	
	public void setColor(int color) {
		this.color = color;
	}
	
	public int getColor() {
		return color;
	}


	public String[] getSignName() {
		return signName;
	}


	public void setSignName(String[] signName) {
		this.signName = signName;
	}
	
	public String getLatinName() {
		String name;
		if (!getName1().equals("null") && !getName2().equals("null"))
			name = getName2() + " " + getName1();
		else if (!getName1().equals("null") && !getName2().equals("null"))
			name = getName2();
		else if (!getName1().equals("null") && !getName2().equals("null"))
			name = getName1();
		else
			name = "no name";
		return name;
	}
}
