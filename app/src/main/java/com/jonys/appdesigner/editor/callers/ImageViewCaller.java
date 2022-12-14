package com.jonys.appdesigner.editor.callers;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

import com.jonys.appdesigner.managers.DrawableManager;

import java.util.HashMap;

public class ImageViewCaller {
	
	private static HashMap<String, ImageView.ScaleType> scaleTypes = new HashMap<>();
	static {
		scaleTypes.put("fitXY", ImageView.ScaleType.FIT_XY);
		scaleTypes.put("fitStart", ImageView.ScaleType.FIT_START);
		scaleTypes.put("fitCenter", ImageView.ScaleType.FIT_CENTER);
		scaleTypes.put("fitEnd", ImageView.ScaleType.FIT_END);
		scaleTypes.put("center", ImageView.ScaleType.CENTER);
		scaleTypes.put("centerCrop", ImageView.ScaleType.CENTER_CROP);
		scaleTypes.put("centerInside", ImageView.ScaleType.CENTER_INSIDE);
	}
	
	public static void setImage(View target, String value, Context context) {
		String name = value.replace("@drawable/", "");
		((ImageView) target).setImageDrawable(DrawableManager.getDrawable(name));
	}
	
	public static void setScaleType(View target, String value, Context context) {
		((ImageView) target).setScaleType(scaleTypes.get(value));
	}
	
	public static void setTint(View target, String value, Context context) {
		((ImageView) target).setColorFilter(Color.parseColor(value));
	}
}