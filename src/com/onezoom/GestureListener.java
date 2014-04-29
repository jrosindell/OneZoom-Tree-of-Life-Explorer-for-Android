package com.onezoom;

import com.onezoom.midnode.PositionData;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

public class GestureListener extends GestureDetector.SimpleOnGestureListener{
	private TreeView treeView;
	private static final float FACTOR = 1.4f;
	public GestureListener(TreeView v) {
		super();
		treeView = v;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		float currentXp = e.getX();
		float currentYp = e.getY();
		
		float shiftXp = currentXp + (PositionData.getXp() - currentXp) * FACTOR - PositionData.getXp();
		float shiftYp = currentYp + (PositionData.getYp() - currentYp) * FACTOR - PositionData.getYp();
		
		treeView.setScaleX(treeView.getScaleX() * FACTOR);
		treeView.setScaleY(treeView.getScaleY() * FACTOR);	
		treeView.setScaleCenterX(currentXp);
		treeView.setScaleCenterY(currentYp);
		
		treeView.zoomin(FACTOR, shiftXp, shiftYp);
		return true;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		treeView.setDistanceX(treeView.getDistanceX() - distanceX);
		treeView.setDistanceY(treeView.getDistanceY() - distanceY);
		treeView.drag(-distanceX, -distanceY);
		return true;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		treeView.hideKeyboard();
		return super.onSingleTapConfirmed(e);
	}

}
