package com.onezoom;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class IntroductionViewGestureListener extends GestureDetector.SimpleOnGestureListener{
	private IntroductionView introductionView;
	
	public IntroductionViewGestureListener(IntroductionView v) {
		super();
		introductionView = v;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (e1.getX() > e2.getX() && Math.abs(velocityX) > 200) {
			introductionView.tutorialForward();
		} else if (e1.getX() < e2.getX() && velocityX > 200) {
			introductionView.tutorialBackward();
		}
		return true;
	}
}
