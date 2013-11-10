package com.onezoom.midnode.displayBinary;

import com.onezoom.midnode.MidNode;
import com.onezoom.midnode.PositionCalculator;

public class BinaryPositionCalculator implements PositionCalculator {

	@Override
	public void recalculate(MidNode midnode, float xp, float yp, float ws) {
		drawreg2(xp, yp, ws * 220, midnode);
	}

	private void drawreg2(float x, float y, float r, MidNode midnode) {
		midnode.positionData.xvar = x;
		midnode.positionData.yvar = y;
		midnode.positionData.rvar = r;
		if (midnode.child1 != null) {
			drawreg2(x + midnode.positionData.nextx1 * midnode.positionData.rvar, 
					y + midnode.positionData.nexty1 * midnode.positionData.rvar,
					r * midnode.positionData.nextr1, midnode.child1);
		}
		
		if (midnode.child2 != null) {
			drawreg2(x + midnode.positionData.nextx2 * midnode.positionData.rvar, 
					y + midnode.positionData.nexty2 * midnode.positionData.rvar,
					r * midnode.positionData.nextr2, midnode.child2);
		}
	}
}
