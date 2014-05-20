package com.onezoom;

import com.onezoom.midnode.PositionCalculator;
import com.onezoom.midnode.PositionData;

import android.view.ScaleGestureDetector;

public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
	private TreeView treeView;
	private float startXp, startYp, startscaleFactor;
	
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
		treeView.onScale = true;
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
		
		System.out.println("scale xp -> " + currentXp);
		System.out.println("scale yp -> " + currentYp);
		treeView.zoomin(scaleFactor, shiftXp, shiftYp);
		return true;
	}

	/**
	 * record start finger position when scale starts.
	 */
	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		startXp = PositionData.getXp();
		startYp = PositionData.getYp();
		startscaleFactor = PositionData.getWs();
		return true;
	}
}
