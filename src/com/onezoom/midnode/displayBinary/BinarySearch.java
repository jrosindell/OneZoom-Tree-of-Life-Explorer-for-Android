package com.onezoom.midnode.displayBinary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import android.util.Log;
import android.widget.Toast;
import au.com.bytecode.opencsv.CSVReader;

import com.onezoom.CanvasActivity;
import com.onezoom.TreeView;
import com.onezoom.midnode.MidNode;
import com.onezoom.midnode.PositionData;
import com.onezoom.midnode.Utility;

public class BinarySearch {
	CanvasActivity client;
	String previousSearch;
	int searchHit;
	int currentHit;
	ArrayList<Record> searchResults;
	
	public BinarySearch(CanvasActivity canvasActivity) {
		client = canvasActivity;
		searchResults = new ArrayList<Record>();
	}
	
	public void performSearch(String arg) {
		if (arg.length() < 3) {
			Toast.makeText(client, "name too short", Toast.LENGTH_LONG).show();
		} else if (arg.equals(previousSearch)) {
			currentHit = (currentHit + 1) % searchHit;
			process(searchResults.get(currentHit));
		} else {
			searchResults.clear();
			previousSearch = arg;
			String filename = client.selectedItem.toLowerCase() + arg.substring(0, 2);
			File file = new File(filename);
			int resourceID = client.getResources().getIdentifier(filename, "raw", client.getPackageName());
			InputStream is = client.getResources().openRawResource(resourceID);
			CSVReader reader = new CSVReader(new InputStreamReader(is));
			searchReader(reader, arg.toLowerCase());
			if (searchHit > 0) {
				process(searchResults.get(currentHit));
			}
		}
	}

	private void process(Record record) {
		int key = Utility.combine(record.fileIndex, record.index);
		MidNode searchedNode = null;
		if (MidNode.initializer.fulltreeHash.containsKey(key)) {
			searchedNode = MidNode.initializer.fulltreeHash.get(key);
		} else {
			MidNode.initializer.initialiseSearchedFile(record.fileIndex);
			searchedNode = MidNode.initializer.fulltreeHash.get(key);
		}
		reanchorNode(searchedNode, 0);
		PositionData.setScreenPosition(360, 500, 1f);
		client.treeView.setDuringInteraction(false);
		client.recalculate();
	}

	private void reanchorNode(MidNode searchedNode, int deanchorWhichChild) {
		searchedNode.positionData.graphref = true;
		if (searchedNode.child1 != null && deanchorWhichChild == 0) {
			deanchor(searchedNode.child1);
			deanchor(searchedNode.child2);
		} else if (searchedNode.child2 != null && deanchorWhichChild == 1) {
			deanchor(searchedNode.child2);
		} else if (searchedNode.child1 != null && deanchorWhichChild == 2) {
			deanchor(searchedNode.child1);
		}		
		if (searchedNode.parent != null) {
			reanchorNode(searchedNode.parent, searchedNode.childIndex);
		}
	}
	

	private void deanchor(MidNode midNode) {
		midNode.positionData.graphref = false;
	}

	private void searchReader(CSVReader reader, String arg) {
		searchHit = 0;	
		currentHit = 0;
		try {
			String[] line;
			reader.readNext();
			while ((line = reader.readNext()) != null) {
				if (line[0].toLowerCase().equals(arg)) {
					searchHit++;
					searchResults.add(new Record(line));
				}
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}	
	}
}

class Record {
	String name;
	int fileIndex;
	int index;
	int childIndex;
	String string;
	public Record(String[] line) {
		string = StringUtils.join(line, " ");
		assert line.length == 4;
		name = line[0];
		fileIndex = Integer.parseInt(line[1]);
		index = Integer.parseInt(line[2]);
		childIndex = Integer.parseInt(line[3]);
	}
	
	public String toString() {
		return string;
	}
}
