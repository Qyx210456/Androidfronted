package com.example.androidfronted.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.androidfronted.R;
import com.example.androidfronted.ui.base.BaseDetailFragment;

/**
 * 账户与安全页面 (二级页面)
 */
public class AccountSecurityFragment extends BaseDetailFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_menu_account_security, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // 自动隐藏导航栏，自动绑定返回键

        // 个人资料点击事件
        view.findViewById(R.id.ll_personal_info).setOnClickListener(v -> {
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new PersonalInformationFragment())
                    .addToBackStack("PersonalInformation")
                    .commit();
        });

        // 账户密码点击事件
        view.findViewById(R.id.ll_account_password).setOnClickListener(v -> {
            // 跳转到账户密码页面
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new AccountPasswordFragment())
                    .addToBackStack("AccountPassword")
                    .commit();
        });
    }
}