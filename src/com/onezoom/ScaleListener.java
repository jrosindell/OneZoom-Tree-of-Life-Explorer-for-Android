package com.onezoom;

import android.view.ScaleGestureDetector;

public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
	private TreeView treeView;

	public ScaleListener(TreeView v) {
		super();
		treeView = v;
	}
	

	/**
	 * Scale is a continuous action.
	 * During scale, reanchor may occur and it will change xp, yp and ws,
	 * that's why startXp, startYp should be reset when it occurs.
	 */
	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		try {			
			float dx = (1 - detector.getScaleFactor()) * detector.getFocusX()
					+ detector.getScaleFactor() * treeView.getDistanceTotalX();
			float dy = (1 - detector.getScaleFactor()) * detector.getFocusY()
					+ detector.getScaleFactor() * treeView.getDistanceTotalY();
			treeView.setDistanceTotalX(dx);
			treeView.setDistanceTotalY(dy);
			treeView.setScaleTotalX(treeView.getScaleTotalX() * detector.getScaleFactor());
			treeView.setScaleTotalY(treeView.getScaleTotalY() * detector.getScaleFactor());
			treeView.invalidate();
		} catch (NullPointerException exception) {
			
		}
		return true;
	}
}
