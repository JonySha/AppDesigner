package com.jonys.appdesigner.tools;

import java.util.ArrayList;
import java.util.HashMap;

import android.view.View;
import android.view.ViewGroup;

import com.jonys.appdesigner.editor.EditorLayout;
import com.jonys.appdesigner.editor.AttributeMap;

public class XmlLayoutGenerator {
	
	StringBuilder builder = new StringBuilder();
	String TAB = "    ";
	
	boolean useSuperclasses;
	
    public String generate(EditorLayout editor, boolean useSuperclasses) {
		this.useSuperclasses = useSuperclasses;
		
		if(editor.getChildCount() == 0) {
			return "";
		}
		
		return peek(editor.getChildAt(0), editor.getViewAttributeMap(), 0);
	}
	
	private String peek(View view, HashMap<View, AttributeMap> attributeMap, int depth) {
		String indent = getIndent(depth);
		int nextDepth = depth;
		
		String className = useSuperclasses ? view.getClass().getSuperclass().getName() : view.getClass().getName();
		
		if(useSuperclasses) {
			if(className.startsWith("android.widget")) {
				className = view.getClass().getSuperclass().getSimpleName();
			}
		}
		
		builder.append(indent + "<" + className + "\n");
		
		if(depth == 0) {
			builder.append(TAB + "xmlns:android=\"http://schemas.android.com/apk/res/android\"\n");
			builder.append(TAB + "xmlns:app=\"http://schemas.android.com/apk/res-auto\"\n");
		}
		
		ArrayList<String> keys = attributeMap.get(view).keySet();
		ArrayList<String> values = attributeMap.get(view).values();
		
		for(String key : keys) {
			builder.append(TAB + indent + key + "=\"" + attributeMap.get(view).getValue(key) + "\"\n");
		}
		
		builder.deleteCharAt(builder.length()-1);
		
		if(view instanceof ViewGroup) {
			ViewGroup group = (ViewGroup) view;
			nextDepth++;
			
			if(group.getChildCount() > 0) {
				builder.append(">\n\n");
				
				for(int i = 0; i < group.getChildCount(); i ++) {
					peek(group.getChildAt(i), attributeMap, nextDepth);
				}
				
				builder.append(indent + "</" + className + ">\n\n");
			}
			else {
				builder.append("/>\n\n");
			}
		}
		else {
			builder.append("/>\n\n");
		}
		
		return builder.toString().trim();
	}
	
	private String getIndent(int depth) {
		String s = "";
		
		for(int i = 0; i < depth; i ++) {
			s += TAB;
		}
		
		return s;
	}
}