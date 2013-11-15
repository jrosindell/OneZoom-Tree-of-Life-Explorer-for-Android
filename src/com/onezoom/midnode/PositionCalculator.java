package com.onezoom.midnode;

public interface PositionCalculator {
	public void recalculate(MidNode midNode, float xp, float yp, float ws);

	public void calculateBoundingBox(MidNode midNode);

	public void recalculateDynamic(float xp, float yp, float ws, MidNode midNode);
}
