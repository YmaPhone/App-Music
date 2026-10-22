package com.ymaphone.hebrewtheme;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import java.util.List;

public class CalendarThemeService extends Service {
    public static final int DEFAULT_THEME = 0;
    public static final int[] THEMES = {0,1,2,3,4,5,6};
    private static final String CHANNEL = "theme_overlay";
    private static CalendarThemeService instance;
    private WindowManager wm;
    private ThemeView overlay;
    private WindowManager.LayoutParams lp;
    private final Handler handler = new Handler();
    private boolean showing;

    public static void refreshFromPrefs() { if (instance != null) instance.update(); }

    @Override public void onCreate() {
        super.onCreate();
        instance = this;
        createNotificationChannel();
        Notification n = new Notification.Builder(this, CHANNEL)
            .setContentTitle("ערכת נושא ללוח עברי")
            .setContentText("שכבת העיצוב פעילה")
            .setSmallIcon(android.R.drawable.ic_menu_view)
            .build();
        startForeground(1001, n);

        wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        overlay = new ThemeView();
        lp = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        update();
        handler.post(checkRunnable);
    }

    private final Runnable checkRunnable = new Runnable() {
        @Override public void run() {
            updateVisibility();
            handler.postDelayed(this, 500);
        }
    };

    private void updateVisibility() {
        if (!Settings.canDrawOverlays(this)) return;
        String top = getTopPackage();
        boolean target = MainActivity.TARGET.equals(top);
        if (target && !showing) {
            try { wm.addView(overlay, lp); showing = true; update(); } catch (Exception ignored) {}
        } else if (!target && showing) {
            try { wm.removeView(overlay); } catch (Exception ignored) {}
            showing = false;
        }
    }

    private String getTopPackage() {
        UsageStatsManager usm = (UsageStatsManager)getSystemService(USAGE_STATS_SERVICE);
        long now = System.currentTimeMillis();
        List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 5000, now);
        if (stats == null || stats.isEmpty()) return "";
        UsageStats best = null;
        for (UsageStats s : stats) {
            if (best == null || s.getLastTimeUsed() > best.getLastTimeUsed()) best = s;
        }
        return best == null ? "" : best.getPackageName();
    }

    private void update() {
        if (overlay == null) return;
        int theme = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE).getInt(MainActivity.KEY, DEFAULT_THEME);
        overlay.setTheme(theme);
    }

    @Override public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }

    @Override public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        if (showing) try { wm.removeView(overlay); } catch (Exception ignored) {}
        showing = false;
        instance = null;
        super.onDestroy();
    }

    @Override public IBinder onBind(Intent intent) { return null; }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel c = new NotificationChannel(CHANNEL, "ערכת נושא", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(c);
        }
    }

    private class ThemeView extends View {
        private int theme = 0;
        ThemeView() { super(CalendarThemeService.this); setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
        void setTheme(int t) { theme = t; invalidate(); }
        @Override protected void onDraw(android.graphics.Canvas c) {
            int color; int alpha;
            switch (theme) {
                case 1: color=Color.rgb(8,35,60); alpha=75; break;
                case 2: color=Color.rgb(10,55,30); alpha=72; break;
                case 3: color=Color.rgb(65,20,90); alpha=62; break;
                case 4: color=Color.rgb(105,35,12); alpha=65; break;
                case 5: color=Color.rgb(100,75,5); alpha=48; break;
                case 6: return;
                default: color=Color.BLACK; alpha=145;
            }
            c.drawColor(Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color)));
        }
    }
}
