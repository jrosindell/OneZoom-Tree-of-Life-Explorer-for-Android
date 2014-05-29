package com.onezoom.midnode;


public class PositionCalculator {
	private static boolean dynamic;
	private static boolean reanchored;
	
	public static boolean isDynamic() {
		return dynamic;
	}


	public static void setDynamic(boolean dynamic) {
		PositionCalculator.dynamic = dynamic;
	}


	public static boolean isReanchored() {
		return reanchored;
	}


	public static void setReanchored(boolean reanchored) {
		PositionCalculator.reanchored = reanchored;
	}
	
	/**
	 * re-calculate as the tree is anchored at root.
	 * @param midnode
	 * @param xp
	 * @param yp
	 * @param ws
	 */
	public void recalculate(MidNode midnode, float xp, float yp, float ws) {
		drawreg2(xp, yp, ws * 220, midnode);
	}
	
	/**
	 * Do actual position calculation.
	 * @param x
	 * @param y
	 * @param r
	 * @param midnode
	 */
	private void drawreg2(float x, float y, float r, MidNode midnode) {
		midnode.positionData.xvar = x;
		midnode.positionData.yvar = y;
		midnode.positionData.rvar = r;		
		boolean tempDvar = midnode.positionData.horizonInsideScreen() && midnode.positionData.nodeBigEnoughToDisplay();
		midnode.positionData.gvar = midnode.positionData.nodeInsideScreen() && midnode.positionData.nodeBigEnoughToDisplay();
		
		if (midnode.child1 != null && tempDvar) {
			drawreg2(x + midnode.positionData.nextx1 * midnode.positionData.rvar, 
					y + midnode.positionData.nexty1 * midnode.positionData.rvar,
					r * midnode.positionData.nextr1, midnode.child1);
		} else if (midnode.child1 != null) {
			midnode.child1.positionData.dvar = false;
		} 
		
		if (midnode.child2 != null && tempDvar) {
			drawreg2(x + midnode.positionData.nextx2 * midnode.positionData.rvar, 
					y + midnode.positionData.nexty2 * midnode.positionData.rvar,
					r * midnode.positionData.nextr2, midnode.child2);
		} else if (midnode.child2 != null) {
			midnode.child2.positionData.dvar = false;
		} 
		
		midnode.positionData.dvar = false;
		if (midnode.child1 != null && midnode.child1.positionData.dvar == true) {
			midnode.positionData.dvar = true;
		} else if (midnode.child2 != null && midnode.child2.positionData.dvar == true) {
			midnode.positionData.dvar = true;
		} else if (midnode.positionData.gvar) {
			midnode.positionData.dvar = true;
		}
	}

	/**
	 * re-calculate from anchored node.
	 * @param xp
	 * @param yp
	 * @param ws
	 * @param midNode
	 */
	public void recalculateDynamic(float xp, float yp, float ws, MidNode midNode) {
		drawregDynamic(xp, yp, ws * 220, midNode);
	}

	
	/**
	 * The same as website code.
	 * 
	 * If a node's child1 graphref is true, then trace down the child1 to find the re-anchored node.
	 * Then use child1 to calculate back the position of this node.
	 * 
	 * The same rule applys to child2.
	 * 
	 * If both children's graphref of a node is false, then this node is re-anchored. So call drawreg2Dynamic
	 * to re-calculate this node.
	 * @param xp
	 * @param yp
	 * @param r
	 * @param midNode
	 */
	private void drawregDynamic(float xp, float yp, float r, MidNode midNode) {
		if (midNode.child1 != null && midNode.child1.positionData.graphref) {
			//re-anchored node is child1 or its descendants.
			drawregDynamic(xp, yp, r, midNode.child1);
			
			//using child1 to back calculating this node.
			midNode.positionData.rvar = midNode.child1.positionData.rvar
					/ midNode.positionData.nextr1;
			midNode.positionData.xvar = midNode.child1.positionData.xvar
					- midNode.positionData.rvar * midNode.positionData.nextx1;
			midNode.positionData.yvar = midNode.child1.positionData.yvar
					- midNode.positionData.rvar * midNode.positionData.nexty1;
			midNode.positionData.dvar = false;

			if (midNode.child2 != null) {
				midNode.child2.positionData.gvar = false;
				midNode.child2.positionData.dvar = false;
			}

			if (midNode.positionData.horizonInsideScreen()) {
				if (
						midNode.getClass() == InteriorNode.class &&
						dynamic &&
						midNode.child2 == null) {
					//dynamically add child2
					midNode.child2 = MidNode.getClient().getInitializer().createTreeStartFromTailNode(2, midNode);
				}
				
				//re-calculate child2
				if (midNode.child2 != null && midNode.positionData.nodeBigEnoughToDisplay()) {
					drawreg2Dynamic(
							midNode.positionData.xvar
									+ ((midNode.positionData.rvar) * (midNode.positionData.nextx2)),
							midNode.positionData.yvar
									+ (midNode.positionData.rvar)
									* (midNode.positionData.nexty2),
							midNode.positionData.rvar
									* midNode.positionData.nextr2,
							midNode.child2);
				}

				if (midNode.positionData.nodeInsideScreen()) {
					midNode.positionData.gvar = true;
					midNode.positionData.dvar = true;
				} else {
					midNode.positionData.gvar = false;
				}

				if (!midNode.positionData.nodeBigEnoughToDisplay()) {
					midNode.child1.positionData.gvar = false;
					midNode.child1.positionData.dvar = false;
					if (midNode.child2 != null) {
						midNode.child2.positionData.gvar = false;
						midNode.child2.positionData.dvar = false;
					}
				}				
			} else {
				// node horizon not inside screen
				midNode.positionData.gvar = false;
			}
			
			
			if (midNode.child1.positionData.dvar
					|| ((midNode.child2 != null) && midNode.child2.positionData.dvar)) {
				midNode.positionData.dvar = true;
			}			
		} else if (midNode.child2 != null && midNode.child2.positionData.graphref) {
			drawregDynamic(xp, yp, r, midNode.child2);
			midNode.positionData.rvar = midNode.child2.positionData.rvar
					/ midNode.positionData.nextr2;
			midNode.positionData.xvar = midNode.child2.positionData.xvar
					- midNode.positionData.rvar * midNode.positionData.nextx2;
			midNode.positionData.yvar = midNode.child2.positionData.yvar
					- midNode.positionData.rvar * midNode.positionData.nexty2;
			midNode.positionData.dvar = false;
			if (midNode.child1 != null) {
				midNode.child1.positionData.gvar = false;
				midNode.child1.positionData.dvar = false;
			}
			if (midNode.positionData.horizonInsideScreen()) {
				if (
						midNode.getClass() == InteriorNode.class &&
						dynamic &&
						midNode.child1 == null) {
					midNode.child1 = MidNode.getClient().getInitializer().createTreeStartFromTailNode(1, midNode);
				}
				
				if (midNode.child1 != null
						&& midNode.positionData.nodeBigEnoughToDisplay()) {
					drawreg2Dynamic(
							midNode.positionData.xvar
									+ ((midNode.positionData.rvar) * (midNode.positionData.nextx1)),
							midNode.positionData.yvar
									+ (midNode.positionData.rvar)
									* (midNode.positionData.nexty1),
							midNode.positionData.rvar
									* midNode.positionData.nextr1,
							midNode.child1);
				}

				if (midNode.positionData.nodeInsideScreen()) {
					midNode.positionData.gvar = true;
					midNode.positionData.dvar = true;
				} else {
					midNode.positionData.gvar = false;
				}

				if (!midNode.positionData.nodeBigEnoughToDisplay()) {
					if (midNode.child1 != null) {
						midNode.child1.positionData.gvar = false;
						midNode.child1.positionData.dvar = false;
					}
					midNode.child2.positionData.gvar = false;
					midNode.child2.positionData.dvar = false;
				}
			} else {
				// node horizon not inside screen
				midNode.positionData.gvar = false;
			}

			if ((midNode.child1 != null && midNode.child1.positionData.dvar)
					|| midNode.child2.positionData.dvar) {
				midNode.positionData.dvar = true;
			}			
		} else {
			//midNode is the re-anchored node. 
			drawreg2Dynamic(xp, yp, r, midNode);
		}
	}
	
	/**
	 * Similar to drawreg2. But if dynamic is set to true, it will create new nodes from file.
	 * @param x
	 * @param y
	 * @param r
	 * @param midnode
	 */
	private void drawreg2Dynamic(float x, float y, float r, MidNode midnode) {
		
		midnode.positionData.xvar = x;
		midnode.positionData.yvar = y;
		midnode.positionData.rvar = r;		
		boolean tempDvar = midnode.positionData.horizonInsideScreen() && midnode.positionData.nodeBigEnoughToDisplay();
		midnode.positionData.gvar = midnode.positionData.nodeInsideScreen() && midnode.positionData.nodeBigEnoughToDisplay();
		
		
		if (midnode.child1 != null && tempDvar) {

			drawreg2Dynamic(x + midnode.positionData.nextx1 * midnode.positionData.rvar, 
					y + midnode.positionData.nexty1 * midnode.positionData.rvar,
					r * midnode.positionData.nextr1, midnode.child1);
		} else if (midnode.child1 != null) {
			midnode.child1.positionData.dvar = false;
		} else if(
				tempDvar && 
				midnode.child1 == null && 
				midnode.getClass() == InteriorNode.class && 
				dynamic) {
				midnode.child1 = MidNode.getClient().getInitializer().createTreeStartFromTailNode(1, midnode);
				drawreg2Dynamic(x + midnode.positionData.nextx1 * midnode.positionData.rvar, 
						y + midnode.positionData.nexty1 * midnode.positionData.rvar,
						r * midnode.positionData.nextr1, midnode.child1);
		}
		
		if (midnode.child2 != null && tempDvar) {
			
			drawreg2Dynamic(x + midnode.positionData.nextx2 * midnode.positionData.rvar, 
					y + midnode.positionData.nexty2 * midnode.positionData.rvar,
					r * midnode.positionData.nextr2, midnode.child2);
		} else if (midnode.child2 != null) {
			midnode.child2.positionData.dvar = false;
		} else if (
				midnode.child2 == null && 
						tempDvar && 
				midnode.getClass() == InteriorNode.class &&
				dynamic) {
				midnode.child2 = MidNode.getClient().getInitializer().createTreeStartFromTailNode(2, midnode);
				drawreg2Dynamic(x + midnode.positionData.nextx2 * midnode.positionData.rvar, 
						y + midnode.positionData.nexty2 * midnode.positionData.rvar,
						r * midnode.positionData.nextr2, midnode.child2);
		}

		midnode.positionData.dvar = false;
		if (midnode.child1 != null && midnode.child1.positionData.dvar == true) {
			midnode.positionData.dvar = true;
		} else if (midnode.child2 != null && midnode.child2.positionData.dvar == true) {
			midnode.positionData.dvar = true;
		} else if (midnode.positionData.gvar) {
			midnode.positionData.dvar = true;
		}
	}


	@SuppressWarnings("unused")
	private void dropInvisibleChunk(MidNode midNode, int depth) {
		//if there are more than 10MB free memory, then return;
//		if (Runtime.getRuntime().freeMemory() > 10 * 1024 * 1024) return;
		//only drop it when find in a depth greater than the threshold.
		//thus if the node's dvar is very close to be true, then choose not to delete it.
		int threshold = 13;
		if (midNode.child1 != null && (depth < threshold || midNode.child1Index > 0)) {
			dropInvisibleChunk(midNode.child1, depth+1);
		} else if (midNode.child1Index < 0 && midNode.child1 != null) {
//			MidNode.initializer.initialisedFile.remove(midNode.child1.fileIndex);
			midNode.child1 = null;
		}
		
		if (midNode.child2 != null && (depth < threshold || midNode.child2Index > 0)) {
			dropInvisibleChunk(midNode.child2, depth+1);
		} else if (midNode.child2Index < 0 && midNode.child2 != null) {
//			MidNode.initializer.initialisedFile.remove(midNode.child2.fileIndex);
			midNode.child2 = null;
		}
	}
	
	/**
	 * Calculate bounding box of a node. 
	 * 
	 * Notice the bounding box does not include the descendants of the node.
	 * The 'H' bounding box which includes the descendants are loaded from file.
	 * @param midNode
	 */
	public void calculateGBoundingBox(MidNode midNode) {
		float max, min;

		max = findMax(
				midNode.positionData.bezsx,
				midNode.positionData.bezex,
				midNode.positionData.bezc1x,
				midNode.positionData.bezc2x);
		
		min = findMin(
				midNode.positionData.bezsx,
				midNode.positionData.bezex,
				midNode.positionData.bezc1x,
				midNode.positionData.bezc2x);
		
		midNode.positionData.gxmax = max + midNode.positionData.bezr / 2;
		midNode.positionData.gxmin = min - midNode.positionData.bezr / 2;
		
		max = findMax(
				midNode.positionData.bezsy,
				midNode.positionData.bezey,
				midNode.positionData.bezc1y,
				midNode.positionData.bezc2y);
		
		min = findMin(
				midNode.positionData.bezsy,
				midNode.positionData.bezey,
				midNode.positionData.bezc1y,
				midNode.positionData.bezc2y);
		
		midNode.positionData.gymax = max + midNode.positionData.bezr / 2;
		midNode.positionData.gymin = min - midNode.positionData.bezr / 2;
		
		// expand the bounding box to include the arc if necessary
		midNode.positionData.gxmax = 
				Math.max(midNode.positionData.gxmax, midNode.positionData.arcx + midNode.positionData.arcr);
		midNode.positionData.gxmin = 
				Math.min(midNode.positionData.gxmin, midNode.positionData.arcx - midNode.positionData.arcr);
		midNode.positionData.gymax =
				Math.max(midNode.positionData.gymax, midNode.positionData.arcy + midNode.positionData.arcr);
		midNode.positionData.gymin =
				Math.min(midNode.positionData.gymin, midNode.positionData.arcy - midNode.positionData.arcr);
		
		if (midNode.getClass() == LeafNode.class) {
			midNode.positionData.hxmax = midNode.positionData.gxmax;
			midNode.positionData.hxmin = midNode.positionData.gxmin;
			midNode.positionData.hymax = midNode.positionData.gymax;
			midNode.positionData.hymin = midNode.positionData.gymin;
		}
	}
	
	private float findMax(float a, float b, float c, float d) {
		return Math.max(Math.max(a, b), Math.max(c, d));
	}
	
	private float findMin(float a, float b, float c, float d) {
		return Math.min(Math.min(a, b), Math.min(c, d));
	}
	
	/**
	 * re-anchor the tree to 'searchedNode'
	 * @param searchedNode
	 */
	public static void reanchorNode(MidNode searchedNode) {
		reanchorNode(searchedNode, 0);
	}
	
	/**
	 * First called from reanchorNode and deanchorOppositeChild is 0, means deanchor both children 
	 * of searchedNode.
	 * 
	 * Then reanchor passed up until parent equals null, which means reanchoring reaches the root.
	 * 
	 * set deanchorOpposite child as childIndex to deanchor another side of the children.
	 * 
	 * For example, if searchedNode's childIndex is 1, then calling reanchorNode (searchedNode.parent, 1)
	 * will call deanchorNode(searchedNode.parent.child2) afterwards.
	 * @param searchedNode
	 * @param deanchorOppositeChild
	 */
	private static  void reanchorNode(MidNode searchedNode, int deanchorOppositeChild) {
		searchedNode.positionData.graphref = true;
		if (searchedNode.child1 != null && searchedNode.child2 != null && deanchorOppositeChild == 0) {
			deanchorNode(searchedNode.child1);
			deanchorNode(searchedNode.child2);
		} else if (searchedNode.child2 != null && deanchorOppositeChild == 1) {
			deanchorNode(searchedNode.child2);
		} else if (searchedNode.child1 != null && deanchorOppositeChild == 2) {
			deanchorNode(searchedNode.child1);
		}		
		if (searchedNode.parent != null) {
			reanchorNode(searchedNode.parent, searchedNode.childIndex);
		}
	}
	
	/**
	 * de-anchor node.
	 * @param midNode
	 */
	private static void deanchorNode(MidNode midNode) {
		midNode.positionData.graphref = false;
	}
	
	/**
	 * This function acts the same as in website version
	 * @param midNode
	 */
	public void reanchor(MidNode midNode) {
		if (midNode.positionData.dvar) {
			midNode.positionData.graphref = true;
			if (
					((midNode.positionData.gvar) || (midNode.child1 == null))
					|| ((midNode.positionData.rvar / 220 > 0.01) && (midNode.positionData.rvar / 220 < 100))
				) {	
				// reanchor here
				reanchored = true;
				MidNode.getClient().treeView.setReanchorJusticeXp(
						MidNode.getClient().treeView.getReanchorJusticeXp() - PositionData.xp + midNode.positionData.xvar);
				MidNode.getClient().treeView.setReanchorJusticeYp(
						MidNode.getClient().treeView.getReanchorJusticeYp() - PositionData.yp + midNode.positionData.yvar);
				MidNode.getClient().treeView.setReanchorJusticeWs(
						MidNode.getClient().treeView.getReanchorJusticeWs() * midNode.positionData.rvar /220 / PositionData.ws);
				PositionData.xp = midNode.positionData.xvar;
				PositionData.yp = midNode.positionData.yvar;
				PositionData.ws = midNode.positionData.rvar / 220;
				
				if (midNode.child1 != null) {
					deanchor(midNode.child2);
					deanchor(midNode.child1);
				}
			} else {
				// reanchor somewhere down the line
				if (midNode.child1.positionData.dvar) {
					deanchor(midNode.child2);
					reanchor(midNode.child1);
				} else {
					deanchor(midNode.child1);
					reanchor(midNode.child2);
				}
			}
		} else {
			midNode.positionData.graphref = true;
			// reanchor here
			reanchored = true;
			PositionData.xp = midNode.positionData.xvar;
			PositionData.yp = midNode.positionData.yvar;
			PositionData.ws = midNode.positionData.rvar / 220;

			if (midNode.child1 != null) {
				deanchor(midNode.child2);
				deanchor(midNode.child1);
			}
		}
		// else not possible to reanchor
	}

	/**
	 * Same as website version.
	 * @param midNode
	 */
	private void deanchor(MidNode midNode) {
		if (midNode.positionData.graphref) {
			if (midNode.child1 != null) {
				deanchor(midNode.child1);
				deanchor(midNode.child2);
			}
			midNode.positionData.graphref = false;
		}
	}
}