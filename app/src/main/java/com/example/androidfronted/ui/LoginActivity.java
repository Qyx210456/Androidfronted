package com.example.androidfronted.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.androidfronted.R;

import java.util.ArrayList;
import java.util.List;

/**
 * 登录主页面
 * - 默认显示密码登录（fragment_password_login）
 * - 点击"验证码登录"切换到验证码登录界面
 * - 滑块式切换动画
 */
public class LoginActivity extends AppCompatActivity {

    private final List<Fragment> fragments = new ArrayList<>();
    private View tabSliderIndicator;
    private TextView tabVerifyCode;
    private TextView tabPassword;
    private int currentTab = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }

        fragments.add(new VerifyCodeLoginFragment());
        fragments.add(new PasswordLoginFragment());

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.viewPager, fragments.get(0), "verify")
                    .add(R.id.viewPager, fragments.get(1), "password")
                    .hide(fragments.get(0))
                    .commit();
        } else {
            Fragment verifyFragment = getSupportFragmentManager().findFragmentByTag("verify");
            Fragment passwordFragment = getSupportFragmentManager().findFragmentByTag("password");

            if (verifyFragment != null) fragments.set(0, verifyFragment);
            if (passwordFragment != null) fragments.set(1, passwordFragment);
        }

        setupTabs();
        setupBottomLinks();
    }

    private void setupTabs() {
        tabSliderIndicator = findViewById(R.id.tabSliderIndicator);
        tabVerifyCode = findViewById(R.id.tabVerifyCode);
        tabPassword = findViewById(R.id.tabPassword);

        tabSliderIndicator.post(() -> {
            View container = findViewById(R.id.tabSliderContainer);
            int containerWidth = container.getWidth();
            int indicatorWidth = (containerWidth - 4) / 2;
            
            tabSliderIndicator.getLayoutParams().width = indicatorWidth;
            tabSliderIndicator.requestLayout();
            
            float initialX = 2 + indicatorWidth;
            tabSliderIndicator.setX(initialX);
        });

        findViewById(R.id.tabVerifyCode).setOnClickListener(v -> switchTab(0));
        findViewById(R.id.tabPassword).setOnClickListener(v -> switchTab(1));
        
        selectTab(1);
    }

    private void setupBottomLinks() {
        findViewById(R.id.registerLink).setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterStep1Activity.class);
            startActivity(intent);
        });
    }

    private void switchTab(int index) {
        if (currentTab == index) return;
        
        getSupportFragmentManager().beginTransaction()
                .hide(fragments.get(0))
                .hide(fragments.get(1))
                .show(fragments.get(index))
                .commit();
        
        animateSlider(index);
        selectTab(index);
        currentTab = index;
    }

    private void animateSlider(int index) {
        View container = findViewById(R.id.tabSliderContainer);
        int containerWidth = container.getWidth();
        int indicatorWidth = (containerWidth - 4) / 2;
        
        float targetX = 2 + (index * indicatorWidth);
        
        tabSliderIndicator.animate()
                .x(targetX)
                .setDuration(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void selectTab(int index) {
        if (index == 0) {
            tabVerifyCode.setTextColor(0xFFFFFFFF);
            tabPassword.setTextColor(0xFF666666);
        } else {
            tabVerifyCode.setTextColor(0xFF666666);
            tabPassword.setTextColor(0xFFFFFFFF);
        }
    }
}