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
import com.example.androidfronted.data.model.CertState;
import com.example.androidfronted.ui.settings.SettingsFragment;
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
    private ImageView ivCertificationStatus;
    private TextView tvCertificationStatus;

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
        ivCertificationStatus = view.findViewById(R.id.iv_certification_status);
        tvCertificationStatus = view.findViewById(R.id.tv_certification_status);

        setupObservers();
        setupClickListeners(view);
        viewModel.loadUserInfo();
        viewModel.loadCertificationStatus();
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

        viewModel.getCertificationState().observe(getViewLifecycleOwner(), certState -> {
            updateCertificationUI(certState);
        });
    }

    /**
     * 更新认证状态 UI
     * @param certState 认证状态
     */
    private void updateCertificationUI(CertState certState) {
        if (certState == null) {
            // 初始状态，不显示图标和文字
            ivCertificationStatus.setVisibility(View.GONE);
            tvCertificationStatus.setVisibility(View.GONE);
        } else if (certState == CertState.CERTIFIED) {
            // 已认证状态
            ivCertificationStatus.setVisibility(View.VISIBLE);
            tvCertificationStatus.setVisibility(View.VISIBLE);
            ivCertificationStatus.setImageResource(R.drawable.ic_profile_verified);
            tvCertificationStatus.setText(R.string.text_certified);
            tvCertificationStatus.setTextColor(getResources().getColor(R.color.white));
        } else {
            // 未认证状态
            ivCertificationStatus.setVisibility(View.VISIBLE);
            tvCertificationStatus.setVisibility(View.VISIBLE);
            ivCertificationStatus.setImageResource(R.drawable.ic_profile_verified_red);
            tvCertificationStatus.setText(R.string.text_not_certified);
            tvCertificationStatus.setTextColor(getResources().getColor(R.color.status_error));
        }
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

        // 4. 设置
        view.findViewById(R.id.item_settings).setOnClickListener(v -> {
            navigateToDetail(new SettingsFragment());
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
        // 重新加载认证状态，确保数据同步
        viewModel.loadCertificationStatus();
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