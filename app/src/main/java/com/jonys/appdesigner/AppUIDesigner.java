package com.jonys.appdesigner;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class AppUIDesigner extends Application {

    private static Context ctx;
	private static SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        ctx = getApplicationContext();
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static Context getContext() {
        return ctx;
    }
	
	public static SharedPreferences getDefaultSharedPreferences() {
		return prefs;
	}
}
