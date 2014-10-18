package com.onezoom;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

public class IntroductionView extends ImageView {
	private int status = 1;
	private int total = 7;
	public CanvasActivity client;
	private GestureDetector gestureDetector;
	private Bitmap[] bitmapArray;

	public IntroductionView(Context context) {
		super(context);
		init(context);
	}
	
	public IntroductionView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public IntroductionView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		this.setBackgroundResource(R.drawable.bg_col);
		client = (CanvasActivity) context;
		gestureDetector = new GestureDetector(context, new IntroductionViewGestureListener(this));
		this.bitmapArray = new Bitmap[total];
	}
	
	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		this.gestureDetector.onTouchEvent(event);
		return true;
	}
	
	/**
	 * Tutorial start.
	 * Create a work thread to load all images into memory
	 * status set to 0 and then call tutorialForward to display tutorial1 in drawable folder.
	 */
	public void startTutorial() {
		status = 0;
		Thread thread = new Thread() {
			public void run() {
				LoadAllTutorial();
			}
		};
		thread.start();
		tutorialForward();
	}
	
	/**
	 * Introduction page of the app has filename tutorial100, so set status as 100.
	 */
	public void showFirstPage() {
		status = 100;
		drawStep();
	}
	
	/**
	 * show color meaning slide has filename tutorial 99, so set status as 99.
	 */
	public void showColorMeaning() {
		status = 99;
		drawStep();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	/**
	 * Load bitmap into memory
	 */
	protected void LoadAllTutorial() {
		for (int i = 1; i < total; i++) {
			if (this.bitmapArray[i] == null) 
				this.bitmapArray[i] = loadBitmap("tutorial" + (i+1));
		}
	}
	
	/**
	 * Recycle bitmaps to free space.
	 */
	private void destroyBitmap() {
		for (int i = 0; i < total; i++) {
			if (this.bitmapArray[i] != null) {
				this.bitmapArray[i].recycle();
				this.bitmapArray[i] = null;
			}
		}
	}

	/**
	 * Draw tutorial according to status.
	 * If the bitmap has not been loaded yet, it load into memory first.
	 *
	 * tutorial1-tutorial7 will be cached into bitmap array.
	 * tutorial of open page and color meaning will be store by introBitmap variable.
	 */
	public void drawStep() {
		Bitmap bitmap = null;
		if (status > total) {
			bitmap = loadBitmap("tutorial" + status);
		} else if (this.bitmapArray[status-1] == null) {
			this.bitmapArray[status-1] = loadBitmap("tutorial" + status);
			bitmap = this.bitmapArray[status-1];
		} else {
			bitmap = this.bitmapArray[status-1];
		}
		
		this.setImageBitmap(bitmap);
		this.invalidate();
	}

	/**
	 * Load next tutorial page.
	 * If status equals total, then quit tutorial slide show.
	 */
	public void tutorialForward() {
		if (status < total) {
			this.status++;
			drawStep();
		}
		else if (status == total) {
			this.destroyBitmap();
			client.endTutorial();
		} else {
			client.endTutorial();
		}
	}

	/**
	 * Load previous tutorial page.
	 * If status equals the first page, then do nothing.
	 */
	public void tutorialBackward() {
		if (status > 1 && status <= total) {
			this.status--;
			drawStep();
		}
	}
	
	/**
	 * Load bitmap from drawable folder.
	 * If can't get view width, then use the width passed from client.
	 * @param index
	 * @return
	 */
	private Bitmap loadBitmap(String filename) {
		if (this.getWidth() == 0) {
			return DecodeBitmapHelper.decodeSampledBitmapFromResource(getResources(),
					getResources().getIdentifier(filename, "drawable", client.getPackageName()),
					this.client.getScreenWidth()/2, this.client.getScreenHeight()/2);
		} else {
			return DecodeBitmapHelper.decodeSampledBitmapFromResource(getResources(),
					getResources().getIdentifier(filename, "drawable", client.getPackageName()),
					this.getWidth()/2, this.getHeight()/2);
		}
	}
}

class DecodeBitmapHelper {
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
	        int reqWidth, int reqHeight) {
	
	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);
	
	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
	
	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}
	
	private static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	
	    if (height > reqHeight || width > reqWidth) {
	
	        final int halfHeight = height / 2;
	        final int halfWidth = width / 2;
	
	        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
	        // height and width larger than the requested height and width.
	        while ((halfHeight / inSampleSize) > reqHeight
	                && (halfWidth / inSampleSize) > reqWidth) {
	            inSampleSize *= 2;
	        }
	    }
	
	    return inSampleSize;
	}
}
