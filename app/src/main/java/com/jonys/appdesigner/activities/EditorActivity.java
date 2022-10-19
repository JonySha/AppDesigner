package com.jonys.appdesigner.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.BaseAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.PopupMenu;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import com.jonys.appdesigner.ProjectFile;
import com.jonys.appdesigner.managers.DrawableManager;
import com.jonys.appdesigner.databinding.ActivityEditorBinding;
import com.jonys.appdesigner.databinding.ListViewItemBinding;
import com.jonys.appdesigner.managers.UndoRedoManager;
import com.jonys.appdesigner.tools.StructureView;
import com.jonys.appdesigner.tools.XmlLayoutGenerator;
import com.jonys.appdesigner.utils.FileUtil;
import com.jonys.appdesigner.utils.InvokeUtil;
import com.jonys.appdesigner.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class EditorActivity extends BaseActivity {

	public static final String EXTRA_KEY_PROJECT = "project";
	public static final String ACTION_OPEN = "com.jonys.appdesigner.open";
	
	private ActivityEditorBinding binding;
	
	private ArrayList<HashMap<String, Object>> views;
	private ArrayList<HashMap<String, Object>> layouts;
	
	private ProjectFile project;
	
	private UndoRedoManager undoRedo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		
		binding = ActivityEditorBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		
		//get project file from intent
		project = getIntent().getParcelableExtra(EXTRA_KEY_PROJECT);
		
		undoRedo = new UndoRedoManager(binding.btnUndo, binding.btnRedo);
		
		binding.title.setText(project.getName());
		
		binding.editorLayout.setStructureView(binding.structureView);
		binding.editorLayout.bindUndoRedoManager(undoRedo);
		binding.structureView.setOnItemClickListener(new StructureView.OnItemClickListener() {

			@Override
			public void onItemClick(View view) {
				binding.editorLayout.showDefinedAttributes(view);
				binding.drawer.closeDrawer(GravityCompat.END);
			}
		});
		
		binding.btnOptions.setOnClickListener(v -> showOptions(v));
		binding.btnMenu.setOnClickListener(v -> binding.drawer.openDrawer(GravityCompat.START));
		binding.btnStructure.setOnClickListener(v -> binding.drawer.openDrawer(GravityCompat.END));
		
		//undo
		binding.btnUndo.setAlpha(0.5f);
		binding.btnUndo.setEnabled(false);
		binding.btnUndo.setOnClickListener(v -> binding.editorLayout.undo());
		
		//redo
		binding.btnRedo.setAlpha(0.5f);
		binding.btnRedo.setEnabled(false);
		binding.btnRedo.setOnClickListener(v -> binding.editorLayout.redo());
		
		//list views groups
		binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Views"));
		binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Layouts"));
		binding.tabLayout.setOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
			
			@Override
			public void onTabSelected(TabLayout.Tab tab) {
				if(tab.getPosition() == 0) {
					binding.listView.setAdapter(new ListViewAdapter(views));
				} else {
					binding.listView.setAdapter(new ListViewAdapter(layouts));
				}
			}

			@Override
			public void onTabUnselected(TabLayout.Tab tab) {
			}

			@Override
			public void onTabReselected(TabLayout.Tab tab) {
			}
		});
		
		//load views from assets
		views = new Gson().fromJson(FileUtil.readFromAsset("views.java", this), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
		layouts = new Gson().fromJson(FileUtil.readFromAsset("layouts.java", this), new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
		
		binding.listView.setAdapter(new ListViewAdapter(views));
		
		if(getIntent().getAction() != null && getIntent().getAction().equals(ACTION_OPEN)) {
			DrawableManager.loadFromFiles(project.getDrawables());
			binding.editorLayout.loadLayoutFromParser(project.getLayout());
		}
		
		binding.editorLayout.updateUndoRedoHistory();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		DrawableManager.loadFromFiles(project.getDrawables());
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		saveXml();
	}
	
	private void showOptions(View anchor) {
		final PopupMenu popupMenu = new PopupMenu(this, anchor);
		popupMenu.inflate(R.menu.editor);
		popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch(item.getItemId()) {
					case R.id.menu_show_xml: {
						String result = new XmlLayoutGenerator().generate(binding.editorLayout, true);
						Intent intent = new Intent(EditorActivity.this, XmlPreviewActivity.class);
						intent.putExtra(XmlPreviewActivity.EXTRA_KEY_XML, result);
						startActivity(intent);
						return true;
					}
					
					case R.id.menu_enable_stroke: {
						binding.editorLayout.toggleStroke();
						return true;
					}
					
					case R.id.menu_drawable_manager: {
						Intent intent = new Intent(EditorActivity.this, DrawableManagerActivity.class);
						intent.putExtra(DrawableManagerActivity.EXTRA_KEY_PROJECT, project);
						startActivity(intent);
						return true;
					}
				}
				
				return false;
			}
		});
		
		popupMenu.show();
	}
	
	private void saveXml() {
		//Toast.makeText(this, "Saved succesfully!", Toast.LENGTH_SHORT).show();
		
		if(binding.editorLayout.getChildCount() == 0) {
			project.saveLayout("");
			return;
		}
		
		String result = new XmlLayoutGenerator().generate(binding.editorLayout, false);
		project.saveLayout(result);
	}
	
	private class ListViewAdapter extends BaseAdapter implements View.OnLongClickListener {
		
		private ArrayList<HashMap<String, Object>> list;
		
		public ListViewAdapter(ArrayList<HashMap<String, Object>> list) {
			this.list = list;
		}
		
		@Override
		public int getCount() {
			return list.size();
		}
		
		@Override
		public long getItemId(int arg0) {
			return 0;
		}
		
		@Override
		public HashMap<String, Object> getItem(int pos) {
			return list.get(pos);
		}
		
		@Override
		public View getView(int pos, View buffer, ViewGroup parent) {
			ListViewItemBinding bind = ListViewItemBinding.inflate(getLayoutInflater());
			bind.name.setText(getItem(pos).get("name").toString());
			bind.image.setImageResource(InvokeUtil.getMipmapId(getItem(pos).get("iconName").toString()));
			
			bind.getRoot().setOnLongClickListener(this);
			bind.getRoot().setTag(pos);
			bind.getRoot().post(() -> {
				bind.getRoot().setTranslationX(-bind.getRoot().getWidth());
				bind.getRoot().animate().translationX(0).setStartDelay(pos * 30).setDuration(300).start();
			});
			
			return bind.getRoot();
		}
		
		@Override
		public boolean onLongClick(View v) {
			v.startDragAndDrop(null, new View.DragShadowBuilder(v), list.get((int) v.getTag()), 0);
			binding.drawer.closeDrawer(GravityCompat.START);
			return true;
		}
	}
}