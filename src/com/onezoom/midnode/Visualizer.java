package com.onezoom.midnode;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;

public class Visualizer{
	Paint paint;
	Paint textPaint;
	Paint signTextPaint;
	Paint signPostPaint;
	Path path;
	Canvas canvas;
	public static final float partl2 = 0.1f;
	public static final float Tsize = 1.1f;
	public static final float leafmult = 3.2f;
	public static final float partc = 0.4f;
	private static final float thresholdDrawTextRoughCircle = 80f;
	private static final float thresholdDrawTextDetailCircle = 300f;
	private static final float thresholdDrawTextRoughLeaf = 35f;
	private static final float thresholdDrawTextDetailLeaf = 140f;
	private static final float rangeBaseForDrawSignPost = 120f;
	private static final boolean drawSignPost = true;
		
	public Visualizer() {
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
	
	/**
	 * Call this method to draw a node and its descendants.
	 * 
	 * dvar tests whether the horizon of a node(including its descendants) is within the screen.
	 * 
	 * lengthbr is used in growth animation to select only parts of the tree which is older the time line.
	 * 
	 * Draw node which is inside screen but has small ratio in order add fake leafs to prevent simply
	 * drawing a branch in the end.
	 * 
	 * Draw node whose lengthbr is smaller than time line to add fake leafs
	 * @param midNode
	 */
	private void drawElement(MidNode midNode) {
		if (midNode.child1 != null && midNode.positionData.dvar && midNode.traitsCalculator.getLengthbr() 
				> TraitsData.timelim) drawElement(midNode.child1);
		if (midNode.child2 != null && midNode.positionData.dvar && midNode.traitsCalculator.getLengthbr() 
				> TraitsData.timelim) drawElement(midNode.child2);
		
		if (midNode.positionData.insideScreen && !midNode.positionData.gvar) {
			this.drawBranch(midNode);
			this.drawLeaf(midNode);
		} else if (!midNode.positionData.gvar) {
			return;
		} else if (midNode.positionData.insideScreen &&
			midNode.traitsCalculator.getLengthbr() <= TraitsData.timelim) {
			this.drawBranch(midNode);
			this.drawLeaf(midNode);
		} else if (midNode.getClass() == InteriorNode.class) {
			drawElement((InteriorNode)midNode);
		} else
			drawElement((LeafNode)midNode);
	}
	
	/**
	 * Draw interior node.
	 * @param midNode
	 */
	private void drawElement(InteriorNode midNode) {
		drawBranch(midNode);
		drawCircle(midNode);
		if (midNode.positionData.rvar >= thresholdDrawTextRoughCircle && midNode.positionData.rvar < thresholdDrawTextDetailCircle)
			drawTextRough(midNode);
		else if (midNode.positionData.rvar > thresholdDrawTextDetailCircle)
			drawTextDetail(midNode);
	}

	/**
	 * Draw leaf node
	 * @param midNode
	 */
	private void drawElement(LeafNode midNode) {
		drawBranch(midNode);
		drawLeaf(midNode);
		if (midNode.positionData.rvar >= thresholdDrawTextRoughLeaf && midNode.positionData.rvar < thresholdDrawTextDetailLeaf)
			drawTextRough(midNode);
		else if (midNode.positionData.rvar > thresholdDrawTextDetailLeaf)
			drawTextDetail(midNode);
	}
	
	/**
	 * Draw circle of interior node.
	 * @param midNode
	 */
	private void drawCircle(InteriorNode midNode) {
		float r = midNode.positionData.rvar;
		float R = r * midNode.positionData.arcr;
		float centerX = midNode.positionData.xvar + r * midNode.positionData.arcx;
		float centerY = midNode.positionData.yvar + r * midNode.positionData.arcy;
		RectF rect = new RectF(
				centerX - R * (1 - partl2 / 2.0f),
				centerY - R * (1 - partl2 / 2.0f),	
				centerX + R * (1 - partl2 / 2.0f),	
				centerY + R * (1 - partl2 / 2.0f));
		
		paint.setStrokeWidth(R * partl2);

		paint.setColor(Utility.barccolor(midNode));

		canvas.drawArc(rect, 0, 360, true, paint);
	}
	
	/**
	 * Draw branch of either interior node or leaf node
	 * @param midNode
	 */
	private void drawBranch(MidNode midNode) {
		TraitsData traits = (TraitsData) midNode.traitsCalculator;
		float x = midNode.positionData.xvar;
		float y = midNode.positionData.yvar;
		float r = midNode.positionData.rvar;
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth((float) (midNode.positionData.rvar * midNode.positionData.bezr));
		paint.setColor(traits.getColor());
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
	
	
	/**
	 * Draw leaf.
	 * Sometimes draw interior node as leaf node to make the view looks nice 
	 * @param midNode
	 */
	private void drawLeaf(MidNode midNode) {
		if (midNode.getClass() == InteriorNode.class)
			drawLeaf((InteriorNode)midNode);	
		else if (midNode.getClass() == LeafNode.class) 
			drawLeaf((LeafNode)midNode);
	}
	
	/**
	 * Draw leaf for leaf node.
	 * @param midNode
	 */
	private void drawLeaf(LeafNode midNode) {
		tipleaflogic(
				midNode.positionData.xvar + midNode.positionData.rvar * midNode.positionData.arcx,
				midNode.positionData.yvar + midNode.positionData.rvar * midNode.positionData.arcy, 
				midNode.positionData.rvar * midNode.positionData.arcr, 
				midNode.positionData.arcAngle, midNode);
	}

	/**
	 * Draw fake leaf for interior node.
	 * @param midNode
	 */
	private void drawLeaf(InteriorNode midNode) {
		tipleaflogic(
				midNode.positionData.xvar + midNode.positionData.rvar * midNode.positionData.nextx1,
				midNode.positionData.yvar + midNode.positionData.rvar * midNode.positionData.nexty1, 
				midNode.positionData.rvar * midNode.positionData.arcr2 * 0.55f, 
				midNode.positionData.arcAngle, midNode);
	}
	
	/**
	 * 
	 * @param x
	 * @param y
	 * @param r
	 * @param angle
	 * @param midNode
	 */
	private void tipleaflogic(float x, float y, float r, float angle,
			MidNode midNode) {		
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(midNode.traitsCalculator.getColor());
		drawleaf(x, y, r, angle);
	}
	
	/**
	 * Actual drawing leaf routine.
	 * @param x
	 * @param y
	 * @param r
	 * @param angle
	 */
	private void drawleaf(float x, float y, float r, float angle) {
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
		path.moveTo(startx, starty);
		path.cubicTo(
				startx + midx + 2f * r / 2.4f * tempcos90pre,
				starty + midy + 2f * r / 2.4f * tempsin90pre,
				startx + 2f * midx + 2f * r / 2.4f * tempcos90pre,
				starty + 2f * midy + 2f * r / 2.4f * tempsin90pre,
				endx, 
				endy);
		path.cubicTo(
				startx + 2f * midx - 2f * r / 2.4f * tempcos90pre,
				starty + 2f * midy - 2f * r / 2.4f * tempsin90pre,
				startx + midx - 2f * r / 2.4f * tempcos90pre,
				starty + midy - 2f * r / 2.4f * tempsin90pre,
				startx, 
				starty);
		canvas.drawPath(path, paint);
	}
	
	/**
	 * Draw sign post.
	 * @param midNode
	 */
	private void drawSignPost(MidNode midNode) {
		if (midNode.traitsCalculator.getLengthbr() < TraitsData.timelim || midNode.positionData.dvar == false)
			return;
		// draw sign posts
		boolean signdrawn = false;
		if (midNode.traitsCalculator.getRichness() > 1) {
			if (midNode.child1 != null && midNode.child2 != null) {
				float r = midNode.positionData.rvar;
				float x = midNode.positionData.xvar;
				float y = midNode.positionData.yvar;
				float radius = midNode.positionData.hxmax - midNode.positionData.hxmin;
				if (r * radius > 1f * rangeBaseForDrawSignPost
						&& r * radius < 4f * rangeBaseForDrawSignPost) {
					//if the ratio of the node is appropriate for drawing signpost
					//and it has a common name, then draw sign post on this node
					if (!midNode.traitsCalculator.getCname().equals("null")) {
						drawSignPostCircle(r, x, y, midNode);
						drawSignPostText(r, x, y, midNode);
						signdrawn = true;  //prevent signpost passing down to its descendants
					}

				} else if (r * radius < 1f * rangeBaseForDrawSignPost) {
					//ratio too samll, prevent signpost passing down.
					signdrawn = true;
				}

				if (!signdrawn) {
					//pass sign post drawing to children
					drawSignPost(midNode.child1);
					drawSignPost(midNode.child2);
				}
			}
		}
	}
	
	/**
	 * Draw the transparent circle of signpost.
	 * @param r
	 * @param x
	 * @param y
	 * @param midNode
	 */
	private void drawSignPostCircle(float r, float x, float y, MidNode midNode) {
		float centerX = x + r * (midNode.positionData.hxmax + midNode.positionData.hxmin) / 2;
		float centerY = y + r * (midNode.positionData.hymax + midNode.positionData.hymin) / 2;
		float radius = r * (midNode.positionData.hxmax - midNode.positionData.hxmin) * midNode.positionData.arcr;
		canvas.drawCircle(centerX, centerY, radius, signPostPaint);
	}
	
	/**
	 * Draw text on signpost
	 * @param r
	 * @param x
	 * @param y
	 * @param midNode
	 */
	private void drawSignPostText(float r, float x, float y, MidNode midNode) {
		float centerX = x + r * (midNode.positionData.hxmax + midNode.positionData.hxmin) / 2;
		float centerY = y + r * (midNode.positionData.hymax + midNode.positionData.hymin) / 2;
		float radius = r * (midNode.positionData.hxmax - midNode.positionData.hxmin) * midNode.positionData.arcr;

		if (midNode.traitsCalculator.getSignName() == null)
			midNode.traitsCalculator.setSignName(splitStringToAtMostThreeParts(midNode.traitsCalculator.getCname()));
		drawTextMultipleLines(midNode.traitsCalculator.getSignName(), centerX, centerY, 2f * radius, signTextPaint);
	}
	
	/**
	 * Draw rough text on interior node when ratio is small.
	 * @param midNode
	 */
	private void drawTextRough(InteriorNode midNode) {
		float x = midNode.positionData.xvar;
		float y = midNode.positionData.yvar;
		float r = midNode.positionData.rvar;
		float radius = r * midNode.positionData.arcr * (1 - partl2 / 2.0f);
		float startX = x + midNode.positionData.arcr / 2f + r * midNode.positionData.arcx - radius;
		float startY = y + midNode.positionData.arcr / 2f + r * midNode.positionData.arcy - radius;

		String outputInfo = Float.toString(midNode.traitsCalculator.getRichness());

		drawTextOneLine(outputInfo, startX + radius, startY + 0.5f * radius,
				radius, textPaint);

		if (!midNode.traitsCalculator.getCname().equals("null")) {
			outputInfo = midNode.traitsCalculator.getCname();
			this.drawTextOnTwoLines(outputInfo, startX + radius, startY + 0.9f * radius,
					0.4f * radius, 1.65f * radius, textPaint);		
		} else {
			outputInfo = String.format("%.1f", midNode.traitsCalculator.getLengthbr()) + " Mya";
			this.drawTextOnTwoLines(outputInfo, startX + radius, startY + 1.1f * radius,
					0.4f * radius, 2.0f * radius, textPaint);		
		}
	}
	
	/**
	 * Draw text in detail on interior node when ratio is high.
	 * @param midNode
	 */
	private void drawTextDetail(InteriorNode midNode) {
		float x = midNode.positionData.xvar;
		float y = midNode.positionData.yvar;
		float r = midNode.positionData.rvar;
		float radius = r * midNode.positionData.arcr * (1 - partl2 / 2.0f);
		float startX = x + midNode.positionData.arcr / 2f + r * midNode.positionData.arcx;
		float startY = y + midNode.positionData.arcr / 2f + r * midNode.positionData.arcy - radius * 0.6f;
		float lineHeight = radius;

	
		if (!midNode.traitsCalculator.getCname().equals("null") &&
				!midNode.traitsCalculator.getCname().equals("")) {
			String name = midNode.traitsCalculator.getCname();
			this.drawTextOneLine(Utility.geologicAge(midNode), 
					startX, startY, lineHeight, textPaint);
			startY += lineHeight / 4.5f;
			this.drawTextOneLine(String.format("%.1f", midNode.traitsCalculator.getLengthbr()) + " million years ago",
					startX, startY, lineHeight * 1.7f, textPaint);
			startY += lineHeight / 2.5f;
			this.drawTextOnTwoLines(name, 
					startX, startY,lineHeight / 3f, lineHeight * 1.5f, textPaint);	
			startY += lineHeight / 1.5f;
			this.drawTextOneLine((midNode.traitsCalculator.getRichness() + " species"),
					startX, startY, lineHeight, textPaint);
		} else {
			String speciesInfo = Integer
					.toString(midNode.traitsCalculator.getRichness()) + " species";
			this.drawTextOneLine(Utility.geologicAge(midNode), 
					startX, startY, lineHeight, textPaint);
			startY += lineHeight / 3f;
			this.drawTextOneLine(String.format("%.1f", midNode.traitsCalculator.getLengthbr()) + " million years ago",
					startX, startY, lineHeight * 1.7f, textPaint);
			startY += lineHeight / 1.7f;
			this.drawTextOneLine(speciesInfo, startX, startY, lineHeight * 1.7f, textPaint);
		}
	}
	
	/**
	 * Draw rough text on leaf node when ratio is small
	 * @param midNode
	 */
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
		String latinName = midNode.traitsCalculator.getLatinName();
		String commonName = midNode.traitsCalculator.getCname();
		
		if (!commonName.equals("null") && !commonName.equals("")) {
			drawTextMultipleLines(commonName.split(" "), startX, startY, lineHeight,
					lineWidth, textPaint);
			return;
		} else if (!latinName.equals("null") && !latinName.equals("")) {
			drawTextMultipleLines(latinName.split(" "), startX, startY, lineHeight,
					lineWidth, textPaint);
			return;
		} else {
			String tempName = "no name";
			drawTextMultipleLines(tempName.split(" "), startX, startY,
					lineHeight, lineWidth, textPaint);
			return;
		}		
	}
	
	/**
	 * Draw text in details on leaf node.
	 * @param midNode
	 */
	private void drawTextDetail(LeafNode midNode) {
		float startX, startY, lineWidth, lineHeight;
		float startWikiX, startWikiY, startArkiveX, startArkiveY;
		startWikiX = midNode.positionData.getWikiX();
		startWikiY = midNode.positionData.getWikiY();
		startArkiveX = midNode.positionData.getArkiveX();
		startArkiveY = midNode.positionData.getArkiveY();
		startX = (startWikiX + startArkiveX)/2;
		startY = startWikiY;
		lineHeight = 1.3f * midNode.positionData.rvar * midNode.positionData.arcr;
		lineWidth = 1.5f * midNode.positionData.rvar * midNode.positionData.arcr;

		String latinName = midNode.traitsCalculator.getLatinName();
		String commonName = midNode.traitsCalculator.getCname();
		String conservationString = Utility.conservationStatus(midNode);

		if( !commonName.equals("null") && !commonName.equals("")){
			drawLink(startWikiX, startWikiY, midNode.positionData.getLinkRadius(), "Wiki");
			drawLink(startArkiveX, startArkiveY, midNode.positionData.getLinkRadius(), "ARKive");
			startY += lineWidth / 6f;
			drawTextOneLine(latinName, startX, startY, lineWidth / 1.5f, textPaint);
			startY += lineHeight / 5f;
			drawTextOnTwoLines(commonName, startX, startY, lineHeight / 6f, lineWidth * 0.7f, textPaint);
			startY += lineHeight / 3f;
			drawTextOneLine(conservationString,startX, startY, lineWidth / 1.5f, textPaint);
		}
		else {
			drawLink(startWikiX, startWikiY, midNode.positionData.getLinkRadius(), "Wiki");
			drawLink(startArkiveX, startArkiveY, midNode.positionData.getLinkRadius(), "ARKive");
			startY += lineHeight / 6f;
			drawTextOneLine("no common name", startX, startY, lineWidth / 1.5f, textPaint);
			startY += lineHeight / 5f;
			drawTextOnTwoLines(latinName,
					startX, startY, lineHeight / 6f, lineWidth * 0.7f, textPaint);
			startY += lineHeight / 3f;
			drawTextOneLine(conservationString,startX, startY, lineWidth / 1.5f, textPaint);
		}
		
	}

	/**
	 * Draw link
	 * @param x
	 * @param y
	 * @param radius
	 * @param linkName
	 */
	private void drawLink(float x, float y, float radius, String linkName) {
		Paint wikiPaint = new Paint();
		wikiPaint.setColor(Color.WHITE);
		wikiPaint.setStyle(Paint.Style.STROKE);
		wikiPaint.setStrokeWidth(radius / 10);
		canvas.drawCircle(x, y, radius, wikiPaint);
		textPaint.setTextSize(radius / linkName.length() * 3f);
		canvas.drawText(linkName, x, y + radius * 0.15f, textPaint);
	}

	
	private void drawTextOneLine(String outputInfo, float startX, float startY, float lineWidth, Paint paint) {
		paint.setTextAlign(Align.CENTER);
		paint.setTextSize(1.7f * lineWidth / outputInfo.length());
		canvas.drawText(outputInfo, startX, startY, paint);
	}
	
	private void drawTextOnTwoLines(String text, float startX, float startY, float lineHeight,
			float lineWidth, Paint textPaint) {
		String[] pieces = text.split(" ");
		
		if (pieces.length > 4) {
			int cut = (pieces.length) / 2;
			for (int i = 1; i < cut; i++) {
				pieces[0] += " " + pieces[i];
			}
			pieces[1] = pieces[cut];
			for (int i = cut + 1; i < pieces.length; i++) {
				pieces[1] += " " + pieces[i]; 
			}
			drawTextOnTwoLines(pieces, startX, startY, lineHeight, lineWidth, textPaint);
		} else if (pieces.length == 4) {
		
			if (pieces[0].length() > pieces[1].length() + pieces[2].length() + pieces[3].length()) {
				pieces[1] += " " + pieces[2] + " " + pieces[3];
			} else if (pieces[3].length() > pieces[0].length() + pieces[1].length() + pieces[2].length()) {
				pieces[0] += " " + pieces[1] + " " + pieces[2];
				pieces[1] = pieces[3];
			} else {
				pieces[0] += " " + pieces[1];
				pieces[1] = pieces[2] + " " + pieces[3];
			}
			drawTextOnTwoLines(pieces, startX, startY, lineHeight, lineWidth, textPaint);
			
		} else if (pieces.length == 3) {

			if (pieces[0].length() > pieces[2].length()) {
				pieces[1] += " " + pieces[2];
			} else {
				pieces[0] += " " + pieces[1];
				pieces[1] = pieces[2];
			}
			drawTextOnTwoLines(pieces, startX, startY, lineHeight, lineWidth, textPaint);

		} else if (pieces.length == 2) {
			drawTextOnTwoLines(pieces, startX, startY, lineHeight, lineWidth, textPaint);
		} else if (pieces.length == 1) {
			drawTextOneLine(pieces[0], startX, startY + lineHeight / 2f, lineWidth, textPaint, 7);
		}
	}
	
	private void drawTextOnTwoLines(String[] text, float startX, float startY, float lineHeight,
			float lineWidth, Paint textPaint) {
		drawTextOneLine(text[0], startX, startY, lineWidth, textPaint, 7);
		drawTextOneLine(text[1], startX, startY + lineHeight, lineWidth, textPaint, 7);
	}

	private void drawTextOneLine(String text, float startX, float startY,
			float lineWidth, Paint textPaint, int minTextLength) {
		textPaint.setTextAlign(Align.CENTER);
		textPaint.setTextSize(1.7f * lineWidth / Math.max(text.length(), minTextLength));
		canvas.drawText(text, startX, startY, textPaint);		
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
}
