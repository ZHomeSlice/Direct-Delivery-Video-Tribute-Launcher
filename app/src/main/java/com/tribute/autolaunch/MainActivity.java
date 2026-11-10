package com.tribute.autolaunch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(48,48,48,48);
        layout.setGravity(Gravity.CENTER);
        TextView text = new TextView(this);
        text.setText("Tribute Auto Launch\n\nEnable 'Tribute wake launcher' in Settings > Accessibility.\n\nOn Android 14, boot and wake launches depend on firmware permissions. Home and Back remain usable.");
        text.setTextSize(22);
        layout.addView(text);
        Button launch = new Button(this);
        launch.setText("Launch Tribute Videos now");
        launch.setOnClickListener(v -> Launcher.open(this));
        layout.addView(launch);
        Button settings = new Button(this);
        settings.setText("Open Accessibility Settings");
        settings.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        layout.addView(settings);
        setContentView(layout);
    }
}
