package com.onezoom.midnode;

import android.graphics.Color;

public class Utility {
	static int colourtype = 3;
	
	/**
	 * return branch color
	 * @param node
	 * @return
	 */
	public static int barccolor(MidNode node) { // branch outline colour logic
		// this script sets the color for the outline of the branches
		TraitsData traits = (TraitsData) node.traitsCalculator;
		int colortoreturn = Color.argb(80, 50, 37, 25);// 'rgba(50,37,25,0.3)';
		if (colourtype == 2) {
			if ((traits.getLengthbr() < 70.6) && (TraitsData.timelim < 70.6)) {
				colortoreturn = Color.argb(80, 200, 200, 200);// 'rgba(200,200,200,0.3)';
			}
		}
		if (colourtype == 3) {
			colortoreturn = Color.argb(80, 200, 200, 200);// 'rgba(0,0,0,0.3)';
		}
		return colortoreturn;
	}
	
	
	/**
	 * return geological age 
	 * @param midNode
	 * @return
	 */
	public static String geologicAge(InteriorNode midNode) {
		float lengthbr = midNode.traitsCalculator.getLengthbr();
		return geologicAge(lengthbr);
	}
	
	/**
	 * return geological age
	 * @param lengthbr
	 * @return
	 */
	private static String geologicAge(float lengthbr) {
		if (lengthbr > 253.8)
			return ("pre Triassic Period");
		else if (lengthbr > 203.6)
			return ("Triassic Period");
		else if (lengthbr > 150.8)
			return ("Jurassic Period");
		else if (lengthbr > 70.6)
			return ("Cretaceous Period");
		else if (lengthbr > 28.4)
			return ("Paleogene Period");
		else if (lengthbr > 3.6)
			return ("Neogene Period");
		else
			return ("Quaternary Period");
	}
	
	/**
	 * return string shown in growth animation
	 * @return
	 */
	public static String growthInfo() {
		float lengthbr = TraitsData.timelim;
		if (lengthbr < 0) return "Present day";
		else {
			return String.format("%.2f", lengthbr) + " Million years ago - " + Utility.geologicAge(lengthbr);
		}
	}

	/**
	 * return string shown as conservation status
	 * @param midNode
	 * @return
	 */
	public static String conservationStatus(LeafNode midNode) {
		if (midNode.traitsCalculator.getRedlist() != null) {
			return "Conservation status: " + conconvert(midNode.traitsCalculator.getRedlist());
		} else {
			return "Conservation status: " + ("Not Evaluated");
		}
	}

	/**
	 * return string shown as population stability
	 * @param midNode
	 * @return
	 */
	@SuppressWarnings("unused")
	private static String populationStability(LeafNode midNode) {
		String popstab = midNode.traitsCalculator.getPopstab();
		String redlist = midNode.traitsCalculator.getRedlist();
		if (popstab != null && popstab.equals("D"))
			return "population decreasing";
		else if (popstab != null && popstab.equals("I"))
			return "population increasing";
		else if (popstab != null && popstab.equals("S"))
			return "population stable";
		else if (redlist != null
				&& (redlist.equals("EX") || redlist.equals("EW")))
			return "population extinct";
		else if (redlist == null
				|| (!(redlist.equals("EX") && !redlist.equals("EW"))))
			return "population stability unknown";
		else
			return "population stability unknown";
	}
	
	/**
	 * convert conservation status abbreviation to full name
	 * @param conservation
	 * @return
	 */
	private static  String conconvert(String conservation) {
		if (conservation.equals("EX"))
			return ("Extinct");
		if (conservation.equals("EW"))
			return ("Extinct in the Wild");
		if (conservation.equals("CR"))
			return ("Critically Endangered");
		if (conservation.equals("EN"))
			return ("Endangered");
		if (conservation.equals("VU"))
			return ("Vulnerable");
		if (conservation.equals("NT"))
			return ("Near Threatened");
		if (conservation.equals("LC"))
			return ("Least Concern");
		if (conservation.equals("DD"))
			return ("Data Deficient");
		if (conservation.equals("NE"))
			return ("Not Evaluated");
		else
			return ("Not Evaluated");
	}
	
	/**
	 * combine fileindex and index of node into a new integer to reduce usage of file space.
	 * @param fileIndex
	 * @param index
	 * @return
	 */
	public static int combine(int fileIndex, int index) {
        //most significant 4 bits set as 1
        //5-22 bits set as file index
        //23-32 bits set as parent index
        int result = 0xF0000000;
        assert index < 1024;
        result = result | index;
        assert fileIndex < 0x4FFFF;
        fileIndex = fileIndex << 10;
        result = result | fileIndex;
        return result;
}

}
