package com.onezoom.midnode;

public class LinkHandler {
	private static String wikiLink;
	private static MidNode wikiNode;
	
	
	public static String getWikiLink() {
		return wikiLink;
	}
	
	public static MidNode getWikiNode() {
		return wikiNode;
	}
	
	/**
	 * Test if finger is on wiki link
	 * @param node
	 * @param fingerX
	 * @param fingerY
	 * @return
	 */
	public static boolean testLink(MidNode node, float fingerX, float fingerY) {
		if (node.positionData.dvar) {
			if (node.child1 == null && node.child2 == null) {
				if( testLinkClick(node, fingerX, fingerY)) return true;
			} else {
				if (testLinkClick(node, fingerX, fingerY)) return true;
				else {
					return forwardTesting(node, fingerX, fingerY);
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

	/**
	 * Test if finger is on the wiki circle of a particular node.
	 * @param node
	 * @param fingerX
	 * @param fingerY
	 * @return
	 */
	private static boolean testLinkClick(MidNode node, float fingerX, float fingerY) {
		float cx = node.positionData.getWikiCenterX();
		
		float cy = node.positionData.getWikiCenterY();
		
		float radius =node.positionData.getWikiRadius();

		if (fingerX > cx - radius && fingerX < cx + radius
				&& fingerY > cy - radius && fingerY < cy + radius) {
			setWikiLink(node);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Record the wiki link and link node.
	 * @param node
	 */
	public static void setWikiLink(MidNode node) {
		wikiLink = node.traitsCalculator.getName2().toLowerCase() + "_" 
				+ node.traitsCalculator.getName1().toLowerCase();
		wikiNode = node;
	}
}
