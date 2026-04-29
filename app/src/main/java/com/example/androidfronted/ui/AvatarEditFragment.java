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
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.example.androidfronted.R;
import com.example.androidfronted.ui.base.BaseDetailFragment;
import com.example.androidfronted.utils.ImageCropHelper;
import com.example.androidfronted.utils.ImageLoader;
import com.example.androidfronted.utils.ImageUrlHelper;
import com.example.androidfronted.viewmodel.auth.PersonalInformationViewModel;
import java.io.File;

public class AvatarEditFragment extends BaseDetailFragment {
    private PersonalInformationViewModel viewModel;
    private ImageView ivAvatar;
    private TextView tvAvatarHint;
    private Button btnSave;
    private File selectedImageFile;
    private String currentAvatarUrl;
    
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ActivityResultLauncher<Intent> imageEditLauncher;

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
        tvAvatarHint = view.findViewById(R.id.tv_avatar_hint);
        btnSave = view.findViewById(R.id.btn_save);

        setupImagePicker();
        setupImageEditLauncher();
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
                            openImageEdit(selectedImageUri);
                        }
                    }
                });
    }

    private void setupImageEditLauncher() {
        imageEditLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri resultUri = result.getData().getParcelableExtra(ImageEditActivity.EXTRA_RESULT_URI);
                        if (resultUri != null) {
                            ivAvatar.setImageURI(resultUri);
                            
                            ImageCropHelper.compressCroppedImage(getContext(), resultUri,
                                new ImageCropHelper.CropCallback() {
                                    @Override
                                    public void onSuccess(Uri compressedUri) {
                                        selectedImageFile = new File(compressedUri.getPath());
                                        uploadAvatar();
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

    private void openImageEdit(Uri imageUri) {
        Intent intent = new Intent(getContext(), ImageEditActivity.class);
        intent.putExtra(ImageEditActivity.EXTRA_IMAGE_URI, imageUri);
        intent.putExtra(ImageEditActivity.EXTRA_CROP_SHAPE, ImageEditActivity.CROP_SHAPE_OVAL);
        imageEditLauncher.launch(intent);
    }

    private void setupObservers() {
        viewModel.getUserInfo().observe(getViewLifecycleOwner(), userInfo -> {
            if (userInfo != null) {
                String avatar = userInfo.getAvatar();
                if (avatar != null && !avatar.isEmpty() && !"null".equals(avatar)) {
                    currentAvatarUrl = ImageUrlHelper.getFullImageUrl(avatar);
                    ImageLoader.loadCircleImage(requireContext(), currentAvatarUrl, ivAvatar);
                    if (tvAvatarHint != null) {
                        tvAvatarHint.setText("点击图片查看现用头像");
                    }
                } else {
                    if (tvAvatarHint != null) {
                        tvAvatarHint.setText("暂无头像，点击下方按钮设置");
                    }
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
                loadUserInfo();
            }
        });
    }

    private void setupClickListeners(View view) {
        view.findViewById(R.id.apply_btn_back).setOnClickListener(v -> navigateBack());

        ivAvatar.setOnClickListener(v -> {
            if (currentAvatarUrl != null && !currentAvatarUrl.isEmpty()) {
                openImagePreview(currentAvatarUrl);
            } else {
                Toast.makeText(getContext(), "暂无头像", Toast.LENGTH_SHORT).show();
            }
        });

        btnSave.setOnClickListener(v -> {
            pickImage();
        });
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(intent);
    }

    private void openImagePreview(String imageUrl) {
        Intent intent = new Intent(getContext(), ImagePreviewActivity.class);
        intent.putExtra(ImagePreviewActivity.EXTRA_SINGLE_IMAGE_URL, imageUrl);
        startActivity(intent);
    }

    private void uploadAvatar() {
        if (selectedImageFile != null) {
            viewModel.updateUserInfo("", selectedImageFile);
        }
    }

    private void loadUserInfo() {
        viewModel.loadUserInfo();
    }
}
