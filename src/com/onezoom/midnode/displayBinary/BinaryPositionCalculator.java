package com.onezoom.midnode.displayBinary;

import com.onezoom.midnode.InteriorNode;
import com.onezoom.midnode.LeafNode;
import com.onezoom.midnode.MidNode;

public class BinaryPositionCalculator {

	
	public void recalculate(MidNode midnode, float xp, float yp, float ws) {
		drawreg2(xp, yp, ws * 220, midnode);
	}

	
	public void recalculateDynamic(float xp, float yp, float ws, MidNode midNode) {
		drawreg2Dynamic(xp, yp, ws * 220, midNode);
	}
	
	public void calculateBoundingBox(MidNode midNode) {
		if (midNode.child1 != null) {
			MidNode.positionCalculator.calculateBoundingBox(midNode.child1);
		}
		if (midNode.child2 != null) {
			MidNode.positionCalculator.calculateBoundingBox(midNode.child2);
		}
		calculateSelfBoundingBox(midNode);
	}
	
	public void calculateGBoundingBox(MidNode midNode) {
		float [] maxAndMinX =new float [2];
		float [] maxAndMinY =new float [2];

		maxAndMinX = findMaxAndMin(
				midNode.positionData.bezsx,
				midNode.positionData.bezex,
				midNode.positionData.bezc1x,
				midNode.positionData.bezc2x);
		
		midNode.positionData.gxmax = maxAndMinX[0] + midNode.positionData.bezr / 2;
		midNode.positionData.gxmin = maxAndMinX[1] - midNode.positionData.bezr / 2;
		
		maxAndMinY = findMaxAndMin(
				midNode.positionData.bezsy,
				midNode.positionData.bezey,
				midNode.positionData.bezc1y,
				midNode.positionData.bezc2y);
		
		midNode.positionData.gymax = maxAndMinY[0] + midNode.positionData.bezr / 2;
		midNode.positionData.gymin = maxAndMinY[1] - midNode.positionData.bezr / 2;
		
		// expand the bounding box to include the arc if necessary
		if (midNode.positionData.gxmax < (midNode.positionData.arcx + midNode.positionData.arcr)) {
			midNode.positionData.gxmax = (midNode.positionData.arcx + midNode.positionData.arcr);
		}
		if (midNode.positionData.gxmin > (midNode.positionData.arcx - midNode.positionData.arcr)) {
			midNode.positionData.gxmin = (midNode.positionData.arcx - midNode.positionData.arcr);
		}
		if (midNode.positionData.gymax < (midNode.positionData.arcy + midNode.positionData.arcr)) {
			midNode.positionData.gymax = (midNode.positionData.arcy + midNode.positionData.arcr);
		}
		if (midNode.positionData.gymin > (midNode.positionData.arcy - midNode.positionData.arcr)) {
			midNode.positionData.gymin = (midNode.positionData.arcy - midNode.positionData.arcr);
		}
		
		if (midNode.getClass() == LeafNode.class) {
			midNode.positionData.hxmax = midNode.positionData.gxmax;
			midNode.positionData.hxmin = midNode.positionData.gxmin;
			midNode.positionData.hymax = midNode.positionData.gymax;
			midNode.positionData.hymin = midNode.positionData.gymin;
		}
	}
	
	private void calculateSelfBoundingBox(MidNode midNode) {
		calculateGBoundingBox(midNode);
		
		midNode.positionData.hxmax = midNode.positionData.gxmax;
		midNode.positionData.hxmin = midNode.positionData.gxmin;
		midNode.positionData.hymax = midNode.positionData.gymax;
		midNode.positionData.hymin = midNode.positionData.gymin;
		
		float max, min;
		
		if (midNode.child1 == null || midNode.child2 == null) return;
		
		max = findMax(
				midNode.positionData.hxmax,
				midNode.positionData.nextx1 + midNode.positionData.nextr1 * midNode.child1.positionData.hxmax,
				midNode.positionData.nextx2 + midNode.positionData.nextr2 * midNode.child2.positionData.hxmax);
		midNode.positionData.hxmax = max;
		
		min = findMin(
				midNode.positionData.hxmin,
				midNode.positionData.nextx1 + midNode.positionData.nextr1 * midNode.child1.positionData.hxmin,
				midNode.positionData.nextx2 + midNode.positionData.nextr2 * midNode.child2.positionData.hxmin);
		midNode.positionData.hxmin = min;
		
		max = findMax(
				midNode.positionData.hymax,
				midNode.positionData.nexty1 + midNode.positionData.nextr1 * midNode.child1.positionData.hymax,
				midNode.positionData.nexty2 + midNode.positionData.nextr2 * midNode.child2.positionData.hymax);
		midNode.positionData.hymax = max;
		
		min = findMin(
				midNode.positionData.hymin,
				midNode.positionData.nexty1 + midNode.positionData.nextr1 * midNode.child1.positionData.hymin,
				midNode.positionData.nexty2 + midNode.positionData.nextr2 * midNode.child2.positionData.hymin);
		midNode.positionData.hymin = min;
	}

	private float[] findMaxAndMin(float a, float b, float c,
			float d) {
		float[] result = new float [2];
		result[0] = a;
		result[1] = a;
	
		if (b > result[0]) 
			result[0] = b;
		else if(b < result[1]) 
			result[1] = b;
		
		if (c > result[0]) 
			result[0] = c;
		else if(c < result[1]) 
			result[1] = c;
		
		if (d > result[0]) 
			result[0] = d;
		else if (d < result[1])
			result[1] = d;
	
		return result;
	}

	private float findMin(float a, float b, float c) {
		float min = a;
		if (min > b) min = b;
		if (min > c) min = c;
		return min;
	}

	private float findMax(float a, float b, float c) {
		float max = a;
		if (max < b) max = b;
		if (max < c) max = c;
		return max;
	}

	private void drawreg2(float x, float y, float r, MidNode midnode) {
		midnode.positionData.xvar = x;
		midnode.positionData.yvar = y;
		midnode.positionData.rvar = r;		
		midnode.positionData.dvar = midnode.positionData.horizonInsideScreen() && midnode.positionData.nodeBigEnoughToDisplay();
		midnode.positionData.gvar = midnode.positionData.nodeInsideScreen() && midnode.positionData.nodeBigEnoughToDisplay();
		
		if (midnode.child1 != null && midnode.positionData.dvar) {
			drawreg2(x + midnode.positionData.nextx1 * midnode.positionData.rvar, 
					y + midnode.positionData.nexty1 * midnode.positionData.rvar,
					r * midnode.positionData.nextr1, midnode.child1);
		}
		
		if (midnode.child2 != null && midnode.positionData.dvar) {
			drawreg2(x + midnode.positionData.nextx2 * midnode.positionData.rvar, 
					y + midnode.positionData.nexty2 * midnode.positionData.rvar,
					r * midnode.positionData.nextr2, midnode.child2);
		}
	}
	
	private void drawreg2Dynamic(float x, float y, float r, MidNode midnode) {
		midnode.positionData.xvar = x;
		midnode.positionData.yvar = y;
		midnode.positionData.rvar = r;		
		midnode.positionData.dvar = midnode.positionData.horizonInsideScreen() && midnode.positionData.nodeBigEnoughToDisplay();
		midnode.positionData.gvar = midnode.positionData.nodeInsideScreen() && midnode.positionData.nodeBigEnoughToDisplay();
		
		if (midnode.child1 != null && midnode.positionData.dvar) {
			drawreg2Dynamic(x + midnode.positionData.nextx1 * midnode.positionData.rvar, 
					y + midnode.positionData.nexty1 * midnode.positionData.rvar,
					r * midnode.positionData.nextr1, midnode.child1);
		} else if (midnode.positionData.dvar && midnode.child1 == null && midnode.getClass() == InteriorNode.class) {
			midnode.child1 = MidNode.initializer.createFollowingOneFile(midnode, 1);
			drawreg2Dynamic(x + midnode.positionData.nextx1 * midnode.positionData.rvar, 
					y + midnode.positionData.nexty1 * midnode.positionData.rvar,
					r * midnode.positionData.nextr1, midnode.child1);
		} else if (!midnode.positionData.dvar && midnode.child1 != null) {
			dropInvisibleChunk(midnode.child1, 1);
		}
		
		if (midnode.child2 != null && midnode.positionData.dvar) {
			drawreg2Dynamic(x + midnode.positionData.nextx2 * midnode.positionData.rvar, 
					y + midnode.positionData.nexty2 * midnode.positionData.rvar,
					r * midnode.positionData.nextr2, midnode.child2);
		} else if (midnode.child2 == null && midnode.positionData.dvar && midnode.getClass() == InteriorNode.class) {
			midnode.child2 = MidNode.initializer.createFollowingOneFile(midnode, 2);
			drawreg2Dynamic(x + midnode.positionData.nextx2 * midnode.positionData.rvar, 
					y + midnode.positionData.nexty2 * midnode.positionData.rvar,
					r * midnode.positionData.nextr2, midnode.child2);
		} else if (!midnode.positionData.dvar && midnode.child2 != null) {
			dropInvisibleChunk(midnode.child2, 1);
		}
	}


	private void dropInvisibleChunk(MidNode midNode, int depth) {
		//only drop it when find in a depth greater than the threshold.
		//thus if the node's dvar is very close to be true, then choose not to delete it.
		int threshold = 1;
		if (midNode.child1 != null && (depth < threshold || midNode.child1Index > 0)) {
			dropInvisibleChunk(midNode.child1, depth+1);
		} else if (midNode.child1Index < 0 && midNode.child1 != null) {
			midNode.child1 = null;
		}
		
		if (midNode.child2 != null && (depth < threshold || midNode.child2Index > 0)) {
			dropInvisibleChunk(midNode.child2, depth+1);
		} else if (midNode.child2Index < 0 && midNode.child2 != null) {
			midNode.child2 = null;
		}
	}
}


//private void drawreg2Dynamic2(float x, float y, float r, MidNode midnode) {
//midnode.positionData.xvar = x;
//midnode.positionData.yvar = y;
//midnode.positionData.rvar = r;		
//midnode.positionData.dvar = midnode.positionData.horizonInsideScreen() && midnode.positionData.nodeBigEnoughToDisplay();
//midnode.positionData.gvar = midnode.positionData.nodeInsideScreen() && midnode.positionData.nodeBigEnoughToDisplay();
//
//if (midnode.child1 != null && midnode.positionData.dvar) {
//	drawreg2Dynamic2(x + midnode.positionData.nextx1 * midnode.positionData.rvar, 
//			y + midnode.positionData.nexty1 * midnode.positionData.rvar,
//			r * midnode.positionData.nextr1, midnode.child1);
//} else if (midnode.positionData.dvar && midnode.child1 == null && midnode.getClass() == InteriorNode.class) {
//	MidNode.initializer.createFollowingNodes(midnode, 1);
//} else if (!midnode.positionData.dvar && midnode.child1 != null) {
//	dropInvisibleChunk(midnode.child1, 1);
//}
//
//if (midnode.child2 != null && midnode.positionData.dvar) {
//	drawreg2Dynamic2(x + midnode.positionData.nextx2 * midnode.positionData.rvar, 
//			y + midnode.positionData.nexty2 * midnode.positionData.rvar,
//			r * midnode.positionData.nextr2, midnode.child2);
//} else if (midnode.child2 == null && midnode.positionData.dvar && midnode.getClass() == InteriorNode.class) {
//	MidNode.initializer.createFollowingNodes(midnode, 2);
//} else if (!midnode.positionData.dvar && midnode.child2 != null) {
//	dropInvisibleChunk(midnode.child2, 1);
//}
//}