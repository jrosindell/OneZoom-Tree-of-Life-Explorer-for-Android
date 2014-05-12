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

	/**
	 * Zoom in the tree.
	 */
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

	/**
	 * Drag the tree
	 */
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		treeView.setDistanceX(treeView.getDistanceX() - distanceX);
		treeView.setDistanceY(treeView.getDistanceY() - distanceY);
		treeView.drag(-distanceX, -distanceY);
		return true;
	}

	/**
	 * Test if clicks on wiki link. If it do, then load wiki link.
	 */
	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		treeView.client.hideKeyBoard(treeView);
		if (treeView.client.hasHitWikiLink(e.getX(), e.getY())) {
			treeView.client.resetSearch();
			treeView.client.hideTreeView();
			treeView.client.loadWikiURL();
			treeView.client.displayWebView();	
		}
		
		return super.onSingleTapConfirmed(e);
	}

}
