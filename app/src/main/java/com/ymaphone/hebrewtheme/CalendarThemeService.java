package com.ymaphone.hebrewtheme;

import android.accessibilityservice.AccessibilityService;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

public class CalendarThemeService extends AccessibilityService {
    public static final int DEFAULT_THEME = 0;
    public static final int[] THEMES = {0,1,2,3,4,5,6};
    private static CalendarThemeService instance;
    private WindowManager wm;
    private ThemeView overlay;
    private WindowManager.LayoutParams lp;
    private boolean showing;

    public static void refreshFromPrefs() { if (instance != null) instance.update(); }

    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        wm = (WindowManager)getSystemService(WINDOW_SERVICE);
        overlay = new ThemeView();
        lp = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        lp.gravity = Gravity.TOP | Gravity.START;
        update();
    }

    private void update() {
        if (overlay == null) return;
        int theme = getSharedPreferences(MainActivity.PREFS, MODE_PRIVATE).getInt(MainActivity.KEY, DEFAULT_THEME);
        overlay.setTheme(theme);
    }

    @Override public void onAccessibilityEvent(AccessibilityEvent event) {
        CharSequence pkg = event.getPackageName();
        boolean target = MainActivity.TARGET.contentEquals(pkg);
        if (target && !showing) { wm.addView(overlay, lp); showing=true; update(); }
        else if (!target && showing) { try { wm.removeView(overlay); } catch(Exception ignored) {} showing=false; }
    }

    @Override public void onInterrupt() {}
    @Override public void onDestroy() { if (showing) try { wm.removeView(overlay); } catch(Exception ignored) {} instance=null; super.onDestroy(); }

    private class ThemeView extends View {
        private int theme=0;
        ThemeView(){ super(CalendarThemeService.this); setLayerType(View.LAYER_TYPE_SOFTWARE,null); }
        void setTheme(int t){ theme=t; invalidate(); }
        @Override protected void onDraw(android.graphics.Canvas c) {
            int color; int alpha;
            switch(theme){
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
