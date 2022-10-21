package com.jonys.appdesigner;

import android.app.Application;
import android.content.Context;

public class AppUIDesigner extends Application {

    private static Context ctx;

    @Override
    public void onCreate() {
		
        super.onCreate();
        ctx = getApplicationContext();
    }

    public static Context getContext() {
        return ctx;
    }
}
