package com.onezoom;

import com.onezoom.midnode.MidNode;
import com.onezoom.midnode.displayBinary.BinaryInitializer;
import com.onezoom.midnode.displayBinary.BinarySearch;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class MemoryThread extends Thread {
	public static final int MSG_RECALCULATE = 0;
	public static final int MSG_INITIALIZATION = 1;
	public static final int MSG_IDLECALCULATION = 2;
	public static final int MSG_RESET = 3;
	public static final int MSG_SEARCH = 4;
	public static final int MSG_BACK_SEARCH = 5;
	public static final int MSG_FORWARD_SEARCH = 6;
	
	private MemoryHandler handler;
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

	public void reset() {
		handler.sendEmptyMessage(MSG_RESET);
	}

	public void search(String userInput) {
		Message msg = new Message();
		msg.what = MSG_SEARCH;
		msg.obj = userInput;
		handler.sendMessage(msg);
	}

	public void backSearch() {
		handler.sendEmptyMessage(MSG_BACK_SEARCH);
	}

	public void forwardSearch() {
		handler.sendEmptyMessage(MSG_FORWARD_SEARCH);
	}
}


class MemoryHandler extends Handler {
	private CanvasActivity client;
	public BinarySearch searchEngine;

	public MemoryHandler(CanvasActivity _client) {
		client = _client;
		searchEngine = new BinarySearch(_client);
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case MemoryThread.MSG_INITIALIZATION:
			client.initialization();
			client.treeView.postInvalidate();
			if (MidNode.initializer.stackOfNodeHasNonInitChildren.size() > 0)
				this.sendEmptyMessage(MemoryThread.MSG_IDLECALCULATION);
			break;
		case MemoryThread.MSG_RECALCULATE:
			if (!this.hasMessages(MemoryThread.MSG_RECALCULATE)) {
				client.treeView.setDuringRecalculation(true);
				client.getTreeRoot().recalculateDynamic();	
				client.treeView.setDuringRecalculation(false);
				client.treeView.postInvalidate();
				if (MidNode.initializer.stackOfNodeHasNonInitChildren.size() > 0)
					this.sendEmptyMessage(MemoryThread.MSG_IDLECALCULATION);
			}
			break;
		case MemoryThread.MSG_RESET:
			if (!this.hasMessages(MemoryThread.MSG_RECALCULATE)) {
				client.treeView.setDuringRecalculation(true);
				client.getTreeRoot().recalculate();	
				client.treeView.setDuringRecalculation(false);
				client.treeView.postInvalidate();
				if (MidNode.initializer.stackOfNodeHasNonInitChildren.size() > 0)
					this.sendEmptyMessage(MemoryThread.MSG_IDLECALCULATION);
			}
			break;
		case MemoryThread.MSG_IDLECALCULATION:
			if (!this.hasMessages(MemoryThread.MSG_RECALCULATE)) {
				MidNode.initializer.idleTimeInitialization();
				if (MidNode.initializer.stackOfNodeHasNonInitChildren.size() > 0)
					this.sendEmptyMessage(MemoryThread.MSG_IDLECALCULATION);
			}
			break;
		case MemoryThread.MSG_SEARCH:
			searchEngine.performSearch((String)msg.obj);
			client.treeView.postInvalidate();
			break;
		case MemoryThread.MSG_BACK_SEARCH:
			searchEngine.performBackSearch();
			client.treeView.postInvalidate();
			break;
		case MemoryThread.MSG_FORWARD_SEARCH:
			searchEngine.performForwardSearch();
			client.treeView.postInvalidate();
			break;
		}
	}
}