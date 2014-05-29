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
		try {
			if (Math.abs(distanceY) + Math.abs(distanceX) < 5) {
				//disturbance ignore.
				return true;
			}
			
			if (treeView.isFirstAction() || treeView.isLastActionAsScale()) {
				treeView.setFirstAction(false);
				treeView.setLastActionAsScale(false);
			}
			
			treeView.setDistanceTotalX(treeView.getDistanceTotalX() - distanceX);
			treeView.setDistanceTotalY(treeView.getDistanceTotalY() - distanceY);
			treeView.invalidate();
			return true;
		} catch (NullPointerException exception) {
			return true;
		}
	}

	/**
	 * Test if clicks on wiki link. If it do, then load wiki link.
	 */
	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		try {
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
				float dx = (1 - TreeView.FACTOR) * e.getX()
						+ TreeView.FACTOR * treeView.getDistanceTotalX();
				float dy = (1 - TreeView.FACTOR) * e.getY()
						+ TreeView.FACTOR * treeView.getDistanceTotalY();
				treeView.setDistanceTotalX(dx);
				treeView.setDistanceTotalY(dy);
				treeView.setScaleTotalX(treeView.getScaleTotalX() * TreeView.FACTOR);
				treeView.setScaleTotalY(treeView.getScaleTotalY() * TreeView.FACTOR);
				treeView.setLastActionAsScale(true);
			}
			
			treeView.invalidate();
		} catch (NullPointerException exception) {
			
		}
		return true;
	}
	
	/**
	 * Double tap also zoom in.
	 */
	@Override
	public boolean onDoubleTap(MotionEvent e) {
		try {
			float dx = (1 - TreeView.FACTOR) * e.getX()
					+ TreeView.FACTOR * treeView.getDistanceTotalX();
			float dy = (1 - TreeView.FACTOR) * e.getY()
					+ TreeView.FACTOR * treeView.getDistanceTotalY();
			treeView.setDistanceTotalX(dx);
			treeView.setDistanceTotalY(dy);
			treeView.setScaleTotalX(treeView.getScaleTotalX() * TreeView.FACTOR);
			treeView.setScaleTotalY(treeView.getScaleTotalY() * TreeView.FACTOR);
			treeView.setLastActionAsScale(true);
			treeView.invalidate();
		} catch (NullPointerException exception) {
			
		}
		return true;
	}

}
