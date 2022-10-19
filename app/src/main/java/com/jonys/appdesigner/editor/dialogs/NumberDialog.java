package com.jonys.appdesigner.editor.dialogs;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.jonys.appdesigner.R;

public class NumberDialog extends AttributeDialog {
	
	private TextInputLayout textInputLayout;
	private TextInputEditText textInputEditText;

	public NumberDialog(Context context, String savedValue, String type) {
		super(context);
		
		textInputLayout = (TextInputLayout) LayoutInflater.from(context).inflate(R.layout.textinputlayout, null, false);
		textInputLayout.setHint("Enter " + type + " value");
		
		textInputEditText = textInputLayout.findViewById(R.id.textinput_edittext);
		
		if(type.equals("float")) {
			textInputEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		}
		else {
			textInputEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
		}
		
		textInputEditText.setText(savedValue.equals("") ? "0" : savedValue);
		textInputEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {
			}

			@Override
			public void onTextChanged(CharSequence text, int p2, int p3, int p4) {
			}

			@Override
			public void afterTextChanged(Editable p1) {
				checkError();
			}
		});
		
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
		listener.onSave(textInputEditText.getText().toString());
	}
		    
	private void checkError() {
		String text = textInputEditText.getText().toString();

		if(text.equals("")) {
			setEnabled(false);
			textInputLayout.setErrorEnabled(true);
			textInputLayout.setError("Field cannot be empty!");
		}
		else {
			setEnabled(true);
			textInputLayout.setErrorEnabled(false);
			textInputLayout.setError("");
		}
	}
}