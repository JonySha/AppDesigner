package com.jonys.appdesigner.editor.callers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;

import com.jonys.appdesigner.managers.DrawableManager;
import com.jonys.appdesigner.utils.ArgumentUtil;
import com.jonys.appdesigner.utils.DimensionUtil;
import com.jonys.appdesigner.managers.IdManager;

public class ViewCaller {
	
	public static void setId(View target, String value, Context context) {
		IdManager.addNewId(target, value);
	}
	
	public static void setLayoutWidth(View target, String value, Context context) {
		target.getLayoutParams().width = (int) DimensionUtil.parse(value, context);
		target.requestLayout();
	}
	
	public static void setLayoutHeight(View target, String value, Context context) {
		target.getLayoutParams().height = (int) DimensionUtil.parse(value, context);
		target.requestLayout();
	}
	
	public static void setBackground(View target, String value, Context context) {
		if(ArgumentUtil.parseType(value, new String[] {"color", "drawable"}).equals(ArgumentUtil.COLOR)) {
			target.setBackgroundColor(Color.parseColor(value));
		}
		else {
			String name = value.replace("@drawable/", "");
			target.setBackgroundDrawable(DrawableManager.getDrawable(name));
		}
	}
	
	public static void setForeground(View target, String value, Context context) {
		if(ArgumentUtil.parseType(value, new String[] {"color", "drawable"}).equals(ArgumentUtil.COLOR)) {
			target.setForeground(new ColorDrawable(Color.parseColor(value)));
		}
		else {
			String name = value.replace("@drawable/", "");
			target.setForeground(DrawableManager.getDrawable(name));
		}
	}
	
	public static void setElevation(View target, String value, Context context) {
		target.setElevation(DimensionUtil.parse(value, context));
	}
	
	public static void setAlpha(View target, String value, Context context) {
		target.setAlpha(Float.valueOf(value));
	}
	
	public static void setRotation(View target, String value, Context context) {
		target.setRotation(Float.valueOf(value));
	}
	
	public static void setRotationX(View target, String value, Context context) {
		target.setRotationX(Float.valueOf(value));
	}
	
	public static void setRotationY(View target, String value, Context context) {
		target.setRotationY(Float.valueOf(value));
	}
	
	public static void setPadding(View target, String value, Context context) {
		int pad = (int) DimensionUtil.parse(value, context);
		target.setPadding(pad, pad, pad, pad);
	}
	
	public static void setPaddingLeft(View target, String value, Context context) {
		int pad = (int) DimensionUtil.parse(value, context);
		target.setPadding(pad, target.getPaddingTop(), target.getPaddingRight(), target.getPaddingBottom());
	}
	
	public static void setPaddingRight(View target, String value, Context context) {
		int pad = (int) DimensionUtil.parse(value, context);
		target.setPadding(target.getPaddingLeft(), target.getPaddingTop(), pad, target.getPaddingBottom());
	}
	
	public static void setPaddingTop(View target, String value, Context context) {
		int pad = (int) DimensionUtil.parse(value, context);
		target.setPadding(target.getPaddingLeft(), pad, target.getPaddingRight(), target.getPaddingBottom());
	}
	
	public static void setPaddingBottom(View target, String value, Context context) {
		int pad = (int) DimensionUtil.parse(value, context);
		target.setPadding(target.getPaddingLeft(), target.getPaddingTop(), target.getPaddingRight(), pad);
	}
}