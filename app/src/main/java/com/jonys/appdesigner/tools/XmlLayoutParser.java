package com.jonys.appdesigner.tools;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.jonys.appdesigner.editor.AttributeInitializer;
import com.jonys.appdesigner.editor.AttributeMap;
import com.jonys.appdesigner.managers.IdManager;
import com.jonys.appdesigner.utils.InvokeUtil;
import com.jonys.appdesigner.utils.FileUtil;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class XmlLayoutParser {
	
	private Context context;
	
	private HashMap<String, ArrayList<HashMap<String, Object>>> attributes;
	private HashMap<String, ArrayList<HashMap<String, Object>>> parentAttributes;
	
	private HashMap<View, AttributeMap> viewAttributeMap = new HashMap<>();
	private AttributeInitializer initializer;
	
	private LinearLayout container;
	
	public XmlLayoutParser(Context context) {
		this.context = context;
		
		attributes = new Gson().fromJson(FileUtil.readFromAsset("attributes.java", context), new TypeToken<HashMap<String, ArrayList<HashMap<String, Object>>>>(){}.getType());
		parentAttributes = new Gson().fromJson(FileUtil.readFromAsset("parent_attributes.java", context), new TypeToken<HashMap<String, ArrayList<HashMap<String, Object>>>>(){}.getType());
		
		initializer = new AttributeInitializer(context, attributes, parentAttributes);
		
		container = new LinearLayout(context);
		container.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
	}
	
	public View getRoot() {
		View view = container.getChildAt(0);
		container.removeView(view);
		return view;
	}
	
	public HashMap<View, AttributeMap> getViewAttributeMap() {
		return viewAttributeMap;
	}
	
	public void parseFromXml(final String xml) {
		ArrayList<View> listViews = new ArrayList<>();
		listViews.add(container);
		
		try {
			final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			final XmlPullParser parser = factory.newPullParser();
			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			parser.setInput(new StringReader(xml));
			
			while(parser.getEventType() != XmlPullParser.END_DOCUMENT) {
				switch(parser.getEventType()) {
					case XmlPullParser.START_TAG: {
						View view = (View) InvokeUtil.createView(parser.getName(), context);
						listViews.add(view);
						
						AttributeMap map = new AttributeMap();
							
						for(int i = 0; i < parser.getAttributeCount(); i ++) {
							if(!parser.getAttributeName(i).startsWith("xmlns")) {
								map.putValue(parser.getAttributeName(i), parser.getAttributeValue(i));
							}
						}
						
						viewAttributeMap.put(view, map);
						break;
					}
					
					case XmlPullParser.END_TAG: {
						int index = parser.getDepth();
						((ViewGroup) listViews.get(index-1)).addView(listViews.get(index));
						listViews.remove(index);
						break;
					}
				}
				
				parser.next();
			}
		}
		catch(XmlPullParserException e) {
			e.printStackTrace();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
		
		IdManager.clear();
		
		for(View view : viewAttributeMap.keySet()) {
			AttributeMap map = viewAttributeMap.get(view);
			
			if(map.contains("android:id")) {
				IdManager.addNewId(view, map.getValue("android:id"));
			}
		}
		
		for(View view : viewAttributeMap.keySet()) {
			AttributeMap map = viewAttributeMap.get(view);
			applyAttributes(view, map);
		}
	}
	
	private void applyAttributes(View target, AttributeMap attributeMap) {
		final ArrayList<HashMap<String, Object>> allAttrs = initializer.getAllAttributesForView(target);
		
		final ArrayList<String> keys = attributeMap.keySet();
		final ArrayList<String> values = attributeMap.values();
		
		for(int i = keys.size()-1; i >= 0; i--) {
			String key = keys.get(i);
			
			HashMap<String, Object> attr = initializer.getAttributeFromKey(key, allAttrs);
			String methodName = attr.get("methodName").toString();
			String className = attr.get("className").toString();
			String value = attributeMap.getValue(key);
			
			if(key.equals("android:id")) {
				continue;
			}
			
			InvokeUtil.invokeMethod(methodName, className, target, value, context);
		}
	}
}