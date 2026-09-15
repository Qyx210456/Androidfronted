package com.example.androidfronted.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.activity.OnBackPressedCallback;
import com.example.androidfronted.R;
import com.example.androidfronted.security.SecureKeyboardManager;
import com.example.androidfronted.ui.base.BaseDetailFragment;

/**
 * 设置密码页：原密码 / 新密码 / 确认新密码 三个输入框
 * - 防截屏：B+C 组合策略由 SecureKeyboardManager 托管（键盘弹起才防护 + 退后台兜底）
 * - 安全键盘：三个输入框统一使用 SafeKeyboard，避免系统输入法记录键值
 */
public class AccountPasswordFragment extends BaseDetailFragment {

    private SecureKeyboardManager secureKeyboard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_menu_account_security_account_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 自动隐藏导航栏，自动绑定返回键
        setupSecureKeyboard(view);
    }

    private void setupSecureKeyboard(View view) {
        LinearLayout keyboardPlace = view.findViewById(R.id.safe_keyboard_place);
        if (keyboardPlace == null) {
            return;
        }
        secureKeyboard = SecureKeyboardManager.attach(
                requireActivity(), keyboardPlace, view, view, "设置密码");
        secureKeyboard.bind(view.findViewById(R.id.et_original_password));
        secureKeyboard.bind(view.findViewById(R.id.et_new_password));
        secureKeyboard.bind(view.findViewById(R.id.et_confirm_new_password));

        // 键盘显示时按返回键先收起键盘，而不是退出页面
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (secureKeyboard == null || !secureKeyboard.onBackPressed()) {
                            setEnabled(false);
                            requireActivity().getOnBackPressedDispatcher().onBackPressed();
                            setEnabled(true);
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (secureKeyboard != null) {
            secureKeyboard.release();
            secureKeyboard = null;
        }
    }
}
