package com.ymaphone.hebrewtheme;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    static final String PREFS = "theme";
    static final String KEY = "overlayColor";
    static final String TARGET = "com.lionscribe.hebdate";

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        SharedPreferences p = getSharedPreferences(PREFS, MODE_PRIVATE);
        int selected = p.getInt(KEY, CalendarThemeService.DEFAULT_THEME);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 40, 32, 32);
        root.setBackgroundColor(Color.rgb(248,248,250));
        root.setGravity(Gravity.CENTER_HORIZONTAL);

        TextView title = new TextView(this);
        title.setText("ערכת נושא – לוח עברי");
        title.setTextSize(26); title.setTextColor(Color.rgb(25,25,30));
        title.setGravity(Gravity.CENTER); title.setPadding(0,0,0,14);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView info = new TextView(this);
        info.setText("בחר עיצוב. השכבה פועלת מעל לוח עברי בלי לשנות את האפליקציה המקורית.");
        info.setTextSize(16); info.setGravity(Gravity.CENTER); info.setTextColor(Color.DKGRAY);
        root.addView(info, new LinearLayout.LayoutParams(-1, -2));

        TextView status = new TextView(this);
        status.setPadding(0,20,0,20); status.setGravity(Gravity.CENTER);
        root.addView(status, new LinearLayout.LayoutParams(-1,-2));

        String[] names = {"כהה", "כחול לילה", "יער", "סגול", "שקיעה", "זהב", "ללא גוון"};
        int[] themes = CalendarThemeService.THEMES;
        for (int i=0;i<names.length;i++) {
            final int theme = themes[i];
            final String name = names[i];
            Button btn = new Button(this);
            btn.setText(name);
            btn.setTextSize(16);
            btn.setAllCaps(false);
            btn.setOnClickListener(v -> {
                getSharedPreferences(PREFS, MODE_PRIVATE).edit().putInt(KEY, theme).apply();
                CalendarThemeService.refreshFromPrefs();
                status.setText("נבחר: " + name);
            });
            root.addView(btn, new LinearLayout.LayoutParams(-1, 58));
        }

        Button overlay = new Button(this);
        overlay.setText("הפעל הרשאת שכבת תצוגה");
        overlay.setAllCaps(false);
        overlay.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + getPackageName()))));
        root.addView(overlay, new LinearLayout.LayoutParams(-1, 58));

        Button usage = new Button(this);
        usage.setText("הפעל הרשאת זיהוי האפליקציה");
        usage.setAllCaps(false);
        usage.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)));
        root.addView(usage, new LinearLayout.LayoutParams(-1, 58));

        Button start = new Button(this);
        start.setText("הפעל ערכת נושא");
        start.setAllCaps(false);
        start.setOnClickListener(v -> {
            if (Settings.canDrawOverlays(this)) {
                startForegroundService(new Intent(this, CalendarThemeService.class));
                status.setText("ערכת הנושא פעילה");
            } else {
                startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())));
            }
        });
        root.addView(start, new LinearLayout.LayoutParams(-1, 58));

        Button launch = new Button(this);
        launch.setText("פתח לוח עברי"); launch.setAllCaps(false);
        launch.setOnClickListener(v -> {
            Intent i = getPackageManager().getLaunchIntentForPackage(TARGET);
            if (i != null) startActivity(i);
        });
        root.addView(launch, new LinearLayout.LayoutParams(-1, 58));

        status.setText("ערכת נושא נבחרה: " + (selected == 0 ? "כהה" : "מותאם"));
        setContentView(root);
    }
}
