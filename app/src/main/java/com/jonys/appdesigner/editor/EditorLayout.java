package com.jonys.appdesigner.editor;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.view.View;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.jonys.appdesigner.editor.dialogs.*;
import com.jonys.appdesigner.databinding.DefinedAttributeItemBinding;
import com.jonys.appdesigner.databinding.DefinedAttributesDialogBinding;
import com.jonys.appdesigner.utils.ArgumentUtil;
import com.jonys.appdesigner.utils.DimensionUtil;
import com.jonys.appdesigner.utils.InvokeUtil;
import com.jonys.appdesigner.utils.FileUtil;
import com.jonys.appdesigner.tools.StructureView;
import com.jonys.appdesigner.managers.IdManager;
import com.jonys.appdesigner.R;

import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class EditorLayout extends LinearLayout {
	
	private static final String TAG = "EditorLayout";
	
	private LayoutInflater inflater;
	
	private final View shadow;
	
	private StructureView structureView;
	
	private HashMap<View, AttributeMap> viewAttributeMap = new HashMap<>();
	
	private HashMap<String, ArrayList<HashMap<String, Object>>> attributes;
	private HashMap<String, ArrayList<HashMap<String, Object>>> parentAttributes;
	
	private final OnDragListener onDragListener = new OnDragListener() {
		
		@Override
		public boolean onDrag(View host, DragEvent event) {
			ViewGroup parent = (ViewGroup) host;
			View draggedView = null;
			
			if(event.getLocalState() instanceof View) {
				draggedView = (View) event.getLocalState();
			}
			
			switch(event.getAction()) {
				case DragEvent.ACTION_DRAG_STARTED: {
					if(draggedView != null) {
						parent.removeView(draggedView);
					}
					return true;
				}
				
				case DragEvent.ACTION_DRAG_EXITED: {
					removeWidget(shadow);
					return true;
				}
				
				case DragEvent.ACTION_DRAG_ENDED: {
					if(!event.getResult() && draggedView != null) {
						IdManager.removeId(draggedView, draggedView instanceof ViewGroup);
						removeViewAttributes(draggedView);
						viewAttributeMap.remove(draggedView);
						updateStructure();
					}
					
					return true;
				}
				
				case DragEvent.ACTION_DRAG_LOCATION: 
				case DragEvent.ACTION_DRAG_ENTERED: {
					if(shadow.getParent() == null) {
						addWidget(shadow, parent, event);
					}
					else {
						if(parent instanceof LinearLayout) {
							int index = parent.indexOfChild(shadow);
							int newIndex = getIndexForNewChildOfLinear((LinearLayout) parent, event);

							if(index != newIndex) {
								parent.removeView(shadow);
								parent.addView(shadow, newIndex);
							}
						}
						else {
							if(shadow.getParent() != parent) {
								addWidget(shadow, parent, event);
							}
						}
					}
					
					return true;
				}
				
				case DragEvent.ACTION_DROP: {
					removeWidget(shadow);
					
					if(draggedView == null) {
						final HashMap<String, Object> data = (HashMap) event.getLocalState();
						final View newView = (View) InvokeUtil.createView(data.get("className").toString(), getContext());
						
						newView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
						newView.setForeground(getResources().getDrawable(R.drawable.background_stroke_dash));
						rearrangeListeners(newView);
						
						if(newView instanceof ViewGroup) {
							newView.setMinimumWidth(getDip(20));
							newView.setMinimumHeight(getDip(20));
							newView.setOnDragListener(onDragListener);
							
							setupViewGroupAnimation((ViewGroup) newView);
						}
						
						AttributeMap map = new AttributeMap();
						map.putValue("android:layout_width", "wrap_content");
						map.putValue("android:layout_height", "wrap_content");
						viewAttributeMap.put(newView, map);
						
						addWidget(newView, parent, event);
						
						if(data.containsKey("defaultAttributes")) {
							applyDefaultAttributes(newView, (Map) data.get("defaultAttributes"));
						}
					}
					else {
						addWidget(draggedView, parent, event);
					}
					
					updateStructure();
					return true;
				}
			}
			
			return false;
		}
	};
	
	public EditorLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		inflater = LayoutInflater.from(context);
		
		shadow = new View(context);
		shadow.setLayoutParams(new ViewGroup.LayoutParams(getDip(30), getDip(25)));
		shadow.setBackgroundColor(Color.DKGRAY);
		
		setOnDragListener(onDragListener);
		setupViewGroupAnimation(this);
		
		attributes = new Gson().fromJson(FileUtil.readFromAsset("attributes.java", context), new TypeToken<HashMap<String, ArrayList<HashMap<String, Object>>>>(){}.getType());
		parentAttributes = new Gson().fromJson(FileUtil.readFromAsset("parent_attributes.java", context), new TypeToken<HashMap<String, ArrayList<HashMap<String, Object>>>>(){}.getType());
	}
	
	public void setStructureView(StructureView view) {
		structureView = view;
	}
	
	private void updateStructure() {
		if(getChildCount() == 0) {
			structureView.clear();
		}
		else {
			structureView.setView(getChildAt(0));
		}
	}
	
	private void rearrangeListeners(final View view) {
		final GestureDetector gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() { 
			
			@Override
			public void onLongPress(MotionEvent event) {	
				view.startDragAndDrop(null, new DragShadowBuilder(view), view, 0);
			} 
		}); 

		view.setOnTouchListener(new OnTouchListener() {
			boolean bClick = true;
			float startX = 0;
			float startY= 0;
			float endX=0;
			float endY = 0;
			float diffX= 0;
			float diffY = 0;

			@Override
			public boolean onTouch(final View v, MotionEvent event) {
				switch (event.getAction()) {
					case MotionEvent.ACTION_DOWN: 
						startX = event.getX(); 
						startY = event.getY(); 
						bClick = true;
						break;

					case MotionEvent.ACTION_UP:
						endX = event.getX(); 
						endY = event.getY(); 
						diffX = Math.abs(startX - endX);
						diffY = Math.abs(startY - endY); 

						if(diffX <= 5 && diffY <= 5 && bClick == true) {
							showDefinedAttributes(v);
						} 
							
						bClick = false;
						break;
				}

				gestureDetector.onTouchEvent(event);
				
				return true;
			}
		});
	}
	
	private void addWidget(View view, ViewGroup newParent, DragEvent event) {
		removeWidget(view);
		
		if(newParent instanceof LinearLayout) {
			int index = getIndexForNewChildOfLinear((LinearLayout) newParent, event);
			newParent.addView(view, index);
		}
		else {
			/*
			hack check scrollview parent
			****/
			try {
				newParent.addView(view, newParent.getChildCount());
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void removeWidget(View view) {
		ViewGroup parent = (ViewGroup) view.getParent();
		
		if(parent != null)
			parent.removeView(view);
	}
	
	private int getIndexForNewChildOfLinear(LinearLayout layout, DragEvent event) {
		int orientation = layout.getOrientation();
		
		if(orientation == LinearLayout.HORIZONTAL) {
			int index = 0;
			
			for(int i = 0; i < layout.getChildCount(); i ++) {
				View child = layout.getChildAt(i);
				
				if(child == shadow)
					continue;
					
				if(child.getRight() < event.getX())
					index++;
			}
			
			return index;
		}
		
		if(orientation == LinearLayout.VERTICAL) {
			int index = 0;

			for(int i = 0; i < layout.getChildCount(); i ++) {
				View child = layout.getChildAt(i);

				if(child == shadow)
					continue;

				if(child.getBottom() < event.getY())
					index++;
			}

			return index;
		}
		
		return -1;
	}
	
	private void setupViewGroupAnimation(ViewGroup group) {
		LayoutTransition transition = new LayoutTransition();
		transition.disableTransitionType(LayoutTransition.DISAPPEARING);
		transition.enableTransitionType(LayoutTransition.CHANGING);
		transition.setDuration(150);
		
		group.setLayoutTransition(transition);
	}
	
	public void showDefinedAttributes(final View target) {
		final BottomSheetDialog dialog = new BottomSheetDialog(getContext());
		
		DefinedAttributesDialogBinding dialogBinding = DefinedAttributesDialogBinding.inflate(inflater);
		dialog.setContentView(dialogBinding.getRoot());
		
		final ArrayList<String> keys = viewAttributeMap.get(target).keySet();
		final ArrayList<String> values = viewAttributeMap.get(target).values();
		
		final ArrayList<HashMap<String, Object>> attrs = new ArrayList<>();
		final ArrayList<HashMap<String, Object>> allAttrs = getAllAttributesForView(target);
		
		for(String key : keys) {
			for(HashMap<String, Object> map : allAttrs) {
				if(map.get("attributeName").toString().equals(key)) {
					attrs.add(map);
					break;
				}
			}
		}
		
		final BaseAdapter adapter = new BaseAdapter() {
			
			@Override
			public int getCount() {
				return keys.size();
			}
		
			@Override
			public long getItemId(int arg0) {
				return 0;
			}
		
			@Override
			public Object getItem(int pos) {
				return null;
			}
		
			@Override
			public View getView(int pos, View buffer, ViewGroup parent) {
				final HashMap<String, Object> item = attrs.get(pos);
				
				final DefinedAttributeItemBinding itemBinding = DefinedAttributeItemBinding.inflate(inflater);
				itemBinding.textName.setText(item.get("name").toString());
				itemBinding.textValue.setText(values.get(pos));
				
				if(item.containsKey("canDelete")) {
					itemBinding.btnDelete.setVisibility(View.GONE);
				}
				
				itemBinding.getRoot().setOnClickListener(v -> {
					showAttributeEdit(target, keys.get(pos));
					dialog.dismiss();
				});
				
				itemBinding.btnDelete.setOnClickListener(v -> {
					dialog.dismiss();
					
					View view = removeAttribute(target, keys.get(pos));
					showDefinedAttributes(view);
				});
				
				return itemBinding.getRoot();
			}
		};
		
		dialogBinding.listView.setAdapter(adapter);
		dialogBinding.widgetName.setText(target.getClass().getSimpleName());
		dialogBinding.btnAdd.setOnClickListener(v -> {
			
			showAvailableAttributes(target);
			dialog.dismiss();
		});
		
		dialogBinding.btnDelete.setOnClickListener(v -> {
			final MaterialAlertDialogBuilder confirm = new MaterialAlertDialogBuilder(getContext());
			confirm.setTitle("Delete view");
			confirm.setMessage("Do you want to remove the view?");
			confirm.setNegativeButton("No", (di, which) -> {});
			confirm.setPositiveButton("Yes", (di, which) -> {
				
				//delete widget and id
				IdManager.removeId(target, target instanceof ViewGroup);
				removeViewAttributes(target);
				removeWidget(target);
				viewAttributeMap.remove(target);
				updateStructure();
				dialog.dismiss();
			});
			confirm.create().show();
		});
		
		dialog.show();
	}
	
	private void showAvailableAttributes(final View target) {
		final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
		builder.setTitle("Available attributes");
		
		final ArrayList<HashMap<String, Object>> availableAttrs = getAvailableAttributesForView(target);
		final ArrayList<String> names = new ArrayList<>();
		
		for(HashMap<String, Object> attr : availableAttrs) {
			names.add(attr.get("name").toString());
		}
		
		builder.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, names), new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface di, int which) {
				showAttributeEdit(target, availableAttrs.get(which).get("attributeName").toString());
			}
		});
		
		builder.create().show();
	}
	
	private void applyDefaultAttributes(final View target, final Map<String, String> defaultAttrs) {
		ArrayList<HashMap<String, Object>> allAttrs = getAllAttributesForView(target);
		
		for(String key : defaultAttrs.keySet()) {
			for(HashMap<String, Object> map : allAttrs) {
				if(map.get("attributeName").toString().equals(key)) {
					applyAttribute(target, defaultAttrs.get(key).toString(), map);
					break;
				}
			}
		}
	}
	
	private void showAttributeEdit(final View target, final String attributeKey) {
		final ArrayList<HashMap<String, Object>> allAttrs = getAllAttributesForView(target);
		final HashMap<String, Object> currentAttr = getAttributeFromKey(attributeKey, allAttrs);
		final AttributeMap attributeMap = viewAttributeMap.get(target);
		
		final String[] argumentTypes = currentAttr.get("argumentType").toString().split("\\|");
		
		if(argumentTypes.length > 1) {
			if(attributeMap.contains(attributeKey)) {
				String argumentType = ArgumentUtil.parseType(attributeMap.getValue(attributeKey), argumentTypes);
				showAttributeEdit(target, attributeKey, argumentType);
				return;
			}
			
			final MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(getContext());
			dialog.setTitle("Select argument type");
			dialog.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, argumentTypes), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface di, int which) {
					showAttributeEdit(target, attributeKey, argumentTypes[which]);
				}
			});
			dialog.create().show();
			return;
		}
		
		showAttributeEdit(target, attributeKey, argumentTypes[0]);
	}
	
	private void showAttributeEdit(final View target, final String attributeKey, final String argumentType) {
		final ArrayList<HashMap<String, Object>> allAttrs = getAllAttributesForView(target);
		final HashMap<String, Object> currentAttr = getAttributeFromKey(attributeKey, allAttrs);
		final AttributeMap attributeMap = viewAttributeMap.get(target);
		
		final String savedValue = attributeMap.contains(attributeKey) ? attributeMap.getValue(attributeKey) : "";
		final String defaultValue = currentAttr.containsKey("defaultValue") ? currentAttr.get("defaultValue").toString() : null;
		
		final Context context = getContext();
		
		AttributeDialog dialog = null;
		
		switch(argumentType) {
			case "size":
			dialog = new SizeDialog(context, savedValue);
			break;
			
			case "dimension":
			dialog = new DimensionDialog(context, savedValue, currentAttr.get("dimensionUnit").toString());
			break;
			
			case "id":
			dialog = new IdDialog(context, savedValue);
			break;
			
			case "view":
			dialog = new ViewDialog(context, savedValue);
			break;
			
			case "boolean":
			case "string":
			dialog = new StringDialog(context, savedValue);
			break;
			
			case "int":
			dialog = new NumberDialog(context, savedValue, "int");
			break;
			
			case "float":
			dialog = new NumberDialog(context, savedValue, "float");
			break;
			
			case "flag":
			dialog = new FlagDialog(context, savedValue, (ArrayList) currentAttr.get("arguments"));
			break;
			
			case "enum":
			dialog = new EnumDialog(context, savedValue, (ArrayList) currentAttr.get("arguments"));
			break;
			
			case "color":
			dialog = new ColorDialog(context, savedValue);
			break;
		}
		
		if(dialog == null) {
			return;
		}
		
		dialog.setTitle(currentAttr.get("name").toString());
		dialog.setOnSaveValueListener(value -> {
			
			if(defaultValue != null && defaultValue.equals(value)) {
				if(attributeMap.contains(attributeKey)) {
					removeAttribute(target, attributeKey);
				}
			}
			else {
				applyAttribute(target, value, currentAttr);
				showDefinedAttributes(target);
			}
		});
		
		dialog.show();
	}
	
	private void applyAttribute(final View target, final String value, final HashMap<String, Object> attribute) {
		String methodName = attribute.get("methodName").toString();
		String className = attribute.get("className").toString();
		String attributeName = attribute.get("attributeName").toString();
		
		viewAttributeMap.get(target).putValue(attributeName, value);
		InvokeUtil.invokeMethod(methodName, className, target, value, getContext());
		
		//update ids attributes for all views
		if(value.startsWith("@+id/") && viewAttributeMap.get(target).contains("android:id")) {
			for(View view : viewAttributeMap.keySet()) {
				AttributeMap map = viewAttributeMap.get(view);
				
				for(String key : map.keySet()) {
					if(map.getValue(key).startsWith("@id/")) {
						map.putValue(key, value.replace("+", ""));
					}
				}
			}
		}
	}
	
	private void removeViewAttributes(View view) {
		viewAttributeMap.remove(view);
		
		if(view instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) view;
			
			for(int i = 0; i < group.getChildCount(); i ++) {
				removeViewAttributes(group.getChildAt(i));
			}
		}
	}
	
	private View removeAttribute(View target, final String attributeKey) {
		final ArrayList<HashMap<String, Object>> allAttrs = getAllAttributesForView(target);
		final HashMap<String, Object> currentAttr = getAttributeFromKey(attributeKey, allAttrs);
		
		final AttributeMap attributeMap = viewAttributeMap.get(target);
		
		if(currentAttr.containsKey("canDelete")) {
			return target;
		}
		
		final String name = attributeMap.contains("android:id") ? attributeMap.getValue("android:id") : null;
		final int id = name != null ? IdManager.getViewId(name.replace("@+id/", "")) : -1;
		
		attributeMap.removeValue(attributeKey);
		
		if(attributeKey.equals("android:id")) {
			IdManager.removeId(target, false);
			target.setId(-1);
			target.requestLayout();
			
			//delete all id attributes for views
			for(View view : viewAttributeMap.keySet()) {
				AttributeMap map = viewAttributeMap.get(view);
				
				for(String key : map.keySet()) {
					String value = map.getValue(key);
					
					if(value.startsWith("@id/") && value.equals(name.replace("+", ""))) {
						map.removeValue(key);
					}
				}
			}
			
			return target;
		}
		
		viewAttributeMap.remove(target);
		
		final ViewGroup parent = (ViewGroup) target.getParent();
		final int indexOfView = parent.indexOfChild(target);
		
		parent.removeView(target);
		
		//если виджет является ViewGroup
		//то сохранить его детей
		final ArrayList<View> childs = new ArrayList<>();
		
		if(target instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) target;
			
			if(group.getChildCount() > 0) {
				for(int i = 0; i < group.getChildCount(); i ++) {
					childs.add(group.getChildAt(i));
				}
			}
			
			group.removeAllViews();
		}
		
		if(name != null) {
			IdManager.removeId(target, false);
		}
		
		target = (View) InvokeUtil.createView(target.getClass().getName(), getContext());
		target.setForeground(getResources().getDrawable(R.drawable.background_stroke_dash));
		rearrangeListeners(target);
		
		if(target instanceof ViewGroup) {
			target.setMinimumWidth(getDip(20));
			target.setMinimumHeight(getDip(20));
			target.setOnDragListener(onDragListener);

			final ViewGroup group = (ViewGroup) target;
			
			if(childs.size() > 0) {
				for(int i = 0; i < childs.size(); i++) {
					group.addView(childs.get(i));
				}
			}
			
			setupViewGroupAnimation(group);
		}
			
		parent.addView(target, indexOfView);
		viewAttributeMap.put(target, attributeMap);
		
		updateStructure();
		
		if(name != null) {
			IdManager.addId(target, name, id);
			target.requestLayout();
		}
		
		final ArrayList<String> keys = attributeMap.keySet();
		final ArrayList<String> values = attributeMap.values();
		
		final ArrayList<HashMap<String, Object>> attrs = new ArrayList<>();
		
		for(String key : keys) {
			for(HashMap<String, Object> map : allAttrs) {
				if(map.get("attributeName").toString().equals(key)) {
					attrs.add(map);
					break;
				}
			}
		}
		
		//init attributes
		for(int i = 0; i < keys.size(); i ++) {
			String key = keys.get(i);
			
			if(key.equals("android:id"))
				continue;
				
			applyAttribute(target, values.get(i), attrs.get(i));
		}
		
		return target;
	}
	
	private ArrayList<HashMap<String, Object>> getAvailableAttributesForView(final View target) {
		final ArrayList<String> keys = viewAttributeMap.get(target).keySet();
		final ArrayList<HashMap<String, Object>> allAttrs = getAllAttributesForView(target);
		
		for(int i = allAttrs.size() - 1; i >= 0; i --) {
			for(String key : keys) {
				if(key.equals(allAttrs.get(i).get("attributeName").toString())) {
					allAttrs.remove(i);
					break;
				}
			}
		}
		
		return allAttrs;
	}
	
	private ArrayList<HashMap<String, Object>> getAllAttributesForView(final View target) {
		ArrayList<HashMap<String, Object>> allAttrs = new ArrayList<>();
		
		Class cls = target.getClass();
		Class viewParentCls = View.class.getSuperclass();
		
		while(cls != viewParentCls) {
			if(attributes.containsKey(cls.getName())) {
				allAttrs.addAll(0, attributes.get(cls.getName()));
			}
			
			cls = cls.getSuperclass();
		}
		
		if(target.getParent() != null && target.getParent() != this) {
			cls = target.getParent().getClass();
		
			while(cls != viewParentCls) {
				if(parentAttributes.containsKey(cls.getName())) {
					allAttrs.addAll(parentAttributes.get(cls.getName()));
				}
			
				cls = cls.getSuperclass();
			}
		}
		
		return allAttrs;
	}
	
	private HashMap<String, Object> getAttributeFromKey(String key, ArrayList<HashMap<String, Object>> list) {
		for(HashMap<String, Object> map : list) {
			if(map.get("attributeName").equals(key)) {
				return map;
			}
		}
		
		return null;
	}
	
	@Override
	public void addView(View arg0, int arg1) {
	    if(getChildCount() == 0) {
			super.addView(arg0, arg1);
		}
	}
	    
	private void toast(String text) {
		Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
	}
	
	private int getDip(int value) {
		return (int) DimensionUtil.getDip(getContext(), value);
	}
}