package com.onezoom.midnode;

import android.util.Log;

import com.onezoom.midnode.displayBinary.BinaryInitializer;

import junit.framework.Assert;


public class InteriorNode extends MidNode {

	public InteriorNode(String[] line) {
		Assert.assertEquals(line.length, 14);
	
		this.index = Integer.parseInt(line[0]);
		this.parentIndex = Integer.parseInt(line[1]);
		this.child1Index = Integer.parseInt(line[2]);
		this.child2Index = Integer.parseInt(line[3]);
		this.traitsCaculator.setName1(line[4]);
		this.traitsCaculator.setName2(line[5]);
		this.traitsCaculator.setCname(line[6]);
		this.traitsCaculator.setRichness(Integer.parseInt(line[7]));
		this.traitsCaculator.setLengthbr(Float.parseFloat(line[8]));
		this.positionData.hxmax = Float.parseFloat(line[9]);
		this.positionData.hxmin = Float.parseFloat(line[10]);
		this.positionData.hymax = Float.parseFloat(line[11]);
		this.positionData.hymin = Float.parseFloat(line[12]);
		this.traitsCaculator.setColor(Integer.parseInt(line[13]));
//		this.traitsCaculator.setColor(BinaryInitializer.getColor());
	}
	
	public InteriorNode(MidNode pNode, String data, boolean buildOneNode, int childIndex) {
		this.childIndex = childIndex;
		
		if (buildOneNode)
			buildOneInteriorNode(pNode, data);
		else 
			buildFullSubtree(pNode, data, childIndex);
	}

	private void buildFullSubtree(MidNode pNode, String data, int childIndex) {
		String[] childrenStrings = buildOneInteriorNode(pNode, data);
		String child1String = childrenStrings[0];
		String child2String = childrenStrings[1];
		
		this.child1 = MidNode.createNode(this, child1String, false, 1);
		this.child2 = MidNode.createNode(this, child2String, false, 2);
	}

	private String[] buildOneInteriorNode(MidNode pNode, String data) {
		parent = pNode;
		data = removeTailComma(data);
		
		String cutname, child1String, child2String;
		String[] cutnames;
		
		cutnames = cutDataToThreeParts(data);
		cutname = cutnames[2];
		
		traitsCaculator.initInteriorNode(cutname);
		
		//make most of child1's richness greater than child2
		if (cutnames[0].length() >= cutnames[1].length()) {
			child1String = cutnames[0];
			child2String = cutnames[1];
		} else {
			child1String = cutnames[1];
			child2String = cutnames[0];
		}
		
		String[] childrenStrings = new String[2];
		childrenStrings[0] = child1String;
		childrenStrings[1] = child2String;
		
		precalculator.preCalcOneInteriorNode(this);
		
		return childrenStrings;
	}

	private String removeTailComma(String data) {
		if (data.charAt(data.length() - 1) == ';') {
			data = data.substring(0, data.length() - 1);
		}
		return data;
	}

	private String[] cutDataToThreeParts(String data) {
		int bracketscount = 0, cut = 0, end = 0;
		for (int i = 0; i < data.length(); i++) {
			if (data.charAt(i) == '(') {
				bracketscount++;
			}
			if (data.charAt(i) == ')') {
				bracketscount--;
			}
			if (data.charAt(i) == ',') {
				if (bracketscount == 1) {
					cut = i;
				}
			}
			if (bracketscount == 0) {
				end = i;
				i = data.length() + 1;
			}
		} // for loop: i loops from 0 to. Find the comma inside the outer
			// brackets

		String cut1 = new String(data.substring(1, cut));
		String cut2 = new String(data.substring(cut + 1, end));
		String cutname = new String(data.substring(end + 1, data.length()));
		return new String[] { cut1, cut2, cutname };
	}
}
