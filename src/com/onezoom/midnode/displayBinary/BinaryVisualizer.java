package com.onezoom.midnode.displayBinary;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;

import com.onezoom.midnode.InteriorNode;
import com.onezoom.midnode.LeafNode;
import com.onezoom.midnode.MidNode;
import com.onezoom.midnode.Utility;

public class BinaryVisualizer{
	Paint paint;
	Paint textPaint;
	Paint signTextPaint;
	Paint signPostPaint;
	Path path;
	Canvas canvas;
	private static final float partl2 = 0.1f;
	private static final int leaftype = 2;
	private static final float Tsize = 1.1f;
	private static final float leafmult = 3.2f;
	private static final float partc = 0.4f;
	private static final float thresholdDrawTextRoughCircle = 80f;
	private static final float thresholdDrawTextDetailCircle = 300f;
	private static final float thresholdDrawTextRoughLeaf = 35f;
	private static final float thresholdDrawTextDetailLeaf = 140f;
	private static final float rangeBaseForDrawSignPost = 120f;
	private static final boolean drawSignPost = true;
		
	public BinaryVisualizer() {
		paint = new Paint();
		textPaint = new Paint();
		textPaint.setColor(Color.WHITE);
		signPostPaint = new Paint();
		signPostPaint.setStyle(Style.FILL);
		signPostPaint.setARGB(190, 255, 255, 255);
		signTextPaint = new Paint();
		signTextPaint.setColor(Color.BLACK);
		path = new Path();
	}
	
	
	public void drawTree(Canvas canvas, MidNode midNode) {
		this.canvas = canvas;
		drawElement(midNode);
		if (drawSignPost) drawSignPost(midNode);
	}
	
	private void drawElement(MidNode midNode) {
		if (midNode.child1 != null && midNode.positionData.dvar && midNode.traitsCaculator.getLengthbr() 
				> BinaryTraitsCalculator.timelim) drawElement(midNode.child1);
		if (midNode.child2 != null && midNode.positionData.dvar && midNode.traitsCaculator.getLengthbr() 
				> BinaryTraitsCalculator.timelim) drawElement(midNode.child2);
		
//		MidNode.countVisitedElement++;
		if (!midNode.positionData.gvar) {
			if (midNode.positionData.insideScreen)
				drawFakeLeaf(midNode);
			return;
		}
//		MidNode.countDrawElement++;
		if (midNode.getClass() == InteriorNode.class)
			drawElement((InteriorNode)midNode);
		else
			drawElement((LeafNode)midNode);
	}
	
	private void drawFakeLeaf(MidNode midNode) {
		drawBranch(midNode);
		if (midNode.getClass() == InteriorNode.class)
			drawLeaf((InteriorNode)midNode);
		else
			drawLeaf((LeafNode)midNode);	}

	private void drawElement(InteriorNode midNode) {
		drawBranch(midNode);
		drawCircle(midNode);
		if (midNode.positionData.rvar >= thresholdDrawTextRoughCircle && midNode.positionData.rvar < thresholdDrawTextDetailCircle)
			drawTextRough(midNode);
		else if (midNode.positionData.rvar > thresholdDrawTextDetailCircle)
			drawTextDetail(midNode);
	}

	private void drawElement(LeafNode midNode) {
		drawBranch(midNode);
		drawLeaf(midNode);
		if (midNode.positionData.rvar >= thresholdDrawTextRoughLeaf && midNode.positionData.rvar < thresholdDrawTextDetailLeaf)
			drawTextRough(midNode);
		else if (midNode.positionData.rvar > thresholdDrawTextDetailLeaf)
			drawTextDetail(midNode);
	}
	
	private void drawSignPost(MidNode midNode) {
		if (midNode.traitsCaculator.getLengthbr() < BinaryTraitsCalculator.timelim || midNode.positionData.dvar == false)
			return;
		// draw sign posts
		boolean signdrawn = false;
		if (midNode.traitsCaculator.getRichness() > 1) {
			if (midNode.child1 != null && midNode.child2 != null) {
				float r = midNode.positionData.rvar;
				float x = midNode.positionData.xvar;
				float y = midNode.positionData.yvar;
				float radius = midNode.positionData.hxmax
						- midNode.positionData.hxmin;
				if (r * radius > 1f * rangeBaseForDrawSignPost
						&& r * radius < 4f * rangeBaseForDrawSignPost) {
					if (!midNode.traitsCaculator.getCname().equals("null")) // white
																	// signposts
					{
						drawSignPostCircle(r, x, y, midNode);
						drawSignPostText(r, x, y, midNode);
						signdrawn = true;

					}

				} else if (r * radius < 1f * rangeBaseForDrawSignPost) {
					signdrawn = true;
				}

				if (!signdrawn) {
					drawSignPost(midNode.child1);
					drawSignPost(midNode.child2);
				}
			}
		}
	}
	
	private void drawSignPostText(float r, float x, float y, MidNode midNode) {
		float centerX = x + r 
				* (midNode.positionData.hxmax + midNode.positionData.hxmin) / 2;
		float centerY = y + r 
				* (midNode.positionData.hymax + midNode.positionData.hymin) / 2;
		float radius = r * (midNode.positionData.hxmax - midNode.positionData.hxmin) * midNode.positionData.arcr;

		if (midNode.traitsCaculator.signName == null)
			midNode.traitsCaculator.signName = splitStringToAtMostThreeParts(midNode.traitsCaculator.getCname());
		drawTextMultipleLines(midNode.traitsCaculator.signName, centerX, centerY, 2f * radius, signTextPaint);
	}

	private void drawSignPostCircle(float r, float x, float y, MidNode midNode) {
		float centerX = x + r 
				* (midNode.positionData.hxmax + midNode.positionData.hxmin) / 2;
		float centerY = y + r 
				* (midNode.positionData.hymax + midNode.positionData.hymin) / 2;
		float radius = r * (midNode.positionData.hxmax - midNode.positionData.hxmin) * midNode.positionData.arcr;
		canvas.drawCircle(centerX, centerY, radius, signPostPaint);
	}
	
	private void drawTextDetail(InteriorNode midNode) {
		float x = midNode.positionData.xvar;
		float y = midNode.positionData.yvar;
		float r = midNode.positionData.rvar;
		float radius = r * midNode.positionData.arcr * (1 - partl2 / 2.0f);
		float startX = x + midNode.positionData.arcr / 2f + r * midNode.positionData.arcx - radius;
		float startY = y + midNode.positionData.arcr / 2f + r * midNode.positionData.arcy - radius;
		float lineWidth = 2f * radius;
		float lineHeight = 1.9f * radius;

		String speciesInfo = (!midNode.traitsCaculator.getCname().equals("null")) ? midNode.traitsCaculator.getCname() : Integer
				.toString(midNode.traitsCaculator.getRichness()) + " species";
		String[] circleDetailText = { Utility.geologicAge(midNode),
				String.format("%.1f", midNode.traitsCaculator.getLengthbr()) + " million years ago",
				speciesInfo};

		drawTextMultipleLines(circleDetailText, startX + radius, startY + 0.45f
				* radius, lineHeight, lineWidth, textPaint);		
	}

	private void drawTextRough(InteriorNode midNode) {
		float x = midNode.positionData.xvar;
		float y = midNode.positionData.yvar;
		float r = midNode.positionData.rvar;
		float radius = r * midNode.positionData.arcr * (1 - partl2 / 2.0f);
		float startX = x + midNode.positionData.arcr / 2f + r * midNode.positionData.arcx - radius;
		float startY = y + midNode.positionData.arcr / 2f + r * midNode.positionData.arcy - radius;

		String outputInfo = Float.toString(midNode.traitsCaculator.getRichness());

		drawTextOneLine(outputInfo, startX + radius, startY + 0.5f * radius,
				radius, textPaint);

		if (!midNode.traitsCaculator.getCname().equals("null")) {
			outputInfo = midNode.traitsCaculator.getCname();
		} else {
			outputInfo = String.format("%.1f", midNode.traitsCaculator.getLengthbr()) + " Mya";
		}

		drawTextOneLine(outputInfo, startX + radius, startY + 1.1f * radius,
				1.7f * radius, textPaint);		
	}

	private void drawTextDetail(LeafNode midNode) {
		float x = midNode.positionData.xvar;
		float y = midNode.positionData.yvar;
		float r = midNode.positionData.rvar;
		float temp_theight = ((r * leafmult * partc - r * leafmult * partl2)
				* Tsize / 3.0f);

		float startX, startY, lineWidth, lineHeight;
		startX = x + r * midNode.positionData.arcx;
		// startY = y + r * arcCenterY;
		startY = y + r * midNode.positionData.arcy - temp_theight * 1.75f;
		lineHeight = 1.3f * r * midNode.positionData.arcr;
		lineWidth = 1.5f * r * midNode.positionData.arcr;

		String name;
		if (!midNode.traitsCaculator.getName1().equals("null") && !midNode.traitsCaculator.getName2().equals("null"))
			name = midNode.traitsCaculator.getName2() + " " + midNode.traitsCaculator.getName1();
		else if (!midNode.traitsCaculator.getName1().equals("null") && !midNode.traitsCaculator.getName2().equals("null"))
			name = midNode.traitsCaculator.getName2();
		else if (!midNode.traitsCaculator.getName1().equals("null") && !midNode.traitsCaculator.getName2().equals("null"))
			name = midNode.traitsCaculator.getName1();
		else
			name = "no name";
		String conservationString = Utility.conservationStatus(midNode);
		String populationString = Utility.populationStability(midNode);
	
		if( !midNode.traitsCaculator.getCname().equals("null")){
			String[] detailInfo = { name, midNode.traitsCaculator.getCname(), conservationString,
					populationString };
			drawTextMultipleLines(detailInfo, startX, startY, lineHeight,
					lineWidth, textPaint);
		}
		else {
			String[]  detailInfo = {name, conservationString, populationString};
			drawTextMultipleLines(detailInfo, startX, startY, lineHeight,
					lineWidth, textPaint);
		}
		
	}

	private void drawTextRough(LeafNode midNode) {
		float x = midNode.positionData.xvar;
		float y = midNode.positionData.yvar;
		float r = midNode.positionData.rvar;
	
		float temp_theight = ((r * leafmult * partc - r * leafmult * partl2)
				* Tsize / 3.0f);
		float startX, startY, lineWidth, lineHeight;
		startX = x + r * midNode.positionData.arcx;
		startY = y + r * midNode.positionData.arcy - 0.5f * temp_theight;
		lineHeight = r * midNode.positionData.arcr;
		lineWidth = r * midNode.positionData.arcr;

		if (!midNode.traitsCaculator.getCname().equals("null")) {
			drawTextMultipleLines(midNode.traitsCaculator.getCname().split(" "), startX, startY, lineHeight,
					lineWidth, textPaint);
			return;
		} else if (!midNode.traitsCaculator.getName2().equals("null")) {
			drawTextMultipleLines(midNode.traitsCaculator.getName2().split(" "), startX, startY, lineHeight,
					lineWidth, textPaint);
			return;
		} else if (!midNode.traitsCaculator.getName1().equals("null")) {
			drawTextMultipleLines(midNode.traitsCaculator.getName1().split(" "), startX, startY, lineHeight,
					lineWidth, textPaint);
			return;
		} else {
			String tempName = "no name";
			drawTextMultipleLines(tempName.split(" "), startX, startY,
					lineHeight, lineWidth, textPaint);
			return;
		}		
	}
	
	private void drawTextOneLine(String outputInfo, float startX, float startY, float lineWidth, Paint paint) {
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(1.7f * lineWidth / outputInfo.length());
		canvas.drawText(outputInfo, startX, startY, paint);
	}

	private void drawTextMultipleLines(String[] split, float startX,
			float startY, float lineHeight, float lineWidth, Paint textPaint) {
		float lineSpace = lineHeight / (split.length + 1);
		float minTextSize = 9999f;
		float tempTextSize;
		float factor;
		if (split.length <= 2) factor = 1.7f;
		else if (split.length == 3) factor = 1.5f;
		else if (split.length == 4) factor = 1.2f;
		else factor = 1.0f;
		for (String string : split) {
			tempTextSize = factor * lineWidth / string.length();
			if (minTextSize > tempTextSize)
				minTextSize = tempTextSize;
		}
		minTextSize = Math.max(minTextSize, lineSpace / 4);
		for (String string : split) {
			drawTextOneLine(string, startX, startY, minTextSize, true, textPaint);
			startY += lineSpace;
		}	
	}
	
	private void drawTextMultipleLines(String[] split, float startX,
			float startY, float radius, Paint textPaint) {
		float lineSpace = radius / (split.length + 1);
		float minTextSize = 9999f;
		
		if (split.length == 2) {
			startY = startY - radius/7f;
			minTextSize = Math.min(1.55f * radius / split[0].length(), 1.55f * radius / split[1].length());
		} else if (split.length == 3) {
			startY = startY - radius/5f;
			minTextSize = Math.min(Math.min(1.35f * radius / split[0].length(), 1.55f * radius / split[0].length()), 
					1.35f * radius / split[2].length());
		} else if (split.length == 1) {
			minTextSize = 1.55f * radius / split[0].length();
		}
		
		for (String string : split) {
			drawTextOneLine(string, startX, startY, minTextSize, true, textPaint);
			startY += lineSpace;
		}	
	}

	private void drawTextOneLine(String string, float startX, float startY,
			float minTextSize, boolean b, Paint paint) {
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(minTextSize);
		canvas.drawText(string, startX, startY, paint);		
	}

	private void drawCircle(InteriorNode midNode) {
		float x = midNode.positionData.xvar;
		float y = midNode.positionData.yvar;
		float r = midNode.positionData.rvar;
		RectF rect = new RectF((float) (x + r * (midNode.positionData.arcx) - r * midNode.positionData.arcr
				* (1 - partl2 / 2.0f)), (float) (y + r * midNode.positionData.arcy - r
				* midNode.positionData.arcr * (1 - partl2 / 2.0f)), (float) (x + r
				* (midNode.positionData.arcx) + r * midNode.positionData.arcr * (1 - partl2 / 2.0f)),
				(float) (y + r * midNode.positionData.arcy + r * midNode.positionData.arcr
						* (1 - partl2 / 2.0f)));
		paint.setStrokeWidth((float) (r * midNode.positionData.arcr * partl2));

		paint.setColor(Utility.barccolor(midNode));

		canvas.drawArc(rect, 0, 360, true, paint);
	}
	
	private void drawLeaf(LeafNode midNode) {
		tipleaflogic(midNode.positionData.xvar + midNode.positionData.rvar * midNode.positionData.arcx,
				midNode.positionData.yvar + midNode.positionData.rvar * midNode.positionData.arcy, 
				midNode.positionData.rvar * midNode.positionData.arcr, 
				midNode.positionData.arcAngle, midNode);
	}
	
	private void drawLeaf(InteriorNode midNode) {
		tipleaflogic(midNode.positionData.xvar + midNode.positionData.rvar * midNode.positionData.arcx2,
				midNode.positionData.yvar + midNode.positionData.rvar * midNode.positionData.arcy2, 
				midNode.positionData.rvar * midNode.positionData.arcr2, 
				midNode.positionData.arcAngle, midNode);
	}
	
	@SuppressWarnings("unused")
	private void tipleaflogic(float x, float y, float r, float angle,
			MidNode midNode) {
		/*
		 * context.strokeStyle = this.leafcolor2(); context.fillStyle =
		 * this.leafcolor1();
		 */
		
		paint.setColor(midNode.traitsCaculator.getColor());
		if (leaftype == 1) {
			drawleaf1(x, y, r);
		} else {
			drawleaf2(x, y, r, angle, midNode);
		}
	}
	
	private void drawleaf1(float x, float y, float r) {
		paint.setStyle(Paint.Style.FILL);
		canvas.drawArc(new RectF((float) (x - r), (float) (y - r),
				(float) (x + r), (float) (y + r)), 0.0f, 360.0f, true, paint);
	}

	private void drawleaf2(float x, float y, float r, float angle,MidNode midNode) {

		paint.setStyle(Paint.Style.FILL);
		paint.setColor(midNode.traitsCaculator.getColor());

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

	private void drawBranch(MidNode midNode) {
		BinaryTraitsCalculator traits = (BinaryTraitsCalculator) midNode.traitsCaculator;
		float x = midNode.positionData.xvar;
		float y = midNode.positionData.yvar;
		float r = midNode.positionData.rvar;
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth((float) (midNode.positionData.rvar * midNode.positionData.bezr));
		paint.setColor(traits.myColor);
		paint.setStyle(Paint.Style.STROKE);
		path.reset();
		path.moveTo(
				(float) (x + r * midNode.positionData.bezsx), 
				(float) (y + r * midNode.positionData.bezsy));
		path.cubicTo(
				(float) (x + r * midNode.positionData.bezc1x),
				(float) (y + r * midNode.positionData.bezc1y), 
				(float) (x + r * midNode.positionData.bezc2x),
				(float) (y + r * midNode.positionData.bezc2y), 
				(float) (x + r * midNode.positionData.bezex), 
				(float) (y + r * midNode.positionData.bezey));
		canvas.drawPath(path, paint);
	}
	
	private String[] splitStringToAtMostThreeParts(String cname) {
		int centerpoint = cname.length() / 4;
		String[] splitstr = cname.split(" ");
		String print1 = "";
		String print2 = "";
		String print3 = "";
		String[] result = new String[3];
		if (splitstr.length == 1) {
			return splitstr;
		} else if (splitstr.length == 2) {
			return splitStringToTwoParts(cname);
		} else if (splitstr.length == 3) {
			print1 = splitstr[0];
			print2 = splitstr[1];
			print3 = splitstr[2];
		} else {
			for (int i = splitstr.length - 1; i >= 0; i--) {
				if (print3.length() >= centerpoint) {
					if (print2.length() >= centerpoint) {
						print1 = " " + splitstr[i] + print1;
					} else {
						print2 = " " + splitstr[i] + print2;
					}
				} else {
					print3 = " " + splitstr[i] + print3;
				}
			}
		}
		
		if ((print1.length() >= (print2.length() + print3.length())) || (print3.length() >= (print1.length() + print2.length()))) {
			return splitStringToTwoParts(cname);
		} else {
			result[0] = print1;
			result[1] = print2;
			result[2] = print3;
			return result;
		}
	}
	
	private String[] splitStringToTwoParts(String cname) {
		int centerpoint = cname.length() / 3;
		String[] splitstr = cname.split(" ");
		String print1 = " ";
		String print2 = " ";
		String[] result = new String[2];
		if (splitstr.length == 1) return splitstr;
		else if (splitstr.length == 2) {
			print1 = splitstr[0];
			print2 = splitstr[1];
		} else {
			for (int i = splitstr.length - 1; i >= 0; i--) {
				if (print2.length() >= centerpoint) {
						print1 = " " + splitstr[i] + print1;
					} else {
						print2 = " " + splitstr[i] + print2;
					}
			}
		}
		result[0] = print1;
		result[1] = print2;
		return result;
	}


	//**********DEBUG FUNCTION******************//
	@SuppressWarnings("unused")
	private void drawBoundingBox(MidNode midNode) {
		float x = midNode.positionData.xvar;
		float y = midNode.positionData.yvar;
		float r = midNode.positionData.rvar;
		Paint paint = new Paint();
		paint.setColor(Color.RED);
		Path path = new Path();
		path.moveTo(midNode.positionData.hxmax * r + x, y + r * midNode.positionData.hymax);
		path.lineTo(midNode.positionData.hxmax * r + x, y + r * midNode.positionData.hymin);
		path.lineTo(midNode.positionData.hxmin * r + x, y + r * midNode.positionData.hymin);
		path.lineTo(midNode.positionData.hxmin * r + x, y + r * midNode.positionData.hymax);
		path.lineTo(midNode.positionData.hxmax * r + x, y + r * midNode.positionData.hymax);
		canvas.drawPath(path, paint);
	}
	
		@SuppressWarnings("unused")
		private void drawBoundingBox2(MidNode midNode) {
			float x = midNode.positionData.xvar;
			float y = midNode.positionData.yvar;
			float r = midNode.positionData.rvar;
			Paint paint = new Paint();
			paint.setColor(Color.RED);
			Path path = new Path();
			path.moveTo(midNode.positionData.gxmax * r + x, y + r * midNode.positionData.gymax);
			path.lineTo(midNode.positionData.gxmax * r + x, y + r * midNode.positionData.gymin);
			path.lineTo(midNode.positionData.gxmin * r + x, y + r * midNode.positionData.gymin);
			path.lineTo(midNode.positionData.gxmin * r + x, y + r * midNode.positionData.gymax);
			path.lineTo(midNode.positionData.gxmax * r + x, y + r * midNode.positionData.gymax);
			canvas.drawPath(path, paint);
		}
}
