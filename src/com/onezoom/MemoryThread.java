package com.onezoom;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class MemoryThread extends Thread {
	public static final int MSG_RECALCULATE = 0;
	public static final int MSG_INITIALIZATION = 1;
	
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
		handler.sendEmptyMessage(MSG_INITIALIZATION);
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
		case MemoryThread.MSG_INITIALIZATION:
			clientActivity.initialization();
		case MemoryThread.MSG_RECALCULATE:
			if (!this.hasMessages(MemoryThread.MSG_RECALCULATE)) {
				clientActivity.treeView.setDuringRecalculation(true);
					clientActivity.getTreeRoot().recalculateDynamic();					
				clientActivity.treeView.setDuringRecalculation(false);
				clientActivity.treeView.postInvalidate();
			}
			break;
		}
	}
}