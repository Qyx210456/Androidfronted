package com.example.androidfronted.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.androidfronted.R;
import com.example.androidfronted.utils.ImageLoader;
import com.example.androidfronted.utils.ImageUrlHelper;
import com.example.androidfronted.viewmodel.auth.PersonalInformationViewModel;

/**
 * "我的"主 Tab 页面
 * 策略：
 * 1. 自身不隐藏导航栏。
 * 2. 在 onResume 中强制显示导航栏，确保从任何子页面返回时导航栏都可见。
 */
public class ProfileFragment extends Fragment {
    private PersonalInformationViewModel viewModel;
    private ImageView ivAvatar;
    private TextView tvUsername;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
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
                if (userInfo.getUsername() != null && !userInfo.getUsername().isEmpty()) {
                    tvUsername.setText(userInfo.getUsername());
                }
                if (userInfo.getAvatar() != null && !userInfo.getAvatar().isEmpty()) {
                    String imageUrl = ImageUrlHelper.getFullImageUrl(userInfo.getAvatar());
                    ImageLoader.loadCircleImage(requireContext(), imageUrl, ivAvatar);
                } else {
                    ivAvatar.setImageResource(R.drawable.user_avatar_test);
                }
            }
        });
    }

    private void setupClickListeners(View view) {
        // 初始化点击事件，跳转到二级页面
        // 二级页面继承自 BaseDetailFragment，会在进入时自动隐藏导航栏

        // 1. 账户与安全
        view.findViewById(R.id.item_account_security).setOnClickListener(v -> {
            navigateToDetail(new AccountSecurityFragment());
        });

        // 2. 个人信息认证
        view.findViewById(R.id.item_personal_auth).setOnClickListener(v -> {
            navigateToDetail(new PersonalInfoFragment());
        });

        // 3. 我的银行卡
        view.findViewById(R.id.item_bank_card).setOnClickListener(v -> {
            navigateToDetail(new MyBankCardsFragment());
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // 【关键逻辑】
        // 每次回到这个主 Tab 页面，强制显示底部导航栏
        // 从子页面返回时，子页面没有恢复导航栏的问题
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavigationVisible(true);
        }
        // 重新加载用户信息，确保数据同步
        viewModel.loadUserInfo();
    }

    /**
     * 统一跳转方法
     */
    private void navigateToDetail(Fragment fragment) {
        if (getActivity() == null) return;

        getParentFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }
}