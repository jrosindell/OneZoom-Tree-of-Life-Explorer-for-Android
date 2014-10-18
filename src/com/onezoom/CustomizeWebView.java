package com.onezoom;


import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class CustomizeWebView extends WebView{
	CanvasActivity client; 
	private String url;
	
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public void loadUrl(String url) {
		this.url = url;
		super.loadUrl(url);
	}

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
	
	public void setClient(CanvasActivity client) {
		this.client = client;
	}
	
	/**
	 * Overload url redirection to prevent openning browser from the app
	 * @param context
	 */
	private void init(Context context) {			
		getSettings().setLoadWithOverviewMode(true);
		getSettings().setUseWideViewPort(true);
		getSettings().setBuiltInZoomControls(true);
		setInitialScale(2);
		
		getSettings().setJavaScriptEnabled(true);
		
		this.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				client.setProgress(progress * 100);
			}
		});
		
		this.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.clearView();
				view.loadUrl(url);
				return false;
			}		
		});
	}
}
