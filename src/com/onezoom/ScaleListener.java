package com.onezoom;

import android.view.ScaleGestureDetector;

public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
	private TreeView treeView;
	private float lastXp, lastYp;

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
			float spanDiff = Math.abs(detector.getCurrentSpanX() - detector.getPreviousSpanX())
					+ Math.abs(detector.getCurrentSpanY() - detector.getPreviousSpanY());
			float focusDiff = Math.abs(lastXp - detector.getFocusX()) + Math.abs(lastYp - detector.getFocusY());
			
			if (thisOnDrag(spanDiff, focusDiff)) {
				lastXp = detector.getFocusX();
				lastYp = detector.getFocusY();
				return true;
			} else if (!thisOnScale(spanDiff, focusDiff)) {
				treeView.testDragAfterScale = false;
				lastXp = detector.getFocusX();
				lastYp = detector.getFocusY();
				return true;
			}
			
			
			treeView.testDragAfterScale = false;
							
			
			if (treeView.isLastActionAsScale()) {
				treeView.setScaleX(treeView.getScaleX() * detector.getScaleFactor());
				treeView.setScaleY(treeView.getScaleY() * detector.getScaleFactor());
			} else {
				treeView.setFirstAction(false);
				treeView.setLastActionAsScale(true);
				treeView.createNewMotion();
				treeView.setScaleX(detector.getScaleFactor());
				treeView.setScaleY(detector.getScaleFactor());
			}
			
			treeView.setScaleCenterX(detector.getFocusX());
			treeView.setScaleCenterY(detector.getFocusY());
			
			lastXp = detector.getFocusX();
			lastYp = detector.getFocusY();
			
			treeView.invalidate();
		} catch (NullPointerException exception) {
			
		}
		return true;
	}

	private boolean thisOnScale(float spanDiff, float focusDiff) {
		if (spanDiff < 4f)
			return false;
		else if (spanDiff < 1.5f * focusDiff)
			return false;
		else if (spanDiff < 4f * focusDiff && !treeView.isLastActionAsScale())
			return false;
		else
			return true;
	}

	private boolean thisOnDrag(float spanDiff, float focusDiff) {
		if (focusDiff > 4 * spanDiff && focusDiff > 2f)
			return true;
		else if (!treeView.isLastActionAsScale() && focusDiff > 1.5f * spanDiff && focusDiff > 2f) {
			return true;
		} else
			return false;
	}

	/**
	 * record start finger position when scale starts.
	 */
	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		lastXp = detector.getFocusX();
		lastYp = detector.getFocusY();
		return true;
	}
}
