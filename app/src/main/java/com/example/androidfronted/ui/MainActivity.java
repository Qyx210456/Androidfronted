package com.example.androidfronted.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.androidfronted.R;
import com.example.androidfronted.util.TokenManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * 应用主页面 Activity
 * - 包含底部导航栏（BottomNavigationView）
 * - 支持三个 Tab：首页（Home）、贷款（Loan）、我的（Profile）
 * - 使用 Fragment 替换实现页面切换，避免重复创建
 */
public class MainActivity extends AppCompatActivity {

    private final HomeFragment homeFragment = new HomeFragment();
    private final LoanFragment loanFragment = new LoanFragment();
    private final ProfileFragment profileFragment = new ProfileFragment();

    // 当前显示的 Fragment，初始为首页
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  启动时检查 Token
        if (new TokenManager(this).getToken() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // 初始化当前 Fragment
        currentFragment = homeFragment;

        // 如果是首次启动（非配置变更恢复），则加载首页
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, homeFragment)
                    .commit();
        }

        // 获取底部导航栏并设置监听
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment targetFragment = null;

            // 根据菜单项 ID 选择目标 Fragment
            if (itemId == R.id.nav_home) {
                targetFragment = homeFragment;
            } else if (itemId == R.id.nav_loan) {
                targetFragment = loanFragment;
            } else if (itemId == R.id.nav_profile) {
                targetFragment = profileFragment;
            }

            // 如果目标 Fragment 有效且与当前不同，则切换
            if (targetFragment != null && targetFragment != currentFragment) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.container, targetFragment)
                        .commit();
                currentFragment = targetFragment; // 更新当前 Fragment 引用
            }

            // 返回 true 表示消费了该事件
            return true;
        });
        handleSearchIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // 更新 intent
        handleSearchIntent(intent);
    }

    private void handleSearchIntent(@Nullable Intent intent) {
        if (intent != null) {
            String keyword = intent.getStringExtra("search_keyword");
            if (keyword != null && !keyword.isEmpty()) {
                // 自动切换到底部导航的“贷款”Tab
                BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
                bottomNav.setSelectedItemId(R.id.nav_loan);

                // 确保 Fragment 事务完成
                getSupportFragmentManager().executePendingTransactions();

            }
        }
    }
}