package com.example.androidfronted.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.androidfronted.R;
import com.example.androidfronted.data.model.CertState;
import com.example.androidfronted.data.repository.NotificationRepository;
import com.example.androidfronted.event.NotificationEvent;
import com.example.androidfronted.ui.settings.SettingsFragment;
import com.example.androidfronted.utils.ImageLoader;
import com.example.androidfronted.utils.ImageUrlHelper;
import com.example.androidfronted.viewmodel.auth.PersonalInformationViewModel;
import com.example.androidfronted.viewmodel.base.ViewModelFactory;
import com.example.androidfronted.viewmodel.notification.NotificationViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class ProfileFragment extends Fragment {
    private PersonalInformationViewModel viewModel;
    private NotificationViewModel notificationViewModel;
    private NotificationRepository notificationRepository;
    private ImageView ivAvatar;
    private TextView tvUsername;
    private ImageView ivCertificationStatus;
    private TextView tvCertificationStatus;
    private FrameLayout flMessageBadge;
    private TextView tvMessageBadge;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        notificationRepository = NotificationRepository.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(PersonalInformationViewModel.class);
        notificationViewModel = new ViewModelProvider(requireActivity(), new ViewModelFactory(requireActivity().getApplication()))
                .get(NotificationViewModel.class);
        ivAvatar = view.findViewById(R.id.iv_avatar);
        tvUsername = view.findViewById(R.id.tv_username);
        ivCertificationStatus = view.findViewById(R.id.iv_certification_status);
        tvCertificationStatus = view.findViewById(R.id.tv_certification_status);
        flMessageBadge = view.findViewById(R.id.fl_message_badge);
        tvMessageBadge = view.findViewById(R.id.tv_message_badge);

        setupObservers();
        setupClickListeners(view);
        viewModel.loadUserInfo();
        viewModel.loadCertificationStatus();
        notificationViewModel.loadUnreadCount();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnreadCountUpdated(NotificationEvent.UnreadCountUpdated event) {
        notificationViewModel.updateUnreadCount(event.getCount());
    }

    private void setupObservers() {
        viewModel.getUserInfo().observe(getViewLifecycleOwner(), userInfo -> {
            if (userInfo != null) {
                if (userInfo.getUsername() != null && !userInfo.getUsername().isEmpty()) {
                    tvUsername.setText(userInfo.getUsername());
                }
                String avatar = userInfo.getAvatar();
                if (avatar != null && !avatar.isEmpty() && !"null".equals(avatar)) {
                    String imageUrl = ImageUrlHelper.getFullImageUrl(avatar);
                    ImageLoader.loadCircleImage(requireContext(), imageUrl, ivAvatar);
                } else {
                    ivAvatar.setImageResource(R.drawable.user_avatar_test);
                }
            }
        });

        viewModel.getCertificationState().observe(getViewLifecycleOwner(), certState -> {
            updateCertificationUI(certState);
        });
        
        notificationViewModel.getUnreadCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null && count > 0) {
                if (count > 99) {
                    tvMessageBadge.setText("99+");
                } else {
                    tvMessageBadge.setText(String.valueOf(count));
                }
                flMessageBadge.setVisibility(View.VISIBLE);
            } else {
                flMessageBadge.setVisibility(View.GONE);
            }
        });
    }

    private void updateCertificationUI(CertState certState) {
        if (certState == null) {
            ivCertificationStatus.setVisibility(View.GONE);
            tvCertificationStatus.setVisibility(View.GONE);
        } else if (certState == CertState.CERTIFIED) {
            ivCertificationStatus.setVisibility(View.VISIBLE);
            tvCertificationStatus.setVisibility(View.VISIBLE);
            ivCertificationStatus.setImageResource(R.drawable.ic_profile_verified);
            tvCertificationStatus.setText(R.string.text_certified);
            tvCertificationStatus.setTextColor(getResources().getColor(R.color.white));
        } else {
            ivCertificationStatus.setVisibility(View.VISIBLE);
            tvCertificationStatus.setVisibility(View.VISIBLE);
            ivCertificationStatus.setImageResource(R.drawable.ic_profile_verified_red);
            tvCertificationStatus.setText(R.string.text_not_certified);
            tvCertificationStatus.setTextColor(getResources().getColor(R.color.status_error));
        }
    }

    private void setupClickListeners(View view) {
        view.findViewById(R.id.item_account_security).setOnClickListener(v -> {
            navigateToDetail(new AccountSecurityFragment());
        });

        view.findViewById(R.id.item_personal_auth).setOnClickListener(v -> {
            navigateToDetail(new PersonalInfoFragment());
        });

        view.findViewById(R.id.item_bank_card).setOnClickListener(v -> {
            navigateToDetail(new MyBankCardsFragment());
        });
        
        view.findViewById(R.id.item_message_center).setOnClickListener(v -> {
            Intent intent = NotificationCenterActivity.newIntent(requireContext());
            startActivity(intent);
        });

        view.findViewById(R.id.item_settings).setOnClickListener(v -> {
            navigateToDetail(new SettingsFragment());
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavigationVisible(true);
        }
        viewModel.loadUserInfo();
        viewModel.loadCertificationStatus();
        fetchUnreadCountFallback();
    }

    private void fetchUnreadCountFallback() {
        notificationRepository.getUnreadCount(new NotificationRepository.UnreadCountCallback() {
            @Override
            public void onSuccess(int count) {
                if (getActivity() != null && isAdded()) {
                    getActivity().runOnUiThread(() -> {
                        notificationViewModel.updateUnreadCount(count);
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    private void navigateToDetail(Fragment fragment) {
        if (getActivity() == null) return;

        getParentFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
