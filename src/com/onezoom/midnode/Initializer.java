package com.onezoom.midnode;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.PriorityQueue;
import java.util.Stack;

import junit.framework.Assert;
import android.content.Context;
import android.util.Pair;
import au.com.bytecode.opencsv.CSVReader;

import com.onezoom.CanvasActivity;

public class Initializer {
	public Hashtable<Integer, MidNode> fulltreeHash;
	Hashtable<Integer, InteriorNode> interiorHash;
	Hashtable<Integer, LeafNode> leafHash;
	Hashtable<Integer, Integer> fileConnection;
	LinkedList<Pair<Integer, MidNode>> listOfHeadNodeInNextFile;
	public PriorityQueue<MidNode> stackOfNodeHasNonInitChildren;
	public CanvasActivity client;
	boolean duringInitalization = false;
	public int fileIndex;

	public Initializer() {
		fileConnection = new Hashtable<Integer, Integer>(1000);
		fulltreeHash = new Hashtable<Integer, MidNode>(60000);
		interiorHash = new Hashtable<Integer, InteriorNode>(1000);
		leafHash = new Hashtable<Integer, LeafNode>(1000);
		listOfHeadNodeInNextFile = new LinkedList<Pair<Integer,MidNode>>();
		stackOfNodeHasNonInitChildren = new PriorityQueue<MidNode>();
	}
	
	public boolean isDuringInitialization() {
		return duringInitalization;
	}

	public void setDuringInitialization(boolean dynamic) {
		this.duringInitalization = dynamic;
	}

	/**
	 * Set context and build file connection.
	 * @param context
	 */
	public void setContext(CanvasActivity context) {
		client = context;
		fillFileConnection();
	}
	
	/**
	 * When the activity starts, this method will be called to build the tree.
	 * @param fileIndex
	 * @return
	 */
	public MidNode createMidNode(String fileIndex) {
		return createTreeStartFromFileIndex(fileIndex, 0);
	}
	
	/**
	 * First create nodes in one file which is specified by fileIndex.
	 * 
	 * When the file is loaded, 
	 * it will push nodes which have further children in other files into listOfHeadNodeInNextFile.
	 * 
	 * Poll the first node in listOfHeadNodeInNextFile list and initialize it if its horizon is visible,
	 * otherwise push the node into the stack of non-initialized nodes waiting for idle time initialization
	 * 
	 * @param fileIndex
	 * @param childIndex
	 * @return
	 */
	private MidNode createTreeStartFromFileIndex(String fileIndex, int childIndex) {
		String selectedGroup = client.selectedItem.toLowerCase(Locale.ENGLISH);
		MidNode fulltree = createNodesInOneFile(client, selectedGroup, fileIndex, childIndex, null);
		while (!listOfHeadNodeInNextFile.isEmpty()) {
			Pair<Integer, MidNode> pair = listOfHeadNodeInNextFile.poll();
			if (pair.first.equals(1)) {	
				int[] infos = findFileAndIndexInfo(pair.second.child1Index);
				if (pair.second.positionData.dvar)
					pair.second.child1 = createNodesInOneFile(client, selectedGroup, Integer.toString(infos[1]), 1, pair.second);
				else {
					stackOfNodeHasNonInitChildren.add(pair.second);
				}
			} else {
				int[] infos = findFileAndIndexInfo(pair.second.child2Index);
				if (pair.second.positionData.dvar)
					pair.second.child2 = createNodesInOneFile(client, selectedGroup, Integer.toString(infos[1]), 2, pair.second);
				else {
					stackOfNodeHasNonInitChildren.add(pair.second);
				}
			}	
		}
		return fulltree;
	}
	
	/**
	 * Create nodes in file 'fileIndex'.
	 * 
	 * 
	 * @param canvasActivity
	 * @param selectedGroup
	 * @param fileIndex
	 * @param childIndex
	 * @param parentNode
	 * @return
	 */
	private MidNode createNodesInOneFile(Context canvasActivity,
			String selectedGroup, String fileIndex, int childIndex, MidNode parentNode) {
		this.fileIndex = Integer.parseInt(fileIndex);
		
		/**
		 * Get file like 'mammalsinterior0'
		 */
		int resourceInteriorID = canvasActivity.getResources().getIdentifier(selectedGroup + "interior" + fileIndex, "raw", canvasActivity.getPackageName());
		InputStream isInterior = canvasActivity.getResources().openRawResource(resourceInteriorID);
		
		/**
		 * Get file like 'mammalsleaf0'
		 */
		int resourceLeafID = canvasActivity.getResources().getIdentifier(selectedGroup + "leaf" + fileIndex, "raw", canvasActivity.getPackageName());
		InputStream isLeaf = canvasActivity.getResources().openRawResource(resourceLeafID);
		
		CSVReader readerInterior = new CSVReader(new InputStreamReader(isInterior));
		CSVReader readerLeaf = new CSVReader(new InputStreamReader(isLeaf));
		interiorHash.clear();
		leafHash.clear();
		
		try {
			//skip the first line.
			readerInterior.readNext();
			readerLeaf.readNext();
			
			InteriorNode interiorNode;
			LeafNode leafNode;
			String[] nextline;
			
			//create all interior node in interior file and put them into interior hash table as well as fulltree hash.
			while ((nextline = readerInterior.readNext()) != null) {
				interiorNode = new InteriorNode(nextline);
				interiorNode.fileIndex = this.fileIndex;
				interiorHash.put(interiorNode.index, interiorNode);
				fulltreeHash.put(Utility.combine(interiorNode.fileIndex, interiorNode.index), interiorNode);
			}
			
			//create all leaf node in leaf file and put them into leaf hash table as well as fulltree hash
			while ((nextline = readerLeaf.readNext()) != null) {
				leafNode = new LeafNode(nextline);
				leafNode.fileIndex = this.fileIndex;
				leafHash.put(leafNode.index, leafNode);
				fulltreeHash.put(Utility.combine(leafNode.fileIndex, leafNode.index), leafNode);
			}						
			readerInterior.close();
			readerLeaf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//Get the root node in this file and build connection between this node and its parent.
		MidNode interNode = interiorHash.get(0);
		interNode.childIndex = childIndex;
		interNode.parent = parentNode;
		
		//Build connection from the root node to all of its descendants in this file.
		buildConnection(interNode);
		
		//pre calculate the nodes in this file.
		precalculateThisChunk(interNode);
		return interNode;
	}
	
	/**
	 * build connections between nodes starting from 'interiorNode'
	 * 
	 * This function connect children-parent relationship between nodes.
	 * It also push the node that has further children need to be initialized in other files
	 * into the listOfHeadNodeInNextFile linkList.
	 * 
	 * @param interiorNode
	 */
	private void buildConnection(MidNode interiorNode) {
		try {
			int child1Id = interiorNode.child1Index;
			int child2Id = interiorNode.child2Index;
	
			if (child1Id < 0) {
				//child1 is in another file
				listOfHeadNodeInNextFile.add(new Pair<Integer, MidNode>(1, interiorNode));
			} else if (interiorHash.containsKey(child1Id)) {
				//child1 is an interior node.
				MidNode child1 = interiorHash.get(child1Id);
				interiorNode.child1 = child1;
				child1.parent = interiorNode;
				child1.childIndex = 1;
				buildConnection(interiorNode.child1);
			} else {
				//child1 is a leaf node.
				MidNode child1 = leafHash.get(child1Id);
				interiorNode.child1 = child1;
				child1.parent = interiorNode;
				child1.childIndex = 1;
			}
			
			if (child2Id < 0) {
				//child2 is in another file
				listOfHeadNodeInNextFile.add(new Pair<Integer, MidNode>(2, interiorNode));
			} else if (interiorHash.containsKey(child2Id)) {
				//child2 is an interior node.
				MidNode child2 = interiorHash.get(child2Id);
				interiorNode.child2 = child2;
				child2.parent = interiorNode;
				child2.childIndex = 2;
				buildConnection(interiorNode.child2);
			} else {
				//child2 is a leaf node.
				MidNode child2 = leafHash.get(child2Id);
				interiorNode.child2 = child2;
				child2.parent = interiorNode;
				child2.childIndex = 2;
			}
		} catch (NullPointerException e) {
			return;
		}
	}
	
	/**
	 * Do recalculation starting from 'interNode'
	 * Since it is called in createNodesInOneFile, it pre-calculates nodes only in one file.
	 * 
	 * re-calculation is needed during first initialization, since first initialization needs to load 
	 * more than one file, which can be achieved only when dvar of head node is true.
	 * 
	 * When dynamically loading one file re-calculation here could be unnecessary.
	 * 
	 * Maybe I should do re-calculation whether it is first initialization or not... I am not sure ..
	 * @param interNode
	 */
	private void precalculateThisChunk(MidNode interNode) {
		MidNode.precalculator.preCalcWholeTree(interNode);

		if (duringInitalization) {
			if (interNode.parent == null) {
				//root of the tree.
				MidNode.positionCalculator.recalculate(
						interNode, PositionData.xp, PositionData.yp, PositionData.ws);
			} else {
				PositionData positionData = interNode.parent.positionData;
				if (interNode.childIndex == 1) {
					MidNode.positionCalculator.recalculate(interNode,
							positionData.xvar + positionData.rvar * positionData.nextx1, 
							positionData.yvar + positionData.rvar * positionData.nexty1,
							positionData.rvar * positionData.nextr1 / 220);
				} else {
					MidNode.positionCalculator.recalculate(interNode,
							positionData.xvar + positionData.rvar * positionData.nextx2,
							positionData.yvar + positionData.rvar * positionData.nexty2,
							positionData.rvar * positionData.nextr2 / 220);
				}
			}
		}
	}
	
	/**
	 * Load a file starting from midnode, which is a tail node from a initialized file.
	 * 
	 * 
	 * First find the next file that need to be initialized by findFileAndIndexInfo. Then initialize that file.
	 * 
	 * In the newly initialized file, 
	 * nodes which has children in other files will be added to stackOfNodeNonInitChildren queue,
	 * waiting for idle time initialization.
	 * 
	 * infos[0] is not used... Maybe I should change the function to only return file index.
	 * @param childIndex
	 * @param midnode
	 * @return
	 */
	public MidNode createTreeStartFromTailNode(int childIndex, MidNode midnode) {
		int[] infos;
		this.listOfHeadNodeInNextFile.clear();
		if (childIndex == 1) {
			infos = findFileAndIndexInfo(midnode.child1Index);
		} else {
			infos = findFileAndIndexInfo(midnode.child2Index);
		}
		int fileIndex = infos[1];
		String selectedGroup = client.selectedItem.toLowerCase(Locale.ENGLISH);
		MidNode root = createNodesInOneFile(client, selectedGroup, Integer.toString(fileIndex), childIndex, midnode);
		copyFromListToStack(this.listOfHeadNodeInNextFile, this.stackOfNodeHasNonInitChildren);
		this.listOfHeadNodeInNextFile.clear();
		return root;
	}
	
	/**
	 * Copy nodes from list into queue
	 */
	private void copyFromListToStack(LinkedList<Pair<Integer, MidNode>> list, PriorityQueue<MidNode> queue) {
		for (int i = 0; i < list.size(); i++) {
			queue.add(list.get(i).second);
		}
	}

	/**
	 * child1ID should be a negative number. It contains fileIndex of where its further children locates.
	 * 
	 * infos[1] contains the file index which is compressed in childID
	 * 
	 * The reason for using such way to decode and encode fileIndex is that to use a negative number to 
	 * distinct from a node which has children within the file and a node whose children is in another file.
	 * @param childID
	 * @return
	 */
	private int[] findFileAndIndexInfo(int childID) {
		int [] infos = new int[2];
		infos[0] = 0;
		infos[1] = childID >> 10;
		infos[1] = infos[1] & 0x03FFFF;
		return infos;
	}
	
	/**
	 * Load files in idle time.
	 * 
	 * First sort all nodes in the stack to find out the one which is closest to screen center.
	 * 
	 * Then poll this node out and initialize its children if it has not been initialized yet.
	 */
	public void idleTimeInitialization() {
		Arrays.sort(this.stackOfNodeHasNonInitChildren.toArray());
		if (!stackOfNodeHasNonInitChildren.isEmpty()) {
			MidNode interNode = stackOfNodeHasNonInitChildren.poll();
			if (interNode == null) return;
			if (interNode.child1 == null) {
				interNode.child1 = createTreeStartFromTailNode(1, interNode);
			} else if (interNode.child2 == null){
				interNode.child2 = createTreeStartFromTailNode(2, interNode);
			}
		}
	}
	
	/**
	 * Initialize file which contains the searched node.
	 * @param fileIndex
	 */
	public void initialiseSearchedFile(int fileIndex) {
		Stack<Integer> filesNeedToBeInit = new Stack<Integer>();
		//find out all files need to be initialized in order to initialize file with index of the argument.
		findAllFileNeedToBeInit(fileIndex, filesNeedToBeInit);		
		//Init all these files.
		initFilesInStack(filesNeedToBeInit);
	}
	
	/**
	 * Find all files need to be loaded by searching parent files until parent file has already been initialised.
	 * @param fileIndex
	 * @param filesNeedToBeInit
	 * @return
	 */
	private void findAllFileNeedToBeInit(int fileIndex,
			Stack<Integer> filesNeedToBeInit) {
		filesNeedToBeInit.add(fileIndex);
		//find parent file of 'fileIndex'
		int parentFileIndex = fileConnection.get(fileIndex);
		if (!fulltreeHash.containsKey(Utility.combine(parentFileIndex, 0))) {
			//parent file has not been initialized yet.
			findAllFileNeedToBeInit(parentFileIndex, filesNeedToBeInit);
		}
	}
	
	/**
	 * Init all files in the stack.
	 * Start from the most recently added files, which should be the parent of other files.
	 * @param filesNeedToBeInit
	 */
	private void initFilesInStack(Stack<Integer> filesNeedToBeInit) {
		while (!filesNeedToBeInit.isEmpty()) {
			Integer fileIndex = filesNeedToBeInit.pop();
		
			/**
			 * fileConnection will get the parent file of 'fileIndex'.
			 * The following statement returns the root of parent file.
			 */
			MidNode rootOfParentFile = fulltreeHash.get(Utility.combine(fileConnection.get(fileIndex), 0));
			initNewFile(rootOfParentFile, fileIndex);
		}
	}
	
	/**
	 * 'node' should be in a file that has already been initialized.
	 * 
	 * Start from root to find the node that has children in another file whose index equals 'fileindex'
	 * 
	 * @param node
	 * @param fileIndex
	 */
	private void initNewFile(MidNode node, Integer fileIndex) {		
		if (node.child1 == null && node.child1Index < 0
				&& findFileAndIndexInfo(node.child1Index)[1] == fileIndex) {
			node.child1 = createTreeStartFromTailNode(1, node);
		} 
		
		if (node.child2 == null && node.child2Index < 0
				&& findFileAndIndexInfo(node.child2Index)[1] == fileIndex) {
			node.child2 = createTreeStartFromTailNode(2, node);
		} 

		if (node.child1 != null && node.child1Index > 0) {
			initNewFile(node.child1, fileIndex);			
		}
		
		if (node.child2 != null && node.child2Index > 0) {
			initNewFile(node.child2, fileIndex);
		}
	}


	/**
	 * This method read data from *search, which stores the parent-child relationship of files.
	 * 
	 * For example, if a node 'A' is in file 30, and it has a child 'B' in file 40, then fileConnection
	 * will add a hash pair <30,40> to fileConnection object.
	 */
	private void fillFileConnection() {
		fileConnection.clear();
		String selectedGroup = client.selectedItem.toLowerCase(Locale.ENGLISH);
		int resourceID = client.getResources().getIdentifier(selectedGroup + "search", "raw", client.getPackageName());
		InputStream is = client.getResources().openRawResource(resourceID);
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