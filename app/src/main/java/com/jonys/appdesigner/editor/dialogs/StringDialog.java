package com.jonys.appdesigner.editor.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jonys.appdesigner.R;

public class StringDialog extends AttributeDialog {
	
	private TextInputLayout textInputLayout;
	private TextInputEditText textInputEditText;
	
    @SuppressLint("InflateParams")
	public StringDialog(Context context, String savedValue) {
		super(context);
		
		textInputLayout = (TextInputLayout) LayoutInflater.from(context).inflate(R.layout.textinputlayout, null, false);
		textInputLayout.setHint("Enter string value");
		
		textInputEditText = textInputLayout.findViewById(R.id.textinput_edittext);
		textInputEditText.setText(savedValue);
		
		setView(textInputLayout, 10);
		showKeyboardWhenOpen();
	}
	
	@Override
	public void show() {
		super.show();
		requestEditText(textInputEditText);
	}
	
	@Override
	protected void onClickSave() {
		super.onClickSave();
		listener.onSave(textInputEditText.getText().toString());
	}
}