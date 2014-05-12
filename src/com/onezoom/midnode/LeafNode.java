package com.onezoom.midnode;



public class LeafNode extends MidNode {

	/**
	 * Initialize leafnode from file.
	 * @param line
	 */
	public LeafNode(String[] line) {		
		this.index = Integer.parseInt(line[0]);
		this.parentIndex = Integer.parseInt(line[1]);
		this.child1Index = -1;
		this.child2Index = -1;
		this.traitsCalculator.setName1(line[2]);
		this.traitsCalculator.setName2(line[3]);
		this.traitsCalculator.setCname(line[4]);
		this.traitsCalculator.setRichness(1);
		this.traitsCalculator.setLengthbr(0f);
		this.traitsCalculator.setRedlist(line[5]);
		this.traitsCalculator.setPopstab(line[6]);
		this.traitsCalculator.setColor(Integer.parseInt(line[7]));
	}
}
