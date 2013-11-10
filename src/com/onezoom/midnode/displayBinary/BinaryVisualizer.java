package com.onezoom.midnode.displayBinary;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import com.onezoom.midnode.InteriorNode;
import com.onezoom.midnode.LeafNode;
import com.onezoom.midnode.MidNode;
import com.onezoom.midnode.Visualizer;

public class BinaryVisualizer implements Visualizer{
	Paint paint;
	Paint textPaint;
	Paint signPaint;
	Path path;
	
	public BinaryVisualizer() {
		paint = new Paint();
		textPaint = new Paint();
		signPaint = new Paint();
		path = new Path();
	}
	
	@Override
	public void drawElement(Canvas canvas, MidNode midNode) {
		if (midNode.child1 != null) drawElement(canvas, midNode.child1);
		if (midNode.child2 != null) drawElement(canvas, midNode.child2);
		
		if (midNode.getClass() == InteriorNode.class)
			drawElement(canvas, (InteriorNode)midNode);
		else
			drawElement(canvas, (LeafNode)midNode);
	}
	
	private void drawElement(Canvas canvas, InteriorNode midNode) {
		drawBranch(canvas, midNode);
	}

	private void drawElement(Canvas canvas, LeafNode midNode) {
		//TODO: incomplete method
	}
	
	private void drawBranch(Canvas canvas, MidNode node) {
		float x = node.positionData.xvar;
		float y = node.positionData.yvar;
		float r = node.positionData.rvar;
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth((float) (node.positionData.rvar * node.positionData.bezr));
//		paint.setColor(node.myColor);
		// paint.setColor(branchcolor());
		paint.setStyle(Paint.Style.STROKE);
		path.reset();
		path.moveTo(
				(float) (x + r * node.positionData.bezsx), 
				(float) (y + r * node.positionData.bezsy));
		path.cubicTo(
				(float) (x + r * node.positionData.bezc1x),
				(float) (y + r * node.positionData.bezc1y), 
				(float) (x + r * node.positionData.bezc2x),
				(float) (y + r * node.positionData.bezc2y), 
				(float) (x + r * node.positionData.bezex), 
				(float) (y + r * node.positionData.bezey));
		canvas.drawPath(path, paint);
	}

}
