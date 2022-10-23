package com.jonys.appdesigner.editor.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.jonys.appdesigner.managers.IdManager;
import com.jonys.appdesigner.R;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class IdDialog extends AttributeDialog {
	
	private TextInputLayout textInputLayout;
	private TextInputEditText textInputEditText;
	
	private ArrayList<String> ids;
	
    @SuppressLint("InflateParams")
	public IdDialog(Context context, String savedValue) {
		super(context);
		
		ids = IdManager.getIds();
		
		textInputLayout = (TextInputLayout) LayoutInflater.from(context).inflate(R.layout.textinputlayout, null, false);
		textInputLayout.setHint("Enter new ID");

		textInputEditText = textInputLayout.findViewById(R.id.textinput_edittext);
		
		if(!savedValue.equals("")) {
			ids.remove(savedValue.replace("@+id/", ""));
			textInputEditText.setText(savedValue.replace("@+id/", ""));
		}
		
		textInputEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {
			}

			@Override
			public void onTextChanged(CharSequence p1, int p2, int p3, int p4) {
			}

			@Override
			public void afterTextChanged(Editable p1) {
				checkErrors();
			}
		});
		
		setView(textInputLayout, 10);
		showKeyboardWhenOpen();
	}
	
	private void checkErrors() {
		String text = textInputEditText.getText().toString();
		
		if(text.equals("")) {
			textInputLayout.setErrorEnabled(true);
			textInputLayout.setError("Field cannot be empty!");
			setEnabled(false);
			return;
		}
		
		if(!Pattern.matches("[a-z_][a-z0-9_]*", text)) {
			textInputLayout.setErrorEnabled(true);
			textInputLayout.setError("Only small letters(a-z) and numbers!");
			setEnabled(false);
			return;
		}
		
		for(String id : ids) {
			if(id.equals(text)) {
				textInputLayout.setErrorEnabled(true);
				textInputLayout.setError("Current ID is unavailable!");
				setEnabled(false);
				return;
			}
		}
		
		textInputLayout.setErrorEnabled(false);
		textInputLayout.setError("");
		setEnabled(true);
	}
	
	@Override
	public void show() {
	    super.show();
		requestEditText(textInputEditText);
		checkErrors();
	}
	
	@Override
	protected void onClickSave() {
	    listener.onSave("@+id/" + textInputEditText.getText().toString());
	}
}