package com.onezoom;

import com.onezoom.midnode.MidNode;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class CanvasActivity extends Activity{
	TreeView treeView;
	private String selectedItem, selectedString;
	private MidNode fulltree;
	private boolean started = false;
	private MemoryThread thread;
	private boolean threadStarted = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.canvas_activity);
		treeView = (TreeView) findViewById(R.id.tree_view);	
		thread = new MemoryThread(this);
		retrieveData();
	}

	@Override
	protected void onStart() {
		super.onStart();
//		
//		//only start the activity once
		if (started) return;
		else started = true;
		
		fulltree = MidNode.createNode(null, selectedString, false, 0);
		fulltree.init();
		MidNode.setScreenSize(0, 0, 800, 1200);
		fulltree.recalculate(200, 900, 1f);
		
		if (!threadStarted) {
			threadStarted = true;
			thread.start();
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		thread.requestStop();
	}


	public MidNode getTreeRoot() {
		return fulltree;
	}
	
	public void recalculate() {
		thread.recalculate();
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

}
