package com.raghav.sos;

import android.accessibilityservice.AccessibilityService;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class AccessibilityKeyDetector  extends AccessibilityService {

    private final String TAG = "AccessKeyDetector";


    @Override

    public boolean onKeyEvent(KeyEvent event) {
        Log.d(TAG,"Key pressed via accessibility is: "+event.getKeyCode());
        //This allows the key pressed to function normally after it has been used by your app.

        return super.onKeyEvent(event);
    }




    @Override
    protected void onServiceConnected() {
        Log.i(TAG,"Service connected");

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

    }


    @Override
    public void onInterrupt() {

    }
}