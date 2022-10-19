package com.jonys.appdesigner.utils;

import android.view.Gravity;

import java.util.HashMap;

public class Constants {
	
	public static final HashMap<String, Integer> gravityMap = new HashMap<>();

	static {
		gravityMap.put("left", Gravity.LEFT);
		gravityMap.put("right", Gravity.RIGHT);
		gravityMap.put("top", Gravity.TOP);
		gravityMap.put("bottom", Gravity.BOTTOM);
		gravityMap.put("center", Gravity.CENTER);
		gravityMap.put("center_horizontal", Gravity.CENTER_HORIZONTAL);
		gravityMap.put("center_vertical", Gravity.CENTER_VERTICAL);
	}
}