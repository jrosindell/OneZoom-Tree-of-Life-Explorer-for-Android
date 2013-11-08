package com.onezoom;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class TreeView extends View {
	CanvasActivity client;
	
	public TreeView(Context context) {
		super(context);
		client = (CanvasActivity) context;
	}
	
	public TreeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		client = (CanvasActivity) context;
	}

	public TreeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		client = (CanvasActivity) context;
	}
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		if (client.getFullTree() != null)
			client.getFullTree().drawElement(canvas);
	}
}
