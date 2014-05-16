package com.onezoom;

import java.util.ArrayList;


import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * This class is the activity which starts when the user enters the app.
 * 
 * In this activity, users could select from one of the trees provided and the selected tree
 * will be stored in com.onezoom.selectedTree buffer and a CanvasActivity will be started after that.
 *
 */
public class MainActivity extends Activity {
	MainActivity thisOne;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		/**
		 * create a list view and add four trees in the list.
		 */
		final ListView listview = (ListView) findViewById(R.id.listview);
	    String[] values = new String[] { "Mammals", "Birds", "Amphibian", "Tetrapods"};
	    final ArrayList<String> list = new ArrayList<String>();
	    for (int i = 0; i < values.length; ++i) {
	      list.add(values[i]);
	    }
	    
	    final ListAdapter adapter = new ArrayAdapter<String>(this,
	            android.R.layout.simple_list_item_1, list);
	    listview.setAdapter(adapter);
	    
	    thisOne = this;
	    
	    /**
	     * Define click event.
	     * When user click on one of the tree, it will store the tree being selected in com.onezoom.selectedTree
		 * buffer and start CanvasActivity class.
	     */
	    listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	    	@Override
	    	public void onItemClick (AdapterView<?> parent, View view, int position, long id){
	    		Intent intent = new Intent(thisOne, CanvasActivity.class);
	    		String selectedItem = (String) listview.getAdapter().getItem(position);
	    		intent.putExtra("com.onezoom.selectedTree", selectedItem);
	    		startActivity(intent);
	    	}	    	
		});	    
	}

	/**
	 * Automatically called when activity starts.
	 * 
	 * This function will inflate action bar from resources.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	
	
}
