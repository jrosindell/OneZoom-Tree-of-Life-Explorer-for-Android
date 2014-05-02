package com.onezoom;

import com.onezoom.midnode.PositionData;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class TreeViewGestureListener extends GestureDetector.SimpleOnGestureListener{
	private TreeView treeView;
	private static final float FACTOR = 1.4f;
	public TreeViewGestureListener(TreeView v) {
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
		if (treeView.client.hasHitWikiLink(e.getX(), e.getY())) {
			treeView.client.hideTreeView();
			treeView.client.loadWikiURL();
			treeView.client.displayWebView();	
		}
		
		return super.onSingleTapConfirmed(e);
	}

}