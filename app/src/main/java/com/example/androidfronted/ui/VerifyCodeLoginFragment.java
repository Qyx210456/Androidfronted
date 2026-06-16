package com.example.androidfronted.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.androidfronted.R;

/**
 * 验证码登录 Fragment（功能暂未开放）
 * - 禁用"立即登录"：点击后仅提示，不执行任何操作
 * - 不进行网络请求、不跳转、不生成 Token
 */
public class VerifyCodeLoginFragment extends Fragment {

    private Button btnLogin;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_verify_code_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        btnLogin = view.findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {
            Toast.makeText(
                    getContext(),
                    "验证码登录功能暂未开放，请使用密码登录",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }
}