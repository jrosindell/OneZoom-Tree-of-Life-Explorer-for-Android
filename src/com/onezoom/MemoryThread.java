package com.onezoom;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class MemoryThread extends Thread {
	public static final int MSG_RECALCULATE = 0;
	private Handler handler;
	private CanvasActivity clientActivity;
	
	/**
	 * @constructor
	 * @param canvasActivity
	 */
	public MemoryThread(CanvasActivity canvasActivity) {
		clientActivity = canvasActivity;
	}
	
	@Override
	public void run() {
		Looper.prepare();
		handler = new MemoryHandler(clientActivity);
		Looper.loop();
		super.run();
	}
	
	public synchronized void requestStop() {
		handler.post(new Runnable() {		
			@Override
			public void run() {
				Looper.myLooper().quit();
			}
		});
	}

	public void recalculate() {
		handler.sendEmptyMessage(MSG_RECALCULATE);
	}
}


class MemoryHandler extends Handler {
	private CanvasActivity clientActivity;
	
	public MemoryHandler(CanvasActivity client) {
		clientActivity = client;
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case MemoryThread.MSG_RECALCULATE:
			if (!this.hasMessages(MemoryThread.MSG_RECALCULATE)) {
				clientActivity.getTreeRoot().recalculate();
				clientActivity.treeView.setDuringRecalculation(false);
				clientActivity.treeView.postInvalidate();
			}
			break;
		}
	}
}