package com.onezoom.midnode.displayBinary;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Locale;

import junit.framework.Assert;
import android.content.Context;
import android.util.Log;
import android.util.Pair;
import au.com.bytecode.opencsv.CSVReader;

import com.onezoom.CanvasActivity;
import com.onezoom.midnode.InteriorNode;
import com.onezoom.midnode.LeafNode;
import com.onezoom.midnode.MidNode;
import com.onezoom.midnode.PositionData;

public class BinaryInitializer {
	Hashtable<Integer, InteriorNode> interiorHash;
	Hashtable<Integer, LeafNode> leafHash;
	LinkedList<Pair<Integer, MidNode>> listOfHeadNodeInNextFile;
	static Context canvasActivity;
	boolean dynamic = false;

	public BinaryInitializer() {
		interiorHash = new Hashtable<Integer, InteriorNode>(200);
		leafHash = new Hashtable<Integer, LeafNode>(200);
		listOfHeadNodeInNextFile = new LinkedList<Pair<Integer,MidNode>>();
	}
	
	public static void setContext(Context context) {
		canvasActivity = context;
	}
	
	public MidNode createMidNode(String fileIndex) {
		return createTreeStartFromFileIndex(fileIndex, 0, null);
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
			} else {
				Assert.assertEquals(pair.first, Integer.valueOf(2));
				int[] infos = findFileAndIndexInfo(pair.second.child2Index);
				if (pair.second.positionData.dvar)
					pair.second.child2 = createNodesInOneFile(canvasActivity, selectedGroup, Integer.toString(infos[1]), 2, pair.second);
			}	
		}
		return fulltree;
	}
	
	
	private MidNode createNodesInOneFile(Context canvasActivity,
			String selectedGroup, String fileIndex, int childIndex, MidNode parentNode) {
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
				interiorHash.put(interiorNode.index, interiorNode);
			}
			
			//create all leaf node in leaf file and put them into hash table
			while ((nextline = readerLeaf.readNext()) != null) {
				leafNode = createLeafNode(nextline);
				leafHash.put(leafNode.index, leafNode);
			}						
			readerInterior.close();
			readerLeaf.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
		//TODO: does not behave correctly when doing dynamically loading
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
		infos[1] = infos[1] & 0x03FF;
		return infos;
	}

	private LeafNode createLeafNode(String[] nextline) {
		return new LeafNode(nextline);
	}

	private InteriorNode createInteriorNode(String[] nextline) {
		// TODO Auto-generated method stub
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
}
