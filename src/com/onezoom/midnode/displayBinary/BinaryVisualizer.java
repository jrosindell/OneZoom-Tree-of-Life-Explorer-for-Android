package com.onezoom.midnode.displayBinary;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

import com.onezoom.midnode.InteriorNode;
import com.onezoom.midnode.LeafNode;
import com.onezoom.midnode.MidNode;
import com.onezoom.midnode.Visualizer;

public class BinaryVisualizer implements Visualizer{
	Paint paint;
	Paint textPaint;
	Paint signPaint;
	Path path;
	private static final int leaftype = 2;
	private static final float partl2 = 0.1f;
	
	public BinaryVisualizer() {
		paint = new Paint();
		textPaint = new Paint();
		signPaint = new Paint();
		path = new Path();
	}
	
	@Override
	public void drawElement(Canvas canvas, MidNode midNode) {
		if (midNode.child1 != null && midNode.positionData.dvar) drawElement(canvas, midNode.child1);
		if (midNode.child2 != null && midNode.positionData.dvar) drawElement(canvas, midNode.child2);
		
		if (midNode.positionData.gvar == false) return;
		MidNode.countDrawElement++;
		if (midNode.getClass() == InteriorNode.class)
			drawElement(canvas, (InteriorNode)midNode);
		else
			drawElement(canvas, (LeafNode)midNode);
	}
	
	private void drawElement(Canvas canvas, InteriorNode midNode) {
		drawBranch(canvas, midNode);
	}

	private void drawElement(Canvas canvas, LeafNode midNode) {
		drawBranch(canvas, midNode);
		drawLeaf(canvas, midNode);
	}
	
	private void drawLeaf(Canvas canvas, LeafNode midNode) {
		tipleaflogic(midNode.positionData.xvar + midNode.positionData.rvar * midNode.positionData.arcx,
				midNode.positionData.yvar + midNode.positionData.rvar * midNode.positionData.arcy, 
				midNode.positionData.rvar * midNode.positionData.arcr, 
				midNode.positionData.arcAngle, canvas, midNode);
	}
	
	@SuppressWarnings("unused")
	private void tipleaflogic(float x, float y, float r, float angle,
			Canvas canvas, LeafNode node) {
		/*
		 * context.strokeStyle = this.leafcolor2(); context.fillStyle =
		 * this.leafcolor1();
		 */
		
		paint.setColor(node.traitsCaculator.getColor());
		if (leaftype == 1) {
			drawleaf1(x, y, r, canvas);
		} else {
			drawleaf2(x, y, r, angle, canvas, node);
		}
	}
	
	private void drawleaf1(float x, float y, float r, Canvas canvas) {
		paint.setStyle(Paint.Style.FILL);
		canvas.drawArc(new RectF((float) (x - r), (float) (y - r),
				(float) (x + r), (float) (y + r)), 0.0f, 360.0f, true, paint);
	}

	private void drawleaf2(float x, float y, float r, float angle, Canvas canvas,LeafNode node) {

		paint.setStyle(Paint.Style.FILL);
		paint.setColor(node.traitsCaculator.getColor());

		float tempsinpre = (float) Math.sin(angle);
		float tempcospre = (float) Math.cos(angle);
		float tempsin90pre = (float) Math.sin(angle + (float) Math.PI / 2.0f);
		float tempcos90pre = (float) Math.cos(angle + (float) Math.PI / 2.0f);

		float startx = x - r * (1 - partl2) * tempcospre;
		float endx = x + r * (1 - partl2) * tempcospre;
		float starty = y - r * (1 - partl2) * tempsinpre;
		float endy = y + r * (1 - partl2) * tempsinpre;
		float midy = (endy - starty) / 3.0f;
		float midx = (endx - startx) / 3.0f;

		Path path = new Path();
		path.moveTo((float) startx, (float) starty);
		path.cubicTo((float) (startx + midx + 2 * r / 2.4 * tempcos90pre),
				(float) (starty + midy + 2 * r / 2.4 * tempsin90pre),
				(float) (startx + 2 * midx + 2 * r / 2.4 * tempcos90pre),
				(float) (starty + 2 * midy + 2 * r / 2.4 * tempsin90pre),
				(float) (endx), (float) (endy));
		path.cubicTo((float) (startx + 2 * midx - 2 * r / 2.4 * tempcos90pre),
				(float) (starty + 2 * midy - 2 * r / 2.4 * tempsin90pre),
				(float) (startx + midx - 2 * r / 2.4 * tempcos90pre),
				(float) (starty + midy - 2 * r / 2.4 * tempsin90pre),
				(float) (startx), (float) (starty));
		canvas.drawPath(path, paint);
	}

	private void drawBranch(Canvas canvas, MidNode node) {
		BinaryTraitsCalculator traits = (BinaryTraitsCalculator) node.traitsCaculator;
		float x = node.positionData.xvar;
		float y = node.positionData.yvar;
		float r = node.positionData.rvar;
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth((float) (node.positionData.rvar * node.positionData.bezr));
		paint.setColor(traits.myColor);
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
