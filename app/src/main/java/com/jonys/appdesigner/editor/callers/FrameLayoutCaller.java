package com.jonys.appdesigner.editor.callers;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import com.jonys.appdesigner.utils.Constants;
import com.jonys.appdesigner.utils.DimensionUtil;

public class FrameLayoutCaller {
	
	public static void setGravity(View target, String value, Context context) {
		String[] flags = value.split("\\|");
		int result = 0;

		for(String flag : flags) {
			result |= Constants.gravityMap.get(flag);
		}

		((FrameLayout.LayoutParams) target.getLayoutParams()).gravity = result;
		target.requestLayout();
	}
	
	public static void setLayoutMargin(View target, String value, Context context) {
		int margin = (int) DimensionUtil.parse(value, context);
		((FrameLayout.LayoutParams) target.getLayoutParams()).setMargins(margin, margin, margin, margin);
		target.requestLayout();
	}

	public static void setLayoutMarginLeft(View target, String value, Context context) {
		int margin = (int) DimensionUtil.parse(value, context);
		((FrameLayout.LayoutParams) target.getLayoutParams()).leftMargin = margin;
		target.requestLayout();
	}

	public static void setLayoutMarginRight(View target, String value, Context context) {
		int margin = (int) DimensionUtil.parse(value, context);
		((FrameLayout.LayoutParams) target.getLayoutParams()).rightMargin = margin;
		target.requestLayout();
	}

	public static void setLayoutMarginTop(View target, String value, Context context) {
		int margin = (int) DimensionUtil.parse(value, context);
		((FrameLayout.LayoutParams) target.getLayoutParams()).topMargin = margin;
		target.requestLayout();
	}

	public static void setLayoutMarginBottom(View target, String value, Context context) {
		int margin = (int) DimensionUtil.parse(value, context);
		((FrameLayout.LayoutParams) target.getLayoutParams()).bottomMargin = margin;
		target.requestLayout();
	}
}