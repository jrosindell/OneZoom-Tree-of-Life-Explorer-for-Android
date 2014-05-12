package com.onezoom;


import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CustomizeWebView extends WebView{
	CanvasActivity client; 

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
	
	/**
	 * Overload url redirection to prevent openning browser from the app
	 * @param context
	 */
	private void init(Context context) {
		this.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return false;
			}		
		});
	}
}
