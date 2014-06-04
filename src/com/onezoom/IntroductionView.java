package com.onezoom;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

public class IntroductionView extends ImageView {
	private int status = 1;
	private int total = 7;
	public CanvasActivity client;
	private GestureDetector gestureDetector;
	private Bitmap nextBitmap;
	private Bitmap previousBitmap;
	private Bitmap currentBitmap;

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
		client = (CanvasActivity) context;
		gestureDetector = new GestureDetector(context, new IntroductionViewGestureListener(this));
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		this.gestureDetector.onTouchEvent(event);
		return true;
	}
	
	public void startTutorial() {
		status = 0;
		tutorialForward();
	}
	

	public void showFirstPage() {
		status = 100;
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawStep(canvas);
	}

	private void drawStep(Canvas canvas) {
		if (this.currentBitmap == null) {
			currentBitmap = decodeSampledBitmapFromResource(getResources(),
					getResources().getIdentifier("tutorial" + status, "drawable", client.getPackageName()),
					this.getWidth()/2, this.getHeight()/2);
		} 
		this.setImageBitmap(this.currentBitmap);
	}

	public void tutorialForward() {
		if (status < total) {
			this.status++;
			this.previousBitmap = this.currentBitmap;
			this.currentBitmap = this.nextBitmap;
			invalidate();
			if (status < total && this.getHeight() != 0) {
				this.nextBitmap = decodeSampledBitmapFromResource(getResources(),
						getResources().getIdentifier("tutorial" + (status+1), "drawable", client.getPackageName()),
						this.getWidth()/2, this.getHeight()/2);
			} else if (status == total) {
				this.nextBitmap = decodeSampledBitmapFromResource(getResources(),
						getResources().getIdentifier("tutorial1", "drawable", client.getPackageName()),
						this.getWidth()/2, this.getHeight()/2);
			}
		}
		else {
			client.endTutorial();
		}
	}

	public void tutorialBackward() {
		if (status > 1 && status <= total) {
			this.status--;
			this.nextBitmap = this.currentBitmap;
			this.currentBitmap = this.previousBitmap;
			invalidate();
			if (status > 1) {
				this.previousBitmap = decodeSampledBitmapFromResource(getResources(),
						getResources().getIdentifier("tutorial" + (status-1), "drawable", client.getPackageName()),
						this.getWidth()/2, this.getHeight()/2);
			}
		}
	}
	
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
	
	public static int calculateInSampleSize(
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
