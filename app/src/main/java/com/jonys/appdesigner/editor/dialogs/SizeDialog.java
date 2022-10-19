package com.jonys.appdesigner.editor.dialogs;

import android.animation.LayoutTransition;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.jonys.appdesigner.utils.DimensionUtil;
import com.jonys.appdesigner.R;

public class SizeDialog extends AttributeDialog {
	
	private TextInputLayout textInputLayout;
	private TextInputEditText textInputEditText;
	
	private RadioGroup group;
	
	public SizeDialog(Context context, String savedValue) {
		super(context);
		
		final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_attribute_size, null, false);
		group = dialogView.findViewById(R.id.radiogroup);
		
		final RadioButton rbMatchParent = dialogView.findViewById(R.id.rb_match_parent);
		final RadioButton rbWrapContent = dialogView.findViewById(R.id.rb_wrap_content);
		final RadioButton rbFixedValue = dialogView.findViewById(R.id.rb_fixed_value);
		
		textInputLayout = dialogView.findViewById(R.id.textinput_layout);
		textInputLayout.setHint("Enter dimension value");
		textInputLayout.setVisibility(View.GONE);
		
		textInputEditText = dialogView.findViewById(R.id.textinput_edittext);
		textInputEditText.setText("0");
		textInputEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
		
		if(savedValue.equals("match_parent")) {
			rbMatchParent.setChecked(true);
		}
		else if(savedValue.equals("wrap_content")) {
			rbWrapContent.setChecked(true);
		}
		else {
			rbFixedValue.setChecked(true);
			textInputLayout.setVisibility(View.VISIBLE);
			textInputEditText.setText(DimensionUtil.getDimenWithoutSuffix(savedValue));
		}
		
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
		
		group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(RadioGroup p1, int id) {
				if(id == R.id.rb_fixed_value) {
					((ViewGroup) dialogView).setLayoutTransition(new LayoutTransition());
					textInputLayout.setVisibility(View.VISIBLE);
					checkError();
				}
				else {
					((ViewGroup) dialogView).setLayoutTransition(new LayoutTransition());
					textInputLayout.setVisibility(View.GONE);
					setEnabled(true);
				}
			}
		});
		
		setView(dialogView, 10);
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
	
	@Override
	protected void onClickSave() {
	    String value = "";
				
		switch(group.getCheckedRadioButtonId()) {
			case R.id.rb_match_parent:
				value = "match_parent";
				break;
					
			case R.id.rb_wrap_content:
				value = "wrap_content";
				break;
						
			case R.id.rb_fixed_value:
				value = textInputEditText.getText().toString() + DimensionUtil.DP;
				break;
		}
				
		listener.onSave(value);
	}
}