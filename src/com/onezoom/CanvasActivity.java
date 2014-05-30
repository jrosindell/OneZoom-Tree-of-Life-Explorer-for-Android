package com.onezoom;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import com.onezoom.midnode.Initializer;
import com.onezoom.midnode.LinkHandler;
import com.onezoom.midnode.Search;
import com.onezoom.midnode.MidNode;
import com.onezoom.midnode.PositionData;
import com.onezoom.midnode.Visualizer;

import android.app.Activity;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This class implements another activity.
 * 
 * It contains two views, one is tree view and the other is web view and only one of them will be displayed 
 * at one time.
 * 
 * When the activity starts, it will call OnCreate function to initialize and OnCreateOptionsMenu to inflate 
 * action bar.
 * 
 * When the activity ends, it will call onDestroy to handle some garbage collection.
 * 
 * @author kaizhong
 *
 */
public class CanvasActivity extends Activity{
	public TreeView treeView;
	public CustomizeWebView webView;
	public IntroductionView introductionView;
	public String selectedItem;
	private MidNode fulltree;
	private MemoryThread memoryThread;
	private GrowthThread growthThread;
	private boolean growing = false;
	private boolean searching = false;
	private boolean viewingWeb = false;
	private boolean setting = false;
	private int orientation;
	private int screenHeight;
	private int screenWidth;
	private static float scaleFactor;
	private Search searchEngine;
	private Toast previousToast;
	private CustomizeSearchView currentSearchView;
	private Initializer initializer;
	private CanvasActivity self;
	private boolean firstTimeOpenTreeSelect = true;

	public Initializer getInitializer() {
		return initializer;
	}
	
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

	public boolean isViewingWeb() {
		return viewingWeb;
	}
	
	public int getOrientation() {
		return orientation;
	}

	public void setOrientation(int orientation) {
		this.orientation = orientation;
	}
	
	/**
	 * The app is initially developed on Samsung Galaxy S III, which has a resolution of 720*1280.
	 * 
	 * To make the app displayed correctly, use a scaleFactor variable to scale the tree based on 
	 * the ratio of user device and Samsung Galaxy S III.
	 */
	private void getDeviceScreenSize() {
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		screenHeight = displaymetrics.heightPixels;
		screenWidth = displaymetrics.widthPixels;		
		scaleFactor = Math.min(screenHeight, screenWidth) / 720f;
	}
	
	/**
	 * Activity starts here.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		self = this;
		/**
		 * Get the tree which was selected by the user.
		 */
		try {
			selectedItem = getIntent().getExtras().getString(
					"com.onezoom.selectedTree");	
			storeCurrentTreeIntoPreference();
		} catch (NullPointerException e) {
			SharedPreferences settings = getSharedPreferences("tree_name", 0);
			selectedItem = settings.getString("tree_name", null);	
			if (selectedItem == null) {
				selectedItem = "Mammals";
				storeCurrentTreeIntoPreference();
			}
		}
		/**
		 * Create two views.
		 */
		setContentView(R.layout.canvas_activity);
		treeView = (TreeView) findViewById(R.id.tree_view);	
		webView = (CustomizeWebView) findViewById(R.id.webview);
		introductionView = (IntroductionView) findViewById(R.id.introductio_view);
		/**
		 * Web view should be activated after user hits on links.
		 */
		hideWebView();
		hideIntroductionView();
		
		searchEngine =  Search.getInstance(this);
		orientation = getResources().getConfiguration().orientation;
		MidNode.setClient(this);
		initializer = new Initializer();
		memoryThread = new MemoryThread(this);
		growthThread = new GrowthThread(this);
		memoryThread.start();  //memory thread will load the tree into memory when it starts.
		growthThread.start();
		getDeviceScreenSize();
	}

	/**
	 * This method calls routines to inflate action bar.
	 * When tree view is displayed, it has grow menu, search menu and main menu.
	 * When web view is displayed, it has only one menu, which is inflated by inflateWebMenu.
	 * App title and icon are removed.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getActionBar().setDisplayShowHomeEnabled(false);
	    getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
		getActionBar().setIcon(android.R.color.transparent);
		if (viewingWeb) inflateWebMenu(menu);
		else if (growing) inflateTreeGrowMenu(menu);
		else if (searching) inflateTreeSearchMenu(menu);
		else if (setting) inflateTreeSettingMenu(menu);
		else inflateTreeMainMenu(menu);  
		return true;
	}
	
	/**
	 * Destroy any running thread.
	 * Hide toast if it is showing or gonna to show.
	 * BinarySearch class in a singleton class. Destroy it otherwise when user re-select a tree,
	 * the searchEngine will still be linked to the previous activity.
	 */
	@Override
	protected void onDestroy() {
		storeBitmapToFile();
		if (treeView.getInitBitmap() != null) {
			treeView.getInitBitmap().recycle();
		}
		if (previousToast != null)
			previousToast.cancel();
		super.onDestroy();
		memoryThread.requestStop();
		growthThread.requestStop();
		Search.destory();
	}

	
	/**
	 * This method is called by memoryThread.
	 * It loads the tree into memory.
	 */
	public void initialization() {
		initializer.setContext(this);
		//140 is the height left for action bar.
		PositionData.setScreenSize(0, 0, screenWidth, screenHeight - 140);

		//set the tree position to fit in the screen.
		resetTreeRootPosition();
		treeView.resetDragScaleParameter();

		//Initialize from file index '0'. 
		//For example, if user select mammals, then initialize from 'mammalsinterior0'
		fulltree = MidNode.startLoadingTree();
		
		fulltree.recalculateDynamic();
		//set tree as being initialized so that tree view draws the tree instead of drawing 'loading'
	}

	
	/**
	 * The following methods are delegated to memoryThread 
	 * to make sure all calculations are being executed sequentially as opposed to concurrently.
	 */	
	
	public void recalculate() {
		memoryThread.recalculate();
	}
	
	public void reset() {
		memoryThread.reset();
	}
	
	/**
	 * Search match for user input and jump the view to the search result.
	 * @param userInput
	 */
	public void search(String userInput) {
		memoryThread.search(userInput);
	}
	
	/**
	 * Jump to previous search hit. If current search is the first hit, it will jump to the last hit.
	 */
	public void backSearch() {
		memoryThread.backSearch();
	}
	
	/**
	 * Jump to the next search hit. If current hit is the last hit, it will jump to the first hit.
	 */
	public void forwardSearch() {
		memoryThread.forwardSearch();
	}
	
	/**
	 * searchAndLoad is called in web view. 
	 * 
	 * It's similar to search. But it will load a new web page 
	 * of the hit rather than jump to the search result in the tree.
	 * @param userInput
	 */
	public void searchAndLoad(String userInput) {
		memoryThread.searchAndLoad(userInput);
	}

	/**
	 * backSearchAndLoad is called in web view.
	 * 
	 * 
	 * It's similar to backSearch. But it will load a new web page 
	 * of the hit rather than jump to the search result in the tree.
	 */
	public void backSearchAndLoad() {
		memoryThread.backSearchAndLoad();
		
	}

	/**
	 * forwardSearchAndLoad is called in web view. 
	 * 
	 * It's similar to forwardSearch. But it will load a new web page 
	 * of the hit rather than jump to the search result in the tree.
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
		case R.id.tree_setting:
			setting = true;
			invalidateOptionsMenu();
			break;
		case R.id.reset:
			resetTree();
			break;
		case R.id.information:
			popupInformationDialog();
			break;
			
		
		//tree setting
		case R.id.common_latin_switch:
			Visualizer.setUsingCommon(!Visualizer.isUsingCommon());
			treeView.setRefreshNeeded(true);
			treeView.invalidate();
			break;
		case R.id.setting_close:
			this.returnToMainMenu();
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
	
	private void inflateTreeSettingMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.setting, menu);
		Spinner spinner = createSpinnerFromResources(R.id.tree_switch, menu, R.array.trees);
		createSpinnerForTreeSelect(spinner);
		spinner = createSpinnerFromResources(R.id.common_latin_switch, menu, R.array.latin_common_switch);
		createSpinnerForLatinCommonSwitch(spinner);
	}
	
	private Spinner createSpinnerFromResources(int id, Menu menu, int arrayID) {
		MenuItem menuItem = menu.findItem(id);
		View menuView = menuItem.getActionView();
		Spinner spinner = (Spinner) menuView;
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        arrayID, android.R.layout.simple_list_item_1);
		spinner.setAdapter(adapter);
		spinner.setPopupBackgroundDrawable(new ColorDrawable(Color.WHITE));
		return spinner;
	}
	
	/**
	 * set spinner listenner
	 */
	private void createSpinnerForLatinCommonSwitch(Spinner spinner) {
		
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) {
					Visualizer.setUsingCommon(true);
				} else {
					Visualizer.setUsingCommon(false);
				}
				self.treeView.setRefreshNeeded(true);
				self.treeView.postInvalidate();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
			
		});
	}
	
	/**
	 * Create spinner
	 */
	private void createSpinnerForTreeSelect(final Spinner spinner) {
		spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				if (self.firstTimeOpenTreeSelect == true) {
					self.firstTimeOpenTreeSelect = false;
				} else if (!spinner.getSelectedItem().toString().equals(self.selectedItem)) {
					Search.destory();
					Intent intent = new Intent(self, CanvasActivity.class);
		    		String selectedItem = spinner.getSelectedItem().toString();
		    		intent.putExtra("com.onezoom.selectedTree", selectedItem);
		    		startActivity(intent);
		    		self.finish();
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {				
			}
			
		});
		String [] array = getResources().getStringArray(R.array.trees);
		spinner.setSelection(getPositionInArray(array, self.selectedItem));
	}
	
	/**
	 * 
	 */
	private int getPositionInArray(String[] array, String item) {
		for (int i = 0; i < array.length; i++) {
			if (array[i].equals(item)) return i;
		}
		return 0;
	}
	
	
	/**
	 * Inflate search menu on tree view.
	 * @param menu
	 */
	private void inflateTreeSearchMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.searhmenu, menu);
		CustomizeSearchView searchView = inflateSearchView(menu, R.id.search_tree);
		searchView.setOnQueryTextListener(searchView.queryTreeListener);
		searchView.setWebView(false);
	}
	
	/**
	 * Inflate web menu.
	 * @param menu
	 */
	private void inflateWebMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.webmenu, menu);		
		CustomizeSearchView searchView = inflateSearchView(menu, R.id.search_web);
		searchView.setOnQueryTextListener(searchView.queryWebListener);
		searchView.setWebView(true);
		searchView.clearFocus();		
	}
	
	/**
	 * Inflate searchView. 
	 * 
	 * It is called both in tree view and web view.
	 * Tree view will use R.id.search_tree as ID, while web view will use R.id.search_web as ID.
	 * 
	 * 
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
		
		this.currentSearchView = searchView;
		return searchView;
	}

	/**
	 * Set search view the same as previous user input. If it does not exist, set is as hint.
	 * @param searchView
	 */
	private void setQueryInSearchView(final CustomizeSearchView searchView) {
		searchView.setQueryHint("Enter Species Name.");
		if (!this.searchEngine.getPreviousSearch().equals("")) {
			searchView.setQuery(this.searchEngine.getPreviousSearch(), false);
		}
	}

	/**
	 * Pop up a dialog which contains information of how to use the app, the color meanings of the app
	 * and the authors of the app.
	 */
	private void popupInformationDialog() {
		Dialog dialog = new CustomDialog(this);
		dialog.show();
	}

	/**
	 * When user press back in search or grow menu, call this function to return to main
	 * menu in tree view.
	 * 
	 * It is also called when user press tree button in web view to force the action bar inflate
	 * the main menu instead of growth menu or search menu.
	 */
	public void returnToMainMenu() {
		growthThread.Close();
		if (this.viewingWeb == false) {
			growing = false;
			searching = false;	
			setting = false;
		}
		invalidateOptionsMenu();
	}

	/**
	 * Reset the tree to the initial position.
	 */
	public void resetTree() {
		treeView.setRefreshNeeded(true);
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
	
	private void hideIntroductionView() {
		this.introductionView.setVisibility(View.GONE);
	}

	/**
	 * Test whether user click is on link.
	 * If it is true, during the test, the selected node and url will be marked and can
	 * be accessed through MidNode class method.
	 * @param mouseX
	 * @param mouseY
	 * @return
	 */
	public boolean hasHitLink(float mouseX, float mouseY) {
		return LinkHandler.testLink(fulltree, mouseX, mouseY);
	}

	public void loadLinkURL() {
		webView.loadUrl(LinkHandler.getLink());
	}
	
	/**
	 * This method is called when user click on a link.
	 * If the node user selected contains keyword of user search, then do nothing.
	 * Otherwise, reset the searchEngine.
	 */
	public void resetSearch() {
		this.searchEngine.resetSearch(LinkHandler.getLink(), LinkHandler.getLinkNode().traitsCalculator.getCname());
	}
	
	private void reloadPage() {
		loadLinkURL();
	}

	/**
	 * Hide keyboard.
	 * @param view
	 */
	public void hideKeyBoard(View view) {
		InputMethodManager imm = (InputMethodManager)this.getSystemService(
			      Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(view.getWindowToken(), 0);		
	}

	/**
	 * This method sets the query text of current search view.
	 * The reason for creating this method to be called after wiki link is that there is some problem
	 * with setting query content after creating web view. It seems that you need to change the query
	 * content in tree view first.
	 */
	public void setQueryOfCurrentSearchView() {
		if (this.currentSearchView != null)
			this.currentSearchView.setQuery(this.searchEngine.getPreviousSearch(), false);
	}
	
	/**
	 * Read bitmap from file. 
	 * This is a initial image of each tree. 
	 * Reading it to display the initial tree in order to reduce loading time.
	 */
	void readBitmapFromFile() {
		FileInputStream inputStream = null;
		try {
			File file1;
			if (orientation == Configuration.ORIENTATION_PORTRAIT) {
				file1 = new File(getExternalFilesDir(null), selectedItem
						+ "portrait" + ".jpg");
			} else {
				file1 = new File(getExternalFilesDir(null), selectedItem
						+ "landscape" + ".jpg");
			}
			//If the bitmap does not exist, then return and bitmap would be null
			if (file1 != null && !file1.exists())
				return;
			inputStream = new FileInputStream(file1);
			// Read object using ObjectInputStream
			treeView.setInitBitmap(BitmapFactory.decodeStream(inputStream)); 
			if (treeView.getInitBitmap() != null) {
				treeView.setCachedBitmap(treeView.getInitBitmap());
			}
		} catch (FileNotFoundException e) {
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				inputStream = null;
			}
		}
	}
	
	/**
	 * Store current tree into preference. 
	 */
	private void storeCurrentTreeIntoPreference() {
		SharedPreferences settings = getSharedPreferences("tree_name", 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("tree_name", selectedItem);
		editor.commit();
	}
	
	
	/**
	 * Store bitmap into file.
	 */
	void storeBitmapToFile() {
		try {
			File file1;
			if (orientation == Configuration.ORIENTATION_PORTRAIT) {
				file1 = new File(getExternalFilesDir(null), selectedItem
						+ "portrait" + ".jpg");
			} else {
				file1 = new File(getExternalFilesDir(null), selectedItem
						+ "landscape" + ".jpg");
			}

			if (treeView.getInitBitmap() != null || !treeView.isTreeBeingInitialized())
				return;
			FileOutputStream outputStream;
			outputStream = new FileOutputStream(file1);

			resetTreeRootPosition();
			this.getTreeRoot().recalculate();
			treeView.setRefreshNeeded(true);
			treeView.setToggle(false);
			
			if (treeView.getInitBitmap() != null)
				treeView.getInitBitmap().recycle();

			treeView.setInitBitmap(treeView.loadBitmapFromView(treeView));
			if (treeView.getInitBitmap() != null && !treeView.getInitBitmap().isRecycled()) {
				treeView.getInitBitmap().compress(Bitmap.CompressFormat.PNG, 100,
						outputStream);
				treeView.getInitBitmap().recycle();
			}
			outputStream.close();
			outputStream = null;
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
		} catch (NullPointerException e) {
		}
	}

	public void showIntroductionSlide() {
		treeView.setVisibility(View.GONE);
		this.introductionView.setVisibility(View.VISIBLE);
		this.introductionView.startTutorial();
	}

	public void endTutorial() {
		this.introductionView.setVisibility(View.GONE);
		treeView.setVisibility(View.VISIBLE);
		treeView.setRefreshNeeded(true);
		treeView.invalidate();
	}
}
