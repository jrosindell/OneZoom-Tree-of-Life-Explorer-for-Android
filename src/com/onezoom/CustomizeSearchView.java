package com.onezoom;

import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.SearchView;

public class CustomizeSearchView extends SearchView{
	private CustomizeSearchView self;
	private boolean isWebView;

	private CanvasActivity client;
	private boolean toggle = true;
	
	
	final SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {	
		@Override
		public boolean onQueryTextSubmit(String userInput) {
			self.setQueryHint("Enter Species Name");
			self.setQuery(userInput, false);
			self.clearFocus();
			client.search(userInput);
			client.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			return false;
		}
		
		@Override
		public boolean onQueryTextChange(String arg0) {
			return false;
		}
	};
	
	final SearchView.OnQueryTextListener queryWebListener = new SearchView.OnQueryTextListener() {	
		@Override
		public boolean onQueryTextSubmit(String userInput) {
			self.setQueryHint("Enter Species Name");
			self.setQuery(userInput, false);
			self.clearFocus();
			client.searchAndLoad(userInput);
			client.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			return false;
		}
		
		@Override
		public boolean onQueryTextChange(String arg0) {
			return false;
		}
	};
	
	public CustomizeSearchView(Context context) {
		super(context);
		self = this;
	}

	@Override
	public void onActionViewCollapsed() {
		if (toggle) {
			toggle = false;
			if (isWebView) {
				client.hideWebView();
				client.displayTreeView();
				client.invalidateOptionsMenu();
			} else {
				client.returnToMainMenu();
			}
		} else {
			toggle = true;
		}
			
		super.onActionViewCollapsed();
	}
	
	public void addClient(CanvasActivity _c) {
		client = _c;
	}
	
	public boolean isWebView() {
		return isWebView;
	}

	public void setWebView(boolean isWebView) {
		this.isWebView = isWebView;
	}

}
