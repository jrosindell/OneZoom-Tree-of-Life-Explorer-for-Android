package com.onezoom;

import com.onezoom.midnode.PositionData;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class TreeViewGestureListener extends GestureDetector.SimpleOnGestureListener{
	private TreeView treeView;
	public TreeViewGestureListener(TreeView v) {
		super();
		treeView = v;
	}

	/**
	 * Drag the tree
	 */
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		if (Math.abs(distanceY) + Math.abs(distanceX) < 5) {
			//disturbance ignore.
			return true;
		}
		
		if (treeView.isFirstAction() || treeView.isLastActionAsScale()) {
			treeView.createNewMotion();
			treeView.setFirstAction(false);
			treeView.setLastActionAsScale(false);
		}
		
		treeView.setDistanceX(treeView.getDistanceX() - distanceX);
		treeView.setDistanceY(treeView.getDistanceY() - distanceY);
		treeView.invalidate();
		return true;
	}

	/**
	 * Test if clicks on wiki link. If it do, then load wiki link.
	 */
	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		treeView.client.hideKeyBoard(treeView);
		if (treeView.client.hasHitLink(e.getX(), e.getY())) {
			/**
			 * search will be reset if the node being hit is not the node being searched.
			 */
			treeView.client.resetSearch();
			
			/**
			 * search view text field will also be reset if the link is not on the node being searched.
			 */
			treeView.client.setQueryOfCurrentSearchView();
			
			treeView.client.hideTreeView();
			treeView.client.loadLinkURL();
			treeView.client.displayWebView();	
		} else {
			treeView.createNewMotion();
			treeView.setScaleX(treeView.getScaleX() * TreeView.FACTOR);
			treeView.setScaleY(treeView.getScaleY() * TreeView.FACTOR);	
			treeView.setScaleCenterX(e.getX());
			treeView.setScaleCenterY(e.getY());
			treeView.setLastActionAsScale(true);
		}
		
		treeView.invalidate();
		return true;
	}
	
	/**
	 * Double tap also zoom in.
	 */
	@Override
	public boolean onDoubleTap(MotionEvent e) {
		float currentXp = e.getX();
		float currentYp = e.getY();
		
		float shiftXp = currentXp + (PositionData.getXp() - currentXp) * TreeView.FACTOR - PositionData.getXp();
		float shiftYp = currentYp + (PositionData.getYp() - currentYp) * TreeView.FACTOR - PositionData.getYp();
		
		treeView.setLastActionAsScale(true);

		treeView.setScaleX(treeView.getScaleX() * TreeView.FACTOR);
		treeView.setScaleY(treeView.getScaleY() * TreeView.FACTOR);	
		treeView.setScaleCenterX(currentXp);
		treeView.setScaleCenterY(currentYp);
		
		treeView.zoomin(TreeView.FACTOR, shiftXp, shiftYp);
		
		treeView.invalidate();
		return true;
	}

}
