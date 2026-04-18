package com.example.androidfronted.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidfronted.R;
import com.example.androidfronted.data.local.entity.NotificationEntity;
import com.example.androidfronted.event.NotificationEvent;
import com.example.androidfronted.util.InAppNotificationManager;
import com.example.androidfronted.util.NotificationStateManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ProductApplySuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_apply_success);

        findViewById(R.id.btnBackToList).setOnClickListener(v -> goBackToList());
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        InAppNotificationManager.getInstance().onActivityResumed(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        InAppNotificationManager.getInstance().onActivityDestroyed(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewNotification(NotificationEvent.NewNotification event) {
        if (NotificationStateManager.getInstance().isAppInForeground()) {
            InAppNotificationManager.getInstance().showNotification(event.getNotification());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOfflineNotificationSummary(NotificationEvent.OfflineNotificationSummary event) {
        if (event.getCount() > 0 && event.getLatestNotification() != null) {
            InAppNotificationManager.getInstance().showOfflineNotificationSummary(event.getCount(), event.getLatestNotification());
        }
    }

    private void goBackToList() {
        String from = getIntent().getStringExtra("from");
        Intent intent;
        if ("home".equals(from)) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, ProductAllActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
