package com.example.android.gradle.plugin;

import android.app.Application;

import com.Bar;

public class FooApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        new Bar().doSomething();
    }
}
