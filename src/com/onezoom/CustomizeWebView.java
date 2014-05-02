package com.onezoom;

import java.util.Stack;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class CustomizeWebView extends WebView{
	CanvasActivity client; 
	private GestureDetector gestureDetector;

	public CustomizeWebView(Context context) {
		super(context);
		init(context);
	}
	
	public CustomizeWebView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public CustomizeWebView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context) {
		client = (CanvasActivity) context;
		gestureDetector = new GestureDetector(context, new WebViewGestureListener(this));
		this.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return false;
			}		
		});
	}
	
	public void addClient(CanvasActivity activity) {
		client = activity;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
	}

	public void makeToast(String string) {
		Toast.makeText(client, string, Toast.LENGTH_SHORT).show();
	}

	public void backNavigate() {
		if (client.isSearching() && client.isSubmitSearching()) {
			client.backSearchAndLoad();
		} else {
			client.hideWebView();
			client.displayTreeView();
		}
	}
	
	public void forwardNavigate() {
		if (client.isSearching()) {
			client.forwardSearchAndLoad();
		}
	}

}
