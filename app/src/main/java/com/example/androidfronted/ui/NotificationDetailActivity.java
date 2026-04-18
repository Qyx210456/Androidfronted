package com.example.androidfronted.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidfronted.R;
import com.example.androidfronted.data.local.entity.NotificationEntity;
import com.example.androidfronted.ui.adapter.NotificationAdapter;
import com.example.androidfronted.util.NotificationStateManager;
import com.example.androidfronted.viewmodel.notification.NotificationViewModel;
import com.example.androidfronted.viewmodel.base.ViewModelFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationDetailActivity extends AppCompatActivity {
    private static final String EXTRA_TYPE = "type";
    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_NOTIFICATION_ID = "notification_id";
    
    private String notificationType;
    private String notificationTitle;
    private int targetNotificationId = -1;
    
    private NotificationViewModel viewModel;
    private NotificationAdapter adapter;
    
    private RecyclerView recyclerView;
    private LinearLayout emptyView;
    private ProgressBar progressBar;
    private TextView tvTitle;

    public static Intent newIntent(Context context, String type, String title) {
        Intent intent = new Intent(context, NotificationDetailActivity.class);
        intent.putExtra(EXTRA_TYPE, type);
        intent.putExtra(EXTRA_TITLE, title);
        return intent;
    }
    
    public static Intent newIntent(Context context, int notificationId) {
        Intent intent = new Intent(context, NotificationDetailActivity.class);
        intent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_menu_notification_list_detail_business);
        
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );
        
        handleIntent(getIntent());
        
        viewModel = new ViewModelProvider(this, new ViewModelFactory(getApplication()))
                .get(NotificationViewModel.class);
        
        initViews();
        setupRecyclerView();
        setupClickListeners();
        setupObservers();
        
        viewModel.loadNotifications();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
        if (notificationType != null && tvTitle != null) {
            tvTitle.setText(notificationTitle);
        }
    }

    private void handleIntent(Intent intent) {
        targetNotificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1);
        
        if (targetNotificationId != -1) {
            notificationType = "business";
            notificationTitle = getString(R.string.service_notification);
        } else {
            notificationType = intent.getStringExtra(EXTRA_TYPE);
            notificationTitle = intent.getStringExtra(EXTRA_TITLE);
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.empty_view);
        progressBar = findViewById(R.id.progress_bar);
        tvTitle = findViewById(R.id.tv_title);
        
        if (notificationTitle != null) {
            tvTitle.setText(notificationTitle);
        }
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this, notification -> {
            viewModel.markAsRead(notification.getId());
            cancelNotification(notification.getId());
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    private void cancelNotification(int notificationId) {
        android.app.NotificationManager notificationManager = 
                (android.app.NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(2000 + notificationId);
        NotificationStateManager.getInstance().removeActiveNotification(notificationId);
    }

    private void setupClickListeners() {
        findViewById(R.id.apply_btn_back).setOnClickListener(v -> navigateBack());
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });
        
        viewModel.getNotifications().observe(this, notifications -> {
            if (notifications != null) {
                List<NotificationEntity> filteredList = filterNotifications(notifications);
                updateUI(filteredList);
            } else {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        });
        
        viewModel.getErrorMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<NotificationEntity> filterNotifications(List<NotificationEntity> notifications) {
        List<NotificationEntity> filteredList = new ArrayList<>();
        
        for (NotificationEntity notification : notifications) {
            boolean match = false;
            if ("business".equals(notificationType)) {
                match = "business".equals(notification.getBusinessType()) || 
                        "REPAYMENT".equals(notification.getBusinessType()) || 
                        "LOAN_APPLICATION_STATUS".equals(notification.getBusinessType());
            } else {
                match = notificationType.equals(notification.getBusinessType());
            }
            
            if (match) {
                filteredList.add(notification);
            }
        }
        
        return filteredList;
    }

    private void updateUI(List<NotificationEntity> notifications) {
        if (notifications.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            Collections.sort(notifications, (n1, n2) -> {
                if (n1.getCreatedAt() == null && n2.getCreatedAt() == null) return 0;
                if (n1.getCreatedAt() == null) return 1;
                if (n2.getCreatedAt() == null) return -1;
                return n2.getCreatedAt().compareTo(n1.getCreatedAt());
            });
            
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            adapter.setNotifications(notifications);
        }
    }

    private void navigateBack() {
        Intent intent = new Intent(this, NotificationCenterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        navigateBack();
    }
}
