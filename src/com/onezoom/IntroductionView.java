package com.onezoom;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

public class IntroductionView extends ImageView {
	private int status = 1;
	private int total = 7;
	public CanvasActivity client;
	private GestureDetector gestureDetector;
	private Bitmap[] bitmapArray;
	private Bitmap introBitmap;

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
		this.bitmapArray = new Bitmap[total];
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		this.gestureDetector.onTouchEvent(event);
		return true;
	}
	
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
	

	protected void LoadAllTutorial() {
		for (int i = 0; i < total; i++) {
			if (this.bitmapArray[i] == null) 
				this.bitmapArray[i] = loadBitmap(i+1);
		}
	}

	public void showFirstPage() {
		status = 100;
		invalidate();
	}

	private void drawStep() {
		Bitmap bitmap = null;
		if (status == 100 && this.introBitmap == null) {
			this.introBitmap = loadBitmap(status);
			bitmap = this.introBitmap;
		} else if (status == 100) {
			bitmap = this.introBitmap;
		} else if (this.bitmapArray[status-1] == null) {
			this.bitmapArray[status-1] = loadBitmap(status);
			bitmap = this.bitmapArray[status-1];
		} else {
			bitmap = this.bitmapArray[status-1];
		}
		this.setImageBitmap(bitmap);
	}

	public void tutorialForward() {
		if (status < total) {
			this.status++;
			drawStep();
		}
		else {
			this.destroyBitmap();
			client.endTutorial();
		}
	}

	private void destroyBitmap() {
		for (int i = 0; i < total; i++) {
			this.bitmapArray[i].recycle();
			this.bitmapArray[i] = null;
		}
	}

	public void tutorialBackward() {
		if (status > 1 && status <= total) {
			this.status--;
			drawStep();
		}
	}
	
	private Bitmap loadBitmap(int index) {
		System.out.println("index -> " + index);
		if (this.getWidth() == 0) {
			return DecodeBitmapHelper.decodeSampledBitmapFromResource(getResources(),
					getResources().getIdentifier("tutorial" + index, "drawable", client.getPackageName()),
					this.client.getScreenWidth()/2, this.client.getScreenHeight()/2);
		} else {
			return DecodeBitmapHelper.decodeSampledBitmapFromResource(getResources(),
					getResources().getIdentifier("tutorial" + index, "drawable", client.getPackageName()),
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
