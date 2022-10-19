package com.jonys.appdesigner.fragments;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import com.jonys.appdesigner.R;

public class SettingsFragment extends PreferenceFragmentCompat {
	
	@Override
	public void onCreatePreferences(Bundle bundle, String rootKey) {
	    setPreferencesFromResource(R.xml.preferences, rootKey);
	}
}