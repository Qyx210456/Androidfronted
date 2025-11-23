package com.example.androidfronted.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidfronted.R;
import com.example.androidfronted.data.model.RegisterRequest;
import com.example.androidfronted.data.model.RegisterResponse;
import com.example.androidfronted.data.repository.AuthRepository;

/**
 * 注册第二步：输入手机号
 * - 布局：fragment_register_step2.xml（含 cbAgreement）
 * - 必须勾选协议才能提交注册
 * - 调用 AuthRepository.register()
 */
public class RegisterStep2Activity extends AppCompatActivity {

    private EditText etPhone;
    private EditText etVerifyCode; // UI 存在，但当前未使用验证码逻辑
    private Button btnGetCode;
    private Button btnRegister;
    private CheckBox cbAgreement;

    private String username;
    private String password;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_register_step2);

        username = getIntent().getStringExtra("USERNAME");
        password = getIntent().getStringExtra("PASSWORD");

        if (username == null || password == null) {
            Toast.makeText(this, "注册信息缺失", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        authRepository = new AuthRepository(this);
        initViews();
        setupListeners();
    }

    private void initViews() {
        etPhone = findViewById(R.id.etPhone);
        etVerifyCode = findViewById(R.id.etVerifyCode);
        btnGetCode = findViewById(R.id.btnGetVerifyCode);
        btnRegister = findViewById(R.id.btnLogin);
        cbAgreement = findViewById(R.id.cbAgreement); // 存在于 fragment_register_step2.xml
    }

    private void setupListeners() {
        btnGetCode.setOnClickListener(v -> Toast.makeText(this, "验证码功能暂未开放", Toast.LENGTH_SHORT).show());
        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String phone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cbAgreement.isChecked()) {
            Toast.makeText(this, "请同意《服务条款》和《隐私政策》", Toast.LENGTH_SHORT).show();
            return;
        }

        RegisterRequest request = new RegisterRequest(username, phone, password);
        authRepository.register(request, new AuthRepository.AuthCallback<RegisterResponse>() {
            @Override
            public void onSuccess(RegisterResponse response) {
                Toast.makeText(RegisterStep2Activity.this, "注册成功！", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterStep2Activity.this, LoginActivity.class));
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(RegisterStep2Activity.this, "注册失败: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}