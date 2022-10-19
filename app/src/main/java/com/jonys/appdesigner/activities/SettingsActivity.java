package com.jonys.appdesigner.activities;

import android.os.Bundle;

import com.jonys.appdesigner.databinding.ActivitySettingsBinding;
import com.jonys.appdesigner.fragments.SettingsFragment;
import com.jonys.appdesigner.R;

public class SettingsActivity extends BaseActivity {
	
	private ActivitySettingsBinding binding;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
		
		binding = ActivitySettingsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		
		binding.btnBack.setOnClickListener(v -> finish());
		
		getSupportFragmentManager().beginTransaction().replace(R.id.prefs_view, new SettingsFragment()).commit();
	}
}