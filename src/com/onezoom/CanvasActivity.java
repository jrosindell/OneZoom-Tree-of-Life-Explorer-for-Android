package com.onezoom;


import java.util.TreeMap;

import com.onezoom.midnode.MidNode;
import com.onezoom.midnode.PositionData;
import com.onezoom.midnode.displayBinary.BinarySearch;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

public class CanvasActivity extends Activity{
	public TreeView treeView;
	public static String selectedItem;
	private MidNode fulltree;
	private boolean started = false;
	private MemoryThread memoryThread;
	private GrowthThread growthThread;
	private boolean threadStarted = false;
	private boolean growing = false;
	private BinarySearch searchEngine;
	private int orientation;
	private int screenHeight;
	private int screenWidth;
	private static float scaleFactor;

	private TreeMap<String, String> groupIndexMap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.canvas_activity);
		treeView = (TreeView) findViewById(R.id.tree_view);	

		memoryThread = new MemoryThread(this);
		growthThread = new GrowthThread(this);
		searchEngine = new BinarySearch(this);
		
		groupIndexMap = new TreeMap<String, String>();
		groupIndexMap.put("Mammals", "0");
		groupIndexMap.put("Tetrapods", "0");
		groupIndexMap.put("Amphibian", "0");
		groupIndexMap.put("Birds", "0");
		
		selectedItem = getIntent().getExtras().getString(
				"com.onezoom.selectedTree");
		
		getDeviceScreenSize();
	}

	public TreeView getTreeView() {
		return treeView;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setDisplayHomeAsUpEnabled(false);
		if (growing) inflateGrowMenu(menu);
		else inflateSearchMenu(menu);    
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
		super.onDestroy();
		memoryThread.requestStop();
		growthThread.requestStop();
	}

	public MidNode getTreeRoot() {
		return fulltree;
	}

	

	public void initialization() {
		MidNode.setContext(this);
		setScreenSize();

		resetScreenPosition();
		fulltree = MidNode.createNode(groupIndexMap.get(selectedItem));
		fulltree.recalculate();
		fulltree.init();
		fulltree.outputInitElement();
		treeView.setTreeBeingInitialized(true);	
	}
	
	public void recalculate() {
		memoryThread.recalculate();
	}
	
	public void reset() {
		memoryThread.reset();
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			growing = false;
			invalidateOptionsMenu();
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
		
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public void resetTree() {
		treeView.setDuringInteraction(false);
		resetScreenPosition();
		this.reset();
	}

	private void inflateSearchMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.searchmenu, menu);

		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		final SearchView searchView = (SearchView) menu.findItem(R.id.search)
				.getActionView();
		searchView.setSearchableInfo(searchManager
				.getSearchableInfo(getComponentName()));
		searchView.setIconifiedByDefault(true);
		MenuItem searchMenuItem = (MenuItem) menu.findItem(R.id.search);
		searchView.setQueryHint("Enter Species Name");
		searchView.clearFocus();
		
		searchView.setOnQueryTextListener(new OnQueryTextListener() {		
			@Override
			public boolean onQueryTextSubmit(String arg0) {
				searchView.setQueryHint("Enter Species Name");
				searchView.setQuery(arg0, false);
				searchView.clearFocus();
				searchEngine.performSearch(arg0);
				getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
				return false;
			}
			
			@Override
			public boolean onQueryTextChange(String arg0) {
				return false;
			}
		});
	}
	
	private void inflateGrowMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.grow, menu);
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
	
	private void setScreenSize() {
		MidNode.setScreenSize(0, 0, screenWidth, screenHeight - 140);
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

	private void resetScreenPosition() {
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			PositionData.setScreenPosition(265f * scaleFactor, 800f * scaleFactor, 0.73f * scaleFactor);
		} else {
			PositionData.setScreenPosition(500f * scaleFactor, 545f * scaleFactor, 0.73f * scaleFactor);
		}
	}
	
	public void setPositionToMoveNodeCenter() {
		PositionData.setScreenPosition(treeView.getWidth()/2, treeView.getHeight()/2, 1f * scaleFactor);
	}
}
