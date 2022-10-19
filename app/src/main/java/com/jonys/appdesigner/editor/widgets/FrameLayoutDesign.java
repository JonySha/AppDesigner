package com.jonys.appdesigner.editor.widgets;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.widget.FrameLayout;

import com.jonys.appdesigner.R;

public class FrameLayoutDesign extends FrameLayout {
	
	private Drawable strokeDrawable;
	private boolean drawStrokeEnabled;
	
	public FrameLayoutDesign(Context context) {
		super(context);
		
		strokeDrawable = context.getDrawable(R.drawable.background_stroke_dash);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		strokeDrawable.setBounds(0, 0, w, h);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		
		if(drawStrokeEnabled)
			strokeDrawable.draw(canvas);
	}
	
	public void setStrokeEnabled(boolean enabled) {
		drawStrokeEnabled = enabled;
		invalidate();
	}
}