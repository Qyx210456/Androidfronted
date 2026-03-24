package com.example.androidfronted.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.androidfronted.R;
import com.example.androidfronted.viewmodel.auth.RegisterStep2ViewModel;
import com.example.androidfronted.viewmodel.base.NavigationEvent;

/**
 * 注册第二步：输入手机号
 * - 布局：fragment_register_step2.xml（含 cbAgreement）
 * - 必须勾选协议才能提交注册
 * - 调用 AuthRepository.register()
 */
public class RegisterStep2Activity extends AppCompatActivity {

    private RegisterStep2ViewModel viewModel;

    private EditText etPhone;
    private EditText etVerifyCode;
    private TextView btnGetCode;
    private Button btnRegister;
    private CheckBox cbAgreement;
    private TextView tvBackStep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_register_step2);

        viewModel = new ViewModelProvider(this).get(RegisterStep2ViewModel.class);

        String username = getIntent().getStringExtra("USERNAME");
        String password = getIntent().getStringExtra("PASSWORD");

        if (username == null || password == null) {
            Toast.makeText(this, "注册信息缺失", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewModel.setUsername(username);
        viewModel.setPassword(password);

        setupObservers();
        initViews();
        setupListeners();
    }

    private void setupObservers() {
        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getValidationError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getRegisterSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "注册成功！", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(RegisterStep2Activity.this, LoginActivity.class));
                finish();
            }
        });

        viewModel.getNavigationEvent().observe(this, event -> {
            if (event != null) {
                handleNavigation(event);
            }
        });
    }

    private void initViews() {
        etPhone = findViewById(R.id.etPhone);
        etVerifyCode = findViewById(R.id.etVerifyCode);
        btnGetCode = findViewById(R.id.tvGetVerifyCode);
        btnRegister = findViewById(R.id.btnRegister);
        cbAgreement = findViewById(R.id.cbAgreement);
        tvBackStep = findViewById(R.id.tvBackStep);
    }

    private void setupListeners() {
        btnGetCode.setOnClickListener(v -> Toast.makeText(this, "验证码功能暂未开放", Toast.LENGTH_SHORT).show());
        tvBackStep.setOnClickListener(v -> viewModel.navigateBackToStep1());
        btnRegister.setOnClickListener(v -> {
            viewModel.setPhone(etPhone.getText().toString().trim());
            viewModel.setVerifyCode(etVerifyCode.getText().toString().trim());
            viewModel.setAgreementChecked(cbAgreement.isChecked());
            viewModel.register();
        });

        etPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                viewModel.setPhone(etPhone.getText().toString().trim());
            }
        });

        cbAgreement.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setAgreementChecked(isChecked);
        });
    }

    private void handleNavigation(NavigationEvent event) {
        switch (event.getNavigationType()) {
            case NavigationEvent.NAVIGATE_TO_LOGIN:
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                break;
            case NavigationEvent.NAVIGATE_BACK:
                String username = getIntent().getStringExtra("USERNAME");
                String password = getIntent().getStringExtra("PASSWORD");
                Intent intent = new Intent(this, RegisterStep1Activity.class);
                intent.putExtra("USERNAME", username);
                intent.putExtra("PASSWORD", password);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
                break;
        }
    }
}