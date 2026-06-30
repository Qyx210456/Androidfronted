package com.example.androidfronted.util;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.androidfronted.R;

public class ToastUtils {

    private static Dialog currentDialog;
    public static boolean pendingRegisterSuccess = false;

    public static void showSuccessToast(Context context, String message) {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
            currentDialog = null;
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View toastView = inflater.inflate(R.layout.custom_success_toast, null);

        TextView tvMessage = toastView.findViewById(R.id.tv_toast_message);
        tvMessage.setText(message);

        currentDialog = new Dialog(context, R.style.CustomToastStyle);
        currentDialog.setContentView(toastView);
        currentDialog.setCancelable(true);

        Window window = currentDialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER;
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            params.dimAmount = 0f;
            window.setAttributes(params);
            window.setBackgroundDrawableResource(android.R.color.transparent);
        }

        currentDialog.show();

        toastView.postDelayed(() -> {
            if (currentDialog != null && currentDialog.isShowing()) {
                currentDialog.dismiss();
                currentDialog = null;
            }
        }, 2000);
    }

    public static void showToast(Context context, String message) {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
            currentDialog = null;
        }

        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show();
    }

    public static void cancelCurrentToast() {
        if (currentDialog != null && currentDialog.isShowing()) {
            currentDialog.dismiss();
            currentDialog = null;
        }
    }
}