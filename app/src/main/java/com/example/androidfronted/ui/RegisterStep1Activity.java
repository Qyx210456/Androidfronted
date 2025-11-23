package com.example.androidfronted.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidfronted.R;

/**
 * 注册第一步：输入用户名、密码、确认密码
 * - 布局：fragment_register_step1.xml（无 cbAgreement）
 * - 不校验协议（协议在第二步）
 * - 验证密码一致性后跳转到第二步
 */
public class RegisterStep1Activity extends AppCompatActivity {

    private EditText etUsername;
    private EditText etPassword;
    private EditText etConfirmPassword;
    private ImageView ivTogglePassword;
    private ImageView ivToggleConfirm;
    private Button btnNext;

    private boolean isPasswordVisible = false;
    private boolean isConfirmVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_register_step1);

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        ivToggleConfirm = findViewById(R.id.ivToggleConfirmPassword);
        btnNext = findViewById(R.id.btnNext);
        // 注意：此处不 findViewById(R.id.cbAgreement)，因为 step1 布局中不存在
    }

    private void setupClickListeners() {
        ivTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        ivToggleConfirm.setOnClickListener(v -> toggleConfirmPasswordVisibility());
        btnNext.setOnClickListener(v -> goToStep2());
        findViewById(R.id.loginLink).setOnClickListener(v -> finish());
    }

    private void goToStep2() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirm = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirm)) {
            Toast.makeText(this, "请填写所有字段", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirm)) {
            Toast.makeText(this, "两次密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, RegisterStep2Activity.class);
        intent.putExtra("USERNAME", username);
        intent.putExtra("PASSWORD", password);
        startActivity(intent);
    }

    private void togglePasswordVisibility() {
        toggleVisibility(etPassword, ivTogglePassword, () -> isPasswordVisible = !isPasswordVisible, () -> isPasswordVisible);
    }

    private void toggleConfirmPasswordVisibility() {
        toggleVisibility(etConfirmPassword, ivToggleConfirm, () -> isConfirmVisible = !isConfirmVisible, () -> isConfirmVisible);
    }

    private void toggleVisibility(EditText et, ImageView iv, Runnable toggleFlag, java.util.concurrent.Callable<Boolean> getFlag) {
        try {
            if (getFlag.call()) {
                et.setTransformationMethod(PasswordTransformationMethod.getInstance());
                iv.setImageResource(R.drawable.ic_eye_off);
            } else {
                et.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                iv.setImageResource(R.drawable.ic_eye_on);
            }
            toggleFlag.run();
            et.setSelection(et.getText().length());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}