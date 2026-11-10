package com.tribute.autolaunch;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

final class Launcher {
    private Launcher() {}
    static void open(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setComponent(new ComponentName("com.tukios.silk", "com.tukios.silk.MainActivity"));
            intent.addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
            context.startActivity(intent);
            Log.i("TributeAutoLaunch", "Requested Tribute Videos launch");
        } catch (Exception e) {
            Log.e("TributeAutoLaunch", "Launch failed; Android may block background activity starts", e);
        }
    }
}
