package com.onezoom;



import com.onezoom.midnode.MidNode;
import com.onezoom.midnode.PositionData;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

public class CanvasActivity extends Activity{
	public TreeView treeView;
	public CustomizeWebView webView;
	public static String selectedItem;
	private MidNode fulltree;
	private boolean started = false;
	private MemoryThread memoryThread;
	private GrowthThread growthThread;
	private boolean threadStarted = false;
	private boolean growing = false;
	private boolean searching = false;
	private boolean viewingWeb = false;
	private boolean jumpingFromWebView = false;
	private int orientation;
	private int screenHeight;
	private int screenWidth;
	private static float scaleFactor;
	Toast previousToast;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//init view
		selectedItem = getIntent().getExtras().getString(
				"com.onezoom.selectedTree");
		
		
		setContentView(R.layout.canvas_activity);
		treeView = (TreeView) findViewById(R.id.tree_view);	
		webView = (CustomizeWebView) findViewById(R.id.webview);
		hideWebView();
		
		
		memoryThread = new MemoryThread(this);
		growthThread = new GrowthThread(this);
		
		getDeviceScreenSize();
	}

	public TreeView getTreeView() {
		return treeView;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(false);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		if (viewingWeb) inflateWebMenu(menu);
		else if (growing) inflateGrowMenu(menu);
		else if (searching) inflateSearchMenu(menu);
		else inflateViewMenu(menu);  
		return true;
	}
	
	
	@Override
	protected void onStart() {
		super.onStart();
		orientation = getResources().getConfiguration().orientation;
//		
//		//only start the activity once
		if (started) return;
		else started = true;
		
		if (!threadStarted) {
			threadStarted = true;
			memoryThread.start();
			growthThread.start();
		}
	}
	
	@Override
	protected void onDestroy() {
		if (previousToast != null)
			previousToast.cancel();
		super.onDestroy();
		memoryThread.requestStop();
		growthThread.requestStop();
	}

	public MidNode getTreeRoot() {
		return fulltree;
	}

	
	/**
	 * Start loading the tree into memory.
	 */
	public void initialization() {
		MidNode.setContext(this);
		MidNode.setScreenSize(0, 0, screenWidth, screenHeight - 140);

		resetTreeRootPosition();
		
		//Initialize from file index '0'. 
		//For example, if user select mammals, then initialize from 'mammalsinterior0'
		fulltree = MidNode.createNode("0");
		
		fulltree.recalculate();
		
		//set tree as being initialized so that the view will draw the tree instead of draw 'loading'
		treeView.setTreeBeingInitialized(true);	
	}
	
	public void recalculate() {
		memoryThread.recalculate();
	}
	
	public void reset() {
		memoryThread.reset();
	}
	
	public void search(String userInput) {
		memoryThread.search(userInput);
	}
	
	public void backSearch() {
		memoryThread.backSearch();
	}
	
	public void forwardSearch() {
		memoryThread.forwardSearch();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			if (this.viewingWeb) {
				this.hideWebView();
				this.displayTreeView();
			} else {
				this.returnToMainMenu();
			}
			
			break;
		
		case R.id.grow:
			growing = true;
			invalidateOptionsMenu();
			break;
		
		case R.id.grow_play:
			growthThread.Play();
			break;
			
		case R.id.grow_pause:
			growthThread.Pause();
			break;
			
		case R.id.grow_stop:
			growthThread.Stop();
			break;
			
		case R.id.grow_reverse:
			growthThread.Revert();
			break;
		
		case R.id.grow_close:
			growing = false;
			growthThread.Close();
			invalidateOptionsMenu();
			break;

		case R.id.reset:
			resetTree();
			break;
		
		case R.id.search:
			searching = true;
			invalidateOptionsMenu();
			break;
			
		case R.id.back:
			backSearch();
			break;
			
		case R.id.forward:
			forwardSearch();
			break;
			
		case R.id.back_navigation:
			webView.backNavigate();
			break;
			
		case R.id.forward_navigation:
			webView.forwardNavigate();
			break;
			
		case R.id.reload_page:
			this.reloadPage();
			break;
			
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void returnToMainMenu() {
		growthThread.Close();
		if (this.viewingWeb == false) {
			growing = false;
			searching = false;	
		}
		
		invalidateOptionsMenu();
	}

	public void resetTree() {
		treeView.setDuringInteraction(false);
		resetTreeRootPosition();
		this.reset();
	}

	private void inflateSearchMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.searhmenu, menu);

		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		final CustomizeSearchView searchView = (CustomizeSearchView) menu.findItem(R.id.search)
				.getActionView();
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
		searchView.setIconifiedByDefault(true);
		
		MenuItem searchMenuItem = (MenuItem) menu.findItem(R.id.search);
		searchMenuItem.expandActionView();
		
		searchView.setQueryHint("Enter Species Name");
		searchView.addClient(this);
		searchView.setOnQueryTextListener(searchView.queryTextListener);
		
		int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
		TextView textView = (TextView) searchView.findViewById(id);
		textView.setTextColor(Color.BLACK);
		
		if (this.jumpingFromWebView) {
			searchView.clearFocus();			
			this.jumpingFromWebView = false;
		} 
	}
	
	private void inflateGrowMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.grow, menu);
	}
	
	private void inflateViewMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.viewmenu, menu);
	}
	
	private void inflateWebMenu(Menu menu) {
		this.jumpingFromWebView = true;
		getMenuInflater().inflate(R.menu.webmenu, menu);
	}
	
	public int getOrientation() {
		return orientation;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}
	
	private void getDeviceScreenSize() {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		screenHeight = displaymetrics.heightPixels;
		screenWidth = displaymetrics.widthPixels;		
		scaleFactor = Math.min(screenHeight, screenWidth) / 720f;
	}
	
	public int getScreenHeight() {
		return screenHeight;
	}

	public static float getScaleFactor() {
		return scaleFactor;
	}

	public int getScreenWidth() {
		return screenWidth;
	}

	private void resetTreeRootPosition() {
		//user scale factor to adjust the size of the tree according to the size of device width
		//set the root to (265,800) or (500,545) to ensure that all nodes are drawn on the screen
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			PositionData.setScreenPosition(265f * scaleFactor, 800f * scaleFactor, 0.73f * scaleFactor);
		} else {
			PositionData.setScreenPosition(500f * scaleFactor, 545f * scaleFactor, 0.73f * scaleFactor);
		}
	}
	
	public void moveRootToCenter() {
		//move root to center.
		//However, if root has been reanchored, the reanchored node will be moved to center.
		//Notice that the leaf or interior circle will not be moved to center but the start position of 
		//their branch will be moved to center.
		PositionData.setScreenPosition(treeView.getWidth()/2, treeView.getHeight()/2, 1f * scaleFactor);
	}

	public void showToast(String text) {
		if (previousToast != null)
			previousToast.cancel();
		previousToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
		previousToast.show();
	}

	public void hideWebView() {
		webView.setVisibility(View.GONE);
		this.viewingWeb = false;
		if (webView.backPages.empty() || !webView.backPages.peek().equals(webView.getUrl())) {
			webView.backPages.push(webView.getUrl());
		}
		this.invalidateOptionsMenu();
	}
	
	public void hideTreeView() {
		treeView.setVisibility(View.GONE);
		this.viewingWeb = true;
		this.invalidateOptionsMenu();
	}
	
	public void displayWebView() {
		webView.setVisibility(View.VISIBLE);
	}
	
	public void displayTreeView() {
		treeView.setVisibility(View.VISIBLE);
	}

	public boolean hasHitWikiLink(float mouseX, float mouseY) {
		return this.fulltree.testLink(mouseX, mouseY);
	}

	public void loadWikiURL() {
		String wikiLink = "http://en.wikipedia.org/wiki/" + fulltree.wikilink();
		webView.loadUrl(wikiLink);
	}
	
	private void reloadPage() {
		loadWikiURL();
	}

	public void hideKeyBoard(View view) {
		InputMethodManager imm = (InputMethodManager)this.getSystemService(
			      Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);		
	}
}
