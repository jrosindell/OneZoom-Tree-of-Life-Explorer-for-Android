package com.onezoom.midnode;

public class InteriorNode extends MidNode {
	
	/**
	 * Initialize interior node from file.
	 * @param line
	 */
	public InteriorNode(String[] line) {	
		this.index = Integer.parseInt(line[0]);
		this.parentIndex = Integer.parseInt(line[1]);
		this.child1Index = Integer.parseInt(line[2]);
		this.child2Index = Integer.parseInt(line[3]);
		this.traitsCalculator.setName1(line[4]);
		this.traitsCalculator.setName2(line[5]);
		this.traitsCalculator.setCname(line[6]);
		this.traitsCalculator.setRichness(Integer.parseInt(line[7]));
		this.traitsCalculator.setLengthbr(Float.parseFloat(line[8]));
		this.positionData.hxmax = Float.parseFloat(line[9]);
		this.positionData.hxmin = Float.parseFloat(line[10]);
		this.positionData.hymax = Float.parseFloat(line[11]);
		this.positionData.hymin = Float.parseFloat(line[12]);
		this.traitsCalculator.setColor(Integer.parseInt(line[13]));
	}
}
