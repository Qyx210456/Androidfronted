package com.example.androidfronted.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
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
    private TextView btnGetCode;
    private Button btnRegister;
    private CheckBox cbAgreement;
    private TextView tvBackStep;
    private String username;
    private String password;
    private AuthRepository authRepository;
    // 手机号规则（中国大陆）
    private static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";

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
        btnGetCode = findViewById(R.id.tvGetVerifyCode);
        btnRegister = findViewById(R.id.btnRegister);
        cbAgreement = findViewById(R.id.cbAgreement); // 存在于 fragment_register_step2.xml
        tvBackStep = findViewById(R.id.tvBackStep);
    }


    private void setupListeners() {
        btnGetCode.setOnClickListener(v -> Toast.makeText(this, "验证码功能暂未开放", Toast.LENGTH_SHORT).show());
        tvBackStep.setOnClickListener(v -> {
            // 返回到注册第一步并携带数据
            Intent intent = new Intent(this, RegisterStep1Activity.class);
            // 可以携带数据回去，方便用户修改
            intent.putExtra("USERNAME", username);
            intent.putExtra("PASSWORD", password);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish(); // 结束当前第二步页面
        });
        btnRegister.setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String phone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!phone.matches(PHONE_PATTERN)) {
            Toast.makeText(this, "请输入有效的中国大陆手机号", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!cbAgreement.isChecked()) {
            Toast.makeText(this, "请同意《服务条款》和《隐私政策》", Toast.LENGTH_SHORT).show();
            return;
        }

        RegisterRequest request = new RegisterRequest(username, phone, password);
        authRepository.register(request, new AuthRepository.AuthCallback<>() {
            @Override
            public void onSuccess(RegisterResponse response) {
                Toast.makeText(RegisterStep2Activity.this, "注册成功！", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterStep2Activity.this, LoginActivity.class));
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                if ("该手机号已被注册".equals(errorMessage)) {
                    Toast.makeText(RegisterStep2Activity.this, "该手机号已被注册", Toast.LENGTH_SHORT).show();
                } else {
                    // 其他错误（如网络问题、服务器异常等）
                    Toast.makeText(RegisterStep2Activity.this, "注册失败: " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}