package com.example.androidfronted.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.androidfronted.R;

/**
 * 验证码登录 Fragment（功能暂未开放）
 * - 保留“去注册”跳转逻辑
 * - 禁用“立即登录”：点击后仅提示，不执行任何操作
 * - 不进行网络请求、不跳转、不生成 Token
 */
public class VerifyCodeLoginFragment extends Fragment {

    private Button btnLogin;
    private LinearLayout registerLink;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_verify_code_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // 初始化视图（仅保留必要控件）
        btnLogin = view.findViewById(R.id.btnLogin);
        registerLink = view.findViewById(R.id.registerLink);

        // 设置“去注册”点击事件（保持可用）
        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RegisterStep1Activity.class);
            startActivity(intent);
        });

        // 设置“立即登录”点击事件 → 仅提示，不登录
        btnLogin.setOnClickListener(v -> {
            Toast.makeText(
                    getContext(),
                    "验证码登录功能暂未开放，请使用密码登录",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }
}