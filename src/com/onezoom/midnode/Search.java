package com.onezoom.midnode;

//import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;
import au.com.bytecode.opencsv.CSVReader;

import com.onezoom.CanvasActivity;

public class Search {
	private static Search instance = null;
	CanvasActivity client;
	String previousSearch = "";
	int searchHit;
	int currentHit;
	ArrayList<Record> searchResults;
	
	/**
	 * The class is a singleton class.
	 * Outside class should call getInstance to create or get a instance of it.
	 * @param canvasActivity
	 */
	private Search(CanvasActivity canvasActivity) {
		client = canvasActivity;
		searchResults = new ArrayList<Record>();
	}
	
	public static Search getInstance(CanvasActivity canvasActivity) {
		if (instance == null) {
			instance = new Search(canvasActivity);
		}
		return instance;
	}
	
	public String getPreviousSearch() {
		return previousSearch;
	}

	public static void destory() {
		instance = null;
	}
	
	/**
	 * If user input equals previous search, then acts the same as performForwardSearch
	 *
	 * If user input is different and valid, 
	 * Then search through files under raw folder and then 
	 * get the node and show the result by calling processAndShowSearchResult.
	 * @param userInput
	 */
	public void performSearch(String userInput) {
		if (userInput.length() < 3) {
			showResult(0);
		} else if (userInput.equals(previousSearch)) {
			showResult(1);
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
	
	public void performBackSearch() {
		showResult(-1);
	}

	public void performForwardSearch() {
		showResult(1);
	}
	
	private void showResult(int indexChange) {
		if (previousSearch == null || previousSearch.length() == 0) {
			client.showToast("You have to enter a name");
		} else if (previousSearch.length() < 3) {
			client.showToast("Name should contain at least 3 characters");
		} else if (searchHit > 0){
			currentHit = (currentHit + indexChange + searchHit) % searchHit;
			processAndShowSearchResult();
		} else if (searchHit == 0) {
			client.showToast("Sorry, no result has been found.");
		}
	}
	
	/**
	 * searchHit equals 0 means the node cannot be found in the tree.
	 */
	private void processAndShowSearchResult() {
		if (searchHit > 0) {
			process(searchResults.get(currentHit));
			client.showToast(searchResult(currentHit, searchHit));
		} else {
			client.showToast("Sorry, no result has been found");
		}
	}
	
	/**
	 * Returns a string showing how many search in total and the current search index.
	 * @param currentHit
	 * @param total
	 * @return
	 */
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

	/**
	 * If the node has been initialized, then return it directly, 
	 * otherwise initialize the file that contains the node.
	 * 
	 * Then set wiki link in case search is executed in web view. 
	 * Then reanchor search node and move it to the center of the screen.
	 * @param record
	 */
	private void process(Record record) {
		int key = Utility.combine(record.fileIndex, record.index);
		MidNode searchedNode = null;
		if (MidNode.initializer.fulltreeHash.containsKey(key)) {
			searchedNode = MidNode.initializer.fulltreeHash.get(key);
		} else {
			MidNode.initializer.initialiseSearchedFile(record.fileIndex);
			searchedNode = MidNode.initializer.fulltreeHash.get(key);
		}
		
		LinkHandler.setWikiLink(searchedNode);
		PositionCalculator.reanchorNode(searchedNode);
		client.moveRootToCenter();
		PositionData.moveNodeToCenter(searchedNode);
		client.treeView.setDuringInteraction(false);
		client.recalculate();
	}

	/**
	 * Search userInput through reader.
	 * If new line has complete match of userInput, 
	 * then insert the new record in the beginning of the result array. 
	 * If this node has already been recorded and it contains complete match of userInput,
	 * then delete previous record and insert new one in front of the array.
	 * @param reader
	 * @param userInput
	 */
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
						if (newRecord.contains(userInput)) {
							//exact match appears first
							searchResults.add(0, newRecord);
						} else {
							searchResults.add(newRecord);					
						}
						searchHit++;
					} else if (newRecord.contains(userInput)){
						//If node has already been added to list but not has the exact match name, then delete it and append 
						//the exact match at the initial position of the list.
						deletePreviousResult(searchResults, newRecord);
						searchResults.add(0, new Record(line));
					}
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}
	

	/**
	 * delete repetiveRecord in search results array.
	 * @param searchResults
	 * @param repetiveRecord
	 */
	private void deletePreviousResult(ArrayList<Record> searchResults,
			Record repetiveRecord) {
		for (int i = 0; i < searchResults.size(); i++) {
			if (searchResults.get(i).equals(repetiveRecord)) {
				searchResults.remove(i);
			}
		}
	}

	/**
	 * If both wiki name, which contains latin name of a node and cname does not contains the previous
	 * search key word, then reset previousSearch to empty.
	 * @param wikilink
	 * @param cname
	 */
	public void resetSearch(String wikilink, String cname) {
		if (!wikilink.toLowerCase(Locale.ENGLISH).contains(previousSearch.toLowerCase(Locale.ENGLISH))
				&& !cname.toLowerCase(Locale.ENGLISH).contains(previousSearch.toLowerCase(Locale.ENGLISH))) {
			this.previousSearch = "";
		}
	}
}

class Record {
	String name;
	int fileIndex;
	int index;
	int childIndex;
	
	public Record(String[] line) {
		assert line.length == 4;
		name = line[0];
		fileIndex = Integer.parseInt(line[1]);
		index = Integer.parseInt(line[2]);
		childIndex = Integer.parseInt(line[3]);
	}
	
	/**
	 * Test if name has complete match of user input.
	 * @param userInput
	 * @return
	 */
	public boolean contains(String userInput) {
		String[] names = name.split(" ");
		for (int i = 0; i < names.length; i++) {
			if (names[i].toLowerCase(Locale.ENGLISH).equals(userInput.toLowerCase(Locale.ENGLISH)))
				return true;
		}
		return false;
	}

	/**
	 * Test if two records equals. 
	 * A same node maybe recorded in different files since it has different latin name and common name.
	 * For example, "South China Field Mouse" appears in file 'mammalsch' 
	 * and it also appears in file 'mammalsap' because it has latin name 'apodemus drawco'.
	 */
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
