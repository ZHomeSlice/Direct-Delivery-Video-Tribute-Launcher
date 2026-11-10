package com.tribute.autolaunch;

import android.accessibilityservice.AccessibilityService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;

public class WakeService extends AccessibilityService {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private long lastRequest = 0;
    private boolean screenWasOff = false;
    private final BroadcastReceiver screenReceiver = new BroadcastReceiver() {
        @Override public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) screenWasOff = true;
            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction()) && screenWasOff) {
                screenWasOff = false;
                scheduleLaunch(1500);
            }
        }
    };
    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(screenReceiver, filter);
        // Also covers service startup after boot, if accessibility is enabled.
        scheduleLaunch(6000);
    }
    private void scheduleLaunch(long delay) {
        long now = android.os.SystemClock.elapsedRealtime();
        if (now - lastRequest < 4000) return;
        lastRequest = now;
        handler.postDelayed(() -> Launcher.open(this), delay);
    }
    @Override public void onAccessibilityEvent(AccessibilityEvent event) { /* Do not monitor app usage or interrupt Home/Back. */ }
    @Override public void onInterrupt() {}
    @Override public void onDestroy() {
        try { unregisterReceiver(screenReceiver); } catch (Exception ignored) {}
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
