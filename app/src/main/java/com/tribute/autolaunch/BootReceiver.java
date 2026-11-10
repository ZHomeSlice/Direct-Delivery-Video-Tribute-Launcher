package com.tribute.autolaunch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if (!Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) return;
        // Best effort: Android 14 may reject a background activity start.
        android.content.BroadcastReceiver.PendingResult pending = goAsync();
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            try { Launcher.open(context); } finally { pending.finish(); }
        }, 5000);
    }
}
