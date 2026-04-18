package com.example.androidfronted.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.example.androidfronted.R;
import com.example.androidfronted.data.local.entity.NotificationEntity;
import com.example.androidfronted.data.repository.NotificationRepository;
import com.example.androidfronted.event.NotificationEvent;
import com.example.androidfronted.util.InAppNotificationManager;
import com.example.androidfronted.util.NotificationStateManager;
import com.example.androidfronted.util.TokenManager;
import com.example.androidfronted.viewmodel.base.ViewModelFactory;
import com.example.androidfronted.viewmodel.notification.NotificationViewModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String TAG_HOME = "home";
    private static final String TAG_LOAN = "loan";
    private static final String TAG_PROFILE = "profile";
    private static final String EXTRA_SHOW_PROFILE = "show_profile";

    private HomeFragment homeFragment;
    private LoanFragment loanFragment;
    private ProfileFragment profileFragment;
    private Fragment currentMainFragment;
    private BottomNavigationView bottomNav;
    private NotificationViewModel notificationViewModel;
    private NotificationRepository notificationRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "=== MainActivity 启动 ===");

        TokenManager tokenManager = new TokenManager(getApplicationContext());
        String token = tokenManager.getToken();

        Log.d(TAG, "Token检查: " + (token != null ? "存在(" + token.length() + "字符)" : "不存在"));

        if (!tokenManager.isTokenValid()) {
            Log.d(TAG, "Token 无效或已过期，跳转到 LoginActivity");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        Log.d(TAG, "Token 有效，加载主界面");
        setContentView(R.layout.activity_main);

        notificationRepository = NotificationRepository.getInstance(this);
        notificationViewModel = new ViewModelProvider(this, new ViewModelFactory(getApplication()))
                .get(NotificationViewModel.class);

        requestNotificationPermission();
        startSseNotificationService();
        fetchOfflineNotifications();
        initializeMainUI();
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
        cancelAllSystemNotifications();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        InAppNotificationManager.getInstance().onActivityDestroyed(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewNotification(NotificationEvent.NewNotification event) {
        if (NotificationStateManager.getInstance().isAppInForeground() 
                && !NotificationStateManager.getInstance().isInNotificationCenter()) {
            InAppNotificationManager.getInstance().showNotification(event.getNotification());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOfflineNotificationSummary(NotificationEvent.OfflineNotificationSummary event) {
        Log.d(TAG, "Received OfflineNotificationSummary: count=" + event.getCount());
        if (NotificationStateManager.getInstance().hasOfflineNotificationBeenShown()) {
            Log.d(TAG, "Offline notification already shown, skipping");
            return;
        }
        if (event.getCount() > 0 && event.getLatestNotification() != null) {
            InAppNotificationManager.getInstance().showOfflineNotificationSummary(event.getCount(), event.getLatestNotification());
        }
    }

    private void fetchOfflineNotifications() {
        Log.d(TAG, "Fetching offline notifications on cold start...");
        notificationRepository.fetchOfflineNotifications(new NotificationRepository.OfflineNotificationCallback() {
            @Override
            public void onSuccess(int unreadCount, NotificationEntity latestUnread) {
                Log.d(TAG, "Offline notifications fetched: unreadCount=" + unreadCount);
                
                EventBus.getDefault().post(new NotificationEvent.UnreadCountUpdated(unreadCount));
                
                if (unreadCount > 0 && latestUnread != null) {
                    EventBus.getDefault().post(new NotificationEvent.OfflineNotificationSummary(unreadCount, latestUnread));
                    Log.d(TAG, "OfflineNotificationSummary event posted");
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to fetch offline notifications: " + errorMessage);
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        
        if (intent != null && intent.getBooleanExtra(EXTRA_SHOW_PROFILE, false)) {
            Log.d(TAG, "onNewIntent: 显示 ProfileFragment");
            getSupportFragmentManager().popBackStackImmediate(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE);
            getSupportFragmentManager().executePendingTransactions();
            showMainFragment(profileFragment, TAG_PROFILE);
            bottomNav.setSelectedItemId(R.id.nav_profile);
        }
    }

    private void initializeMainUI() {
        Log.d(TAG, "开始初始化主界面");

        bottomNav = findViewById(R.id.bottomNav);
        
        if (bottomNav == null) {
            Log.e(TAG, "底部导航栏未找到，界面初始化失败");
            Toast.makeText(this, "界面加载失败", Toast.LENGTH_SHORT).show();
            return;
        }

        initMainFragments();
        
        Intent intent = getIntent();
        if (intent != null && intent.getBooleanExtra(EXTRA_SHOW_PROFILE, false)) {
            showMainFragment(profileFragment, TAG_PROFILE);
            bottomNav.setSelectedItemId(R.id.nav_profile);
        } else {
            showMainFragment(homeFragment, TAG_HOME);
        }
        
        setupBottomNavigation();
        
        Log.d(TAG, "主界面初始化完成");
    }

    private void initMainFragments() {
        FragmentManager fm = getSupportFragmentManager();
        
        homeFragment = (HomeFragment) fm.findFragmentByTag(TAG_HOME);
        loanFragment = (LoanFragment) fm.findFragmentByTag(TAG_LOAN);
        profileFragment = (ProfileFragment) fm.findFragmentByTag(TAG_PROFILE);
        
        if (homeFragment == null) {
            homeFragment = new HomeFragment();
        }
        if (loanFragment == null) {
            loanFragment = new LoanFragment();
        }
        if (profileFragment == null) {
            profileFragment = new ProfileFragment();
        }
        
        ensureMainFragmentsAdded();
    }

    private void ensureMainFragmentsAdded() {
        FragmentManager fm = getSupportFragmentManager();
        androidx.fragment.app.FragmentTransaction transaction = fm.beginTransaction();
        
        if (fm.findFragmentByTag(TAG_HOME) == null) {
            transaction.add(R.id.container, homeFragment, TAG_HOME);
        }
        if (fm.findFragmentByTag(TAG_LOAN) == null) {
            transaction.add(R.id.container, loanFragment, TAG_LOAN);
        }
        if (fm.findFragmentByTag(TAG_PROFILE) == null) {
            transaction.add(R.id.container, profileFragment, TAG_PROFILE);
        }
        
        transaction.hide(homeFragment);
        transaction.hide(loanFragment);
        transaction.hide(profileFragment);
        transaction.commitNow();
    }

    private void showMainFragment(Fragment fragment, String tag) {
        FragmentManager fm = getSupportFragmentManager();
        
        androidx.fragment.app.FragmentTransaction transaction = fm.beginTransaction();
        
        if (currentMainFragment != null) {
            transaction.hide(currentMainFragment);
        }
        transaction.show(fragment);
        transaction.commit();
        
        currentMainFragment = fragment;
        Log.d(TAG, "显示主 Fragment: " + tag);
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            
            if (itemId == R.id.nav_home) {
                showMainFragment(homeFragment, TAG_HOME);
            } else if (itemId == R.id.nav_loan) {
                showMainFragment(loanFragment, TAG_LOAN);
            } else if (itemId == R.id.nav_profile) {
                showMainFragment(profileFragment, TAG_PROFILE);
            }
            
            return true;
        });
    }

    public void setBottomNavigationVisible(boolean visible) {
        if (bottomNav != null) {
            bottomNav.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;
    
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
            }
        }
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "通知权限被拒绝，将无法接收消息通知", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    private void startSseNotificationService() {
        Intent serviceIntent = new Intent(this, com.example.androidfronted.service.SseNotificationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }
    
    private void cancelAllSystemNotifications() {
        android.app.NotificationManager notificationManager = 
                (android.app.NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        
        NotificationStateManager stateManager = NotificationStateManager.getInstance();
        java.util.Set<Integer> activeNotifications = stateManager.getActiveNotifications();
        
        for (Integer notificationId : activeNotifications) {
            notificationManager.cancel(2000 + notificationId);
        }
        stateManager.clearAllNotifications();
    }
}
