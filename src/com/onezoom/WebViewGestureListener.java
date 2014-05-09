package com.onezoom;

import android.view.GestureDetector;
import android.view.MotionEvent;

public class WebViewGestureListener extends GestureDetector.SimpleOnGestureListener{
	private CustomizeWebView webView;
	
	public WebViewGestureListener(CustomizeWebView v) {
		super();
		webView = v;
	}
}
