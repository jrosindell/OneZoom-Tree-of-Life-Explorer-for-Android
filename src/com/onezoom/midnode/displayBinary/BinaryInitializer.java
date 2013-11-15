package com.onezoom.midnode.displayBinary;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Locale;

import junit.framework.Assert;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import au.com.bytecode.opencsv.CSVReader;

import com.onezoom.CanvasActivity;
import com.onezoom.midnode.Initializer;
import com.onezoom.midnode.InteriorNode;
import com.onezoom.midnode.LeafNode;
import com.onezoom.midnode.MidNode;
import com.onezoom.midnode.MidNodeOneChunk;
import com.onezoom.midnode.PositionData;

public class BinaryInitializer implements Initializer {
	Hashtable<Integer, InteriorNode> interiorHash;
	Hashtable<Integer, LeafNode> leafHash;
	LinkedList<Pair<Integer, MidNode>> list;
	LinkedList<Node> chunkList;
	static Context canvasActivity;

	public BinaryInitializer() {
		interiorHash = new Hashtable<Integer, InteriorNode>(200);
		leafHash = new Hashtable<Integer, LeafNode>(200);
		list = new LinkedList<Pair<Integer,MidNode>>();
		chunkList = new LinkedList<Node>();
	}
	
	public static void setContext(Context context) {
		canvasActivity = context;
	}
	
	@Override
	public MidNodeOneChunk createTreeChunk(String fileNumber) {
		String selectedGroup = CanvasActivity.selectedItem.toLowerCase();
		MidNodeOneChunk rootChunk = createOneChunk(canvasActivity, selectedGroup, fileNumber, 0, null);
		MidNodeOneChunk tempChunk;
		while (!chunkList.isEmpty()) {
			Node node = chunkList.poll();
			if (node.childIndex == 1) {
				int[] infos = findFileAndIndexInfo(node.midNode.child1Index);
				if (node.midNode.positionData.dvar) {
					tempChunk = createOneChunk(canvasActivity, selectedGroup, Integer.toString(infos[1]), 1, node.midNode);
					node.midNode.child1 = tempChunk.getroot();
					node.parentChunk.addChildren(tempChunk);
				}
			} else {
				Assert.assertEquals(node.childIndex, 2);
				int[] infos = findFileAndIndexInfo(node.midNode.child2Index);
				if (node.midNode.positionData.dvar) {
					tempChunk = createOneChunk(canvasActivity, selectedGroup, Integer.toString(infos[1]), 2, node.midNode);
					node.midNode.child2 = tempChunk.getroot();
					node.parentChunk.addChildren(tempChunk);
				}
			}	
		}
		return rootChunk;
	}
	
	@Override
	public void createTreeChunk(MidNode midNode, int childIndex) {
		//called during recalculation, dynamically create children chunk based on midnode and which children the children chunk is
		String selectedGroup = CanvasActivity.selectedItem.toLowerCase();
		int[] infos;
		String fileIndex;
		if (childIndex == 1) {
			Assert.assertEquals(midNode.child1Index < 0, true);
			infos = findFileAndIndexInfo(midNode.child1Index);
			fileIndex = Integer.toString(infos[1]);
			midNode.child1 = createMidNode(fileIndex, childIndex, midNode);
		} else {
			Assert.assertEquals(midNode.child2Index < 0, true);
			infos = findFileAndIndexInfo(midNode.child2Index);
			fileIndex = Integer.toString(infos[1]);
			midNode.child2 = createMidNode(fileIndex, childIndex, midNode);
		}		
	}

	@Override
	public MidNode createMidNode(String fileIndex) {
		return createMidNode(fileIndex, 0, null);
	}
	
	private MidNode createMidNode(String fileIndex, int childIndex, MidNode parent) {
		String selectedGroup = CanvasActivity.selectedItem.toLowerCase();
		MidNode fulltree = createChunkOfNode(canvasActivity, selectedGroup, fileIndex, childIndex, parent);
		while (!list.isEmpty()) {
			Pair<Integer, MidNode> pair = list.poll();
			if (pair.first.equals(1)) {
				int[] infos = findFileAndIndexInfo(pair.second.child1Index);
				if (pair.second.positionData.dvar)
					pair.second.child1 = createChunkOfNode(canvasActivity, selectedGroup, Integer.toString(infos[1]), 1, pair.second);
			} else {
//				assert pair.first.equals(2);
				Assert.assertEquals(pair.first, Integer.valueOf(2));
				int[] infos = findFileAndIndexInfo(pair.second.child2Index);
				if (pair.second.positionData.dvar)
					pair.second.child2 = createChunkOfNode(canvasActivity, selectedGroup, Integer.toString(infos[1]), 2, pair.second);
			}	
		}
		return fulltree;
	}
	
	private MidNodeOneChunk createOneChunk(Context canvasActivity,
			String selectedGroup, String fileNumber, int childIndex, MidNode parentNode) {
		//create a new chunk, return it. put following chunks in chunklist
		//set relations between them.
		MidNode rootNode = createChunkOfNode(canvasActivity, selectedGroup, fileNumber, childIndex, parentNode);
		MidNodeOneChunk chunk = new MidNodeOneChunk(rootNode);
		while (!list.isEmpty()) {
			Pair<Integer, MidNode> pair = list.poll();
			chunkList.add(new Node(pair.first, pair.second, chunk));
		}
		return chunk;
	}
	
	
	private MidNode createChunkOfNode(Context canvasActivity,
			String selectedGroup, String fileIndex, int childIndex, MidNode parentNode) {
		int resourceInteriorID = canvasActivity.getResources().getIdentifier(selectedGroup + "interior" + fileIndex, "raw", canvasActivity.getPackageName());
		Log.d("debug", "resource: "  + selectedGroup + "interior" + fileIndex);
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
			while ((nextline = readerInterior.readNext()) != null) {
				interiorNode = createInteriorNode(nextline);
				interiorHash.put(interiorNode.index, interiorNode);
			}
			
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

	private void precalculateThisChunk(MidNode interNode) {
		//TODO: does not behave correctly when doing dynamically loading
		MidNode.precalculator.preCalcWholeTree(interNode);

		if (interNode.parent == null) {
			interNode.recalculate(200, 900, 1);
			Log.d("debug", "im up..... xp.");
		}
		else {
			Log.d("debug", "im here..... xp.");
			PositionData positionData = interNode.parent.positionData;
			if (interNode.childIndex == 1) {
				interNode.recalculate(
						positionData.xvar + positionData.rvar * positionData.nextx1,
						positionData.yvar + positionData.rvar * positionData.nexty1,
						positionData.rvar * positionData.nextr1 / 220);
			} else {
				interNode.recalculate(
						positionData.xvar + positionData.rvar * positionData.nextx2,
						positionData.yvar + positionData.rvar * positionData.nexty2,
						positionData.rvar * positionData.nextr2 / 220);
			}
		}
	}

	private void buildConnection(MidNode interiorNode) {
		Assert.assertEquals(interiorNode.getClass(), InteriorNode.class);
//		assert interiorNode.getClass() == InteriorNode.class;
		
		int child1Id = interiorNode.child1Index;
		int child2Id = interiorNode.child2Index;

		if (child1Id < 0) {
			list.add(new Pair<Integer, MidNode>(1, interiorNode));
		} else if (interiorHash.containsKey(child1Id)) {
			MidNode child1 = interiorHash.get(child1Id);
			interiorNode.child1 = child1;
			child1.parent = interiorNode;
			child1.childIndex = 1;
			buildConnection(interiorNode.child1);
		} else {
			MidNode child1 = leafHash.get(child1Id);
			Assert.assertNotNull(child1);
//			assert child1 != null;
			interiorNode.child1 = child1;
			child1.parent = interiorNode;
			child1.childIndex = 1;
		}
		
		if (child2Id < 0) {
			list.add(new Pair<Integer, MidNode>(2, interiorNode));
		} else if (interiorHash.containsKey(child2Id)) {
			MidNode child2 = interiorHash.get(child2Id);
			interiorNode.child2 = child2;
			child2.parent = interiorNode;
			child2.childIndex = 2;
			buildConnection(interiorNode.child2);
		} else {
			MidNode child2 = leafHash.get(child2Id);
			Assert.assertNotNull(child2);
//			assert child2 != null;
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
}

class Node {
	int childIndex;
	MidNode midNode;
	MidNodeOneChunk parentChunk;
	public Node(int index, MidNode node, MidNodeOneChunk chunk) {
		childIndex = index;
		midNode = node;
		parentChunk = chunk;
	}
}
