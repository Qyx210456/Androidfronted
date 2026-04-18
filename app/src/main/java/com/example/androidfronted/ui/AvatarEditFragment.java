package com.example.androidfronted.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.example.androidfronted.R;
import com.example.androidfronted.ui.base.BaseDetailFragment;
import com.example.androidfronted.utils.ImageLoader;
import com.example.androidfronted.utils.ImageUploadHelper;
import com.example.androidfronted.utils.ImageUrlHelper;
import com.example.androidfronted.viewmodel.auth.PersonalInformationViewModel;
import java.io.File;

public class AvatarEditFragment extends BaseDetailFragment {
    private PersonalInformationViewModel viewModel;
    private ImageView ivAvatar;
    private Button btnSave;
    private File selectedImageFile;
    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_menu_account_security_avatar_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(PersonalInformationViewModel.class);
        ivAvatar = view.findViewById(R.id.iv_avatar);
        btnSave = view.findViewById(R.id.btn_save);

        setupImagePicker();
        setupObservers();
        setupClickListeners(view);
        loadUserInfo();
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            ivAvatar.setImageURI(selectedImageUri);
                            ImageUploadHelper.compressImage(getContext(), selectedImageUri, 
                                new ImageUploadHelper.ImageUploadCallback() {
                                    @Override
                                    public void onSuccess(String imagePath) {
                                        selectedImageFile = new File(imagePath);
                                        Toast.makeText(getContext(), "图片选择成功", Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onError(String errorMessage) {
                                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                    }
                                });
                        }
                    }
                });
    }

    private void setupObservers() {
        viewModel.getUserInfo().observe(getViewLifecycleOwner(), userInfo -> {
            if (userInfo != null) {
                String avatar = userInfo.getAvatar();
                if (avatar != null && !avatar.isEmpty() && !"null".equals(avatar)) {
                    String imageUrl = ImageUrlHelper.getFullImageUrl(avatar);
                    ImageLoader.loadCircleImage(requireContext(), imageUrl, ivAvatar);
                }
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getUpdateResult().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getCode() == 200) {
                Toast.makeText(getContext(), "头像更新成功", Toast.LENGTH_SHORT).show();
                navigateBack();
            }
        });
    }

    private void setupClickListeners(View view) {
        view.findViewById(R.id.apply_btn_back).setOnClickListener(v -> navigateBack());

        ivAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            imagePickerLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> {
            if (selectedImageFile != null) {
                viewModel.updateUserInfo("", selectedImageFile);
            } else {
                Toast.makeText(getContext(), "请选择头像", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserInfo() {
        viewModel.loadUserInfo();
    }
}