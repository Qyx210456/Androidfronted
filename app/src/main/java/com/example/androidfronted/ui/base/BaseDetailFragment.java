package com.example.androidfronted.ui.base;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.example.androidfronted.R;
import com.example.androidfronted.ui.MainActivity;

/**
 * 详情页通用基类
 * 策略：只负责隐藏导航栏，不负责显示。
 * 显示逻辑由主 Tab 页面 (如 ProfileFragment) 的 onResume 接管。
 * 优点：避免多级子页面返回时导航栏闪烁。
 */
public abstract class BaseDetailFragment extends Fragment {
    private boolean backButtonListenerSet = false;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 统一设置左上角返回按钮
        setupBackButton(view);
    }

    @Override
    public void onStart() {
        super.onStart();
        // 在Fragment开始显示时隐藏底部导航栏
        hideBottomNavigation();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 在Fragment恢复时重新设置返回按钮监听器
        // 因为从子页面返回时，返回按钮的监听器可能被覆盖
        backButtonListenerSet = false;
        View view = getView();
        if (view != null) {
            setupBackButton(view);
        }
    }

    /**
     * 隐藏底部导航栏
     */
    private void hideBottomNavigation() {
        Log.d("BaseDetailFragment", "hideBottomNavigation called");
        if (getActivity() instanceof MainActivity) {
            Log.d("BaseDetailFragment", "getActivity() is MainActivity");
            // 使用postDelayed确保在UI渲染完成后才隐藏底部导航栏
            getActivity().runOnUiThread(() -> {
                ((MainActivity) getActivity()).setBottomNavigationVisible(false);
                Log.d("BaseDetailFragment", "setBottomNavigationVisible(false) called on UI thread");
            });
        } else {
            Log.d("BaseDetailFragment", "getActivity() is not MainActivity, getActivity() = " + (getActivity() != null ? getActivity().getClass().getName() : "null"));
        }
    }

    /**
     * 设置返回按钮监听
     * 默认查找 R.id.apply_btn_back，若找不到则尝试 R.id.apply_history_btn_back
     */
    protected void setupBackButton(View view) {
        Log.d("BaseDetailFragment", "setupBackButton called");
        
        if (backButtonListenerSet) {
            Log.d("BaseDetailFragment", "backButton listener already set, skipping");
            return;
        }
        
        View backButton = view.findViewById(R.id.apply_btn_back);
        if (backButton == null) {
            backButton = view.findViewById(R.id.apply_history_btn_back);
        }

        if (backButton != null) {
            Log.d("BaseDetailFragment", "backButton found, setting click listener");
            backButton.setOnClickListener(v -> {
                Log.d("BaseDetailFragment", "backButton clicked");
                v.setPressed(false);
                navigateBack();
            });
            backButton.setOnTouchListener((v, event) -> {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            });
            backButtonListenerSet = true;
        } else {
            Log.d("BaseDetailFragment", "backButton not found!");
        }
    }

    /**
     * 执行返回操作
     * 注意：此处不调用 showBottomNavigation，交给主 Tab 的 onResume 处理
     */
    protected void navigateBack() {
        Log.d("BaseDetailFragment", "navigateBack called");
        FragmentManager fragmentManager = getParentFragmentManager();
        int backStackCount = fragmentManager.getBackStackEntryCount();
        Log.d("BaseDetailFragment", "BackStack count: " + backStackCount);
        
        if (backStackCount > 0) {
            Log.d("BaseDetailFragment", "Popping back stack");
            Log.d("BaseDetailFragment", "Current fragments: " + fragmentManager.getFragments());
            fragmentManager.popBackStackImmediate();
            Log.d("BaseDetailFragment", "After popBackStackImmediate, BackStack count: " + fragmentManager.getBackStackEntryCount());
            Log.d("BaseDetailFragment", "After popBackStackImmediate, fragments: " + fragmentManager.getFragments());
        } else if (getActivity() != null) {
            Log.d("BaseDetailFragment", "Finishing activity");
            getActivity().finish();
        } else {
            Log.d("BaseDetailFragment", "Cannot navigate back - no back stack and no activity");
        }
    }

    // 移除了 onDestroyView 中的 showBottomNavigation 逻辑
    // 防止在子页面栈内切换时错误地显示导航栏
}
