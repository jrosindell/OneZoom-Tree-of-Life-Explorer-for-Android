package com.onezoom;

import com.onezoom.midnode.MidNode;
import com.onezoom.midnode.displayBinary.BinaryInitializer;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class MemoryThread extends Thread {
	public static final int MSG_RECALCULATE = 0;
	public static final int MSG_INITIALIZATION = 1;
	public static final int MSG_IDLECALCULATION = 2;
	
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
			clientActivity.treeView.postInvalidate();
			if (MidNode.initializer.stackOfNodeHasNonInitChildren.size() > 0)
				this.sendEmptyMessage(MemoryThread.MSG_IDLECALCULATION);
			break;
		case MemoryThread.MSG_RECALCULATE:
			if (!this.hasMessages(MemoryThread.MSG_RECALCULATE)) {
				clientActivity.treeView.setDuringRecalculation(true);
				clientActivity.getTreeRoot().recalculateDynamic();					
//				Log.d("debug", "stack size after recalculate: " + MidNode.initializer.stackOfNodeHasNonInitChildren.size());
				clientActivity.treeView.setDuringRecalculation(false);
				clientActivity.treeView.postInvalidate();
				if (MidNode.initializer.stackOfNodeHasNonInitChildren.size() > 0)
					this.sendEmptyMessage(MemoryThread.MSG_IDLECALCULATION);
			}
			break;
		case MemoryThread.MSG_IDLECALCULATION:
			if (!this.hasMessages(MemoryThread.MSG_RECALCULATE)) {
//				clientActivity.treeView.setDuringRecalculation(true);
				MidNode.initializer.idleTimeInitialization();
//				clientActivity.treeView.setDuringRecalculation(false);
//				Log.d("debug", "stack size after ini: " + MidNode.initializer.stackOfNodeHasNonInitChildren.size());
				if (MidNode.initializer.stackOfNodeHasNonInitChildren.size() > 0)
					this.sendEmptyMessage(MemoryThread.MSG_IDLECALCULATION);
			}
			break;
		}
	}
}