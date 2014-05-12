package com.onezoom;



import com.onezoom.midnode.MidNode;
import com.onezoom.midnode.PositionData;
import com.onezoom.midnode.displayBinary.BinarySearch;

import android.util.Log;
import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

public class CanvasActivity extends Activity{
	public TreeView treeView;
	public CustomizeWebView webView;
	public static String selectedItem;
	private MidNode fulltree;
	private MemoryThread memoryThread;
	private GrowthThread growthThread;
	private boolean growing = false;
	private boolean searching = false;
	private boolean submitSearching = false;
	private boolean viewingWeb = false;
	private int orientation;
	private int screenHeight;
	private int screenWidth;
	private static float scaleFactor;
	private BinarySearch searchEngine;
	private Toast previousToast;

	public MidNode getTreeRoot() {
		return fulltree;
	}
	
	public TreeView getTreeView() {
		return treeView;
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

	public boolean isSearching() {
		return searching;
	}

	public boolean isSubmitSearching() {
		return submitSearching;
	}

	public boolean isViewingWeb() {
		return viewingWeb;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//get tree that is selected by the user
		selectedItem = getIntent().getExtras().getString(
				"com.onezoom.selectedTree");
		
		
		setContentView(R.layout.canvas_activity);
		treeView = (TreeView) findViewById(R.id.tree_view);	
		webView = (CustomizeWebView) findViewById(R.id.webview);
		
		//only display tree view when the activity start
		hideWebView();
		
		searchEngine =  BinarySearch.getInstance(this);
		orientation = getResources().getConfiguration().orientation;
		memoryThread = new MemoryThread(this);
		growthThread = new GrowthThread(this);
		memoryThread.start();
		growthThread.start();
		getDeviceScreenSize();
	}

	/**
	 * This method calls routines to inflate action bar.
	 * When tree view is displayed, it has grow menu, search menu and main menu.
	 * When web view is displayed, it has only one menu.
	 * App title and icon are removed.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getActionBar().setDisplayShowHomeEnabled(false);
	    getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(false);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		getActionBar().setIcon(android.R.color.transparent);
		if (viewingWeb) inflateWebMenu(menu);
		else if (growing) inflateTreeGrowMenu(menu);
		else if (searching) inflateTreeSearchMenu(menu);
		else inflateTreeMainMenu(menu);  
		return true;
	}
	
	/**
	 * Destroy any running thread.
	 * Hide toast if it is showing or gonna to show.
	 * BinarySearch class in a singleton class. Destroy it otherwise when user re-select a tree,
	 * the searchEngine will still be linked to previous activity.
	 */
	@Override
	protected void onDestroy() {
		if (previousToast != null)
			previousToast.cancel();
		super.onDestroy();
		memoryThread.requestStop();
		growthThread.requestStop();
		BinarySearch.destory();
	}

	
	/**
	 * This method is called by memoryThread.
	 * It loads the tree into memory.
	 */
	public void initialization() {
		MidNode.setContext(this);
		//140 is the height left for action bar.
		MidNode.setScreenSize(0, 0, screenWidth, screenHeight - 140);

		//set the tree position to fit in the screen.
		resetTreeRootPosition();
		
		//Initialize from file index '0'. 
		//For example, if user select mammals, then initialize from 'mammalsinterior0'
		fulltree = MidNode.createNode("0");
		
		fulltree.recalculate();
		
		//set tree as being initialized so that tree view draws the tree instead of drawing 'loading'
		treeView.setTreeBeingInitialized(true);	
	}

	
	/**
	 * The following methods are delegated to memoryThread 
	 * to make sure they are being executed sequentially as opposed to concurrently.
	 */	
	
	public void recalculate() {
		memoryThread.recalculate();
	}
	
	public void reset() {
		memoryThread.reset();
	}
	
	/**
	 * search is called in tree view.
	 * @param userInput
	 */
	public void search(String userInput) {
		this.submitSearching = true;
		memoryThread.search(userInput);
	}
	
	/**
	 * backSearch is called in tree view.
	 */
	public void backSearch() {
		memoryThread.backSearch();
	}
	
	/**
	 * forwardSearch is called in tree view.
	 */
	public void forwardSearch() {
		memoryThread.forwardSearch();
	}
	
	/**
	 * searchAndLoad is called in web view. A new webpage will be loaded after search.
	 * @param userInput
	 */
	public void searchAndLoad(String userInput) {
		this.submitSearching = true;
		memoryThread.searchAndLoad(userInput);
	}

	/**
	 * backSearchAndLoad is called in web view. A new webpage will be loaded after search.
	 */
	public void backSearchAndLoad() {
		memoryThread.backSearchAndLoad();
		
	}

	/**
	 * forwardSearchAndLoad is called in web view. A new webpage will be loaded after search.
	 */
	public void forwardSearchAndLoad() {
		memoryThread.forwardSearchAndLoad();		
	}
	
	
	/**
	 * This method response to user click on ActionBar.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {	
		//Icons on main menu of tree view.
		case R.id.search_main_tree:
			searching = true;
			invalidateOptionsMenu();
			break;
		case R.id.grow:
			growing = true;
			invalidateOptionsMenu();
			break;
		case R.id.information:
			popupInformationDialog();
			break;
		case R.id.reset:
			resetTree();
			break;
			
		
		//Icons on search menu of tree view.
		case R.id.search_backward_tree:
			backSearch();
			break;		
		case R.id.search_forward_tree:
			forwardSearch();
			break;
		case R.id.search_close:
			this.returnToMainMenu();
			break;
			
			
		//Icons on grow menu of tree view
		case R.id.grow_reverse:
			growthThread.Revert();
			break;
		case R.id.grow_pause:
			growthThread.Pause();
			break;
		case R.id.grow_play:
			growthThread.Play();
			break;
		case R.id.grow_stop:
			growthThread.Stop();
			break;
		case R.id.grow_close:
			growing = false;
			growthThread.Close();
			invalidateOptionsMenu();
			break;
			
			
		//Icons on menu of web view
		case R.id.reload_page:
			this.reloadPage();
			break;
		case R.id.search_backward_web:
			this.backSearchAndLoad();
			break;
		case R.id.search_forward_web:
			this.forwardSearchAndLoad();
			break;
		case R.id.web_back_to_tree:
			this.hideWebView();
			this.displayTreeView();
			this.returnToMainMenu();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void inflateTreeGrowMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.grow, menu);
	}
	
	private void inflateTreeMainMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.viewmenu, menu);
	}
	
	private void inflateTreeSearchMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.searhmenu, menu);
		CustomizeSearchView searchView = inflateSearchView(menu, R.id.search_tree);
		searchView.setOnQueryTextListener(searchView.queryTreeListener);
		searchView.setWebView(false);
	}
	
	private void inflateWebMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.webmenu, menu);		
		CustomizeSearchView searchView = inflateSearchView(menu, R.id.search_web);
		searchView.setOnQueryTextListener(searchView.queryWebListener);
		searchView.setWebView(true);
		searchView.clearFocus();		
	}
	
	/**
	 * Inflate searchView. 
	 * Tree view will use R.id.search_tree as ID, while web view will use R.id.search_web as ID.
	 * @param menu
	 * @param resourceID
	 * @return
	 */
	private CustomizeSearchView inflateSearchView(Menu menu, int resourceID) {
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		final CustomizeSearchView searchView = (CustomizeSearchView) menu.findItem(resourceID)
				.getActionView();
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
		searchView.setIconifiedByDefault(true);
		
		MenuItem searchMenuItem = (MenuItem) menu.findItem(resourceID);
		searchMenuItem.expandActionView();
		
		setQueryInSearchView(searchView);
		
		searchView.addClient(this);
		//set text color as black
		int id = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
		TextView textView = (TextView) searchView.findViewById(id);
		textView.setTextColor(Color.BLACK);
		
		return searchView;
	}

	/**
	 * Set search view the same as previous user input. If it does not exist, set it as using hint.
	 * @param searchView
	 */
	private void setQueryInSearchView(final CustomizeSearchView searchView) {
		if (!this.searchEngine.getPreviousSearch().equals("")) {
			searchView.setQuery(this.searchEngine.getPreviousSearch(), false);
		} else {
			searchView.setQueryHint("Enter Species Name");			
		}
	}

	/**
	 * Show a dialog which tells users how to understand the tree
	 */
	private void popupInformationDialog() {
		Dialog dialog = new CustomDialog(this);
		dialog.show();
	}

	/**
	 * When user press back in search or grow menu, call this function to return to main
	 * menu in tree view.
	 * 
	 * It is also called when user press tree button in web view.
	 */
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
	
	/**
	 * reset tree position to its initial position. 
	 * The factors are set to assure that the tree will be fitted within the screen.
	 */
	private void resetTreeRootPosition() {
		//user scale factor to adjust the size of the tree according to the size of device width
		//set the root to (265,800) or (500,545) to ensure that all nodes are drawn on the screen
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			PositionData.setScreenPosition(265f * scaleFactor, 800f * scaleFactor, 0.73f * scaleFactor);
		} else {
			PositionData.setScreenPosition(500f * scaleFactor, 545f * scaleFactor, 0.73f * scaleFactor);
		}
	}
	
	/**
	 * Move root to the center of the screen.
	 * However, if the tree has been re-anchored, the re-anchored node will be moved to center.
	 * 
	 * This will happen when the tree has been zoomed in deeply or when search is processed,
	 * the re-anchored node will be the search result.
	 * 
	 * Use this function to bring the searched node into the center of the screen.
	 */
	public void moveRootToCenter() {
		//move root to center.
		//However, if root has been reanchored, the reanchored node will be moved to center.
		//Notice that the leaf or interior circle will not be moved to center but the start position of 
		//their branch will be moved to center.
		PositionData.setScreenPosition(treeView.getWidth()/2, treeView.getHeight()/2, 1f * scaleFactor);
	}

	/**
	 * This method is used by searchEngine to show search result.
	 * 
	 * This method should have been declared in BinarySearch class. 
	 * But due to some reason, it does not disappear appropriately if declared there. 
	 * Therefore, this method is moved here 
	 * such that its disappearance can be handled when activity is destroyed.
	 * @param text
	 */
	public void showToast(String text) {
		if (previousToast != null)
			previousToast.cancel();
		previousToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
		previousToast.show();
	}

	public void hideWebView() {
		webView.setVisibility(View.GONE);
		this.viewingWeb = false;
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

	/**
	 * Test whether user click is on wiki link.
	 * If it is true, during the test, the selected node and wiki url will be marked and can
	 * be accessed through MidNode class method.
	 * @param mouseX
	 * @param mouseY
	 * @return
	 */
	public boolean hasHitWikiLink(float mouseX, float mouseY) {
		return this.fulltree.testLink(mouseX, mouseY);
	}

	public void loadWikiURL() {
		String wikiLink = "http://en.wikipedia.org/wiki/" + fulltree.wikilink();
		webView.loadUrl(wikiLink);
	}
	
	/**
	 * This method is called when user click on a wiki link.
	 * If the node user selected contains keyword of user search, then do nothing.
	 * Otherwise, reset the searchEngine.
	 */
	public void resetSearch() {
		this.searchEngine.resetSearch(fulltree.wikilink(), fulltree.wikiNode().traitsCalculator.getCname());
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
