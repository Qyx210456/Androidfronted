package com.example.androidfronted.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.androidfronted.R;
import com.example.androidfronted.util.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private HomeFragment homeFragment;
    private LoanFragment loanFragment;
    private ProfileFragment profileFragment;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("MyApp", "=== MainActivity 启动 ===");

        // 检查登录状态
        TokenManager tokenManager = new TokenManager(getApplicationContext());
        String token = tokenManager.getToken();

        Log.d("MyApp", "Token检查: " + (token != null ? "存在(" + token.length() + "字符)" : "不存在"));

        // 判断 Token 是否有效
        if (!tokenManager.isTokenValid()) {
            Log.d("MyApp", "Token 无效或已过期，跳转到 LoginActivity");
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish(); // 结束当前 Activity，防止用户回退
            return;
        }

        Log.d("MyApp", "Token 有效，加载主界面");
        setContentView(R.layout.activity_main);

        // 初始化主界面
        initializeMainUI();
    }

    private void initializeMainUI() {
        Log.d("MyApp", "开始初始化主界面");

        // 检查布局组件
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        if (bottomNav == null) {
            Log.e("MyApp", "底部导航栏未找到，界面初始化失败");
            Toast.makeText(this, "界面加载失败", Toast.LENGTH_SHORT).show();
            return;
        }

        // 初始化 Fragment
        homeFragment = new HomeFragment();
        loanFragment = new LoanFragment();
        profileFragment = new ProfileFragment();
        currentFragment = homeFragment;

        // 默认加载首页
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, homeFragment)
                .commit();

        Log.d("MyApp", "首页 Fragment 已显示");

        // 设置底部导航监听
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment targetFragment = null;

            Log.d("MyApp", "底部导航点击: " + itemId);

            if (itemId == R.id.nav_home) {
                targetFragment = homeFragment;
            } else if (itemId == R.id.nav_loan) {
                targetFragment = loanFragment;
            } else if (itemId == R.id.nav_profile) {
                targetFragment = profileFragment;
            }

            if (targetFragment != null && targetFragment != currentFragment) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, targetFragment)
                        .commit();
                currentFragment = targetFragment;
                Log.d("MyApp", "切换到: " + targetFragment.getClass().getSimpleName());
            }

            return true;
        });

        // 默认选中首页
        bottomNav.setSelectedItemId(R.id.nav_home);
        Log.d("MyApp", "主界面初始化完成");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleSearchIntent(intent);
    }

    private void handleSearchIntent(@Nullable Intent intent) {
        if (intent != null) {
            String keyword = intent.getStringExtra("search_keyword");
            if (keyword != null && !keyword.isEmpty()) {
                BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
                if (bottomNav != null) {
                    bottomNav.setSelectedItemId(R.id.nav_loan);
                }
            }
        }
    }
}