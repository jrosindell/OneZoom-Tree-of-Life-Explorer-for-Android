package com.onezoom.midnode;

import com.onezoom.CanvasActivity;

public interface Initializer {
	public MidNode createMidNode(CanvasActivity canvasActivity,
			String selectedGroup, String fileIndex);
}
