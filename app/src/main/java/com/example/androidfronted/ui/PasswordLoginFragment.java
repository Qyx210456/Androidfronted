package com.example.androidfronted.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.androidfronted.R;
import com.example.androidfronted.data.local.entity.NotificationEntity;
import com.example.androidfronted.data.model.LoginRequest;
import com.example.androidfronted.data.model.LoginResponse;
import com.example.androidfronted.data.repository.AuthRepository;
import com.example.androidfronted.data.repository.NotificationRepository;
import com.example.androidfronted.event.NotificationEvent;
import com.example.androidfronted.service.SseNotificationService;
import com.example.androidfronted.util.TokenManager;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

/**
 * 密码登录 Fragment
 * - 布局：fragment_password_login.xml
 * - 必须勾选协议才能登录（协议复选框在Activity中）
 * - 使用 AuthRepository 发起请求
 */
public class PasswordLoginFragment extends Fragment {

    private EditText etPhone;
    private EditText etPassword;
    private ImageView ivTogglePassword;
    private Button btnLogin;
    private AuthRepository authRepository;
    private NotificationRepository notificationRepository;
    private boolean isPasswordVisible = false;

    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]{8,20}$";
    private static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        authRepository = AuthRepository.getInstance(requireContext());
        notificationRepository = NotificationRepository.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_password_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        setupListeners();
    }

    private void initViews(View view) {
        etPhone = view.findViewById(R.id.etPhone);
        etPassword = view.findViewById(R.id.etPassword);
        ivTogglePassword = view.findViewById(R.id.ivTogglePassword);
        btnLogin = view.findViewById(R.id.btnLogin);
    }

    private void setupListeners() {
        ivTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            ivTogglePassword.setImageResource(R.drawable.ic_eye_off);
        } else {
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            ivTogglePassword.setImageResource(R.drawable.ic_eye_on);
        }

        isPasswordVisible = !isPasswordVisible;
        etPassword.setSelection(etPassword.getText().length());
    }

    private void attemptLogin() {
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(getContext(), "请输入手机号", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!phone.matches(PHONE_PATTERN)) {
            Toast.makeText(getContext(), "请输入有效的中国大陆手机号", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "请输入密码", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.matches(PASSWORD_PATTERN)) {
            Toast.makeText(getContext(), "密码需包含大小写字母、数字和特殊字符（如!@#$%&*?），长度8-20位", Toast.LENGTH_LONG).show();
            return;
        }

        if (getActivity() instanceof LoginActivity) {
            CheckBox cbAgreement = getActivity().findViewById(R.id.cbAgreement);
            if (cbAgreement == null || !cbAgreement.isChecked()) {
                Toast.makeText(getContext(), "请同意《用户协议》和《隐私政策》", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        LoginRequest request = new LoginRequest(phone, password);
        Log.d("LOGIN_TOKEN", "=== 开始登录请求 ===");
        Log.d("LOGIN_TOKEN", "手机号: " + phone);
        Log.d("LOGIN_TOKEN", "密码长度: " + password.length());
        
        authRepository.login(request, new AuthRepository.AuthCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse response) {
                Log.d("LOGIN_TOKEN", "=== 登录成功 ===");
                
                String token = response.getData().getToken();
                String refreshToken = response.getData().getRefreshToken();
                
                Log.d("LOGIN_TOKEN", "完整Token: " + token);
                Log.d("LOGIN_TOKEN", "完整RefreshToken: " + refreshToken);
                Log.d("LOGIN_TOKEN", "Token长度: " + (token != null ? token.length() : 0));
                Log.d("LOGIN_TOKEN", "RefreshToken长度: " + (refreshToken != null ? refreshToken.length() : 0));
                
                Log.d("LOGIN_TOKEN", "完整响应JSON: " + new Gson().toJson(response));

                TokenManager tokenManager = new TokenManager(requireContext());
                tokenManager.saveToken(token);

                String savedToken = tokenManager.getToken();
                String savedRefreshToken = tokenManager.getRefreshToken();
                
                Log.d("LOGIN_TOKEN", "=== Token保存验证 ===");
                Log.d("LOGIN_TOKEN", "保存后的Token: " + savedToken);
                Log.d("LOGIN_TOKEN", "保存后的RefreshToken: " + savedRefreshToken);
                Log.d("LOGIN_TOKEN", "Token是否保存成功: " + (savedToken != null && !savedToken.isEmpty()));
                Log.d("LOGIN_TOKEN", "RefreshToken是否保存成功: " + (savedRefreshToken != null && !savedRefreshToken.isEmpty()));

                Intent serviceIntent = new Intent(requireContext(), SseNotificationService.class);
                requireContext().startService(serviceIntent);
                Log.d("PasswordLogin", "SSE service started");

                fetchOfflineNotifications();

                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("show_login_success", true);
                startActivity(intent);
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), "登录失败: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchOfflineNotifications() {
        Log.d("PasswordLogin", "Fetching offline notifications...");
        notificationRepository.fetchOfflineNotifications(new NotificationRepository.OfflineNotificationCallback() {
            @Override
            public void onSuccess(int unreadCount, NotificationEntity latestUnread) {
                Log.d("PasswordLogin", "Offline notifications fetched: unreadCount=" + unreadCount);
                
                EventBus.getDefault().post(new NotificationEvent.UnreadCountUpdated(unreadCount));
                
                if (unreadCount > 0 && latestUnread != null) {
                    EventBus.getDefault().post(new NotificationEvent.OfflineNotificationSummary(unreadCount, latestUnread));
                    Log.d("PasswordLogin", "OfflineNotificationSummary event posted");
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("PasswordLogin", "Failed to fetch offline notifications: " + errorMessage);
            }
        });
    }
}