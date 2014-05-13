package com.onezoom;

import com.onezoom.midnode.Search;
import com.onezoom.midnode.MidNode;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class MemoryThread extends Thread {
	public static final int MSG_RECALCULATE = 0;
	public static final int MSG_INITIALIZATION = 1;
	public static final int MSG_IDLECALCULATION = 2;
	public static final int MSG_RESET = 3;
	public static final int MSG_SEARCH = 4;
	public static final int MSG_BACK_SEARCH = 5;
	public static final int MSG_FORWARD_SEARCH = 6;
	public static final int MSG_SEARCH_LOAD = 7;
	public static final int MSG_BACK_SEARCH_LOAD = 8;
	public static final int MSG_FORWARD_SEARCH_LOAD = 9;

	
	private MemoryHandler handler;
	private CanvasActivity client;
	
	/**
	 * @constructor
	 * @param canvasActivity
	 */
	public MemoryThread(CanvasActivity canvasActivity) {
		client = canvasActivity;
	}
	
	/**
	 * When the activity starts, this method is called and do initialization of the tree.
	 */
	@Override
	public void run() {
		Looper.prepare();
		handler = new MemoryHandler(client);
		handler.sendEmptyMessage(MSG_INITIALIZATION);
		Looper.loop();
		super.run();
	}
	
	/**
	 * Destroy this thread.
	 */
	public synchronized void requestStop() {
		handler.post(new Runnable() {		
			@Override
			public void run() {
				Looper.myLooper().quit();
				MidNode.destory();
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

	public void searchAndLoad(String userInput) {
		Message msg = new Message();
		msg.what = MSG_SEARCH_LOAD;
		msg.obj = userInput;
		handler.sendMessage(msg);
	}

	public void backSearchAndLoad() {
		handler.sendEmptyMessage(MSG_BACK_SEARCH_LOAD);		
	}

	public void forwardSearchAndLoad() {
		handler.sendEmptyMessage(MSG_FORWARD_SEARCH_LOAD);		
	}
}


class MemoryHandler extends Handler {
	private CanvasActivity client;
	public Search searchEngine;

	public MemoryHandler(CanvasActivity _client) {
		client = _client;
		searchEngine = Search.getInstance(_client);
	}

	/**
	 * Handler of the thread.
	 * Do idle time calculation when the thread is not occupied with other messages.
	 */
	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case MemoryThread.MSG_INITIALIZATION:
			MidNode.createStaticObjects();
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
//				client.getTreeRoot().recalculateDynamic();
				client.treeView.setDuringRecalculation(false);
				client.treeView.postInvalidate();
				if (MidNode.initializer.stackOfNodeHasNonInitChildren.size() > 0)
					this.sendEmptyMessage(MemoryThread.MSG_IDLECALCULATION);
			}
			break;
		case MemoryThread.MSG_IDLECALCULATION:
			if (!this.HasMessages()) {
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
		case MemoryThread.MSG_SEARCH_LOAD:
			searchEngine.performSearch((String)msg.obj);
			client.loadLinkURL();
			client.webView.postInvalidate();
			break;
		case MemoryThread.MSG_BACK_SEARCH_LOAD:
			searchEngine.performBackSearch();
			client.loadLinkURL();
			client.webView.postInvalidate();
			break;
		case MemoryThread.MSG_FORWARD_SEARCH_LOAD:
			searchEngine.performForwardSearch();
			client.loadLinkURL();
			client.webView.postInvalidate();
			break;
		}
	}

	private boolean HasMessages() {
		for (int i = 0; i < 10; i++) {
			if (this.hasMessages(i) && i != 2)
				return true;
		}
		return false;
	}
}