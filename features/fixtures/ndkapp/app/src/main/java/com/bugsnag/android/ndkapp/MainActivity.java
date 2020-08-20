package com.bugsnag.android.ndkapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.bugsnag.android.Bugsnag;
import com.example.Bar;

public class MainActivity extends Activity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bugsnag.start(this);
        TextView view = new TextView(this);
        view.setText(stringFromJNI());
        setContentView(view);
        Log.v("Bugsnag", new Bar().doSomething());
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
