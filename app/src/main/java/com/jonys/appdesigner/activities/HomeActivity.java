package com.jonys.appdesigner.activities;

import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

// import com.itsaky.androidide.logsender.LogSender;
import com.jonys.appdesigner.ProjectFile;
import com.jonys.appdesigner.activities.EditorActivity;
import com.jonys.appdesigner.databinding.ListProjectItemBinding;
import com.jonys.appdesigner.utils.FileUtil;
import com.jonys.appdesigner.databinding.ActivityHomeBinding;
import com.jonys.appdesigner.databinding.TextinputlayoutBinding;
import com.jonys.appdesigner.R;

import java.io.File;
import java.util.ArrayList;

public class HomeActivity extends BaseActivity {
	
	private ActivityHomeBinding binding;
	
	private ArrayList<ProjectFile> projects = new ArrayList<>();
	private ProjectListAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// LogSender.startLogging(this);
    	super.onCreate(savedInstanceState);
		
		binding = ActivityHomeBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		
		binding.drawer.setScrimColor(0x00000000);
		binding.drawer.setDrawerElevation(20);
		binding.drawer.setDrawerListener(new DrawerLayout.DrawerListener() {
			
			@Override
			public void onDrawerSlide(View view, float progress) {
				float pos = view.getWidth() * progress;
				binding.content.setTranslationX(pos);
			}

			@Override
			public void onDrawerOpened(View p1) {
			}

			@Override
			public void onDrawerClosed(View p1) {
			}

			@Override
			public void onDrawerStateChanged(int p1) {
			}
		});
		
		binding.fab.setOnClickListener(v -> showCreateProjectDialog());
		
		binding.btnMenu.setOnClickListener(v -> binding.drawer.openDrawer(GravityCompat.START));
		binding.btnGit.setOnClickListener(v -> openUrl("https://github.com/JonySha/AppDesigner"));
		binding.btnAbout.setOnClickListener(v -> showAbout());
		binding.btnSettings.setOnClickListener(v -> {
			
			Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
			startActivity(intent);
		});
		
		adapter = new ProjectListAdapter();
		binding.listProjects.setAdapter(adapter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		loadProjects();
	}
	
	private class ProjectListAdapter extends BaseAdapter {
		
		@Override
		public int getCount() {
			return projects.size();
		}

		@Override
		public ProjectFile getItem(int pos) {
			return projects.get(pos);
		}

		@Override
		public long getItemId(int p1) {
			return 0;
		}

		@Override
		public View getView(int pos, View buffer, ViewGroup p3) {
			ProjectFile project = getItem(pos);
			
			ListProjectItemBinding bind = ListProjectItemBinding.inflate(getLayoutInflater());
			bind.icon.setText(project.getName().substring(0, 1).toUpperCase());
			bind.name.setText(project.getName());
			bind.getRoot().setOnClickListener(v -> openProject(project));
			
			bind.btnOptions.setOnClickListener(v -> {
				
				final PopupMenu popupMenu = new PopupMenu(HomeActivity.this, v);
				popupMenu.inflate(R.menu.project_item_options);
				popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
						
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						if(item.getItemId() == R.id.menu_rename) {
							renameProject(project);
							return true;
						}
							
						if(item.getItemId() == R.id.menu_delete) {
							deleteProject(project);
							return true;
						}
							
						return false;
					}
				});
					
				popupMenu.show();
			});
			
			return bind.getRoot();
		}
	}
	
	private void showCreateProjectDialog() {
		final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		builder.setTitle("Create project");
		
		final TextinputlayoutBinding bind = TextinputlayoutBinding.inflate(getLayoutInflater());
		final TextInputEditText editText = bind.textinputEdittext;
		final TextInputLayout inputLayout = bind.textinputLayout;
		
		final int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
		builder.setView(bind.getRoot(), padding, padding, padding, padding);
		builder.setNegativeButton("Cancel", (di, which) -> {});
		builder.setPositiveButton("Create", (di, which) -> createProject(bind.textinputEdittext.getText().toString()));
		
		final AlertDialog dialog = builder.create();
		dialog.getWindow().setSoftInputMode(dialog.getWindow().getAttributes().SOFT_INPUT_STATE_VISIBLE);
		dialog.show();
		
		inputLayout.setHint("Enter new project name");
		editText.setText("NewProject" + new SimpleDateFormat("YYMMddHHmmss").format(Calendar.getInstance().getTime()));
		editText.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {
			}

			@Override
			public void onTextChanged(CharSequence p1, int p2, int p3, int p4) {
			}

			@Override
			public void afterTextChanged(Editable p1) {
				checkNameErrors(editText.getText().toString(), null, inputLayout, dialog);
			}
		});
		
		editText.requestFocus();
		
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

		if(!editText.getText().toString().equals("")) {
			editText.setSelection(0, editText.getText().toString().length());
		}
		
		checkNameErrors(editText.getText().toString(), "", inputLayout, dialog);
	}
	
	private void loadProjects() {
		projects.clear();
		
		File root = new File(FileUtil.getPackageDataDir(this) + "/projects/");
		
		if(!root.exists()) {
			FileUtil.makeDir(FileUtil.getPackageDataDir(this) + "/projects/");
		}
		
		for(File file : root.listFiles()) {
			String path = file.getPath();
			
			ProjectFile project = new ProjectFile(path);
			projects.add(project);
		}
		
		adapter.notifyDataSetChanged();
	}
	
	private void createProject(String name) {
		final String projectDir = FileUtil.getPackageDataDir(this) + "/projects/" + name;
		FileUtil.makeDir(projectDir);
		FileUtil.makeDir(projectDir + "/drawable/");
		FileUtil.copyFileFromAsset("default_image.png", projectDir + "/drawable", this);
		
		ProjectFile project = new ProjectFile(projectDir);
		project.saveLayout("");
		
		final Intent intent = new Intent(this, EditorActivity.class);
		intent.putExtra(EditorActivity.EXTRA_KEY_PROJECT, project);
		startActivity(intent);
	}
	
	private void renameProject(final ProjectFile project) {
		final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		builder.setTitle("Rename project");
		
		final TextinputlayoutBinding bind = TextinputlayoutBinding.inflate(getLayoutInflater());
		final TextInputEditText editText = bind.textinputEdittext;
		final TextInputLayout inputLayout = bind.textinputLayout;
		
		editText.setText(project.getName());
		inputLayout.setHint("Enter new project name");
		
		final int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
		builder.setView(bind.getRoot(), padding, padding, padding, padding);
		builder.setNegativeButton("Cancel", (di, which) -> {});
		builder.setPositiveButton("Rename", (di, which) -> {
			
			String path = project.getPath();
			String newPath = path.substring(0, path.lastIndexOf("/")) + "/" + editText.getText().toString();
			project.rename(newPath);
			
			adapter.notifyDataSetChanged();
		});
		
		final AlertDialog dialog = builder.create();
		dialog.getWindow().setSoftInputMode(dialog.getWindow().getAttributes().SOFT_INPUT_STATE_VISIBLE);
		dialog.show();
		
		editText.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {
			}

			@Override
			public void onTextChanged(CharSequence p1, int p2, int p3, int p4) {
			}

			@Override
			public void afterTextChanged(Editable p1) {
				checkNameErrors(editText.getText().toString(), project.getName(), inputLayout, dialog);
			}
		});

		checkNameErrors(editText.getText().toString(), project.getName(), inputLayout, dialog);

		editText.requestFocus();
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

		if(!editText.getText().toString().equals("")) {
			editText.setSelection(0, editText.getText().toString().length());
		}
	}
	
	private void deleteProject(final ProjectFile file) {
		final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		builder.setTitle("Delete project");
		builder.setMessage("Are you sure you want to remove the project?");
		builder.setNegativeButton("No", (di, which) -> {});
		builder.setPositiveButton("Yes", (di, which) -> {
			
			projects.remove(file);
			FileUtil.deleteFile(file.getPath());
			((BaseAdapter) binding.listProjects.getAdapter()).notifyDataSetChanged();
		});
		
		builder.create().show();
	}
	
	private void openProject(final ProjectFile project) {
		Intent intent = new Intent(this, EditorActivity.class);
		intent.putExtra(EditorActivity.EXTRA_KEY_PROJECT, project);
		intent.setAction(EditorActivity.ACTION_OPEN);
		startActivity(intent);
	}
	
	private void checkNameErrors(String name, String currentName, TextInputLayout inputLayout, AlertDialog dialog) {
		if(name.equals("")) {
			inputLayout.setErrorEnabled(true);
			inputLayout.setError("Field cannot be empty!");
			dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
			return;
		}
		
		for(ProjectFile file : projects) {
			if(name.equals(currentName))
				break;
				
			if(file.getName().equals(name)) {
				inputLayout.setErrorEnabled(true);
				inputLayout.setError("Current name is unavailable!");
				dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
				return;
			}
		}
		
		inputLayout.setErrorEnabled(false);
		inputLayout.setError("");
		dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
	}
	
	private void showAbout() {
		MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		builder.setTitle("About");
		builder.setMessage("App Designer - lightweight and powerful app to create a beautiful app design");
		builder.setPositiveButton("OK", (di, which) -> {});
		builder.create().show();
	}
	
	private void openUrl(String url) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(url));
		startActivity(intent);
	}
}