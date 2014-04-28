package com.onezoom.midnode.displayBinary;

import junit.framework.Assert;
import android.util.Log;

import com.onezoom.midnode.InteriorNode;
import com.onezoom.midnode.LeafNode;
import com.onezoom.midnode.MidNode;
import com.onezoom.midnode.PositionData;

public class BinaryPrecalculator {
	private static final float partl1 = 0.55f; // size of line
	private static final float ratioOfChild1 = 1/1.3f;
	private static final float ratioOfChild2 = 1/2.25f;
	private static final float angleOfChild1Right = (float) (0.22f * Math.PI);
	private static final float angleOfChild2Left = (float) (0.46f * Math.PI);
	private static final float leafmult = 3.2f;
	private static final float posmult = leafmult -2f;
	private static final float partc = 0.5f;

	private static float rootAngle = (float) (Math.PI * 3 / 2);
	
	
	public void preCalcWholeTree(MidNode tree) {
		precalcOneNode(tree);
		if (tree.child1 != null) preCalcWholeTree(tree.child1);
		if (tree.child2 != null) preCalcWholeTree(tree.child2);
	}

	
	public void preCalcWholeTree(MidNode tree, Float angle) {
		rootAngle = angle;
		preCalcWholeTree(tree);
	}
	
	
	public void preCalcOneInteriorNode(InteriorNode interiorNode) {
//		assert interiorNode != null;
		Assert.assertNotNull(interiorNode);
		MidNode parent = interiorNode.getParent();
		if (parent == null) {
			Log.d("debug", "calculate as root");
			precalcRoot(interiorNode.positionData);
		} else if (interiorNode.childIndex == 1){
			precalcAsRightChildren(interiorNode.positionData, parent.positionData);
		} else {
//			assert interiorNode.childIndex == 2;
			Assert.assertEquals(2, interiorNode.childIndex);
			precalcAsLeftChildren(interiorNode.positionData, parent.positionData);
		}
		precalcInteriorCircle(interiorNode.positionData);
	}

	
	public void preCalcOneLeafNode(LeafNode leafNode) {
//		assert leafNode != null;
		Assert.assertNotNull(leafNode);
		MidNode parent = leafNode.getParent();
		if (leafNode.childIndex == 1) {
			precalcAsRightChildren(leafNode.positionData, parent.positionData);
		} else {
			assert leafNode.childIndex == 2;
			Assert.assertEquals(leafNode.childIndex, 2);
			precalcAsLeftChildren(leafNode.positionData, parent.positionData);
		}
		precalcLeafShape(leafNode.positionData);
		//TODO: leaf node needs more precalc
	}
	
	public void precalcOneNode(MidNode node) {
		if (node.getClass() == InteriorNode.class) {
			preCalcOneInteriorNode((InteriorNode)node);
		} else {
//			assert node.getClass() == LeafNode.class;
			Assert.assertEquals(node.getClass(), LeafNode.class);
			preCalcOneLeafNode((LeafNode)node);
		}
		MidNode.positionCalculator.calculateGBoundingBox(node);
	}
	
	private void precalcAsLeftChildren(PositionData thisData,
			PositionData parentData) {
//		assert thisData != null;
//		assert parentData != null;
		Assert.assertNotNull(thisData);
		Assert.assertNotNull(parentData);
		precalcBezierAsLeftChildren(thisData, parentData);
		precalcNextReference(thisData);
	}

	private void precalcAsRightChildren(PositionData thisData,
			PositionData parentData) {
//		assert thisData != null;
//		assert parentData != null;
		Assert.assertNotNull(thisData);
		Assert.assertNotNull(parentData);
		precalcBezierAsRightChildren(thisData, parentData);
		precalcNextReference(thisData);
	}

	private void precalcBezierAsLeftChildren(PositionData thisData,
			PositionData parentData) {
		thisData.arcAngle = parentData.arcAngle - angleOfChild2Left;
		thisData.bezsx = -(0.3f)*(getCos(parentData.arcAngle))/ratioOfChild2;
		thisData.bezsy = -(0.3f)*(getSin(parentData.arcAngle))/ratioOfChild2;
		thisData.bezex = getCos(thisData.arcAngle);
		thisData.bezey = getSin(thisData.arcAngle);
		thisData.bezc1x = 0.1f*(getCos(parentData.arcAngle))/ratioOfChild2;
		thisData.bezc1y = 0.1f*(getSin(parentData.arcAngle))/ratioOfChild2;
		thisData.bezc2x = 0.9f*getCos(thisData.arcAngle);
		thisData.bezc2y = 0.9f*getSin(thisData.arcAngle);
		thisData.bezr = partl1;		
	}

	private void precalcBezierAsRightChildren(PositionData thisData,
			PositionData parentData) {
		thisData.arcAngle = parentData.arcAngle + angleOfChild1Right;
		thisData.bezsx = -(0.3f)*(getCos(parentData.arcAngle))/ratioOfChild1;
		thisData.bezsy = -(0.3f)*(getSin(parentData.arcAngle))/ratioOfChild1;
		thisData.bezex = getCos(thisData.arcAngle);
		thisData.bezey = getSin(thisData.arcAngle);
		thisData.bezc1x = -0.3f*(getCos(parentData.arcAngle))/ratioOfChild1;
		thisData.bezc1y = -0.3f*(getSin(parentData.arcAngle))/ratioOfChild1;
		thisData.bezc2x = 0.15f*(getCos(parentData.arcAngle))/ratioOfChild1;
		thisData.bezc2y = 0.15f*(getSin(parentData.arcAngle))/ratioOfChild1;
		thisData.bezr = partl1;
	}

	private void precalcRoot(PositionData positionData) {
//		assert positionData != null;
		Assert.assertNotNull(positionData);
		positionData.bezsx = 0;
		positionData.bezsy = 0;
		positionData.bezex = 0;
		positionData.bezey = -1;
		positionData.bezc1x = 0;
		positionData.bezc1y = -0.05f;
		positionData.bezc2x = 0;
		positionData.bezc2y = -0.95f;
		positionData.bezr = partl1;
		positionData.arcAngle = rootAngle;
		precalcNextReference(positionData);
	}

	private void precalcNextReference(PositionData positionData) {
		positionData.nextr1 = ratioOfChild1;
		positionData.nextr2 = ratioOfChild2;
		positionData.nextx1 = (1.3f*getCos(positionData.arcAngle))
				+(((positionData.bezr)-(partl1*ratioOfChild1))/2.0f)*getCos90Pre(positionData.arcAngle);
		positionData.nexty1 = (1.3f*getSin(positionData.arcAngle))
				+(((positionData.bezr)-(partl1*ratioOfChild1))/2.0f)*getSin90Pre(positionData.arcAngle); // y reference point for both children
		positionData.nextx2 = (1.3f*getCos(positionData.arcAngle))
				-(((positionData.bezr)-(partl1*ratioOfChild2))/2.0f)*getCos90Pre(positionData.arcAngle); // x refernece point for both children
		positionData.nexty2 = (1.3f*getSin(positionData.arcAngle))
				-(((positionData.bezr)-(partl1*ratioOfChild2))/2.0f)*getSin90Pre(positionData.arcAngle); // y reference point for both children	}
	}
	
	private void precalcInteriorCircle(PositionData positionData) {
		positionData.arcx = positionData.bezex;
		positionData.arcy = positionData.bezey;
		positionData.arcr = positionData.bezr / 2;
		positionData.arcx2 = positionData.bezex  + posmult * getCos(positionData.arcAngle);
		positionData.arcy2 = positionData.bezey + posmult * getSin(positionData.arcAngle);
		positionData.arcr2 = leafmult * partc;
	}
	
	private void precalcLeafShape(PositionData positionData) {
		positionData.arcx = positionData.bezex  + posmult * getCos(positionData.arcAngle);
		positionData.arcy = positionData.bezey + posmult * getSin(positionData.arcAngle);
		positionData.arcr = leafmult * partc;
	}
	
	private float getCos(Float angle) { return (float) Math.cos(angle); }
	private float getSin(Float angle) { return (float) Math.sin(angle); }	
	private float getSin90Pre(Float angle) { return (float) Math.sin(angle + Math.PI / 2.0); }
	private float getCos90Pre(Float angle) { return (float) Math.cos(angle + Math.PI / 2.0); }

	public static float getAngleofchild1right() {
		return angleOfChild1Right;
	}

	public static float getAngleofchild2left() {
		return angleOfChild2Left;
	}


}
