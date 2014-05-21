package com.onezoom.midnode;

//import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
			/**
			 * User input invalid, set previous search as user input and show result.
			 */
			previousSearch = userInput;
			showResult(0);
		} else if (userInput.equals(previousSearch)) {
			/**
			 * User input equals previousSearch, show next result.
			 */
			showResult(1);
		} else {
			searchResults.clear();
			previousSearch = userInput;
			
			/**
			 * Load the file that ends with the first two letters of user input.
			 * 
			 * Suppose user input 'homo', reader should load 'mammalsho'
			 */
			String filename = CanvasActivity.selectedItem.toLowerCase(Locale.ENGLISH) + userInput.substring(0, 2).toLowerCase();
			int resourceID = client.getResources().getIdentifier(filename, "raw", client.getPackageName());
			InputStream is = client.getResources().openRawResource(resourceID);
			CSVReader reader = new CSVReader(new InputStreamReader(is));
			
			/**
			 * search node in that file
			 */
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
	 * Then set link in case search is executed in web view. 
	 * Then reanchor search node and move it to the center of the screen.
	 * @param record
	 */
	private void process(Record record) {
		int key = Utility.combine(record.fileIndex, record.index);
		MidNode searchedNode = null;
		if (MidNode.getClient().getInitializer().fulltreeHash.containsKey(key)) {
			/**
			 * The node has already been initialized.
			 */
			searchedNode = MidNode.getClient().getInitializer().fulltreeHash.get(key);
		} else {
			/**
			 * The node has not been initialized. Initialize it and then get it.
			 */
			MidNode.getClient().getInitializer().initialiseSearchedFile(record.fileIndex);
			searchedNode = MidNode.getClient().getInitializer().fulltreeHash.get(key);
		}
		
		LinkHandler.setLink(searchedNode);
		
		/**
		 * Move the node to screen center.
		 */
		PositionCalculator.reanchorNode(searchedNode);
		client.moveRootToCenter();
		PositionData.moveNodeToCenter(searchedNode);
		
		client.treeView.setDuringInteraction(false);
		client.recalculate();
	}

	/**
	 * Search userInput through reader.
	 * 
	 * If new line has complete match of userInput and it has not been recorded yet,
	 * then insert the new record in the beginning of the result array. 
	 * 
	 * If it contains complete match of userInput and it has already been recorded,
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
				/**
				 * line[0] is the name of the node in the file.
				 */
				if (line[0].toLowerCase(Locale.ENGLISH).contains(userInput.toLowerCase(Locale.ENGLISH))) {
					Record newRecord = new Record(line);
					System.out.println("line[0] -> " + line[0] + "*****");
					if (!searchResults.contains(newRecord)) {
						//this line has not been recorded yet.
						if (newRecord.equalUserInput(userInput)) {
							/**
							 * record line equals user input
							 */
							newRecord.setPriority(1);
						} else if (newRecord.containsWord(userInput)) {
							/**
							 * record line contains user input
							 */
							newRecord.setPriority(2);
						} 
						searchResults.add(newRecord);
						searchHit++;
						
					} else if (newRecord.equalUserInput(userInput)) {
						deletePreviousResult(searchResults, newRecord);
						newRecord.setPriority(1);
						searchResults.add(newRecord);
					} else if (newRecord.containsWord(userInput)){	
						/**
						 * If the node represented by this line has already been added to list and 
						 * the new line has exact match of userInput,
						 * then delete the node which was added via a previous line and 
						 * push the new record into the head of the array.
						 */			
						deletePreviousResult(searchResults, newRecord);
						newRecord.setPriority(2);
						searchResults.add(newRecord);
					}
				}
			}
			Collections.sort(searchResults, new SearchResultComparator());
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
	 * If both link name, which contains latin name of a node and cname does not contains the previous
	 * search key word, then reset previousSearch to empty.
	 * 
	 * This function is called when a wiki link or arkive link is clicked. 
	 * 
	 * If the node being clicked is not the one being searched, then reset search result.
	 * @param link
	 * @param cname
	 */
	public void resetSearch(String link, String cname) {
		if (!link.toLowerCase(Locale.ENGLISH).contains(previousSearch.toLowerCase(Locale.ENGLISH))
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
	int priority;
	
	public Record(String[] line) {
		assert line.length == 4;
		name = line[0];
		fileIndex = Integer.parseInt(line[1]);
		index = Integer.parseInt(line[2]);
		childIndex = Integer.parseInt(line[3]);
		priority = 3;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public void setPriority(int i) {
		priority = i;
	}

	public boolean equalUserInput(String userInput) {
		if (name.toLowerCase(Locale.ENGLISH).equals(userInput.toLowerCase(Locale.ENGLISH)))
			return true;
		else
			return false;
	}
	
	/**
	 * Test if name has complete match of user input.
	 * @param userInput
	 * @return
	 */
	public boolean containsWord(String userInput) {
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

class SearchResultComparator implements Comparator<Record> {
	@Override
	public int compare(Record left, Record right) {
		if (left.getPriority() < right.getPriority()) return -1;
		else if (left.getPriority() > right.getPriority()) return 1;
		else return 0;
	}
}
