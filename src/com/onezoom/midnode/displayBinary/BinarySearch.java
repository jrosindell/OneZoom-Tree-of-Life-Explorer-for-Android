package com.onezoom.midnode.displayBinary;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import android.util.Log;
import android.widget.Toast;
import au.com.bytecode.opencsv.CSVReader;

import com.onezoom.CanvasActivity;
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
	
	public void performSearch(String userInput) {
		if (userInput.length() < 3) {
			Toast.makeText(client, "name too short", Toast.LENGTH_LONG).show();
		} else if (userInput.equals(previousSearch)) {
			currentHit = (currentHit + 1) % searchHit;
			processAndShowSearchResult();
		} else {
			searchResults.clear();
			previousSearch = userInput;
			String filename = CanvasActivity.selectedItem.toLowerCase(Locale.ENGLISH) + userInput.substring(0, 2).toLowerCase();
			int resourceID = client.getResources().getIdentifier(filename, "raw", client.getPackageName());
			InputStream is = client.getResources().openRawResource(resourceID);
			CSVReader reader = new CSVReader(new InputStreamReader(is));
			searchReader(reader, userInput.toLowerCase(Locale.ENGLISH));
			processAndShowSearchResult();
		}
	}
	
	private void processAndShowSearchResult() {
		if (searchHit > 0) {
			process(searchResults.get(currentHit));
			Toast.makeText(client, searchResult(currentHit, searchHit), Toast.LENGTH_LONG).show();	
		} else {
			Toast.makeText(client, "No Result", Toast.LENGTH_LONG).show();
		}
	}
	
	private String searchResult(int currentHit, int total) {
		if (currentHit == 0)
			return "The 1st hit, " + total + " hits in total";
		else if (currentHit == 1) 
			return "The 2nd hit, " + total + " hits in total";
		else if (currentHit == 2) 
			return "The 3rd hit, " + total + " hits in total";
		else
			return "The " + (currentHit+1) + "st hit, " + total + " hits in total";
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
//		PositionData.setScreenPosition(360, 500, 1f);
		client.setPositionToMoveNodeCenter();
		PositionData.moveNodeToCenter(searchedNode);
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

	private void searchReader(CSVReader reader, String userInput) {
		searchHit = 0;	
		currentHit = 0;
		try {
			String[] line;
			reader.readNext();
			while ((line = reader.readNext()) != null) {
				if (line[0].toLowerCase(Locale.ENGLISH).contains(userInput.toLowerCase())) {
					Record newRecord = new Record(line);
					if (!searchResults.contains(newRecord)) {
						if (newRecord.name.toLowerCase(Locale.ENGLISH).equals(userInput.toLowerCase())) {
							//exact match appears first
							searchResults.add(0, newRecord);
						} else {
							searchResults.add(newRecord);							
						}
						searchHit++;
					} else if (newRecord.name.toLowerCase(Locale.ENGLISH).equals(userInput.toLowerCase())) {
						//If node has already been added to list but not has the exact match name, then delete it and append 
						//the exact match at the initial position of the list.
						deletePreviousResult(searchResults, newRecord);
						searchResults.add(0, new Record(line));
					}
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

	private void deletePreviousResult(ArrayList<Record> searchResults,
			Record newRecord) {
		for (int i = 0; i < searchResults.size(); i++) {
			if (searchResults.get(i).equals(newRecord)) {
				searchResults.remove(i);
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

	@Override
	public boolean equals(Object another) {
		if (this == another) return true;
		if (another == null) return false;
		Record that = (Record)another;
		if (this.fileIndex == that.fileIndex
				&& this.index == that.index)
			return true;
		return false;
	}
}
