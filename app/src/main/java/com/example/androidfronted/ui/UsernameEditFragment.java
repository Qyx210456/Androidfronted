package com.example.androidfronted.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.example.androidfronted.R;
import com.example.androidfronted.ui.base.BaseDetailFragment;
import com.example.androidfronted.viewmodel.auth.PersonalInformationViewModel;

public class UsernameEditFragment extends BaseDetailFragment {
    private PersonalInformationViewModel viewModel;
    private EditText etUsername;
    private Button btnSave;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_menu_account_security_username_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(PersonalInformationViewModel.class);
        etUsername = view.findViewById(R.id.et_username);
        btnSave = view.findViewById(R.id.btn_save);

        setupObservers();
        setupClickListeners(view);
        loadUserInfo();
    }

    private void setupObservers() {
        viewModel.getUserInfo().observe(getViewLifecycleOwner(), userInfo -> {
            if (userInfo != null && userInfo.getUsername() != null) {
                etUsername.setText(userInfo.getUsername());
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getUpdateResult().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getCode() == 200) {
                Toast.makeText(getContext(), "用户名更新成功", Toast.LENGTH_SHORT).show();
                navigateBack();
            }
        });
    }

    private void setupClickListeners(View view) {
        view.findViewById(R.id.apply_btn_back).setOnClickListener(v -> navigateBack());

        btnSave.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(getContext(), "请输入用户名", Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.updateUserInfo(username, null);
        });
    }

    private void loadUserInfo() {
        viewModel.loadUserInfo();
    }
}