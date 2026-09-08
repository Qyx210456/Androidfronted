package com.example.androidfronted.security;

import android.app.Activity;
import android.view.WindowManager;

/**
 * 防截屏防录屏工具
 *
 */
public final class ScreenCaptureGuard {

    private ScreenCaptureGuard() {
    }

    /** 开启防截屏防录屏 */
    public static void enable(Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }

    /** 关闭防截屏防录屏 */
    public static void disable(Activity activity) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
    }
}
