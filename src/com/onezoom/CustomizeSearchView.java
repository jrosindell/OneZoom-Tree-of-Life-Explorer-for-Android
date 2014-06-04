package com.onezoom;

import android.content.Context;
import android.view.WindowManager;
import android.widget.SearchView;

public class CustomizeSearchView extends SearchView{
	private CustomizeSearchView self;
	private boolean isWebView;
	private CanvasActivity client;
	
	public CustomizeSearchView(Context context) {
		super(context);
		self = this;
	}
	
	public boolean isWebView() {
		return isWebView;
	}

	public void setWebView(boolean isWebView) {
		this.isWebView = isWebView;
	}
	
	public void addClient(CanvasActivity _c) {
		client = _c;
	}
	
	/**
	 * Search listener in tree view.
	 */
	final SearchView.OnQueryTextListener queryTreeListener = new SearchView.OnQueryTextListener() {	
		@Override
		public boolean onQueryTextSubmit(String userInput) {
			self.clearFocus();
			client.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			client.search(userInput);
			return false;
		}
		
		@Override
		public boolean onQueryTextChange(String arg0) {
			return false;
		}
	};
	
	/**
	 * Search listener in web view.
	 * Load new web pages in web view when search has been submitted.
	 */
	final SearchView.OnQueryTextListener queryWebListener = new SearchView.OnQueryTextListener() {	
		@Override
		public boolean onQueryTextSubmit(String userInput) {
			self.clearFocus();
			client.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			client.searchAndLoad(userInput);
			return false;
		}
		
		@Override
		public boolean onQueryTextChange(String arg0) {
			return false;
		}
	};
}
