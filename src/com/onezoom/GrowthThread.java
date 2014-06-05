package com.onezoom;

import java.util.Map;
import java.util.TreeMap;

import com.onezoom.midnode.TraitsData;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

class GrowthThread extends Thread {
	Handler handler;
	CanvasActivity client;
	boolean started = false;
	int state;
	static final int MSG_PLAY = 0;
	static final int MSG_PAUSE = 1;
	static final int MSG_STOP = 2;
	static final int MSG_REVERT = 3;
	static final int MSG_CLOSE = 4;
	static final int MSG_START_PLAY = 5;
	static final int MSG_START_REVERT = 6;
	static final int STATE_PAUSE = 20;
	static final int STATE_INIT = 21;
	static final int STATE_PLAY = 22;
	static final int STATE_REVERT = 23;
	static final int STATE_STOP= 24;

	final int GROWTH_RATE = 10;
	Map<String, Integer> map;
	static int treeAge = 0;
	
	/**
	 * Map stores the age of each group.
	 * @param activity
	 */
	public GrowthThread(CanvasActivity activity) {
		client = activity;
		state = STATE_INIT;
		map = new TreeMap<String, Integer>();
		map.put("Mammals", 166);
		map.put("Birds", 113);
		map.put("Tetrapods", 390);
		map.put("Amphibians", 370);
		map.put("Turtles", 210);
	}
	
	/**
	 * When the activity starts, this method is called.
	 */
	@Override
	public void run() {
		try {
			Looper.prepare();
			handler = new growthHandler(client, this);
			Looper.loop();
		} catch (Throwable t) {
		}
	}
	
	/**
	 * Destroy this thread.
	 */
	public synchronized void requestStop() {
		handler.post(new Runnable() {
			@Override
			public void run() {
				TraitsData.timelim = -1;
				Looper.myLooper().quit();
			}
		});
	}
	
	/**
	 * Only reset the time line when state is in STATE_STOP or STATE_REVERT.
	 * When state is STATE_PLAY, revert play with the current time line instead of reset it.
	 * When state is STATE_STOP, the time line is already -1, so it doesn't need to be reset.
	 */
	public void Revert() {
		if (state == STATE_STOP || state == STATE_REVERT) {
			TraitsData.timelim = -1;
		}
		state = STATE_REVERT;
		treeAge = map.get(client.selectedItem);
		handler.sendEmptyMessage(MSG_START_REVERT);
	}
	
	public void Pause() {
		state = STATE_PAUSE;
		handler.sendEmptyMessage(MSG_PAUSE);
	}
	
	/**
	 * Reset time line to the origin of the tree selected. 
	 * If state is STATE_REVERT, start play directly from the current time line.
	 */
	public void Play() {
		if (state == STATE_INIT || state == STATE_STOP || state == STATE_PLAY) {
			treeAge = map.get(client.selectedItem);
			TraitsData.timelim = treeAge;
		}
		state = STATE_PLAY;
		handler.sendEmptyMessage(MSG_START_PLAY);
	}
	
	public void Close() {
		state = STATE_INIT;
		handler.sendEmptyMessage(MSG_CLOSE);
	}
	
	public void Stop() {
		state = STATE_STOP;
		handler.sendEmptyMessage(MSG_STOP);
	}
}

class growthHandler extends Handler {
	CanvasActivity client;
	GrowthThread thread;
	public growthHandler(CanvasActivity activity, GrowthThread thread) {
		this.thread = thread;
		client = activity;
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {	
		
		/**
		 * When user press revert button, the handler first handle MSG_START_REVERT, which will then
		 * call MSG_REVERT until the age exceeds the age of the tree.
		 */
		case GrowthThread.MSG_START_REVERT:
			removeMessages(GrowthThread.MSG_PLAY);
			removeMessages(GrowthThread.MSG_REVERT);
			client.resetTree();
			client.treeView.setDuringGrowthAnimation(true);
			this.sendEmptyMessage(GrowthThread.MSG_REVERT);
			break;			
		case GrowthThread.MSG_REVERT:
			TraitsData.timelim += 0.65;
			//set refresh needed to refresh view by drawing the tree instead of using previous cached bitmap
			client.treeView.setRefreshNeeded(true);
			client.treeView.postInvalidate();
			if (TraitsData.timelim < GrowthThread.treeAge)
				sendEmptyMessageDelayed(GrowthThread.MSG_REVERT, 40);
			else
				thread.state = GrowthThread.STATE_STOP;
			break;
			
			
		case GrowthThread.MSG_PAUSE:
			this.removeMessages(GrowthThread.MSG_PLAY);
			this.removeMessages(GrowthThread.MSG_REVERT);
			this.removeMessages(GrowthThread.MSG_START_PLAY);
			this.removeMessages(GrowthThread.MSG_START_REVERT);
			break;
		
		/**
		 * When user press start button, the handler first handle MSG_START_PLAY, which will then
		 * call MSG_PLAY until the age become negative which means the tree has been grown fully.
		 */
		case GrowthThread.MSG_START_PLAY:
			removeMessages(GrowthThread.MSG_REVERT);
			removeMessages(GrowthThread.MSG_PLAY);
			client.resetTree();
			client.treeView.setDuringGrowthAnimation(true);
			this.sendEmptyMessage(GrowthThread.MSG_PLAY);
			break;
		case GrowthThread.MSG_PLAY:
			TraitsData.timelim -= 0.65;
			//set refresh needed to refresh view by drawing the tree instead of using previous cached bitmap
			client.treeView.setRefreshNeeded(true);
			client.treeView.postInvalidate();
			if (TraitsData.timelim > 0)
				sendEmptyMessageDelayed(GrowthThread.MSG_PLAY, 40);
			else
				thread.state = GrowthThread.STATE_STOP;
			break;
	
			
		case GrowthThread.MSG_STOP:
			TraitsData.timelim = -1;
			this.removeMessages(GrowthThread.MSG_PLAY);
			this.removeMessages(GrowthThread.MSG_REVERT);
			this.removeMessages(GrowthThread.MSG_START_PLAY);
			this.removeMessages(GrowthThread.MSG_START_REVERT);
			client.treeView.setRefreshNeeded(true);
			client.treeView.postInvalidate();
			break;
		
		case GrowthThread.MSG_CLOSE:
			TraitsData.timelim = -1;
			this.removeMessages(GrowthThread.MSG_PLAY);
			this.removeMessages(GrowthThread.MSG_REVERT);
			this.removeMessages(GrowthThread.MSG_START_PLAY);
			this.removeMessages(GrowthThread.MSG_START_REVERT);
			client.treeView.setRefreshNeeded(true);
			client.treeView.setDuringGrowthAnimation(false);
			client.treeView.postInvalidate();
			break;

		default:
			break;
		}
	}
}

