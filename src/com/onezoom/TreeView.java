package com.onezoom;

import com.onezoom.midnode.MidNode;
import com.onezoom.midnode.PositionCalculator;
import com.onezoom.midnode.PositionData;
import com.onezoom.midnode.Utility;
import com.onezoom.midnode.Visualizer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
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
	private ScaleListener scaleListener;
	private boolean treeBeingInitialized = false;
	private boolean duringRecalculation = false;
	private boolean duringInteraction = false;
	private boolean duringGrowthAnimation = false;
	private boolean refreshNeeded = true;
	private Bitmap cachedBitmap;
	private Bitmap initBitmap;
	private Bitmap bitmapForGenerateCache;
	private Paint paint;
	private boolean toggle = true;
	public static final float FACTOR = 1.4f;
	public boolean testDragAfterScale;
	private boolean lastActionAsScale = false;
	private boolean isFirstAction = true;
	private float scaleTotalX = 1f;
	private float scaleTotalY = 1f;
	private float distanceTotalX = 0f;
	private float distanceTotalY = 0f;
	private float xp, yp, ws;
	private float reanchorJusticeXp, reanchorJusticeYp, reanchorJusticeWs;
	private boolean firstTimeDrawAndCache = true;
	
	public boolean isFirstAction() {
		return isFirstAction;
	}


	public void setFirstAction(boolean isFirstAction) {
		this.isFirstAction = isFirstAction;
	}
	
	public float getScaleTotalX() {
		return scaleTotalX;
	}

	public void setScaleTotalX(float scaleTotalX) {
		this.scaleTotalX = scaleTotalX;
	}

	public float getScaleTotalY() {
		return scaleTotalY;
	}

	public void setScaleTotalY(float scaleTotalY) {
		this.scaleTotalY = scaleTotalY;
	}

	public float getDistanceTotalX() {
		return distanceTotalX;
	}

	public void setDistanceTotalX(float distanceTotalX) {
		this.distanceTotalX = distanceTotalX;
	}

	public float getDistanceTotalY() {
		return distanceTotalY;
	}

	public void setDistanceTotalY(float distanceTotalY) {
		this.distanceTotalY = distanceTotalY;
	}

	public float getReanchorJusticeXp() {
		return reanchorJusticeXp;
	}


	public void setReanchorJusticeXp(float reanchorJusticeXp) {
		this.reanchorJusticeXp = reanchorJusticeXp;
	}


	public float getReanchorJusticeYp() {
		return reanchorJusticeYp;
	}


	public void setReanchorJusticeYp(float reanchorJusticeYp) {
		this.reanchorJusticeYp = reanchorJusticeYp;
	}


	public float getReanchorJusticeWs() {
		return reanchorJusticeWs;
	}


	public void setReanchorJusticeWs(float reanchorJusticeWs) {
		this.reanchorJusticeWs = reanchorJusticeWs;
	}


	public boolean isLastActionAsScale() {
		return lastActionAsScale;
	}

	public void setLastActionAsScale(boolean lastActionAsScale) {
		this.lastActionAsScale = lastActionAsScale;
	}
	
	public boolean isToggle() {
		return toggle;
	}

	public void setToggle(boolean toggle) {
		this.toggle = toggle;
	}
	
	public void setCachedBitmap(Bitmap cachedBitmap) {
		this.cachedBitmap = cachedBitmap;
	}
	
	public Bitmap getInitBitmap() {
		return initBitmap;
	}

	public void setInitBitmap(Bitmap initBitmap) {
		this.initBitmap = initBitmap;
	}

	
	public boolean isRefreshNeeded() {
		return refreshNeeded;
	}

	public void setRefreshNeeded(boolean refreshNeeded) {
		this.refreshNeeded = refreshNeeded;
	}
	
	public ScaleListener getScaleListener() {
		return scaleListener;
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
		scaleListener = new ScaleListener(this);
		scaleDetector = new ScaleGestureDetector(context, scaleListener);
		//this will not be used. set to 1,1 to speed up the app
		cachedBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); 
		paint = new Paint();
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		super.onWindowFocusChanged(hasWindowFocus);
		this.bitmapForGenerateCache = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
	}
	
	void resetDragScaleParameter() {
		xp = PositionData.xp;
		yp = PositionData.yp;
		ws = PositionData.ws;
		scaleTotalX = 1f;
		scaleTotalY = 1f;
		distanceTotalX = 0f;
		distanceTotalY = 0f;
		this.reanchorJusticeWs = 1f;
		this.reanchorJusticeXp = 0f;
		this.reanchorJusticeYp = 0f;
		PositionCalculator.setReanchored(false);
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
		if (!this.treeBeingInitialized) {
			return true;
		}
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
		case MotionEvent.ACTION_DOWN:
			duringInteraction = true;
			refreshNeeded = true;
			break;
		case MotionEvent.ACTION_UP:
			duringInteraction = false;
			isFirstAction = true;
			lastActionAsScale = false;
			PositionData.xp = this.xp;
			PositionData.yp = this.yp;
			PositionData.ws = this.ws;
			
			this.zoomin(scaleTotalX * this.reanchorJusticeWs, 
					distanceTotalX + PositionData.getXp() * (scaleTotalX - 1) + this.reanchorJusticeXp,
					distanceTotalY + PositionData.getYp() * (scaleTotalY - 1) + this.reanchorJusticeYp);
			break;
		default:
				break;
		}
		
		if (PositionCalculator.isReanchored()) {
			return true;
		}
		
		testDragAfterScale = true;
		scaleDetector.onTouchEvent(event);
		if (testDragAfterScale) {
			gestureDetector.onTouchEvent(event);
		}		
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
		if (!treeBeingInitialized && this.getInitBitmap() == null) {
			drawLoading(canvas);
		} else if (!treeBeingInitialized && !this.getInitBitmap().isRecycled()) {
			drawUsingCachedBitmap(canvas, this.getInitBitmap());
			drawLoadingAtBottomOfScreen(canvas);
		} else {
			if ((!duringRecalculation && !duringInteraction && refreshNeeded)){
				drawElementAndCache(canvas);
			} else {
				drawUsingCachedBitmap(canvas, this.cachedBitmap);
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
			if (this.firstTimeDrawAndCache) {
				this.drawUsingCachedBitmap(canvas, cachedBitmap);
				this.firstTimeDrawAndCache = false;
			} else {
				drawUsingCachedBitmapWithoutScale(canvas, cachedBitmap);
			}
			/**
			 * The tree is going to be redrawn using real data
			 * and a new bitmap will be cached, therefore, the scale variables should be reset.
			 * 
			 */
			cachedBitmap = loadBitmapFromView(this);
			refreshNeeded = false;
			invalidate();		
		} else {
			canvas.drawColor(Color.rgb(220, 235, 255));//rgb(255,255,200)');
			Visualizer.count = 0;
			MidNode.visualizer.drawTree(canvas, client.getTreeRoot());
			if (this.isDuringGrowthAnimation()) {
				drawGrowthPeriodInfo(canvas, paint);
			}
			this.resetDragScaleParameter();
			toggle = !toggle;
		}
	}
	
	private void drawUsingCachedBitmapWithoutScale(Canvas canvas, Bitmap cached) {
		canvas.drawBitmap(cached, null, new Rect(0, 0, getWidth(),getHeight()), paint);
	}

	/**
	 * Scale and translate the bitmap to give a user preview of the tree.
	 * @param canvas
	 */
	private void drawUsingCachedBitmap(Canvas canvas, Bitmap cached) {
		canvas.translate(distanceTotalX, distanceTotalY);
		canvas.scale(scaleTotalX, scaleTotalY);
		canvas.drawColor(Color.rgb(220, 235, 255));//rgb(255,255,200)');
		canvas.drawBitmap(cached, null, new Rect(0, 0, getWidth(),getHeight()), paint);
	}

	/**
	 * This function will call onDraw().
	 * @param v
	 * @return
	 */
	Bitmap loadBitmapFromView(TreeView v) {
		if (v == null) {
			return null;
		}
		Canvas c = new Canvas(this.bitmapForGenerateCache);		
		/**
		 * This function calls onDraw().
		 */
		v.draw(c);
		return this.bitmapForGenerateCache;
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
	
	public void zoomin(float scaleFactor, float shiftX, float shiftY) {	
		PositionData.shiftScreenPosition(shiftX, shiftY, scaleFactor);
		duringRecalculation = true;
		client.recalculate();
	}
	
	/**
	 * 
	 */
	private void drawLoadingAtBottomOfScreen(Canvas canvas) {
		String text = "Loading...";
		drawTextAtBottomOfScreen(canvas, text, 1.3f);	  
	}


	private void drawTextAtBottomOfScreen(Canvas canvas, String text, float scale) {
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
		textPaint.setTextSize(30 * CanvasActivity.getScaleFactor() * scale);
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

	/**
	 * Draw 'loading' when the tree is not initialized yet.
	 * @param canvas
	 */
	private void drawLoading(Canvas canvas) {
		canvas.drawColor(Color.rgb(220, 235, 255));//rgb(255,255,200)');
		String text = "loading...";
		Paint textPaint = new Paint();
		textPaint.setColor(Color.BLACK);
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
		drawTextAtBottomOfScreen(canvas, text, 1);	                     
	}
}