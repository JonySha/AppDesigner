package com.jonys.appdesigner.managers;

import android.widget.ImageButton;

import java.util.ArrayList;

public class UndoRedoManager {
	
	private final int maxSize = 20;
	
	private ArrayList<String> history;
	private int index;
	
	private ImageButton btnUndo;
	private ImageButton btnRedo;
	
	public UndoRedoManager(ImageButton undo, ImageButton redo) {
		btnUndo = undo;
		btnRedo = redo;
		history = new ArrayList<>();
	}
	
	public void addToHistory(String xml) {
		history.add(xml);
		
		if(history.size() == maxSize) {
			history.remove(0);
		}
		
		index = history.size()-1;
		toggleButtons();
	}
	
	public String undo() {
		if(index > 0) {
			index--;
			toggleButtons();
			return history.get(index);
		}
		
		return "";
	}
	
	public String redo() {
		if(index < history.size()-1) {
			index++;
			toggleButtons();
			return history.get(index);
		}
		
		return "";
	}
	
	private void toggleButtons() {
		btnUndo.setEnabled(isUndoEnabled());
		btnUndo.setAlpha(isUndoEnabled() ? 1 : 0.3f);
		
		btnRedo.setEnabled(isRedoEnabled());
		btnRedo.setAlpha(isRedoEnabled() ? 1 : 0.3f);
	}
	
	public boolean isUndoEnabled() {
		return index > 0;
	}
	
	public boolean isRedoEnabled() {
		return index < history.size()-1;
	}
}