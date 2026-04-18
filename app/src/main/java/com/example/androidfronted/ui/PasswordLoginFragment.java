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
import android.widget.LinearLayout;
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
 * - 布局：fragment_password_login.xml（含 cbAgreement）
 * - 必须勾选协议才能登录
 * - 使用 AuthRepository 发起请求
 */
public class PasswordLoginFragment extends Fragment {

    private EditText etPhone;;
    private EditText etPassword;
    private ImageView ivTogglePassword; // 密码可见性切换图标
    private Button btnLogin;
    private LinearLayout registerLink;
    private CheckBox cbAgreement;
    private AuthRepository authRepository;
    private NotificationRepository notificationRepository;
    private boolean isPasswordVisible = false;

    // 密码输入规则：8-20位，含大小写字母、数字、特殊字符
    private static final String PASSWORD_PATTERN = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$";
   //手机号输入规则：中国大陆
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
        registerLink = view.findViewById(R.id.registerLink);
    }

    private void initViews(View view) {
        etPhone = view.findViewById(R.id.etPhone);
        etPassword = view.findViewById(R.id.etPassword);
        ivTogglePassword = view.findViewById(R.id.ivTogglePassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        cbAgreement = view.findViewById(R.id.cbAgreement);
        registerLink = view.findViewById(R.id.bottomLinks);
    }

    private void setupListeners() {
        ivTogglePassword.setOnClickListener(v -> togglePasswordVisibility());
        btnLogin.setOnClickListener(v -> attemptLogin());
        registerLink.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RegisterStep1Activity.class);
            startActivity(intent);
        });

    }

    /**
     * 切换密码可见性
     */
    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // 隐藏密码
            etPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            ivTogglePassword.setImageResource(R.drawable.ic_eye_off);
        } else {
            // 显示密码
            etPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            ivTogglePassword.setImageResource(R.drawable.ic_eye_on);
        }

        isPasswordVisible = !isPasswordVisible;

        // 将光标移动到文本末尾
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
            Toast.makeText(getContext(), "密码需包含大小写字母、数字和特殊字符（如!@#$%），长度8-20位", Toast.LENGTH_LONG).show();
            return;
        }

        if (!cbAgreement.isChecked()) {
            Toast.makeText(getContext(), "请同意《服务条款》和《隐私政策》", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequest request = new LoginRequest(phone, password);
        authRepository.login(request, new AuthRepository.AuthCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse response) {
                String token = response.getData().getToken();

                // 调试信息
                Log.d("PasswordLogin", "Login success, token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));
                Log.d("LOGIN_DEBUG", "Full response: " + new Gson().toJson(response));
                Log.d("LOGIN_DEBUG", "Token value: [" + response.getData().getToken() + "]");

                // 保存token
                TokenManager tokenManager = new TokenManager(requireContext());
                tokenManager.saveToken(token);

                // 验证token是否保存成功
                String savedToken = tokenManager.getToken();
                Log.d("PasswordLogin", "Token saved: " + (savedToken != null && !savedToken.isEmpty()));

                Intent serviceIntent = new Intent(requireContext(), SseNotificationService.class);
                requireContext().startService(serviceIntent);
                Log.d("PasswordLogin", "SSE service started");

                fetchOfflineNotifications();

                Toast.makeText(getContext(), "登录成功！", Toast.LENGTH_SHORT).show();

                // 跳转到主页面
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
