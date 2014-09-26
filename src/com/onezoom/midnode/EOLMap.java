package com.onezoom.midnode;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.onezoom.CanvasActivity;

import au.com.bytecode.opencsv.CSVReader;


public class EOLMap {
	Map<String, String> map;
	private static EOLMap instance;
	private static CanvasActivity client;
	
	public static EOLMap getInstance() {
		if (instance == null) {
			instance = new EOLMap();
		}
		return instance;	
	}
	
	private EOLMap() {
		map = new HashMap<String, String>();
		int resourceID = client.getResources().getIdentifier("eollink", "raw", client.getPackageName());
		InputStream is = client.getResources().openRawResource(resourceID);
		CSVReader reader = new CSVReader(new InputStreamReader(is));
		try {
			String[] line;
			reader.readNext();
			while ((line = reader.readNext()) != null) {
				map.put(line[0], line[1]);	
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}

	public static void setClient(CanvasActivity canvasActivity) {
		client = canvasActivity;
	}
}