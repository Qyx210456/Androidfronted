package com.example.androidfronted.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.androidfronted.R;
import com.example.androidfronted.data.local.entity.NotificationEntity;

public class InAppNotificationDialog extends Dialog {
    private NotificationEntity notification;
    private OnNotificationClickListener listener;
    private OnDismissListener dismissListener;
    private Handler autoDismissHandler;
    private Runnable autoDismissRunnable;
    private static final long AUTO_DISMISS_DELAY = 5000;

    public interface OnNotificationClickListener {
        void onNotificationClick(NotificationEntity notification);
    }

    public interface OnDismissListener {
        void onDismiss();
    }

    public InAppNotificationDialog(Context context, NotificationEntity notification) {
        super(context, R.style.InAppNotificationDialog);
        this.notification = notification;
        this.autoDismissHandler = new Handler(Looper.getMainLooper());
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.dismissListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_in_app_notification, null);
        setContentView(view);
        
        TextView tvTitle = view.findViewById(R.id.tv_notification_title);
        TextView tvContent = view.findViewById(R.id.tv_notification_content);
        
        if (notification != null) {
            tvTitle.setText(notification.getTitle());
            tvContent.setText(notification.getContent());
        }
        
        view.setOnClickListener(v -> {
            if (listener != null) {
                listener.onNotificationClick(notification);
            }
            dismiss();
        });
        
        Window window = getWindow();
        if (window != null) {
            int statusBarHeight = getStatusBarHeight();
            int marginPx = (int) (16 * getContext().getResources().getDisplayMetrics().density);
            int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
            
            window.setBackgroundDrawableResource(android.R.color.transparent);
            window.setLayout(screenWidth - (marginPx * 2), WindowManager.LayoutParams.WRAP_CONTENT);
            
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            params.y = statusBarHeight + marginPx;
            window.setAttributes(params);
            
            window.setWindowAnimations(R.style.InAppNotificationAnimation);
        }
        
        setCancelable(true);
        setCanceledOnTouchOutside(true);
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getContext().getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Override
    public void show() {
        if (!isContextValid()) {
            return;
        }
        super.show();
        startAutoDismiss();
    }

    @Override
    public void dismiss() {
        stopAutoDismiss();
        if (!isContextValid()) {
            return;
        }
        if (isShowing()) {
            super.dismiss();
        }
        if (dismissListener != null) {
            dismissListener.onDismiss();
        }
    }

    private void startAutoDismiss() {
        autoDismissRunnable = () -> {
            if (!isContextValid()) {
                return;
            }
            if (isShowing()) {
                dismiss();
            }
        };
        autoDismissHandler.postDelayed(autoDismissRunnable, AUTO_DISMISS_DELAY);
    }

    private void stopAutoDismiss() {
        if (autoDismissHandler != null && autoDismissRunnable != null) {
            autoDismissHandler.removeCallbacks(autoDismissRunnable);
        }
    }

    private boolean isContextValid() {
        Context context = getContext();
        if (context == null) {
            return false;
        }
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity.isFinishing()) {
                return false;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (activity.isDestroyed()) {
                    return false;
                }
            }
        }
        return true;
    }
}
