package com.onezoom;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.ImageView;

public class IntroductionView extends ImageView {
	private int status = 1;
	private int total = 3;
	public CanvasActivity client;
	private GestureDetector gestureDetector;

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
		status = 1;
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawStep(canvas);
	}

	private void drawStep(Canvas canvas) {
//		Paint textPaint = new Paint();
//		textPaint.setColor(Color.BLACK);
//		textPaint.setTextSize(100);
//		textPaint.setTextAlign(Align.CENTER);
//		canvas.drawText(status2 + " out of " + total, this.getWidth()/2, this.getHeight()/2, textPaint);
		this.setImageDrawable(getResources().getDrawable(
				getResources().getIdentifier("tutorial" + status, "drawable", client.getPackageName())));
	}

	public void tutorialForward() {
		if (status < total)
			this.status++;
		else 
			client.endTutorial();
	}

	public void tutorialBackward() {
		if (status > 1)
			this.status--;
	}

}
