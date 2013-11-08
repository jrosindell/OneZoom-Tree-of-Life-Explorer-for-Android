package com.onezoom.midnode.displayBinary;

import android.graphics.Canvas;

import com.onezoom.midnode.InteriorNode;
import com.onezoom.midnode.LeafNode;
import com.onezoom.midnode.MidNode;
import com.onezoom.midnode.Visualizer;

public class BinaryVisualizer implements Visualizer{

	@Override
	public void drawElement(Canvas canvas, MidNode midNode) {
		if (midNode.getClass() == InteriorNode.class)
			drawElement(canvas, (InteriorNode)midNode);
		else
			drawElement(canvas, (LeafNode)midNode);
	}
	
	private void drawElement(Canvas canvas, InteriorNode midNode) {
		//TODO: incomplete method
	}
	
	private void drawElement(Canvas canvas, LeafNode midNode) {
		//TODO: incomplete method
	}

}
