package com.onezoom.midnode;

public class LinkHandler {
	private static String wikiLink;
	
	LinkHandler() {
		
	}
	
	public static boolean testLink(MidNode node, float mouseX, float mouseY) {
		if (node.positionData.dvar) {
			if (node.child1 == null && node.child2 == null) {
				if( testLinkClick(node, mouseX, mouseY)) return true;
			} else {
				if (testLinkClick(node, mouseX, mouseY)) return true;
				else {
					return forwardTesting(node, mouseX, mouseY);
				}
			}
		}
		return false;
	}
	
	private static boolean forwardTesting(MidNode node, float mouseX, float mouseY) {
		if (node.child1 != null) {
			if (testLink(node.child1, mouseX, mouseY)) return true;			
		}
		
		if (node.child2 != null) {
			if (testLink(node.child2, mouseX, mouseY)) return true;			
		}		
		return false;
	}

	private static boolean testLinkClick(MidNode node, float mouseX, float mouseY) {
		float cx = node.positionData.getWikiCenterX();
		
		float cy = node.positionData.getWikiCenterY();
		
		float radius =node.positionData.getWikiRadius();

		if (mouseX > cx - radius && mouseX < cx + radius
				&& mouseY > cy - radius && mouseY < cy + radius) {
			wikiLink = node.traitsCalculator.name2.toLowerCase() + "_" 
				+ node.traitsCalculator.name1.toLowerCase();
			return true;
		} else {
			return false;
		}
	}
	
	public static String wikiLink() {
		return wikiLink;
	}
}
