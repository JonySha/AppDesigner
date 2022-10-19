package com.jonys.appdesigner.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jonys.appdesigner.ProjectFile;
import com.jonys.appdesigner.databinding.ActivityDrawableManagerBinding;
import com.jonys.appdesigner.databinding.GridDrawableItemBinding;
import com.jonys.appdesigner.utils.FileUtil;
import com.jonys.appdesigner.R;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;

public class DrawableManagerActivity extends BaseActivity {
	
	public static final String EXTRA_KEY_PROJECT = "project";
	
	private ActivityDrawableManagerBinding binding;
	
	private ProjectFile project;
	
	private ArrayList<DrawableItem> drawables = new ArrayList<>();
	private GridAdapter gridAdapter;
	
	private boolean isSelectedMode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		
		binding = ActivityDrawableManagerBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		
		project = getIntent().getParcelableExtra(EXTRA_KEY_PROJECT);
		
		gridAdapter = new GridAdapter();
		binding.gridView.setAdapter(gridAdapter);
		
		loadDrawables();
		
		binding.btnBack.setOnClickListener(v -> finish());
		binding.fab.setOnClickListener(v -> {
			
			if(isSelectedMode) {
				for(int i = drawables.size()-1; i >= 0; i --) {
					if(drawables.get(i).selected) {
						FileUtil.deleteFile(drawables.get(i).path);
						drawables.remove(i);
					}
				}
					
				stopSelection();
				return;
			}
			
			if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
				requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 10);
				return;
			}
				
			Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
			chooseFile.setType("image/*");
			chooseFile.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
				
			startActivityForResult(chooseFile, 20);
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(resultCode == RESULT_OK) {
			if(data != null) {
				addDrawable(FileUtil.convertUriToFilePath(this, data.getData()));
			}
		}
	}
	
	@Override
	public void onBackPressed() {
		if(isSelectedMode) {
			stopSelection();
			return;
		}
		
		super.onBackPressed();
	}
	
	private void addDrawable(final String path) {
		//имя файла с расширением
		final String lastSegment = FileUtil.getLastSegmentFromPath(path);
		
		//имя без расширения
		final String fileName = lastSegment.substring(0, lastSegment.lastIndexOf("."));
		
		//расширение
		final String extension = lastSegment.substring(lastSegment.lastIndexOf("."), lastSegment.length());
		
		final TextInputLayout inputLayout = (TextInputLayout) getLayoutInflater().inflate(R.layout.textinputlayout, null, false);
		inputLayout.setHint("Enter new name");
		
		final TextInputEditText editText = inputLayout.findViewById(R.id.textinput_edittext);
		editText.setText(fileName);
		
		final int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
		
		final MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
		builder.setView(inputLayout, padding, padding, padding, padding);
		builder.setTitle("Add drawable");
		builder.setNegativeButton("Cancel", (di, which) -> {});
		builder.setPositiveButton("Add", (di, which) -> {
			
			String drawablePath = project.getDrawablePath();
			
			String toPath = drawablePath + editText.getText().toString() + extension;
			FileUtil.copyFile(path, toPath);
			
			Drawable drawable = Drawable.createFromPath(toPath);
			String name = editText.getText().toString();
			drawables.add(new DrawableItem(drawable, name, toPath));
			gridAdapter.notifyDataSetChanged();
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
				checkNameErrors(editText.getText().toString(), inputLayout, dialog);
			}
		});
		
		checkNameErrors(fileName, inputLayout, dialog);
		
		editText.requestFocus();
		InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		inputMethodManager.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);

		if(!editText.getText().toString().equals("")) {
			editText.setSelection(0, editText.getText().toString().length());
		}
	}
	
	private void checkNameErrors(String name, TextInputLayout inputLayout, AlertDialog dialog) {
		if(!Pattern.matches("[a-z][a-z0-9_]*", name)) {
			inputLayout.setErrorEnabled(true);
			inputLayout.setError("Only small letters(a-z) and numbers!");
			dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
			return;
		}
		
		for(DrawableItem item : drawables) {
			if(item.name.equals(name)) {
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
	
	private void loadDrawables() {
		File[] files = project.getDrawables();
		
		for(File file : files) {
			Drawable drawable = Drawable.createFromPath(file.getPath());
			String name = file.getName();
			name = name.substring(0, name.lastIndexOf("."));
			drawables.add(new DrawableItem(drawable, name, file.getPath()));
		}
	}
	
	private void startSelection() {
		isSelectedMode = true;
		gridAdapter.notifyDataSetChanged();
		binding.fab.setImageResource(R.drawable.delete);
	}
	
	private void stopSelection() {
		isSelectedMode = false;
		binding.fab.setImageResource(R.drawable.plus);
		
		for(DrawableItem item : drawables) {
			item.selected = false;
		}
		
		gridAdapter.notifyDataSetChanged();
	}
	
	private class GridAdapter extends BaseAdapter implements View.OnClickListener, View.OnLongClickListener {
		
		@Override
		public int getCount() {
			return drawables.size();
		}

		@Override
		public DrawableItem getItem(int pos) {
			return drawables.get(pos);
		}

		@Override
		public long getItemId(int p1) {
			return 0;
		}

		@Override
		public View getView(int pos, View buffer, ViewGroup p3) {
			DrawableItem item = getItem(pos);
			
			GridDrawableItemBinding bind = GridDrawableItemBinding.inflate(getLayoutInflater());
			bind.name.setText(item.name);
			bind.image.setImageDrawable(item.drawable);
			bind.imgCheck.setImageResource(item.selected ? R.drawable.ic_check_circle : R.drawable.ic_delete_circle);
			
			if(isSelectedMode) {
				bind.imgCheck.animate().alpha(1).setDuration(100).start();
			}
			else {
				bind.imgCheck.animate().alpha(0).setDuration(100).start();
			}
			
			bind.getRoot().setTag(pos);
			bind.getRoot().setOnClickListener(this);
			bind.getRoot().setOnLongClickListener(this);
			
			return bind.getRoot();
		}
		
		@Override
		public void onClick(View v) {
			if(isSelectedMode) {
				DrawableItem item = drawables.get((int) v.getTag());
				item.selected = !item.selected;
				
				ImageView check = v.findViewById(R.id.img_check);
				check.setImageResource(item.selected ? R.drawable.ic_check_circle : R.drawable.ic_delete_circle);
			}
		}

		@Override
		public boolean onLongClick(View v) {
			if(!isSelectedMode) {
				drawables.get((int) v.getTag()).selected = true;
				startSelection();
			}
			return true;
		}
	}
	
	private class DrawableItem {
		
		private Drawable drawable;
		private String name;
		private String path;
		private boolean selected;
		
		public DrawableItem(Drawable drawable, String name, String path) {
			this.drawable = drawable;
			this.name = name;
			this.path = path;
		}
	}
}