package com.jonys.appdesigner.activities;

import android.content.ClipboardManager;
import android.os.Bundle;

import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.jonys.appdesigner.databinding.ActivityXmlPreviewBinding;

public class XmlPreviewActivity extends BaseActivity {
	
	public static final String EXTRA_KEY_XML = "xml";

	private ActivityXmlPreviewBinding binding;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		binding = ActivityXmlPreviewBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		
		binding.text.setText(getIntent().getStringExtra(EXTRA_KEY_XML));
		binding.text.post(()-> {
			
			int pad = binding.paddingLayout.getPaddingLeft() * 2;
			binding.text.setMinWidth(binding.rootLayout.getWidth() - pad);
			binding.text.setMinHeight(binding.rootLayout.getHeight() - pad);
		});
		
		binding.btnBack.setOnClickListener(v -> finish());
		binding.fab.setOnClickListener(v -> {
			
			ClipboardManager clip = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			clip.setText(getIntent().getStringExtra(EXTRA_KEY_XML));
			Toast.makeText(XmlPreviewActivity.this, "Copied", Toast.LENGTH_SHORT).show();
		});
	}
}