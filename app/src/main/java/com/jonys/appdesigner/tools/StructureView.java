package com.jonys.appdesigner.tools;

import android.widget.LinearLayout;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View.OnClickListener;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import java.util.HashMap;

import com.jonys.appdesigner.R;

public class StructureView extends LinearLayout implements OnClickListener {
    
	private LayoutInflater inflater;
	private Paint paint;
	
	private int pointRadius;
	
	private HashMap<TextView, View> textViewMap = new HashMap<>();
	private HashMap<View, TextView> viewTextMap = new HashMap<>();
	
	private OnItemClickListener listener = new OnItemClickListener() {

		@Override
		public void onItemClick(View view) {
		}
	};
	
	public StructureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		inflater = LayoutInflater.from(context);
		
		paint = new Paint();
		paint.setColor(Color.DKGRAY);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(getDip(1));
		
		pointRadius = getDip(3);
		
		setOrientation(VERTICAL);
	}
	
	public void clear() {
		removeAllViews();
		textViewMap.clear();
		viewTextMap.clear();
	}
	
	public void setView(View view) {
		textViewMap.clear();
		viewTextMap.clear();
		removeAllViews();
		peek(view, 1);
	}
	
	private void peek(View view, int depth) {
		int nextDepth = depth;
		TextView text = (TextView) inflater.inflate(android.R.layout.simple_list_item_1, null, false);
		text.setTextSize(16);
		text.setText(view.getClass().getSimpleName());
		text.setOnClickListener(this);
		
		int pad = getDip(8);
		text.setPadding(getDip(16), pad, pad, pad);
		addView(text);
		
		LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) text.getLayoutParams();
		params.leftMargin = depth * getDip(15);
		
		textViewMap.put(text, view);
		viewTextMap.put(view, text);
		
		if(view instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) view;
			nextDepth++;
			
			for(int i = 0; i < group.getChildCount(); i++) {
				View child = group.getChildAt(i);
				peek(child, nextDepth);
			}
		}
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		
		for(TextView text : textViewMap.keySet()) {
			View view = textViewMap.get(text);
			
			if(view instanceof ViewGroup && ((ViewGroup) view).getChildCount() > 0) {
				float x = text.getX();
				float y = text.getY() + text.getHeight()/2;
				canvas.drawRect(x - pointRadius, y - pointRadius, x + pointRadius, y + pointRadius, paint);
				
				ViewGroup group = (ViewGroup) view;
				
				for(int i = 0; i < group.getChildCount(); i++) {
					TextView current = viewTextMap.get(group.getChildAt(i));
					canvas.drawLine(text.getX(), text.getY() + text.getHeight()/2, text.getX(), current.getY() + current.getHeight()/2, paint);
					canvas.drawLine(text.getX(), current.getY() + current.getHeight()/2, current.getX(), current.getY() + current.getHeight()/2, paint);
				}
			}
			else {
				canvas.drawCircle(text.getX(), text.getY() + text.getHeight()/2, pointRadius, paint);
			}
		}
	}

	@Override
	public void onClick(View v) {
		listener.onItemClick(textViewMap.get((TextView) v));
	}

	private int getDip(int input) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, input, getContext().getResources().getDisplayMetrics());
	}
	
	public void setOnItemClickListener(OnItemClickListener listener) {
		this.listener = listener;
	}
	
	public static abstract interface OnItemClickListener {
		
		public void onItemClick(View view);
	}
}