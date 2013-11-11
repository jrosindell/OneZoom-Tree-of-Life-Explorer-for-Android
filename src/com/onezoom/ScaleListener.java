package com.onezoom;

import com.onezoom.midnode.PositionData;

import android.util.Log;
import android.view.ScaleGestureDetector;

public class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
	private TreeView treeView;
	private float startXp, startYp, startscaleFactor;
	
	public ScaleListener(TreeView v) {
		super();
		treeView = v;
	}

	@Override
	public boolean onScale(ScaleGestureDetector detector) {
		TreeView.onScale = true;
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
		return true;
	}

	@Override
	public boolean onScaleBegin(ScaleGestureDetector detector) {
		startXp = PositionData.getXp();
		startYp = PositionData.getYp();
		startscaleFactor = PositionData.getWs();
		return true;
	}
	
	@Override
	public void onScaleEnd(ScaleGestureDetector detector) {
		// TODO Auto-generated method stub
		super.onScaleEnd(detector);
	}
}
