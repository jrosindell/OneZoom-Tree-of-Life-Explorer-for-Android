package com.onezoom.midnode;

import java.util.Comparator;
import java.util.Locale;

public class SearchRecord {
	String name;
	int fileIndex;
	int index;
	int childIndex;
	int priority;
	public static final SearchResultComparator comparator = new SearchResultComparator();
	
	public SearchRecord(String[] line) {
		name = line[0];
		fileIndex = Integer.parseInt(line[1]);
		index = Integer.parseInt(line[2]);
		childIndex = Integer.parseInt(line[3]);
		priority = 3;
	}
	
	public SearchRecord(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
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
		SearchRecord that = (SearchRecord)another;
		if (this.fileIndex == that.fileIndex
				&& this.index == that.index)
			return true;
		return false;
	}

	public void setPriorityByQuery(String query) {
		if (this.equalUserInput(query)) {
			this.setPriority(1);
		} else if (this.containsWord(query) && this.name.toLowerCase().startsWith(query.toLowerCase())) {
			this.setPriority(2);
		} else if (this.containsWord(query)) {
			this.setPriority(3);
		} else if (this.name.toLowerCase().startsWith(query.toLowerCase())) {
			this.setPriority(4);
		} else {
			this.setPriority(5);
		}
	}
}

class SearchResultComparator implements Comparator<SearchRecord> {
	@Override
	public int compare(SearchRecord left, SearchRecord right) {
		if (left.getPriority() < right.getPriority()) return -1;
		else if (left.getPriority() > right.getPriority()) return 1;
		else return 0;
	}
}