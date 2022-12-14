package com.jonys.appdesigner.managers;

import android.graphics.drawable.Drawable;

import com.jonys.appdesigner.utils.FileUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

public class DrawableManager {
	
	private static HashMap<String, String> items = new HashMap<>();
	
	public static void loadFromFiles(File[] files) {
		items.clear();
		
		for(File f : files) {
			String path = f.getPath();
			String name = FileUtil.getLastSegmentFromPath(path);
			name = name.substring(0, name.lastIndexOf("."));
			
			items.put(name, path);
		}
	}
	
	public static boolean contains(String name) {
		return items.containsKey(name);
	}
	
	public static Drawable getDrawable(String key) {
		return Drawable.createFromPath(items.get(key));
	}
	
	public static Set<String> keySet() {
		return items.keySet();
	}
}