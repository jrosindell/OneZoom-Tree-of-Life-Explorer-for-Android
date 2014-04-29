package com.onezoom.midnode.displayBinary;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Stack;

import junit.framework.Assert;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.Pair;
import au.com.bytecode.opencsv.CSVReader;

import com.onezoom.CanvasActivity;
import com.onezoom.midnode.InteriorNode;
import com.onezoom.midnode.LeafNode;
import com.onezoom.midnode.MidNode;
import com.onezoom.midnode.PositionData;
import com.onezoom.midnode.Utility;

public class BinaryInitializer {
	public Hashtable<Integer, MidNode> fulltreeHash;
	Hashtable<Integer, InteriorNode> interiorHash;
	Hashtable<Integer, LeafNode> leafHash;
	static Hashtable<Integer, Integer> fileConnection;
	LinkedList<Pair<Integer, MidNode>> listOfHeadNodeInNextFile;
//	public ArrayList<Integer> initialisedFile;
	public PriorityQueue<MidNode> stackOfNodeHasNonInitChildren;
	public static CanvasActivity canvasActivity;
	boolean dynamic = false;
	public static int fileIndex;

	public BinaryInitializer() {
		fileConnection = new Hashtable<Integer, Integer>(1000);
		fulltreeHash = new Hashtable<Integer, MidNode>(20000);
		interiorHash = new Hashtable<Integer, InteriorNode>(1000);
		leafHash = new Hashtable<Integer, LeafNode>(1000);
		listOfHeadNodeInNextFile = new LinkedList<Pair<Integer,MidNode>>();
		stackOfNodeHasNonInitChildren = new PriorityQueue<MidNode>();
	}

	public static void setContext(CanvasActivity context) {
		canvasActivity = context;
		fillFileConnection();
	}
	
	public MidNode createMidNode(String fileIndex) {
		listOfHeadNodeInNextFile.clear();
		fulltreeHash.clear();
//		initialisedFile.clear();
		return createTreeStartFromFileIndex(fileIndex, 0, null);
	}
	
	public MidNode createTreeStartFromTailNode(int childIndex, MidNode midnode) {
		int[] infos;
		if (childIndex == 1) {
			infos = findFileAndIndexInfo(midnode.child1Index);
		} else {
			infos = findFileAndIndexInfo(midnode.child2Index);
		}
		int fileIndex = infos[1];
		String selectedGroup = CanvasActivity.selectedItem.toLowerCase(Locale.ENGLISH);
		return createNodesInOneFile(canvasActivity, selectedGroup, Integer.toString(fileIndex), childIndex, midnode);
	}

	public MidNode createTreeStartFromFileIndex(String fileIndex, int childIndex, MidNode parent) {
		String selectedGroup = CanvasActivity.selectedItem.toLowerCase(Locale.ENGLISH);
		MidNode fulltree = createNodesInOneFile(canvasActivity, selectedGroup, fileIndex, childIndex, parent);
		while (!listOfHeadNodeInNextFile.isEmpty()) {
			Pair<Integer, MidNode> pair = listOfHeadNodeInNextFile.poll();
			if (pair.first.equals(1)) {	
				int[] infos = findFileAndIndexInfo(pair.second.child1Index);
				if (pair.second.positionData.dvar)
					pair.second.child1 = createNodesInOneFile(canvasActivity, selectedGroup, Integer.toString(infos[1]), 1, pair.second);
				else {
					Assert.assertEquals(pair.second.child1Index < 0, true);
					Assert.assertNull(pair.second.child1);
					stackOfNodeHasNonInitChildren.add(pair.second);
				}
			} else {
				Assert.assertEquals(pair.first, Integer.valueOf(2));
				int[] infos = findFileAndIndexInfo(pair.second.child2Index);
				
				if (pair.second.positionData.dvar)
					pair.second.child2 = createNodesInOneFile(canvasActivity, selectedGroup, Integer.toString(infos[1]), 2, pair.second);
				else {
					Assert.assertEquals(pair.second.child2Index < 0, true);
					Assert.assertNull(pair.second.child2);
					stackOfNodeHasNonInitChildren.add(pair.second);
				}
			}	
		}
		return fulltree;
	}
	
	
	private MidNode createNodesInOneFile(Context canvasActivity,
			String selectedGroup, String fileIndex, int childIndex, MidNode parentNode) {
		BinaryInitializer.fileIndex = Integer.parseInt(fileIndex);
		int resourceInteriorID = canvasActivity.getResources().getIdentifier(selectedGroup + "interior" + fileIndex, "raw", canvasActivity.getPackageName());
		InputStream isInterior = canvasActivity.getResources().openRawResource(resourceInteriorID);
		int resourceLeafID = canvasActivity.getResources().getIdentifier(selectedGroup + "leaf" + fileIndex, "raw", canvasActivity.getPackageName());
		InputStream isLeaf = canvasActivity.getResources().openRawResource(resourceLeafID);
		CSVReader readerInterior = new CSVReader(new InputStreamReader(isInterior));
		CSVReader readerLeaf = new CSVReader(new InputStreamReader(isLeaf));
		interiorHash.clear();
		leafHash.clear();
		try {
			readerInterior.readNext();
			readerLeaf.readNext();
			InteriorNode interiorNode;
			LeafNode leafNode;
			String[] nextline;
			//create all interior node in interior file and put them into hash table
			while ((nextline = readerInterior.readNext()) != null) {
				interiorNode = createInteriorNode(nextline);
				interiorNode.fileIndex = BinaryInitializer.fileIndex;
				interiorHash.put(interiorNode.index, interiorNode);
				fulltreeHash.put(Utility.combine(interiorNode.fileIndex, interiorNode.index), interiorNode);
			}
			
			//create all leaf node in leaf file and put them into hash table
			while ((nextline = readerLeaf.readNext()) != null) {
				leafNode = createLeafNode(nextline);
				leafNode.fileIndex = BinaryInitializer.fileIndex;
				leafHash.put(leafNode.index, leafNode);
				fulltreeHash.put(Utility.combine(leafNode.fileIndex, leafNode.index), leafNode);
			}						
			readerInterior.close();
			readerLeaf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		MidNode interNode = interiorHash.get(0);
		interNode.childIndex = childIndex;
		interNode.parent = parentNode;
		buildConnection(interNode);
		precalculateThisChunk(interNode);
		return interNode;
	}

	public boolean isDynamic() {
		return dynamic;
	}

	public void setDynamic(boolean dynamic) {
		this.dynamic = dynamic;
	}
	
	private void precalculateThisChunk(MidNode interNode) {
		MidNode.precalculator.preCalcWholeTree(interNode);

		if (!dynamic) {
			if (interNode.parent == null) {
				interNode.recalculate(PositionData.xp, PositionData.yp,
						PositionData.ws);
			} else {
				PositionData positionData = interNode.parent.positionData;
				if (interNode.childIndex == 1) {
					interNode.recalculate(positionData.xvar + positionData.rvar
							* positionData.nextx1, positionData.yvar
							+ positionData.rvar * positionData.nexty1,
							positionData.rvar * positionData.nextr1 / 220);
				} else {
					interNode.recalculate(positionData.xvar + positionData.rvar
							* positionData.nextx2, positionData.yvar
							+ positionData.rvar * positionData.nexty2,
							positionData.rvar * positionData.nextr2 / 220);
				}
			}
		}
	}

	private void buildConnection(MidNode interiorNode) {
		Assert.assertEquals(interiorNode.getClass(), InteriorNode.class);
		
		int child1Id = interiorNode.child1Index;
		int child2Id = interiorNode.child2Index;

		if (child1Id < 0) {
			listOfHeadNodeInNextFile.add(new Pair<Integer, MidNode>(1, interiorNode));
		} else if (interiorHash.containsKey(child1Id)) {
			MidNode child1 = interiorHash.get(child1Id);
			interiorNode.child1 = child1;
			child1.parent = interiorNode;
			child1.childIndex = 1;
			buildConnection(interiorNode.child1);
		} else {
			MidNode child1 = leafHash.get(child1Id);
			if (child1 == null) {
				Log.d("debug", "size child 1 id -> " + child1Id);
				Log.d("debug", "size of leaf hash -> " + leafHash.toString());
			}
			Assert.assertNotNull(child1);
			interiorNode.child1 = child1;
			child1.parent = interiorNode;
			child1.childIndex = 1;
		}
		
		if (child2Id < 0) {
			listOfHeadNodeInNextFile.add(new Pair<Integer, MidNode>(2, interiorNode));
		} else if (interiorHash.containsKey(child2Id)) {
			MidNode child2 = interiorHash.get(child2Id);
			interiorNode.child2 = child2;
			child2.parent = interiorNode;
			child2.childIndex = 2;
			buildConnection(interiorNode.child2);
		} else {
			MidNode child2 = leafHash.get(child2Id);
			if (child2 == null) {
				Log.d("debug", "size child 2 id -> " + child2Id);
				Log.d("debug", "size of leaf hash -> " + leafHash.size());
			}
			Assert.assertNotNull(child2);
			interiorNode.child2 = child2;
			child2.parent = interiorNode;
			child2.childIndex = 2;
		}
	}

	private int[] findFileAndIndexInfo(int child1Id) {
		int [] infos = new int[2];
		infos[0] = 0;
		infos[1] = child1Id >> 10;
		infos[1] = infos[1] & 0x03FFFF;
		return infos;
	}

	private LeafNode createLeafNode(String[] nextline) {
		return new LeafNode(nextline);
	}

	private InteriorNode createInteriorNode(String[] nextline) {
		return new InteriorNode(nextline);
	}
	

	public MidNode createFollowingOneFile(MidNode midnode, int childIndex) {
		String selectedGroup = CanvasActivity.selectedItem.toLowerCase(Locale.ENGLISH);
		int[] infos;
		if (childIndex == 1) {
			infos = findFileAndIndexInfo(midnode.child1Index);
		} else {
			infos = findFileAndIndexInfo(midnode.child2Index);
		}
		String fileIndex = Integer.toString(infos[1]);
		return createNodesInOneFile(canvasActivity, selectedGroup, fileIndex, childIndex, midnode);
	}
	
	public static int getColor() {
		if (fileIndex % 5 == 0) {
			return Color.argb(255, 255, 0, 0);
		} else if (fileIndex % 5 == 1) {
			return Color.argb(255, 255, 255, 0);
		} else if (fileIndex % 5 == 2) {
			return Color.argb(255, 0, 255, 0);
		} else if (fileIndex % 5 == 3) {
			return Color.argb(255, 0, 255, 255);
		} else {
			return Color.argb(255, 0, 0, 255);
		}
	}

	public void idleTimeInitialization() {
		if (!stackOfNodeHasNonInitChildren.isEmpty()) {
			MidNode interNode = stackOfNodeHasNonInitChildren.poll();
			//internode might have been deleted...
			if (interNode == null) return;
			if (interNode.child1 == null) {
				Assert.assertEquals(interNode.child1Index < 0, true);
				interNode.child1 = createTreeStartFromTailNode(1, interNode);
			} else if (interNode.child2 == null){
				Assert.assertNull(interNode.child2);
				Assert.assertEquals(interNode.child2Index < 0, true);
				interNode.child2 = createTreeStartFromTailNode(2, interNode);
			}
			listOfHeadNodeInNextFile.clear();
		}
	}

	public void initialiseSearchedFile(int fileIndex) {
		Stack<Integer> filesNeedToBeInit = new Stack<Integer>();
		MidNode searchroot = findAllFileNeedToBeInit(fileIndex, filesNeedToBeInit);
		initFilesInStack(searchroot, filesNeedToBeInit);
	}

	private MidNode findAllFileNeedToBeInit(int fileIndex,
			Stack<Integer> filesNeedToBeInit) {
		filesNeedToBeInit.add(fileIndex);
		int parentFileIndex = fileConnection.get(fileIndex);
		if (fulltreeHash.containsKey(Utility.combine(parentFileIndex, 0))) {
			return fulltreeHash.get(Utility.combine(parentFileIndex, 0));
		} else {
			return findAllFileNeedToBeInit(parentFileIndex, filesNeedToBeInit);
		}
	}

	
	private void initFilesInStack(MidNode searchroot,
			Stack<Integer> filesNeedToBeInit) {
		Assert.assertNotNull(searchroot);
		while (!filesNeedToBeInit.isEmpty()) {
			searchroot = initNewFile(searchroot, filesNeedToBeInit.pop());
		}
	}
	
	
	private MidNode initNewFile(MidNode searchroot, Integer fileIndex) {
		MidNode search1 = null;
		MidNode search2 = null;
		if (searchroot.child1 == null && searchroot.child1Index < 0
				&& findFileAndIndexInfo(searchroot.child1Index)[1] == fileIndex) {
			searchroot.child1 = createFollowingOneFile(searchroot, 1);
			return searchroot.child1;
		} else if (searchroot.child2 == null && searchroot.child2Index < 0
				&& findFileAndIndexInfo(searchroot.child2Index)[1] == fileIndex) {
			searchroot.child2 = createFollowingOneFile(searchroot, 2);
			return searchroot.child2;
		} else if (searchroot.child1 != null) {
			search1 = initNewFile(searchroot.child1, fileIndex);
		} else if (searchroot.child2 != null) {
			search2 = initNewFile(searchroot.child2, fileIndex);
		} else {
			return null;
		}
		if (search1 != null) return search1;
		else return search2;
	}

	private static void fillFileConnection() {
		fileConnection.clear();
		String selectedGroup = CanvasActivity.selectedItem.toLowerCase(Locale.ENGLISH);
		int resourceID = canvasActivity.getResources().getIdentifier(selectedGroup + "search", "raw", canvasActivity.getPackageName());
		InputStream is = canvasActivity.getResources().openRawResource(resourceID);
		CSVReader reader = new CSVReader(new InputStreamReader(is));
		try {
			reader.readNext();
			String[] nextline;
			while ((nextline = reader.readNext()) != null) {
				fileConnection.put(Integer.parseInt(nextline[0]), Integer.parseInt(nextline[1]));
			}							
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

//private void precalculateOneNode(MidNode interNode) {
//	MidNode.precalculator.precalcOneNode(interNode);
//	if (interNode.parent == null) {
//		interNode.positionData.xvar = PositionData.xp;
//		interNode.positionData.yvar = PositionData.yp;
//		interNode.positionData.rvar = PositionData.ws * 220;
//	} else {
//		PositionData positionData = interNode.parent.positionData;
//		if (interNode.childIndex == 1) {
//			interNode.positionData.xvar = positionData.xvar + positionData.rvar
//					* positionData.nextx1;
//			interNode.positionData.yvar = positionData.yvar
//					+ positionData.rvar * positionData.nexty1;
//			interNode.positionData.rvar = positionData.rvar * positionData.nextr1;
//		} else {
//			interNode.positionData.xvar = positionData.xvar + positionData.rvar
//					* positionData.nextx2;
//			interNode.positionData.yvar = positionData.yvar
//					+ positionData.rvar * positionData.nexty2;
//			interNode.positionData.rvar = positionData.rvar * positionData.nextr2;
//		}
//	}
//	interNode.positionData.dvar = interNode.positionData.horizonInsideScreen() && interNode.positionData.nodeBigEnoughToDisplay();
//	interNode.positionData.gvar = interNode.positionData.nodeInsideScreen() && interNode.positionData.nodeBigEnoughToDisplay();
//}
//private void processNode(MidNode interNode) {		
//int child1Index = interNode.child1Index;
//if (child1Index < 0) {
//	listOfHeadNodeInNextFile.add(new Pair<Integer, MidNode>(1, interNode));
//} else if (interiorRawStringHashtable.containsKey(child1Index)) {
//	interNode.child1 = createInteriorNode(interiorRawStringHashtable.get(child1Index));
//	interNode.child1.parent = interNode;
//	interNode.child1.childIndex = 1;
//	precalculateOneNode(interNode.child1);	
//	processNode(interNode.child1);
//} else {
//	interNode.child1 = createLeafNode(leafRawStringHashtable.get(child1Index));
//	interNode.child1.parent = interNode;
//	interNode.child1.childIndex = 1;
//	precalculateOneNode(interNode.child1);
//}
//
//int child2Index = interNode.child2Index;
//if (child2Index < 0) {
//	listOfHeadNodeInNextFile.add(new Pair<Integer, MidNode>(2, interNode));
//} else if (interiorRawStringHashtable.containsKey(child2Index)) {
//	interNode.child2 = createInteriorNode(interiorRawStringHashtable.get(child2Index));
//	interNode.child2.parent = interNode;
//	interNode.child2.childIndex = 2;
//	precalculateOneNode(interNode.child2);
//	processNode(interNode.child2);
//} else {
//	interNode.child2 = createLeafNode(leafRawStringHashtable.get(child2Index));
//	interNode.child2.parent = interNode;
//	interNode.child2.childIndex = 2;
//	precalculateOneNode(interNode.child2);
//}
//}

//public MidNode createTreeStopTillDvarFalse(String fileIndex, int childIndex, MidNode parent) {
//String selectedGroup = CanvasActivity.selectedItem.toLowerCase(Locale.ENGLISH);
//MidNode fulltree = createNodesInOneFileTillDvarFalse(canvasActivity, selectedGroup, fileIndex, childIndex, parent);
//while (!listOfHeadNodeInNextFile.isEmpty()) {
//	Pair<Integer, MidNode> pair = listOfHeadNodeInNextFile.poll();
//	if (pair.first.equals(1)) {
//		int[] infos = findFileAndIndexInfo(pair.second.child1Index);
//		if (pair.second.positionData.dvar)
//			pair.second.child1 = createNodesInOneFileTillDvarFalse(canvasActivity, selectedGroup, Integer.toString(infos[1]), 1, pair.second);
//	} else {
//		Assert.assertEquals(pair.first, Integer.valueOf(2));
//		int[] infos = findFileAndIndexInfo(pair.second.child2Index);
//		if (pair.second.positionData.dvar)
//			pair.second.child2 = createNodesInOneFileTillDvarFalse(canvasActivity, selectedGroup, Integer.toString(infos[1]), 2, pair.second);
//	}	
//}
//return fulltree;
//}

//private MidNode createNodesInOneFileTillDvarFalse(Context canvasActivity,
//String selectedGroup, String fileIndex, int childIndex,
//MidNode parentNode) {
//BinaryInitializer.fileIndex = Integer.parseInt(fileIndex);
//int resourceInteriorID = canvasActivity.getResources().getIdentifier(selectedGroup + "interior" + fileIndex, "raw", canvasActivity.getPackageName());
//InputStream isInterior = canvasActivity.getResources().openRawResource(resourceInteriorID);
//int resourceLeafID = canvasActivity.getResources().getIdentifier(selectedGroup + "leaf" + fileIndex, "raw", canvasActivity.getPackageName());
//InputStream isLeaf = canvasActivity.getResources().openRawResource(resourceLeafID);
//CSVReader readerInterior = new CSVReader(new InputStreamReader(isInterior));
//CSVReader readerLeaf = new CSVReader(new InputStreamReader(isLeaf));
//interiorRawStringHashtable.clear();
//leafRawStringHashtable.clear();
//try {
//readerInterior.readNext();
//readerLeaf.readNext();
//String[] nextline;
////create all interior node in interior file and put them into hash table
//while ((nextline = readerInterior.readNext()) != null) {
//	interiorRawStringHashtable.put(Integer.parseInt(nextline[0]), nextline);
//}
//
////create all leaf node in leaf file and put them into hash table
//while ((nextline = readerLeaf.readNext()) != null) {
//	leafRawStringHashtable.put(Integer.parseInt(nextline[0]), nextline);
//}						
//readerInterior.close();
//readerLeaf.close();
//} catch (IOException e) {
//e.printStackTrace();
//}
//MidNode interNode = createInteriorNode(interiorRawStringHashtable.get(0));
//interNode.childIndex = childIndex;
//interNode.parent = parentNode;
//precalculateOneNode(interNode);	
//processNode(interNode);
//return interNode;
//}


//public void createFollowingNodes(MidNode midnode, int childIndex) {
//String selectedGroup = CanvasActivity.selectedItem.toLowerCase(Locale.ENGLISH);
//int[] infos;
//if (childIndex == 1) {
//	if (midnode.child1Index < 0) {
//		infos = findFileAndIndexInfo(midnode.child1Index);
//		String fileIndex = Integer.toString(infos[1]);
//		createNodesInOneFileTillDvarFalse(canvasActivity, selectedGroup, fileIndex, childIndex, midnode);
//	} else {
//		createNodesInSameFile(midnode, childIndex);
//	}
//} else {
//	if (midnode.child2Index < 0) {
//		infos = findFileAndIndexInfo(midnode.child2Index);
//		String fileIndex = Integer.toString(infos[1]);
//		createNodesInOneFileTillDvarFalse(canvasActivity, selectedGroup, fileIndex, childIndex, midnode);
//	} else {
//		createNodesInSameFile(midnode, childIndex);				
//	}
//}
//}
//
//private void createNodesInSameFile(MidNode midnode, int childIndex) {
//Log.d("debug", "file index: " 	+ midnode.fileIndex + " node index: " + midnode.index);
//String selectedGroup = CanvasActivity.selectedItem.toLowerCase(Locale.ENGLISH);
//String fileIndex = Integer.toString(midnode.fileIndex);
//BinaryInitializer.fileIndex = midnode.fileIndex;
//int resourceInteriorID = canvasActivity.getResources().getIdentifier(selectedGroup + "interior" + fileIndex, "raw", canvasActivity.getPackageName());
//InputStream isInterior = canvasActivity.getResources().openRawResource(resourceInteriorID);
//int resourceLeafID = canvasActivity.getResources().getIdentifier(selectedGroup + "leaf" + fileIndex, "raw", canvasActivity.getPackageName());
//InputStream isLeaf = canvasActivity.getResources().openRawResource(resourceLeafID);
//CSVReader readerInterior = new CSVReader(new InputStreamReader(isInterior));
//CSVReader readerLeaf = new CSVReader(new InputStreamReader(isLeaf));
//interiorRawStringHashtable.clear();
//leafRawStringHashtable.clear();		
//try {
//	readerInterior.readNext();
//	readerLeaf.readNext();
//	String[] nextline;
//	//create all interior node in interior file and put them into hash table
//	while ((nextline = readerInterior.readNext()) != null) {
//		interiorRawStringHashtable.put(Integer.parseInt(nextline[0]), nextline);
//	}
//	
//	//create all leaf node in leaf file and put them into hash table
//	while ((nextline = readerLeaf.readNext()) != null) {
//		leafRawStringHashtable.put(Integer.parseInt(nextline[0]), nextline);
//	}						
//	readerInterior.close();
//	readerLeaf.close();
//} catch (IOException e) {
//	e.printStackTrace();
//}
////
////MidNode interNode = createInteriorNode(interiorRawStringHashtable.get(midnode.index));
////Assert.assertNotNull(interNode);
//processNode(midnode);
//
//while (!listOfHeadNodeInNextFile.isEmpty()) {
//	Pair<Integer, MidNode> pair = listOfHeadNodeInNextFile.poll();
//	if (pair.first.equals(1)) {
//		int[] infos = findFileAndIndexInfo(pair.second.child1Index);
//		if (pair.second.positionData.dvar)
//			pair.second.child1 = createNodesInOneFileTillDvarFalse(canvasActivity, selectedGroup, Integer.toString(infos[1]), 1, pair.second);
//	} else {
//		Assert.assertEquals(pair.first, Integer.valueOf(2));
//		int[] infos = findFileAndIndexInfo(pair.second.child2Index);
//		if (pair.second.positionData.dvar)
//			pair.second.child2 = createNodesInOneFileTillDvarFalse(canvasActivity, selectedGroup, Integer.toString(infos[1]), 2, pair.second);
//	}	
//}
//}
