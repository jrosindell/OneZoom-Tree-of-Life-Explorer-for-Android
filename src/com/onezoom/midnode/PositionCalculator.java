package com.onezoom.midnode;

public interface PositionCalculator {
	public void recalculate(MidNode midNode, float xp, float yp, float ws);

	public void calculateBoundingBox(MidNode midNode);
}
