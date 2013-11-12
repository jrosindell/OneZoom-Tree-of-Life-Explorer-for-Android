package com.onezoom;

import com.onezoom.midnode.MidNode;
import com.onezoom.midnode.PositionData;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;

public class CanvasActivity extends Activity{
	TreeView treeView;
	String selectedItem;
	private String selectedString;
	private MidNode fulltree;
	private boolean started = false;
	private MemoryThread memoryThread;
	private GrowthThread growthThread;
	private boolean threadStarted = false;
	private boolean growing = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.canvas_activity);
		treeView = (TreeView) findViewById(R.id.tree_view);	
		memoryThread = new MemoryThread(this);
		growthThread = new GrowthThread(this);
		retrieveData();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getActionBar().setDisplayHomeAsUpEnabled(false);
		if (growing) inflateGrowMenu(menu);
		else inflateSearchMenu(menu);    
		return true;
	}
	
	
	@Override
	protected void onStart() {
		super.onStart();
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
		fulltree = MidNode.createNode(null, selectedString, false, 0);
		fulltree.init();
		MidNode.setScreenSize(0, 0, 800, 1200);
		fulltree.recalculate(200, 900, 1f);
		treeView.setTreeBeingInitialized(true);		
	}
	
	public void recalculate() {
		memoryThread.recalculate();
	}
	
	/**
	 * Load Data from data class to a string called selectedstring.
	 */
	private void retrieveData() {
		selectedItem = getIntent().getExtras().getString(
				"com.onezoom.selectedTree");

		if (selectedItem.equals("Mammals")) {
			selectedString = Data.newMammalsString;
		} else if (selectedItem.equals("Birds")) {
			selectedString = Data.newBirdsString;
		} else if (selectedItem.equals("Amphibian")) {
			selectedString = Data.newApibiansString;
		} else if (selectedItem.equals("Snakes")) {
			selectedString = Data.newSnakeString;
		} else if (selectedItem.equals("Turtles")) {
			selectedString = Data.newTurtleString;
		} else if (selectedItem.equals("Cross Species")) {
			selectedItem = "CrossSpecies";
			selectedString = Data.newCrocsString;
		} else if (selectedItem.equals("Squamates")) {
			selectedString = Data.newSquamatesString;
		} else if (selectedItem.equals("Tetrapods")) {
			selectedString = Data.newTetrapodsString;
		}		
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

	private void resetTree() {
		treeView.setDuringInteraction(false);
		PositionData.setScreenPosition(200, 900, 1);
		this.recalculate();
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
		searchMenuItem.expandActionView();
		searchView.setQueryHint("Enter Species Name");
		searchView.clearFocus();
		
		searchView.setOnQueryTextListener(new OnQueryTextListener() {		
			@Override
			public boolean onQueryTextSubmit(String arg0) {
				searchView.setQueryHint("Enter Species Name");
				searchView.setQuery("", false);
				searchView.clearFocus();
//				BasicNode.searchEngine.performSearch(DrawCanvas.this);
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
}
