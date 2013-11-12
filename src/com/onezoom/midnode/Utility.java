package com.onezoom.midnode;

import com.onezoom.midnode.displayBinary.BinaryTraitsCalculator;

import android.graphics.Color;

public class Utility {
	static int colourtype = 3;
	
	public static int barccolor(MidNode node) { // branch outline colour logic
		// this script sets the color for the outline of the branches
		BinaryTraitsCalculator traits = (BinaryTraitsCalculator) node.traitsCaculator;
		int colortoreturn = Color.argb(80, 50, 37, 25);// 'rgba(50,37,25,0.3)';
		if (colourtype == 2) {
			if ((traits.lengthbr < 70.6) && (BinaryTraitsCalculator.timelim < 70.6)) {
				colortoreturn = Color.argb(80, 200, 200, 200);// 'rgba(200,200,200,0.3)';
			}
		}
		if (colourtype == 3) {
			colortoreturn = Color.argb(80, 200, 200, 200);// 'rgba(0,0,0,0.3)';
		}
		return colortoreturn;
	}
	
	public static int leafcolor1(LeafNode node) {
		BinaryTraitsCalculator traits = (BinaryTraitsCalculator) node.traitsCaculator;
		// for the leaf fill
		if ((traits.redlist != null) && (colourtype == 3)) {
			return (redlistcolor(traits.redlist));
		} else {
			if (colourtype == 3) {
				return (branchcolor(node));
			} else {
				return (Color.argb(255, 0, 100, 0));
			}
		}
	}
	
	
	
	public static int redlistcolor(String codein) {
		if (codein == "EX")
			return Color.argb(255, 0, 0, 180);
		else if (codein == "EW")
			return Color.argb(255, 60, 50, 135);
		else if (codein == "CR")
			return Color.argb(255, 210, 0, 10);
		else if (codein == "EN")
			return Color.argb(255, 125, 50, 0);
		else if (codein == "VU")
			return Color.argb(255, 85, 85, 30);
		else if (codein == "NT")
			return Color.argb(255, 65, 120, 0);
		else if (codein == "LC")
			return Color.argb(255, 0, 180, 20);
		else if (codein == "DD")
			return Color.argb(255, 80, 80, 80);
		else if (codein == "NE")
			return Color.argb(255, 0, 0, 0);
		else
			return Color.argb(255, 0, 0, 0);
	}
	
	
	public static int branchcolor(MidNode node) {// branch colour logic
		BinaryTraitsCalculator traits = (BinaryTraitsCalculator) node.traitsCaculator;
		// this script sets the colours of the branches
		int colortoreturn = Color.argb(255, 100, 75, 50);
		if (colourtype == 2) // there are two different color schemes in this
								// version described by the colourtype variable
		{
			// this.lengthbr is the date of the node
			// timelim is the cut of date beyond which the tree is not drawn
			// (when using growth animation functions
			if ((traits.lengthbr < 150.8) && (BinaryTraitsCalculator.timelim < 150.8)) {
				colortoreturn = Color.argb(255, 180, 50, 25);
			}
			if ((traits.lengthbr < 70.6) && (BinaryTraitsCalculator.timelim < 70.6)) {
				colortoreturn = Color.argb(255, 50, 25, 50);
			}
		} else {

			int conservation = (4 * (traits.num_CR) + 3 * (traits.num_EN) + 2
					* (traits.num_VU) + traits.num_NT);
			int num_surveyed = (traits.num_CR + traits.num_EN + traits.num_VU
					+ traits.num_NT + traits.num_LC);
			if (colourtype == 3) {
				if (num_surveyed == 0) {
					if (((traits.num_NE >= traits.num_DD) && (traits.num_NE >= traits.num_EW))
							&& (traits.num_NE >= traits.num_EX)) {
						colortoreturn = redlistcolor("NE");
					} else {
						if ((traits.num_DD >= traits.num_EX)
								&& (traits.num_DD >= traits.num_EW)) {
							colortoreturn = redlistcolor("DD");
						} else {
							if (traits.num_EW >= traits.num_EX) {
								colortoreturn = redlistcolor("EW");
							} else {
								colortoreturn = redlistcolor("EX");
							}
						}
					}
				} else {
					if ((conservation / num_surveyed) > 3.5) {
						colortoreturn = redlistcolor("CR");
					} else {
						if ((conservation / num_surveyed) > 2.5) {
							colortoreturn = redlistcolor("EN");
						} else {
							if ((conservation / num_surveyed) > 1.5) {
								colortoreturn = redlistcolor("VU");
							} else {
								if ((conservation / num_surveyed) > 0.5) {
									colortoreturn = redlistcolor("NT");
								} else {
									colortoreturn = redlistcolor("LC");
								}
							}
						}
					}
				}
			}
		}
		// the current logic uses different colorschemes for pre, post and
		// during the Cretaceous period, if color type = 2
		// otherwise it uses a fixed brown color for the branches
		// when the tree is growing it only allows branches to be coloured for a
		// certain period if the tree has already growed up to that period.
		return colortoreturn;
	}

	public static String geologicAge(InteriorNode midNode) {
		float lengthbr = midNode.traitsCaculator.getLengthbr();
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

	public static String conservationStatus(LeafNode midNode) {
		if (midNode.traitsCaculator.getRedlist() != null) {
			return "Conservation status: " + conconvert(midNode.traitsCaculator.getRedlist());
		} else {
			return "Conservation status: " + ("Not Evaluated");
		}
	}

	public static String populationStability(LeafNode midNode) {
		String popstab = midNode.traitsCaculator.getPopstab();
		String redlist = midNode.traitsCaculator.getRedlist();
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

}
