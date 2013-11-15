package com.onezoom.midnode;

import com.onezoom.CanvasActivity;

public interface Initializer {
	public MidNode createMidNode(String fileIndex);

	public MidNodeOneChunk createTreeChunk(String fileNumber);

	void createTreeChunk(MidNode midNode, int childIndex);
}
