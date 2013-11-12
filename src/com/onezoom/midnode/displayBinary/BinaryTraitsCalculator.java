package com.onezoom.midnode.displayBinary;

import android.util.Log;

import com.onezoom.midnode.MidNode;
import com.onezoom.midnode.TraitsCaculator;
import com.onezoom.midnode.Utility;

public class BinaryTraitsCalculator implements TraitsCaculator{
	public static float timelim = -1;
	public float lengthbr;
	public String name1, name2, cname;
	public String popstab, redlist;
	public int num_CR, num_D, num_DD, num_EN, num_EW, num_EX, num_I, num_NE, num_S, num_U, num_VU, num_NT, num_LC;
	public int richness_val;
	public int myColor;
	
	@Override
	public void initLeafNode(String data) {
		// TODO Auto-generated method stub
		setLengthAndRichness(data);
		if (data.length() > 0) {
			setLeafNames(data);
		} else {
			this.name1 = null;
			this.name2 = null;
		}
	}
	
	@Override
	public void initInteriorNode(String data) {
		String cutname = setLengthAndRichness(data);
		
		if (cutname.length() > 0 && !isFloat(cutname)) {
			setNames(cutname);
		} else {
			this.name1 = null;
			this.name2 = null;
			this.cname = null;
		}
	}
	
	@Override
	public void setColor(MidNode midNode) {
		setMyColor(midNode);
		if (midNode.child1 != null) {
			midNode.child1.traitsCaculator.setColor(midNode.child1);
		}
		if (midNode.child2 != null) {
			midNode.child2.traitsCaculator.setColor(midNode.child2);
		}
	}
	
	@Override
	public int getColor() {
		return myColor;
	}
	
	public void concalc(MidNode node) {
		num_EX = 0;
		num_EW = 0;
		num_CR = 0;
		num_EN = 0;
		num_VU = 0;
		num_NT = 0;
		num_LC = 0;
		num_DD = 0;
		num_NE = 0;

		num_I = 0;
		num_D = 0;
		num_S = 0;
		num_U = 0;

		if ((node.child1 != null) && (node.child2 != null)) {
			node.child1.traitsCaculator.concalc(node.child1);
			node.child2.traitsCaculator.concalc(node.child2);
			BinaryTraitsCalculator trait1 = (BinaryTraitsCalculator) node.child1.traitsCaculator;
			BinaryTraitsCalculator trait2 = (BinaryTraitsCalculator) node.child2.traitsCaculator;
			num_EX = (trait1.num_EX) + (trait2.num_EX);
			num_EW = (trait1.num_EW) + (trait2.num_EW);
			num_CR = (trait1.num_CR) + (trait2.num_CR);
			num_EN = (trait1.num_EN) + (trait2.num_EN);
			num_VU = (trait1.num_VU) + (trait2.num_VU);
			num_NT = (trait1.num_NT) + (trait2.num_NT);
			num_LC = (trait1.num_LC) + (trait2.num_LC);
			num_DD = (trait1.num_DD) + (trait2.num_DD);
			num_NE = (trait1.num_NE) + (trait2.num_NE);

			num_I = (trait1.num_I) + (trait2.num_I);
			num_D = (trait1.num_D) + (trait2.num_D);
			num_S = (trait1.num_S) + (trait2.num_S);
			num_U = (trait1.num_U) + (trait2.num_U);

		} else {
			if (redlist != null) {
				if (redlist.equals("EX")) {
					num_EX = 1;

				} else if (redlist.equals("EW")) {
					num_EW = 1;

				} else if (redlist.equals("CR")) {
					num_CR = 1;

				} else if (redlist.equals("EN")) {
					num_EN = 1;

				} else if (redlist.equals("VU")) {
					num_VU = 1;

				} else if (redlist.equals("NT")) {
					num_NT = 1;

				} else if (redlist.equals("LC")) {
					num_LC = 1;

				} else if (redlist.equals("DD")) {
					num_DD = 1;

				} else if (redlist.equals("NE")) {
					num_NE = 1;

				} else {
					num_NE = 1;
				}
			} else {
				num_NE = 1;
			}

			if (popstab != null) {
				// Log.d("debug", popstab);
				if (popstab.equals("I")) {
					num_I = 1;

				} else if (popstab.equals("S")) {
					num_S = 1;

				} else if (popstab.equals("D")) {
					num_D = 1;

				} else if (popstab.equals("U")) {
					num_U = 1;

				} else {
					num_U = 1;

				}
			} else {
				num_U = 1;
			}
		}
	}

	
	
	private void setMyColor(MidNode midNode) {
		myColor = Utility.branchcolor(midNode);
	}

	public String toString() {
		return Integer.toString(num_CR+ num_D+ num_DD+ num_EN+ num_EW+ num_EX+ 
				num_I+ num_NE+ num_S+ num_U+ num_VU+ num_NT+ num_LC);
	}

	private void setNames(String cutname) {
		int lengthcut = cutname.indexOf('{');
		if (lengthcut != -1) {
			setNameInDetail(cutname, lengthcut);
		} else {
			this.cname = null;
			this.name1 = cutname;
			this.name2 = null;
		}		
	}

	private void setNameInDetail(String cutname, int lengthcut) {
		this.cname = cutname.substring(lengthcut + 1, cutname.length() - 1);

		if (lengthcut != 1) {
			this.name1 = cutname.substring(0, lengthcut);
		} else {
			this.name1 = null;
		}
		// now we need to split [] out of cname and replace * with ,
		lengthcut = cname.indexOf('[');
		if (lengthcut == -1) {
			this.name2 = null;
		} else {
			this.name2 = (this.cname).substring(lengthcut + 1,
					(this.cname).length() - 1);
			this.cname = (this.cname).substring(0, lengthcut);
		}
		this.cname = this.cname.replace('*', ',');		
	}

	private boolean isFloat(String cutname) {
		try {
			Float.parseFloat(cutname);
		} catch (NumberFormatException e) {
			return false;
		}
		// only got here if we didn't return false
		return true;
	}

	private void setLeafNames(String data) {
		int lengthcut = data.indexOf('{');
		if (lengthcut != -1) {
			this.cname = data.substring(lengthcut + 1, data.length() - 1);
			data = data.substring(0, lengthcut);
			lengthcut = cname.indexOf('_');

			if (lengthcut == -1) {
				this.popstab = "U";
				this.redlist = "NE";
			} else {
				redlist = cname.substring(lengthcut + 1, lengthcut + 3);
				popstab = cname.substring(lengthcut + 4, lengthcut + 5);
				this.cname = this.cname.substring(0, lengthcut);
			}
		}
		lengthcut = data.indexOf('_');

		if (lengthcut == -1) {
			this.name1 = data;
			this.name2 = null;
		} else {
			this.name1 = data.substring(lengthcut + 1, data.length());
			this.name2 = data.substring(0, lengthcut);
		}
	}

	private String setLengthAndRichness(String data) {
		int lengthcut1 = data.lastIndexOf('[');
		int lengthcut2 = data.lastIndexOf(']');
		String temp = data.substring(lengthcut1 + 1, lengthcut2);
		int cut = temp.indexOf('_');
		this.richness_val = Integer.parseInt(temp.substring(0, cut));
		this.lengthbr = Float.parseFloat(temp.substring(cut + 1));
		return data.substring(0, lengthcut1);
	}

	@Override
	public float getLengthbr() {
		return lengthbr;
	}

	@Override
	public int getRichness() {
		return richness_val;
	}

	@Override
	public String getCname() {
		return cname;
	}

	@Override
	public String getName1() {
		return name1;
	}

	@Override
	public String getName2() {
		return name2;
	}

	@Override
	public String getRedlist() {
		return redlist;
	}

	@Override
	public String getPopstab() {
		return popstab;
	}


}
