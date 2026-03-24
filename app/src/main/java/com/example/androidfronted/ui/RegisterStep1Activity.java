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
import androidx.lifecycle.ViewModelProvider;

import com.example.androidfronted.R;
import com.example.androidfronted.viewmodel.auth.RegisterStep1ViewModel;
import com.example.androidfronted.viewmodel.base.NavigationEvent;

/**
 * 注册第一步：输入用户名、密码、确认密码
 * - 布局：fragment_register_step1.xml（无 cbAgreement）
 * - 不校验协议（协议在第二步）
 * - 验证密码一致性后跳转到第二步
 */
public class RegisterStep1Activity extends AppCompatActivity {

    private RegisterStep1ViewModel viewModel;

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

        viewModel = new ViewModelProvider(this).get(RegisterStep1ViewModel.class);

        setupObservers();
        initViews();
        receiveDataFromStep2();
        setupClickListeners();
    }

    private void setupObservers() {
        viewModel.getValidationError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getNavigationEvent().observe(this, event -> {
            if (event != null) {
                handleNavigation(event);
            }
        });
    }

    private void receiveDataFromStep2() {
        Intent intent = getIntent();
        if (intent != null) {
            String savedUsername = intent.getStringExtra("USERNAME");
            String savedPassword = intent.getStringExtra("PASSWORD");

            if (savedUsername != null || savedPassword != null) {
                Toast.makeText(this, "已恢复之前填写的数据", Toast.LENGTH_SHORT).show();
            }

            if (savedUsername != null && etUsername != null) {
                etUsername.setText(savedUsername);
                viewModel.setUsername(savedUsername);
            }
            if (savedPassword != null && etPassword != null && etConfirmPassword != null) {
                etPassword.setText(savedPassword);
                etConfirmPassword.setText(savedPassword);
                viewModel.setPassword(savedPassword);
                viewModel.setConfirmPassword(savedPassword);
            }
        }
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        ivTogglePassword = findViewById(R.id.ivTogglePassword);
        ivToggleConfirm = findViewById(R.id.ivToggleConfirmPassword);
        btnNext = findViewById(R.id.btnNext);
    }

    private void setupClickListeners() {
        ivTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        ivToggleConfirm.setOnClickListener(v -> toggleConfirmPasswordVisibility());
        btnNext.setOnClickListener(v -> {
            viewModel.setUsername(etUsername.getText().toString().trim());
            viewModel.setPassword(etPassword.getText().toString().trim());
            viewModel.setConfirmPassword(etConfirmPassword.getText().toString().trim());
            viewModel.navigateToStep2();
        });
        findViewById(R.id.loginLink).setOnClickListener(v -> viewModel.navigateToLogin());

        etUsername.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                viewModel.setUsername(etUsername.getText().toString().trim());
            }
        });

        etPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                viewModel.setPassword(etPassword.getText().toString().trim());
            }
        });

        etConfirmPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                viewModel.setConfirmPassword(etConfirmPassword.getText().toString().trim());
            }
        });
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

    private void handleNavigation(NavigationEvent event) {
        switch (event.getNavigationType()) {
            case NavigationEvent.NAVIGATE_TO_REGISTER_STEP_2:
                RegisterStep1ViewModel.RegisterData data = (RegisterStep1ViewModel.RegisterData) event.getData();
                if (data != null) {
                    Intent intent = new Intent(this, RegisterStep2Activity.class);
                    intent.putExtra("USERNAME", data.username);
                    intent.putExtra("PASSWORD", data.password);
                    startActivity(intent);
                }
                break;
            case NavigationEvent.NAVIGATE_TO_LOGIN:
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
            case NavigationEvent.NAVIGATE_BACK:
                finish();
                break;
        }
    }
}