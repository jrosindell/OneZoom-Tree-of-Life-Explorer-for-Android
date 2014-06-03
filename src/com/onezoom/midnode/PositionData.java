package com.onezoom.midnode;

import com.onezoom.CanvasActivity;


public class PositionData implements Comparable<PositionData>{
	public float bezsx, bezsy, bezex, bezey, bezc1x, bezc1y, bezc2x, bezc2y, bezr;
	public float xvar, yvar, rvar;
	public float arcAngle, arcx, arcy, arcr, arcx2, arcy2, arcr2;
	public float nextr1, nextr2, nextx1, nextx2, nexty1, nexty2;
	public float hxmax, hymax, hxmin, hymin, gxmax, gymax, gxmin, gymin;
	public boolean dvar, gvar;
	public boolean insideScreen;
	public static final float threshold = 3f;
	public static float xp = 0f;
	public static float yp = 0f;
	public static float ws = 1f;
	public boolean graphref = false;
	public static int screenXmin, screenXmax, screenYmin, screenYmax;
	public boolean signpost;
	
	public static float getXp() {
		return xp;
	}

	public static float getYp() {
		return yp;
	}

	public static float getWs() {
		return ws;
	}
	
	public float getWikiX() {
		return xvar + rvar * arcx - 0.12f * rvar * arcr;
	}
	
	public float getWikiY() {
		float temp_theight = (rvar * Visualizer.leafmult * Visualizer.partc
				- rvar * Visualizer.leafmult * Visualizer.partl2)
				* Visualizer.Tsize / 3.0f;
		return yvar + rvar * arcy - temp_theight * 2.25f;
	}
	
	public float getArkiveX() {
		return xvar + rvar * arcx  + 0.12f * rvar * arcr;
	}

	public float getArkiveY() {
		return getWikiY();
	}
	
	public float getLinkRadius() {
		return 0.105f * rvar * arcr;
	}
	
	public static void setScreenSize(int left, int bottom, int width, int height) {
		screenXmax = left + width;
		screenXmin = left;
		screenYmax = bottom + height;
		screenYmin = bottom;
	}

	public static void shiftScreenPosition(float distanceX, float distanceY, float factor) {
		xp = xp + distanceX;
		yp = yp + distanceY;
		ws = ws * factor;
	}

	public static void setScreenPosition(float xp2, float yp2, float ws2) {
		xp = xp2;
		yp = yp2;
		ws = ws2;
	}
	
	public boolean horizonInsideScreen() {
		if (hxmax * rvar + xvar < screenXmin) return false;
		if (hymax * rvar + yvar < screenYmin) return false;
		if (hxmin * rvar + xvar > screenXmax) return false;
		if (hymin * rvar + yvar > screenYmax) return false;
		return true;
	}

	public boolean nodeInsideScreen() {
		insideScreen = false;
		if (gxmax * rvar + xvar < screenXmin) return false;
		if (gymax * rvar + yvar < screenYmin) return false;
		if (gxmin * rvar + xvar > screenXmax) return false;
		if (gymin * rvar + yvar > screenYmax) return false;
		insideScreen = true;
		return true;
	}
	
	public boolean nodeBigEnoughToDisplay() {
		if (rvar < threshold) return false;

		return true;
	}
	
	/**
	 * Compare the closeness of two node to the center of the screen.
	 * 
	 * It is used to decide which chunk of file should be initialized first.
	 * 
	 * The node which is closer to the center of the screen will be initialized earlier.
	 * 
	 * If this node is more likely to be initialized, return -1;
	 */
	@Override
	public int compareTo(PositionData another) {
		float distanceToCenter = this.getDistanceToCenter(this);
		float thatDistanceToCenter = this.getDistanceToCenter(another);
		if (distanceToCenter > thatDistanceToCenter) return 1;
		else if (distanceToCenter < thatDistanceToCenter) return -1;
		else return 0;
	}
	
	private float getDistanceToCenter(PositionData pd) {
		float centerX = pd.xvar + (pd.hxmax + pd.hxmin) * pd.rvar / 2;
		float centerY = pd.yvar + (pd.hymax + pd.hymin) * pd.rvar / 2;
		float distanceToCenter =  Math.abs(centerX * 2 - screenXmax - screenXmin) 
				+ Math.abs(centerY * 2 - screenYmax - screenYmin);
		if (pd.rvar < 220)
			distanceToCenter = distanceToCenter / pd.rvar;
		else 
			distanceToCenter = distanceToCenter * pd.rvar / 220 / 220;
		return distanceToCenter;
	}

	/**
	 * Before this function being called, t
	 * he searched node should have already be moved into the center of the screen.
	 * But only the start position of its branch is in the center.
	 * 
	 * This function moves the center of the leaf of interior circle of the node into screen center.
	 * @param searchedNode
	 */
	public static void moveNodeToCenter(MidNode searchedNode) {
		PositionData.shiftScreenPosition(
				-220 * searchedNode.positionData.arcx * CanvasActivity.getScaleFactor(), 
				-220 * searchedNode.positionData.arcy * CanvasActivity.getScaleFactor(),
				1f);	
	}
	
	public String toString() {
		float distanceToCenter = this.getDistanceToCenter(this);
		return Float.toString(distanceToCenter);
	}
}
