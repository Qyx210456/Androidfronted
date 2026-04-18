package com.example.androidfronted;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.example.androidfronted.util.NotificationStateManager;

public class MyApplication extends Application {
    private static final String TAG = "MyApplication";
    private int activityCount = 0;
    private boolean isInitialized = false;

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationStateManager.getInstance().setAppInForeground(true);
        
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}

            @Override
            public void onActivityStarted(Activity activity) {
                activityCount++;
                Log.d(TAG, "Activity started: " + activity.getClass().getSimpleName() + ", count: " + activityCount);
                if (activityCount >= 1) {
                    NotificationStateManager.getInstance().setAppInForeground(true);
                }
            }

            @Override
            public void onActivityResumed(Activity activity) {}

            @Override
            public void onActivityPaused(Activity activity) {}

            @Override
            public void onActivityStopped(Activity activity) {
                activityCount--;
                Log.d(TAG, "Activity stopped: " + activity.getClass().getSimpleName() + ", count: " + activityCount);
                if (activityCount == 0) {
                    NotificationStateManager.getInstance().setAppInForeground(false);
                }
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}

            @Override
            public void onActivityDestroyed(Activity activity) {}
        });
    }
}
