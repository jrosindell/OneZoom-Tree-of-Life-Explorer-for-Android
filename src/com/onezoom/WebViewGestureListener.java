package com.onezoom;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class WebViewGestureListener extends GestureDetector.SimpleOnGestureListener{
	private CustomizeWebView webView;
	
	public WebViewGestureListener(CustomizeWebView v) {
		super();
		webView = v;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if(e1 == null || e2 == null) return false;
        if(e1.getPointerCount() > 1 || e2.getPointerCount() > 1) return false;
        else {
            try { // right to left swipe .. go to next page
                if(e1.getX() - e2.getX() > 100 && Math.abs(velocityX) > 800) {
                	webView.forwardNavigate();
                	return true;
                } //left to right swipe .. go to prev page
                else if (e2.getX() - e1.getX() > 100 && Math.abs(velocityX) > 800) {
                    //return to tree view
                	webView.backNavigate();
                    return true;
                } //bottom to top, go to next document
                else if(e1.getY() - e2.getY() > 100 && Math.abs(velocityY) > 800 
                        && webView.getScrollY() >= webView.getScale() * (webView.getContentHeight() - webView.getHeight())) {
                    //do your stuff
                    return true;
                } //top to bottom, go to prev document
                else if (e2.getY() - e1.getY() > 100 && Math.abs(velocityY) > 800 ) {
                    //do your stuff
                    return true;
                } 
            } catch (Exception e) { // nothing
            }
            return false;
        }
	}
}
