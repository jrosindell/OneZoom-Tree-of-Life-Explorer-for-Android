package com.onezoom.midnode;

import java.util.Locale;

public class LinkHandler {
	private static String currentLink = "";
	private static MidNode linkNode;
	
	public static String getLink() {
		return currentLink;
	}
	
	public static MidNode getLinkNode() {
		return linkNode;
	}
	
	/**
	 * Test if finger is on links
	 * @param node
	 * @param fingerX
	 * @param fingerY
	 * @return
	 */
	public static boolean testLink(MidNode node, float fingerX, float fingerY) {
		try {
			if (node.positionData.dvar) {
				if (node.child1 == null && node.child2 == null) {
					/**
					 * Leaf node.
					 * If finger is on the link of the node, return true;
					 */
					if( testLinkClick(node, fingerX, fingerY)) return true;
				} else {
					/**
					 * Interior node. Test its forward children. Test should not be executed on interior node.
					 */
					return forwardTesting(node, fingerX, fingerY);
				}
			}
			return false;
		} catch (NullPointerException e) {
			return false;
		}
	}
	
	/**
	 * Test if the links of node's children and their descendants has been hit.
	 * @param node
	 * @param mouseX
	 * @param mouseY
	 * @return
	 */
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
	 * Test if finger is on the links of a particular node.
	 * @param node
	 * @param fingerX
	 * @param fingerY
	 * @return
	 */
	private static boolean testLinkClick(MidNode node, float fingerX, float fingerY) {
		float wikiX = node.positionData.getWikiX();
		float wikiY = node.positionData.getWikiY();
		
		float eolX = node.positionData.getEOLX();
		float eolY = node.positionData.getEOLY();
		
		float radius =node.positionData.getLinkRadius();

		if (fingerX > wikiX - radius && fingerX < wikiX + radius
				&& fingerY > wikiY - radius && fingerY < wikiY + radius
				&& node.positionData.rvar > Visualizer.getThresholddrawtextdetailleaf()) {
			/**
			 * User hit wiki link. Set wiki link url and record which node has been hit.
			 * 
			 * Only when leaf is big enough to see the link can user hit it.
			 */
			setWikiLink(node);
			return true;
		} else if (fingerX > eolX - radius && fingerX < eolX + radius
				&& fingerY > eolY - radius && fingerY < eolY + radius
				&& node.positionData.rvar > Visualizer.getThresholddrawtextdetailleaf()) {
			/**
			 * User hit eol link. Set eol link url and record which node has been hit.
			 * 
			 * Only when leaf is big enough to see the link can user hit it.
			 */
			setEOLLink(node);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Record the eol link and link node.
	 * @param node
	 */
	private static void setEOLLink(MidNode node) {	
		String linknumber = EOLMap.getInstance().map.get(node.traitsCalculator.getName2().toLowerCase(Locale.ENGLISH) + " "
				+ node.traitsCalculator.getName1().toLowerCase(Locale.ENGLISH));
		currentLink = "http://eol.org/pages/" + linknumber + "/overview";
		linkNode = node;
	}

	/**
	 * Record the wiki link and link node.
	 * @param node
	 */
	private static void setWikiLink(MidNode node) {
		currentLink = "http://en.wikipedia.org/wiki/" 
				+ node.traitsCalculator.getName2().toLowerCase(Locale.ENGLISH) + "_" 
				+ node.traitsCalculator.getName1().toLowerCase(Locale.ENGLISH);
		linkNode = node;
	}

	/**
	 * Record link and link node.
	 * 
	 * This method is called in Search.java after the node being searched is found.
	 * 
	 * Set the links according to current url
	 * so that if users press back or next button, they can be linked to the correct page.
	 * @param searchedNode
	 */
	public static void setLink(MidNode searchedNode) {
		if (currentLink.contains("wikipedia")) {
			setWikiLink(searchedNode);
		} else if (currentLink.contains("eol")) {
			setEOLLink(searchedNode);
		} else {
			setWikiLink(searchedNode); //set as default. it'll be used by search to match key word.
		}
	}
}
