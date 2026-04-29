package com.example.androidfronted.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidfronted.R;
import com.example.androidfronted.data.local.entity.NotificationEntity;
import com.example.androidfronted.event.NotificationEvent;
import com.example.androidfronted.ui.adapter.NotificationAdapter;
import com.example.androidfronted.ui.notification.NotificationBusinessDetailFragment;
import com.example.androidfronted.util.InAppNotificationManager;
import com.example.androidfronted.util.NotificationStateManager;
import com.example.androidfronted.viewmodel.notification.NotificationViewModel;
import com.example.androidfronted.viewmodel.base.ViewModelFactory;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class NotificationDetailActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {
    private static final String EXTRA_TYPE = "type";
    private static final String EXTRA_TITLE = "title";
    private static final String EXTRA_NOTIFICATION_ID = "notification_id";
    
    private String notificationType;
    private String notificationTitle;
    private String originalTitle;
    private int targetNotificationId = -1;
    
    private NotificationViewModel viewModel;
    private NotificationAdapter adapter;
    
    private ConstraintLayout mainContent;
    private FrameLayout fragmentContainer;
    private RecyclerView recyclerView;
    private LinearLayout emptyView;
    private ProgressBar progressBar;
    private TextView tvTitle;
    private ImageView ivBack;
    private TextView tvCancel;
    private TextView tvDelete;
    
    private boolean isMultiSelectMode = false;

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
        
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        
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
        getSupportFragmentManager().removeOnBackStackChangedListener(this);
        InAppNotificationManager.getInstance().onActivityDestroyed(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewNotification(NotificationEvent.NewNotification event) {
        InAppNotificationManager.getInstance().showNotification(event.getNotification());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOfflineNotificationSummary(NotificationEvent.OfflineNotificationSummary event) {
        if (event.getCount() > 0 && event.getLatestNotification() != null) {
            InAppNotificationManager.getInstance().showOfflineNotificationSummary(event.getCount(), event.getLatestNotification());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
        if (notificationType != null && tvTitle != null) {
            tvTitle.setText(notificationTitle);
        }
        
        getSupportFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        
        mainContent.setVisibility(View.VISIBLE);
        fragmentContainer.setVisibility(View.GONE);
        
        exitMultiSelectMode();
        viewModel.loadNotifications();
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
        
        originalTitle = notificationTitle;
    }

    private void initViews() {
        mainContent = findViewById(R.id.main_content);
        fragmentContainer = findViewById(R.id.container);
        recyclerView = findViewById(R.id.recyclerView);
        emptyView = findViewById(R.id.empty_view);
        progressBar = findViewById(R.id.progress_bar);
        tvTitle = findViewById(R.id.tv_title);
        ivBack = findViewById(R.id.apply_btn_back);
        tvCancel = findViewById(R.id.tv_cancel);
        tvDelete = findViewById(R.id.tv_delete);
        
        if (notificationTitle != null) {
            tvTitle.setText(notificationTitle);
        }
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this, notification -> {
            android.util.Log.d("NotificationDetailActivity", "Notification clicked: " + notification.getId());
            viewModel.markAsRead(notification.getId());
            cancelNotification(notification.getId());
            navigateToBusinessDetail(notification);
        });
        
        adapter.setMultiSelectListener(new NotificationAdapter.OnMultiSelectListener() {
            @Override
            public void onEnterMultiSelectMode() {
                enterMultiSelectMode();
            }

            @Override
            public void onDeleteSingle(NotificationEntity notification) {
                showDeleteConfirmDialog(notification.getId());
            }

            @Override
            public void onSelectedCountChanged(int count) {
                updateDeleteButton(count);
            }
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
        ivBack.setOnClickListener(v -> {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else {
                navigateBack();
            }
        });
        
        tvCancel.setOnClickListener(v -> exitMultiSelectMode());
        
        tvDelete.setOnClickListener(v -> {
            Set<Integer> selectedIds = adapter.getSelectedIds();
            if (!selectedIds.isEmpty()) {
                showBatchDeleteConfirmDialog(new ArrayList<>(selectedIds));
            }
        });
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            android.util.Log.d("NotificationDetailActivity", "isLoading changed: " + isLoading);
            if (isLoading != null && isLoading) {
                progressBar.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.GONE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        });
        
        viewModel.getNotifications().observe(this, notifications -> {
            android.util.Log.d("NotificationDetailActivity", "notifications changed, size: " + (notifications != null ? notifications.size() : "null"));
            if (notifications != null) {
                List<NotificationEntity> filteredList = filterNotifications(notifications);
                android.util.Log.d("NotificationDetailActivity", "filteredList size: " + filteredList.size());
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
        
        viewModel.getSuccessMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                viewModel.clearSuccessMessage();
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
        android.util.Log.d("NotificationDetailActivity", "updateUI called, notifications size: " + notifications.size());
        
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
            
            android.util.Log.d("NotificationDetailActivity", "Adapter notifications set, item count: " + adapter.getItemCount());
        }
    }

    private void navigateBack() {
        Intent intent = new Intent(this, NotificationCenterActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
    
    private void navigateToBusinessDetail(NotificationEntity notification) {
        android.util.Log.d("NotificationDetailActivity", "navigateToBusinessDetail called");
        android.util.Log.d("NotificationDetailActivity", "notification id: " + notification.getId());
        android.util.Log.d("NotificationDetailActivity", "businessId: " + notification.getBusinessId());
        android.util.Log.d("NotificationDetailActivity", "businessType: " + notification.getBusinessType());
        android.util.Log.d("NotificationDetailActivity", "title: " + notification.getTitle());
        
        NotificationBusinessDetailFragment fragment = NotificationBusinessDetailFragment.newInstance(
            notification.getId(),
            notification.getBusinessId(),
            notification.getBusinessType(),
            notification.getTitle(),
            notification.getContent(),
            notification.getCreatedAt()
        );
        
        showFragment(fragment);
    }
    
    private void showFragment(Fragment fragment) {
        android.util.Log.d("NotificationDetailActivity", "showFragment called");
        
        mainContent.setVisibility(View.GONE);
        fragmentContainer.setVisibility(View.VISIBLE);
        
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.container, fragment)
            .addToBackStack(null)
            .commit();
        
        android.util.Log.d("NotificationDetailActivity", "Fragment transaction committed");
    }

    @Override
    public void onBackStackChanged() {
        int backStackCount = getSupportFragmentManager().getBackStackEntryCount();
        android.util.Log.d("NotificationDetailActivity", "onBackStackChanged, count: " + backStackCount);
        
        if (backStackCount == 0) {
            mainContent.setVisibility(View.VISIBLE);
            fragmentContainer.setVisibility(View.GONE);
        } else {
            mainContent.setVisibility(View.GONE);
            fragmentContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        if (isMultiSelectMode) {
            exitMultiSelectMode();
        } else if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            navigateBack();
        }
    }
    
    private void enterMultiSelectMode() {
        isMultiSelectMode = true;
        
        ivBack.setVisibility(View.GONE);
        tvCancel.setVisibility(View.VISIBLE);
        tvDelete.setVisibility(View.GONE);
        
        tvTitle.setText("消息");
        
        adapter.setMultiSelectMode(true);
    }
    
    private void exitMultiSelectMode() {
        isMultiSelectMode = false;
        
        ivBack.setVisibility(View.VISIBLE);
        tvCancel.setVisibility(View.GONE);
        tvDelete.setVisibility(View.GONE);
        
        tvTitle.setText(originalTitle);
        
        adapter.setMultiSelectMode(false);
    }
    
    private void updateDeleteButton(int count) {
        if (count > 0) {
            tvDelete.setVisibility(View.VISIBLE);
            tvDelete.setText("删除(" + count + ")");
        } else {
            tvDelete.setVisibility(View.GONE);
        }
    }
    
    private void showDeleteConfirmDialog(int notificationId) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_notification_detect_confirm, null);
        builder.setView(dialogView);
        
        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        TextView tvMessage = dialogView.findViewById(R.id.tv_message);
        tvMessage.setText("确定要删除这条通知吗？");
        
        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            viewModel.deleteNotification(notificationId);
            dialog.dismiss();
        });
        
        dialog.show();
    }
    
    private void showBatchDeleteConfirmDialog(List<Integer> notificationIds) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_notification_detect_confirm, null);
        builder.setView(dialogView);
        
        android.app.AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        
        TextView tvMessage = dialogView.findViewById(R.id.tv_message);
        tvMessage.setText("确定要删除选中的 " + notificationIds.size() + " 条通知吗？");
        
        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            viewModel.deleteNotifications(notificationIds);
            exitMultiSelectMode();
            dialog.dismiss();
        });
        
        dialog.show();
    }
}
