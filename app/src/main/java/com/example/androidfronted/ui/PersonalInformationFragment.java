package com.example.androidfronted.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.example.androidfronted.R;
import com.example.androidfronted.ui.base.BaseDetailFragment;
import com.example.androidfronted.utils.ImageLoader;
import com.example.androidfronted.utils.ImageUrlHelper;
import com.example.androidfronted.viewmodel.auth.PersonalInformationViewModel;

public class PersonalInformationFragment extends BaseDetailFragment {
    private PersonalInformationViewModel viewModel;
    private ImageView ivAvatar;
    private TextView tvUsername;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.profile_menu_account_security_personal_information, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(PersonalInformationViewModel.class);

        ivAvatar = view.findViewById(R.id.iv_avatar);
        tvUsername = view.findViewById(R.id.tv_username);

        setupObservers();
        setupClickListeners(view);
        viewModel.loadUserInfo();
    }

    private void setupObservers() {
        viewModel.getUserInfo().observe(getViewLifecycleOwner(), userInfo -> {
            if (userInfo != null) {
                tvUsername.setText(userInfo.getUsername());
                String avatar = userInfo.getAvatar();
                if (avatar != null && !avatar.isEmpty() && !"null".equals(avatar)) {
                    String imageUrl = ImageUrlHelper.getFullImageUrl(avatar);
                    ImageLoader.loadCircleImage(requireContext(), imageUrl, ivAvatar);
                } else {
                    ivAvatar.setImageResource(R.drawable.ic_profile_avatar);
                }
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners(View view) {
        // 返回按钮
        view.findViewById(R.id.apply_btn_back).setOnClickListener(v -> navigateBack());

        // 头像点击
        view.findViewById(R.id.ll_avatar).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.container, new AvatarEditFragment())
                    .addToBackStack("AvatarEdit")
                    .commit();
        });

        // 用户名点击
        view.findViewById(R.id.ll_username).setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.container, new UsernameEditFragment())
                    .addToBackStack("UsernameEdit")
                    .commit();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // 每次返回都重新加载用户信息，确保数据同步
        viewModel.loadUserInfo();
    }
}