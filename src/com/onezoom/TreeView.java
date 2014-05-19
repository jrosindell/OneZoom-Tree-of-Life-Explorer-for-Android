package com.onezoom;

import com.onezoom.midnode.MidNode;
import com.onezoom.midnode.PositionData;
import com.onezoom.midnode.Utility;

import android.content.Context;
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

/**
 * The tree is drawn on this view.
 * 
 * When the view is invalidated, it will call onDraw to refresh itself.
 * @author kaizhong
 *
 */
public class TreeView extends View {
	public CanvasActivity client;
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
	public static final float FACTOR = 1.4f;
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
		gestureDetector = new GestureDetector(context, new TreeViewGestureListener(this));
		scaleDetector = new ScaleGestureDetector(context, new ScaleListener(this));
		//this will not be used. set to 1,1 to speed up the app
		cachedBitmap = Bitmap.createBitmap(1,1, Bitmap.Config.ARGB_8888); 
		paint = new Paint();
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

	/**
	 * When user finger is on the screen, set duringInteraction as true. 
	 * When it's off the screen, set duringInteraction as false.
	 * 
	 * Test scale using scaleDetector.
	 * Test other actions including drag, double taps and single tap using gestureDetector.
	 */
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

	/**
	 * Draw 'loading' when tree is not ready.
	 * 
	 * If the tree is during recalculation or user is interacting with view, draw using bitmap,
	 * otherwise draw the tree and then cached it as a bitmap.
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (!treeBeingInitialized) {
			drawLoading(canvas);
		} else {
			if (!duringRecalculation && !duringInteraction){
				drawElementAndCache(canvas);
			} else {
				drawUsingCachedBitmap(canvas);
			}
		}
	}
	
	/**
	 * When this function first called, it executes 'if' branch and draws cached bitmap.
	 * Then it calls load bitmap from view, which calls onDraw, which calls drawElementAndCache again. 
	 * Then this function executes the 'else' branch, which does the actual drawing. 
	 * The result of the actual drawing will be returned to loadBitmapFromView and cached in cachedBitmap.
	 * 
	 * At the end of the function call, view will be invalidated and since duringInteraction is set to true, 
	 * view will use the cached bitmap to refresh itself.
	 * 
	 * 
	 * The reason for write this function in such a complex way is that cache bitmap actually calls onDraw 
	 * in this view. 
	 * 
	 * If the method is written plainly as drawElement followed with loadBitmapFromView then this method will
	 * be infinitely called by itself. Hence, a toggle is needed to make sure that loadBitmap will call this
	 * method with drawElement routine.
	 * 
	 * drawUsingCachedBitmap in 'if' branch is used to prevent the view from blinking.
	 * set duringInteraction as true so that following invalidating view caused by delayed message in
	 * other threads use the cached bitmap in order to speed up the app.
	 * @param canvas
	 */
	private void drawElementAndCache(Canvas canvas) {	
		if (toggle) {
			toggle = !toggle;
			drawUsingCachedBitmap(canvas);
			
			/**
			 * The tree is going to be redrawn using real data
			 * and a new bitmap will be cached, therefore, the scale variables should be reset.
			 * 
			 */
			this.scaleX = 1;
			this.scaleY = 1;
			this.distanceX = 0;
			this.distanceY = 0;
			cachedBitmap = loadBitmapFromView(this);
		} else {
			canvas.drawColor(Color.rgb(220, 235, 255));//rgb(255,255,200)');
			MidNode.visualizer.drawTree(canvas, client.getTreeRoot());
			if (this.isDuringGrowthAnimation()) {
				drawGrowthPeriodInfo(canvas, paint);
			}
			toggle = !toggle;
		}
		duringInteraction = true;
		invalidate();		
	}

	/**
	 * Scale and translate the bitmap to give a user preview of the tree.
	 * @param canvas
	 */
	private void drawUsingCachedBitmap(Canvas canvas) {
		canvas.translate(distanceX, distanceY);
		canvas.scale(scaleX, scaleY, scaleCenterX, scaleCenterY);
		canvas.drawBitmap(cachedBitmap, null, new Rect(0, 0, getWidth(),getHeight()), paint);
	}

	/**
	 * This function will call onDraw().
	 * @param v
	 * @return
	 */
	private Bitmap loadBitmapFromView(TreeView v) {
		if (v == null) {
			return null;
		}
		Bitmap bitmap = Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bitmap);
		c.translate(-v.getScrollX(), -v.getScrollY());
		
		/**
		 * This function calls onDraw().
		 */
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
	

	/**
	 * Draw 'loading' when the tree is not initialized yet.
	 * @param canvas
	 */
	private void drawLoading(Canvas canvas) {
		String text = "loading...";
		Paint textPaint = new Paint();
		textPaint.setColor(Color.GREEN);
		textPaint.setTextAlign(Align.CENTER);
		textPaint.setTextSize(100 * CanvasActivity.getScaleFactor());
		int x = getWidth()/2;
		int y = getHeight()/2;
		canvas.drawText(text, x, y, textPaint);		
	}
	
	/**
	 * This method is called during growth animation. It draws time information of growth.
	 * @param canvas
	 * @param paint
	 */
	private void drawGrowthPeriodInfo(Canvas canvas, Paint paint) {
		String text = Utility.growthInfo();
		int x = getWidth()/2;
		
		/**
		 * Text should be near the bottom of the device. 
		 * 
		 * Adjust the distance between the text and the bottom of the screen based on the size of the screen.
		 * 
		 * Multiply height width ratio so that the text is a bit more closer to the bottom of the screen
		 * in landscape view than in portrait view.
		 */
		int y = (int) (getHeight() -
				80 * client.getScreenHeight() / client.getScreenWidth() * CanvasActivity.getScaleFactor());
		
		Paint textPaint = new Paint();
		textPaint.setTextAlign(Align.CENTER);
		textPaint.setTextSize(30 * CanvasActivity.getScaleFactor());
		textPaint.setColor(Color.argb(150, 0, 0, 0));
		
		/**
		 * Left should be at least 10 units. If text length is small, then the left boarder will be more close
		 * to the center. 
		 * 
		 * Set the right border is similar but in an opposite way.
		 * 
		 * the text is on position y. Therefore add and minus some distance which is linear to the ratio of
		 * the text size as the top border and bottom border.
		 */
		canvas.drawRect(
				Math.max(10, getWidth()/2 - text.length() * textPaint.getTextSize() / 3.3f),   //left
				y - textPaint.getTextSize() * 1.3f,      //top
				Math.min(client.getScreenWidth() - 10, 
						getWidth()/2 + text.length() * textPaint.getTextSize() / 3.3f),   //right
				y + textPaint.getTextSize() * 0.7f, textPaint);  //bottom
		
		textPaint.setColor(Color.WHITE);
		canvas.drawText(text, x, y, textPaint);	                     
	}
}
