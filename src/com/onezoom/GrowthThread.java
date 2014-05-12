package com.onezoom;

import java.util.Map;
import java.util.TreeMap;

import com.onezoom.midnode.TraitsData;
import com.onezoom.midnode.Utility;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

class GrowthThread extends Thread {
	Handler handler;
	CanvasActivity client;
	boolean started = false;
	boolean pause = false;
	static final int MSG_PLAY = 0;
	static final int MSG_PAUSE = 1;
	static final int MSG_STOP = 2;
	static final int MSG_REVERT = 3;
	static final int MSG_CLOSE = 4;
	static final int MSG_START_PLAY = 5;
	static final int MSG_START_REVERT = 6;
	final int GROWTH_RATE = 10;
	Map<String, Integer> map;
	static int treeAge = 0;
	
	/**
	 * Map stores the age of each group.
	 * @param activity
	 */
	public GrowthThread(CanvasActivity activity) {
		client = activity;
		map = new TreeMap<String, Integer>();
		map.put("Mammals", 166);
		map.put("Birds", 113);
		map.put("Tetrapods", 390);
		map.put("Amphibian", 370);
		map.put("Turtles", 210);
	}
	
	/**
	 * When the activity starts, this method is called.
	 */
	@Override
	public void run() {
		try {
			Looper.prepare();
			handler = new growthHandler(client);
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
	
	public void Revert() {
		treeAge = map.get(CanvasActivity.selectedItem);
		handler.removeMessages(MSG_PLAY);
		handler.sendEmptyMessage(MSG_START_REVERT);
	}
	
	public void Pause() {
		pause = true;
		handler.sendEmptyMessage(MSG_PAUSE);
	}
	
	/**
	 * If growth animation is paused, then restart without changing time line. 
	 * Otherwise set time line to the start of selected group.
	 */
	public void Play() {
		if (pause == false) {
			treeAge = map.get(CanvasActivity.selectedItem);
			TraitsData.timelim = treeAge;
		}
		else 
			pause = !pause;
		handler.removeMessages(MSG_REVERT);
		handler.sendEmptyMessage(MSG_START_PLAY);
	}
	
	public void Close() {
		handler.sendEmptyMessage(MSG_CLOSE);
	}
	
	public void Stop() {
		handler.sendEmptyMessage(MSG_STOP);
	}
}

class growthHandler extends Handler {
	CanvasActivity client;
	public growthHandler(CanvasActivity activity) {
		client = activity;
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {	
		case GrowthThread.MSG_START_REVERT:
			client.resetTree();
			this.sendEmptyMessage(GrowthThread.MSG_REVERT);
			break;			
		case GrowthThread.MSG_REVERT:
			TraitsData.timelim += 0.4;
			client.treeView.setDuringInteraction(false);
			client.treeView.setDuringGrowthAnimation(true);
			client.treeView.postInvalidate();
			if (TraitsData.timelim < GrowthThread.treeAge)
				sendEmptyMessageDelayed(GrowthThread.MSG_REVERT, 40);
			break;
			
		case GrowthThread.MSG_PAUSE:
			this.removeMessages(GrowthThread.MSG_PLAY);
			this.removeMessages(GrowthThread.MSG_REVERT);
			this.removeMessages(GrowthThread.MSG_START_PLAY);
			this.removeMessages(GrowthThread.MSG_START_REVERT);
			break;
		
		case GrowthThread.MSG_START_PLAY:
			client.resetTree();
			this.sendEmptyMessage(GrowthThread.MSG_PLAY);
			break;
		case GrowthThread.MSG_PLAY:
			TraitsData.timelim -= 0.4;
			client.treeView.setDuringInteraction(false);
			client.treeView.setDuringGrowthAnimation(true);
			client.treeView.postInvalidate();
			if (TraitsData.timelim > 0)
				sendEmptyMessageDelayed(GrowthThread.MSG_PLAY, 40);
			break;
	
		case GrowthThread.MSG_STOP:
			TraitsData.timelim = -1;
			this.removeMessages(GrowthThread.MSG_PLAY);
			this.removeMessages(GrowthThread.MSG_REVERT);
			this.removeMessages(GrowthThread.MSG_START_PLAY);
			this.removeMessages(GrowthThread.MSG_START_REVERT);
			client.treeView.setDuringInteraction(false);
			client.treeView.postInvalidate();
			break;
		
		case GrowthThread.MSG_CLOSE:
			TraitsData.timelim = -1;
			this.removeMessages(GrowthThread.MSG_PLAY);
			this.removeMessages(GrowthThread.MSG_REVERT);
			this.removeMessages(GrowthThread.MSG_START_PLAY);
			this.removeMessages(GrowthThread.MSG_START_REVERT);
			client.treeView.setDuringInteraction(false);
			client.treeView.setDuringGrowthAnimation(false);
			client.treeView.postInvalidate();
			break;

		default:
			break;
		}
	}
}

