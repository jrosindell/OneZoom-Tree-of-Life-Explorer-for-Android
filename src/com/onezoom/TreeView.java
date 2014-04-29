package com.onezoom;

import com.onezoom.midnode.PositionData;
import com.onezoom.midnode.Utility;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

public class TreeView extends View {
	private CanvasActivity client;
	private GestureDetector gestureDetector;
	private ScaleGestureDetector scaleDetector;
	private boolean treeBeingInitialized = false;
	private boolean duringRecalculation = false;
	private boolean duringInteraction = false;
	private boolean duringGrowthAnimation = false;
	private Bitmap cachedBitmap;
	private Paint paint;
	private boolean toggle = true;
	private float distanceX, distanceY, scaleX, scaleY, scaleCenterX, scaleCenterY;
	public static boolean onScale;
	
	
	
	public TreeView(Context context) {
		super(context);
		init(context);
	}
	
	public TreeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}
	
	public TreeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}
	
	private void init(Context context) {
		client = (CanvasActivity) context;
		gestureDetector = new GestureDetector(context, new GestureListener(this));
		scaleDetector = new ScaleGestureDetector(context, new ScaleListener(this));
		cachedBitmap = Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888); //this will not be used. set to 1,1 to speed up the app
		paint = new Paint();
	}
	

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			duringInteraction = true;
			break;
		case MotionEvent.ACTION_UP:
			onScale = false;
			duringInteraction = false;
			break;
		default:
				break;
		}

		scaleDetector.onTouchEvent(event);
		if (!onScale)
			gestureDetector.onTouchEvent(event);
	
		invalidate();
		return true;
	}

	
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		if (!treeBeingInitialized) {
			drawLoading(canvas);
		} else {
			if (!duringRecalculation && !duringInteraction){
				client.getTreeRoot().outputInitElement();
				drawElementAndCache(canvas);
			} else {
				canvas.translate(distanceX, distanceY);
				canvas.scale(scaleX, scaleY, scaleCenterX, scaleCenterY);
				canvas.drawBitmap(cachedBitmap, null, new Rect(0, 0, getWidth(),getHeight()), paint);
			}
		}
	}
	
	private void drawElementAndCache(Canvas canvas) {	
		if (toggle) {
			toggle = !toggle;
			canvas.translate(distanceX, distanceY);
			canvas.scale(scaleX, scaleY, scaleCenterX, scaleCenterY);
			canvas.drawBitmap(cachedBitmap, null, new Rect(0, 0, getWidth(),getHeight()), paint);
			this.scaleX = 1;
			this.scaleY = 1;
			this.distanceX = 0;
			this.distanceY = 0;
			cachedBitmap = loadBitmapFromView(this);
		} else {
			client.getTreeRoot().drawElement(canvas);
			if (this.isDuringGrowthAnimation()) {
				drawGrowthPeriodInfo(canvas, paint);
			}
			toggle = !toggle;
		}
		duringInteraction = true;
		invalidate();		
	}

	private Bitmap loadBitmapFromView(TreeView v) {
		if (v == null) {
			return null;
		}
		Bitmap bitmap = Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bitmap);
		c.translate(-v.getScrollX(), -v.getScrollY());
		v.draw(c);
		return bitmap;
	}

	public void drag(float distanceX, float distanceY) {
		PositionData.shiftScreenPosition(distanceX, distanceY, 1f);
		duringRecalculation = true;
		client.recalculate();
	}
	
	public void zoomin(float factor) {
		PositionData.shiftScreenPosition(0, 0, factor);
		duringRecalculation = true;
		client.recalculate();
	}
	
	public void zoomin(float scaleFactor, float focusX, float focusY) {	
		PositionData.shiftScreenPosition(focusX, focusY, scaleFactor);
		duringRecalculation = true;
		client.recalculate();
	}
	
	public boolean isTreeBeingInitialized() {
		return treeBeingInitialized;
	}

	public void setTreeBeingInitialized(boolean treeBeingInitialized) {
		this.treeBeingInitialized = treeBeingInitialized;
	}

	public boolean isDuringRecalculation() {
		return duringRecalculation;
	}

	public void setDuringRecalculation(boolean duringRecalculation) {
		this.duringRecalculation = duringRecalculation;
	}

	public float getDistanceX() {
		return distanceX;
	}

	public float getDistanceY() {
		return distanceY;
	}

	public float getScaleX() {
		return scaleX;
	}

	public float getScaleY() {
		return scaleY;
	}

	public float getScaleCenterX() {
		return scaleCenterX;
	}

	public float getScaleCenterY() {
		return scaleCenterY;
	}

	public void setDistanceX(float distanceX) {
		this.distanceX = distanceX;
	}

	public void setDistanceY(float distanceY) {
		this.distanceY = distanceY;
	}

	public void setScaleX(float scaleX) {
		this.scaleX = scaleX;
	}

	public void setScaleY(float scaleY) {
		this.scaleY = scaleY;
	}

	public void setScaleCenterX(float scaleCenterX) {
		this.scaleCenterX = scaleCenterX;
	}

	public void setScaleCenterY(float scaleCenterY) {
		this.scaleCenterY = scaleCenterY;
	}
	
	public boolean isDuringInteraction() {
		return duringInteraction;
	}

	public void setDuringInteraction(boolean duringInteraction) {
		this.duringInteraction = duringInteraction;
	}

	public boolean isDuringGrowthAnimation() {
		return duringGrowthAnimation;
	}

	public void setDuringGrowthAnimation(boolean duringGrowthAnimation) {
		this.duringGrowthAnimation = duringGrowthAnimation;
	}

	private void drawLoading(Canvas canvas) {
		String text = "loading...";
		Paint textPaint = new Paint();
		textPaint.setColor(Color.GREEN);
		textPaint.setTextAlign(Align.CENTER);
		textPaint.setTextSize(100);
		int x = getWidth()/2;
		int y = getHeight()/2;
		canvas.drawText(text, x, y, textPaint);		
	}
	
	private void drawGrowthPeriodInfo(Canvas canvas, Paint paint) {
		String text = Utility.growthInfo();
		Paint textPaint = new Paint();
		textPaint.setColor(Color.BLACK);
		textPaint.setTextAlign(Align.CENTER);
		textPaint.setTextSize(35);
		int x = getWidth()/2;
		int y = getHeight() - 100;
		canvas.drawText(text, x, y, textPaint);		
	}

	private Bitmap createBitmapAccordingToOrientation() {
		if (client.getOrientation() == Configuration.ORIENTATION_PORTRAIT)
			return Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
		else
			return Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
	}
	
	private void drawBitmapAccordingToOrientation(Canvas canvas, Paint paint, Bitmap bitmap) {
		if (client.getOrientation() == Configuration.ORIENTATION_PORTRAIT)
			canvas.drawBitmap(bitmap, null, new Rect(0, 0, getWidth(),getHeight()), paint);
		else
			canvas.drawBitmap(bitmap, null, new Rect(0, 0, getWidth(),getHeight()), paint);
	}
}
