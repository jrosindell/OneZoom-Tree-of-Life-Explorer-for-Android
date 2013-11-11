package com.onezoom;

import java.util.Map;
import java.util.TreeMap;

import com.onezoom.midnode.displayBinary.BinaryTraitsCalculator;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

class GrowthThread extends Thread {
	Handler handler = new growthHandler();
	CanvasActivity client;
	boolean started = false;
	boolean pause = false;
	static final int MSG_PLAY = 0;
	static final int MSG_PAUSE = 1;
	static final int MSG_STOP = 2;
	static final int MSG_REVERT = 3;
	static final int MSG_CLOSE = 4;
	final int GROWTH_RATE = 10;
	Map<String, Integer> map;
	
	public GrowthThread(CanvasActivity activity) {
		client = activity;
		map = new TreeMap<String, Integer>();
		map.put("Mammals", 166);
		map.put("Birds", 113);
		map.put("Tetrapods", 390);
		map.put("Amphibian", 370);
		map.put("Turtles", 210);
	}
	@Override
	public void run() {
		try {
			// preparing a looper on current thread
			// the current thread is being detected implicitly
			Looper.prepare();

			// now, the handler will automatically bind to the
			// Looper that is attached to the current thread
			// You don't need to specify the Looper explicitly
			handler = new growthHandler();

			// After the following line the thread will start
			// running the message loop and will not normally
			// exit the loop unless a problem happens or you
			// quit() the looper (see below)
			Looper.loop();

		} catch (Throwable t) {
		}
	}
	
	// This method is allowed to be called from any thread
	public synchronized void requestStop() {
		// using the handler, post a Runnable that will quit()
		// the Looper attached to our DownloadThread
		// obviously, all previously queued tasks will be executed
		// before the loop gets the quit Runnable
		handler.post(new Runnable() {
			@Override
			public void run() {
				BinaryTraitsCalculator.timelim = -1;
				Looper.myLooper().quit();
			}
		});
	}
	
	public void Play() {
		if (!pause)
			BinaryTraitsCalculator.timelim = map.get(client.selectedItem);
		else 
			pause = !pause;
		handler.removeMessages(MSG_REVERT);
		handler.sendEmptyMessage(MSG_PLAY);
	}
	
	public void Pause() {
		handler.sendEmptyMessage(MSG_PAUSE);
	}
	
	public void Close() {
		handler.sendEmptyMessage(MSG_CLOSE);
	}
	
	public void Revert() {
		handler.removeMessages(MSG_PLAY);
		handler.sendEmptyMessage(MSG_REVERT);
	}
	
	public void Stop() {
		handler.sendEmptyMessage(MSG_STOP);
	}

	class growthHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_PLAY:
				BinaryTraitsCalculator.timelim -= 4;
				client.treeView.setDuringInteraction(false);
				client.treeView.postInvalidate();
				if (BinaryTraitsCalculator.timelim > 0)
					sendEmptyMessageDelayed(MSG_PLAY, 400);
				break;
		
			case MSG_PAUSE:
				pause = true;
				handler.removeMessages(MSG_PLAY);
				break;
		
			case MSG_STOP:
				client.treeView.setDuringInteraction(false);
				client.treeView.postInvalidate();
				handler.removeMessages(MSG_PLAY);
				handler.removeMessages(MSG_REVERT);
				BinaryTraitsCalculator.timelim = -1;
				break;
		
			case MSG_REVERT:
				BinaryTraitsCalculator.timelim += 4;
				client.treeView.setDuringInteraction(false);
				client.treeView.postInvalidate();
				if (BinaryTraitsCalculator.timelim < 400)
					sendEmptyMessageDelayed(MSG_REVERT, 400);
				break;
			
			case MSG_CLOSE:
				BinaryTraitsCalculator.timelim = -1;
				client.treeView.setDuringInteraction(false);
				client.treeView.postInvalidate();
				break;
	
			default:
				break;
			}
		}
	}
}

