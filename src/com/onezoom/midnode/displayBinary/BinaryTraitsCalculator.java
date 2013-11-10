package com.onezoom.midnode.displayBinary;

import com.onezoom.midnode.TraitsCaculator;

public class BinaryTraitsCalculator implements TraitsCaculator{
	private String name1, name2, cname;
	private float lengthbr;
	private String popstab, redlist;
	private int num_CR, num_D, num_DD, num_EN, num_EW, num_EX, num_I, num_NE, num_S, num_U, num_VU, num_NT, num_LC;
	private int richness_val;
	
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
	
	public String toString() {
		if (name1 == null) return "null";
		else return name1;
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

}
