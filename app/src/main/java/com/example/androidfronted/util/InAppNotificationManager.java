package com.example.androidfronted.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.androidfronted.R;
import com.example.androidfronted.data.local.entity.NotificationEntity;
import com.example.androidfronted.ui.InAppNotificationDialog;
import com.example.androidfronted.ui.NotificationDetailActivity;

import java.lang.ref.WeakReference;

public class InAppNotificationManager {
    private static final String TAG = "InAppNotificationManager";
    private static InAppNotificationManager instance;
    private WeakReference<Activity> currentActivityRef;
    private InAppNotificationDialog currentDialog;

    private InAppNotificationManager() {}

    public static synchronized InAppNotificationManager getInstance() {
        if (instance == null) {
            instance = new InAppNotificationManager();
        }
        return instance;
    }

    public void onActivityResumed(Activity activity) {
        currentActivityRef = new WeakReference<>(activity);
    }

    public void onActivityDestroyed(Activity activity) {
        if (currentActivityRef != null) {
            Activity currentActivity = currentActivityRef.get();
            if (currentActivity == activity) {
                dismissDialog();
                currentActivityRef = null;
            }
        }
    }

    public void showNotification(NotificationEntity notification) {
        Activity activity = getCurrentActivity();
        if (activity == null || !isActivityValid(activity)) {
            return;
        }

        if (!NotificationStateManager.getInstance().tryAcquireInAppNotificationLock()) {
            return;
        }

        dismissDialog();

        currentDialog = new InAppNotificationDialog(activity, notification);
        currentDialog.setOnNotificationClickListener(clickedNotification -> {
            NotificationStateManager.getInstance().releaseInAppNotificationLock();
            navigateToNotificationDetail(activity, clickedNotification.getBusinessType());
        });
        currentDialog.setOnDismissListener(dialog -> {
            NotificationStateManager.getInstance().releaseInAppNotificationLock();
        });
        currentDialog.show();
    }

    public void showOfflineNotificationSummary(int count, NotificationEntity latestNotification) {
        Activity activity = getCurrentActivity();
        if (activity == null || !isActivityValid(activity)) {
            return;
        }

        if (NotificationStateManager.getInstance().hasOfflineNotificationBeenShown()) {
            android.util.Log.d(TAG, "Offline notification already shown, skipping");
            return;
        }

        if (!NotificationStateManager.getInstance().tryAcquireInAppNotificationLock()) {
            return;
        }

        dismissDialog();

        NotificationStateManager.getInstance().setOfflineNotificationShown(true);

        NotificationEntity summaryNotification = new NotificationEntity(
                0,
                latestNotification.getUserId(),
                latestNotification.getBusinessId(),
                latestNotification.getBusinessType(),
                "您有" + count + "条未读服务通知",
                latestNotification.getTitle(),
                false,
                latestNotification.getCreatedAt(),
                null
        );

        currentDialog = new InAppNotificationDialog(activity, summaryNotification);
        currentDialog.setOnNotificationClickListener(clickedNotification -> {
            NotificationStateManager.getInstance().releaseInAppNotificationLock();
            navigateToNotificationDetail(activity, clickedNotification.getBusinessType());
        });
        currentDialog.setOnDismissListener(dialog -> {
            NotificationStateManager.getInstance().releaseInAppNotificationLock();
        });
        currentDialog.show();
    }

    private void navigateToNotificationDetail(Context context, String businessType) {
        String normalizedType = normalizeType(businessType);
        String title = getTitleForType(context, normalizedType);
        Intent intent = NotificationDetailActivity.newIntent(context, normalizedType, title);
        context.startActivity(intent);
    }

    private String normalizeType(String type) {
        if ("business".equals(type) || "REPAYMENT".equals(type) || "LOAN_APPLICATION_STATUS".equals(type)) {
            return "business";
        }
        return type;
    }

    private String getTitleForType(Context context, String type) {
        if ("business".equals(type)) {
            return context.getString(R.string.service_notification);
        } else if ("system".equals(type)) {
            return context.getString(R.string.system_notification);
        } else if ("marketing".equals(type)) {
            return context.getString(R.string.marketing_notification);
        }
        return context.getString(R.string.service_notification);
    }

    private Activity getCurrentActivity() {
        if (currentActivityRef == null) {
            return null;
        }
        Activity activity = currentActivityRef.get();
        if (activity == null || !isActivityValid(activity)) {
            return null;
        }
        return activity;
    }

    private boolean isActivityValid(Activity activity) {
        if (activity == null) {
            return false;
        }
        if (activity.isFinishing()) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (activity.isDestroyed()) {
                return false;
            }
        }
        return true;
    }

    private void dismissDialog() {
        if (currentDialog != null) {
            try {
                if (currentDialog.isShowing()) {
                    currentDialog.dismiss();
                }
            } catch (Exception e) {
                android.util.Log.e(TAG, "Error dismissing dialog: " + e.getMessage());
            }
            currentDialog = null;
        }
    }
}
