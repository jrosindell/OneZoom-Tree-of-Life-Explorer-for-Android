package com.onezoom;

import com.onezoom.midnode.PositionCalculator;
import com.onezoom.midnode.PositionData;

import android.view.ScaleGestureDetector;

public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
	private TreeView treeView;
	private float startXp, startYp, startscaleFactor, lastXp, lastYp;
	private boolean previousScale;
	
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
		float spanDiff = Math.abs(detector.getCurrentSpanX() - detector.getPreviousSpanX())
				+ Math.abs(detector.getCurrentSpanY() - detector.getPreviousSpanY());
		float focusDiff = Math.abs(lastXp - detector.getFocusX()) + Math.abs(lastYp - detector.getFocusY());
		
		if (thisOnDrag(spanDiff, focusDiff)) {
			this.previousScale = false;
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
		treeView.setLastActionAsScale(true);
		this.previousScale = true;
		
		if (PositionCalculator.isReanchored()) {
			startXp = PositionData.getXp();
			startYp = PositionData.getYp();
			startscaleFactor = PositionData.getWs();
			PositionCalculator.setReanchored(false);
		}
						
		float shiftXp, shiftYp;
		float scaleFactor = detector.getScaleFactor();
		float scaleTotal = scaleFactor * PositionData.getWs() / startscaleFactor;
		float currentXp = detector.getFocusX();
		float currentYp = detector.getFocusY();
		shiftXp = currentXp + (startXp - currentXp) * scaleTotal - PositionData.getXp();
		shiftYp = currentYp + (startYp - currentYp) * scaleTotal - PositionData.getYp();
		
		treeView.setScaleX(treeView.getScaleX() * scaleFactor);
		treeView.setScaleY(treeView.getScaleY() * scaleFactor);	
		treeView.setScaleCenterX(currentXp);
		treeView.setScaleCenterY(currentYp);
		
		treeView.zoomin(scaleFactor, shiftXp, shiftYp);
		lastXp = detector.getFocusX();
		lastYp = detector.getFocusY();
		
		treeView.invalidate();
		return true;
	}

	private boolean thisOnScale(float spanDiff, float focusDiff) {
		if (spanDiff < 10f)
			return false;
		else if (spanDiff < 1.5f * focusDiff)
			return false;
		else if (spanDiff < 4f * focusDiff && !this.previousScale)
			return false;
		else
			return true;
	}

	private boolean thisOnDrag(float spanDiff, float focusDiff) {
		if (focusDiff > 4 * spanDiff && focusDiff > 2f)
			return true;
		else if (!previousScale && focusDiff > 1.5f * spanDiff && focusDiff > 2f) {
			return true;
		} else
			return false;
	}

	/**
	 * record start finger position when scale starts.
	 */
	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		startXp = PositionData.getXp();
		startYp = PositionData.getYp();
		startscaleFactor = PositionData.getWs();
		lastXp = detector.getFocusX();
		lastYp = detector.getFocusY();
		previousScale = true;
		return true;
	}
}
