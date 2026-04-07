package com.example.androidfronted.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;
import com.example.androidfronted.R;
import com.example.androidfronted.ui.LoginActivity;
import com.example.androidfronted.ui.base.BaseDetailFragment;
import com.example.androidfronted.viewmodel.auth.SettingsViewModel;
import com.example.androidfronted.viewmodel.base.NavigationEvent;

public class SettingsFragment extends BaseDetailFragment {
    private SettingsViewModel viewModel;
    private Button btnLogout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_menu_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        
        btnLogout = view.findViewById(R.id.btn_log_out);
        btnLogout.setOnClickListener(v -> showLogoutConfirmDialog());
        
        setupObservers();
    }

    private void setupObservers() {
        viewModel.getNavigationEvent().observe(getViewLifecycleOwner(), event -> {
            if (event != null && event.getNavigationType() == NavigationEvent.NAVIGATE_TO_LOGIN) {
                navigateToLogin();
            }
        });
    }

    private void showLogoutConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_logout_confirm, null);
        builder.setView(dialogView);
        
        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.drawable.dialog_holo_light_frame);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_product_info_card);
        
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnLogoutConfirm = dialogView.findViewById(R.id.btn_logout);
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnLogoutConfirm.setOnClickListener(v -> {
            dialog.dismiss();
            viewModel.logout();
        });
        
        dialog.show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
