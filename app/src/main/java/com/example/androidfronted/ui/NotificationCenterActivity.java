package com.example.androidfronted.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.androidfronted.R;
import com.example.androidfronted.data.local.entity.NotificationEntity;
import com.example.androidfronted.data.repository.NotificationRepository;
import com.example.androidfronted.event.NotificationEvent;
import com.example.androidfronted.util.DateUtils;
import com.example.androidfronted.util.NotificationStateManager;
import com.example.androidfronted.viewmodel.base.ViewModelFactory;
import com.example.androidfronted.viewmodel.notification.NotificationViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class NotificationCenterActivity extends AppCompatActivity {
    private static final String EXTRA_SHOW_PROFILE = "show_profile";
    
    private NotificationViewModel viewModel;
    private NotificationRepository notificationRepository;
    
    private FrameLayout flBusinessBadge;
    private FrameLayout flSystemBadge;
    private FrameLayout flMarketingBadge;
    private TextView tvBusinessBadge;
    private TextView tvSystemBadge;
    private TextView tvMarketingBadge;
    private TextView tvBusinessContent;
    private TextView tvSystemContent;
    private TextView tvMarketingContent;
    private TextView tvBusinessTime;
    private TextView tvSystemTime;
    private TextView tvMarketingTime;
    private LinearLayout llBusinessNotification;
    private LinearLayout llSystemNotification;
    private LinearLayout llMarketingNotification;
    private LinearLayout llOneClickRead;

    public static Intent newIntent(Context context) {
        return new Intent(context, NotificationCenterActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        setContentView(R.layout.profile_menu_notification);
        
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        
        viewModel = new ViewModelProvider(this, new ViewModelFactory(getApplication()))
                .get(NotificationViewModel.class);
        notificationRepository = NotificationRepository.getInstance(this);
        
        initViews();
        setupClickListeners();
        setupObservers();
        
        fetchNotificationsFromServer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnreadCountUpdated(NotificationEvent.UnreadCountUpdated event) {
        viewModel.updateUnreadCount(event.getCount());
        viewModel.loadNotifications();
    }

    private void fetchNotificationsFromServer() {
        notificationRepository.getNotifications(new NotificationRepository.NotificationCallback() {
            @Override
            public void onSuccess(List<NotificationEntity> notifications) {
                viewModel.loadNotifications();
                notificationRepository.getUnreadCount(new NotificationRepository.UnreadCountCallback() {
                    @Override
                    public void onSuccess(int count) {
                        EventBus.getDefault().post(new NotificationEvent.UnreadCountUpdated(count));
                    }

                    @Override
                    public void onError(String errorMessage) {
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                viewModel.loadNotifications();
                viewModel.loadUnreadCount();
            }
        });
    }

    private void initViews() {
        flBusinessBadge = findViewById(R.id.fl_business_badge);
        flSystemBadge = findViewById(R.id.fl_system_badge);
        flMarketingBadge = findViewById(R.id.fl_marketing_badge);
        tvBusinessBadge = findViewById(R.id.tv_business_badge);
        tvSystemBadge = findViewById(R.id.tv_system_badge);
        tvMarketingBadge = findViewById(R.id.tv_marketing_badge);
        tvBusinessContent = findViewById(R.id.tv_business_content);
        tvSystemContent = findViewById(R.id.tv_system_content);
        tvMarketingContent = findViewById(R.id.tv_marketing_content);
        tvBusinessTime = findViewById(R.id.tv_business_time);
        tvSystemTime = findViewById(R.id.tv_system_time);
        tvMarketingTime = findViewById(R.id.tv_marketing_time);
        llBusinessNotification = findViewById(R.id.ll_business_notification);
        llSystemNotification = findViewById(R.id.ll_system_notification);
        llMarketingNotification = findViewById(R.id.ll_marketing_notification);
        llOneClickRead = findViewById(R.id.ll_one_click_read);
    }

    private void setupClickListeners() {
        findViewById(R.id.apply_btn_back).setOnClickListener(v -> navigateBackToProfile());
        
        llBusinessNotification.setOnClickListener(v -> {
            navigateToDetail("business", getString(R.string.service_notification));
        });
        
        llSystemNotification.setOnClickListener(v -> {
            navigateToDetail("system", getString(R.string.system_notification));
        });
        
        llMarketingNotification.setOnClickListener(v -> {
            navigateToDetail("marketing", getString(R.string.marketing_notification));
        });
        
        llOneClickRead.setOnClickListener(v -> {
            showOneClickReadConfirmDialog();
        });
    }

    private void showOneClickReadConfirmDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_notification_one_click_read_confirm, null);
        builder.setView(dialogView);
        
        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            viewModel.markAllAsRead();
            dialog.dismiss();
        });
        
        dialog.show();
    }

    private void setupObservers() {
        viewModel.getNotifications().observe(this, notifications -> {
            if (notifications != null) {
                updateNotificationUI(notifications);
            }
        });
        
        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
        
        viewModel.getSuccessMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                viewModel.clearSuccessMessage();
            }
        });
    }

    private void updateNotificationUI(List<NotificationEntity> notifications) {
        List<NotificationEntity> businessList = new ArrayList<>();
        List<NotificationEntity> systemList = new ArrayList<>();
        List<NotificationEntity> marketingList = new ArrayList<>();
        
        for (NotificationEntity notification : notifications) {
            String type = notification.getBusinessType();

            if ("LOAN_APPLICATION_STATUS".equals(type)) {
                businessList.add(notification);
            } else if ("business".equals(type) || "REPAYMENT".equals(type)) {
                businessList.add(notification);
            } else {
                if ("system".equals(type)) {
                    systemList.add(notification);
                } else if ("marketing".equals(type)) {
                    marketingList.add(notification);
                }
            }
        }
        
        updateCategoryUI(businessList, tvBusinessContent, tvBusinessTime, flBusinessBadge, tvBusinessBadge, "服务通知");
        updateCategoryUI(systemList, tvSystemContent, tvSystemTime, flSystemBadge, tvSystemBadge, "系统通知");
        updateCategoryUI(marketingList, tvMarketingContent, tvMarketingTime, flMarketingBadge, tvMarketingBadge, "活动福利");
    }

    private void updateCategoryUI(List<NotificationEntity> list, TextView tvContent, TextView tvTime, 
                                   FrameLayout flBadge, TextView tvBadge, String categoryType) {
        if (list.isEmpty()) {
            tvContent.setText("暂无" + categoryType);
            tvTime.setText("");
            flBadge.setVisibility(View.GONE);
        } else {
            NotificationEntity latest = list.get(0);
            tvContent.setText(latest.getTitle());
            tvTime.setText(DateUtils.formatTime(latest.getCreatedAt()));
            
            int unreadCount = 0;
            for (NotificationEntity notification : list) {
                if (!notification.isReadFlag()) {
                    unreadCount++;
                }
            }
            
            if (unreadCount > 0) {
                if (unreadCount > 99) {
                    tvBadge.setText("99+");
                } else {
                    tvBadge.setText(String.valueOf(unreadCount));
                }
                flBadge.setVisibility(View.VISIBLE);
            } else {
                flBadge.setVisibility(View.GONE);
            }
        }
    }

    private void navigateToDetail(String type, String title) {
        Intent intent = NotificationDetailActivity.newIntent(this, type, title);
        startActivity(intent);
    }
    
    private void navigateBackToProfile() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_SHOW_PROFILE, true);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationStateManager.getInstance().setInNotificationCenter(true);
        cancelAllActiveNotifications();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        NotificationStateManager.getInstance().setInNotificationCenter(false);
    }
    
    @Override
    public void onBackPressed() {
        navigateBackToProfile();
    }
    
    private void cancelAllActiveNotifications() {
        NotificationStateManager stateManager = NotificationStateManager.getInstance();
        Set<Integer> activeNotifications = stateManager.getActiveNotifications();
        android.app.NotificationManager notificationManager = 
                (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        for (Integer notificationId : activeNotifications) {
            notificationManager.cancel(2000 + notificationId);
        }
        stateManager.clearAllNotifications();
    }
}
