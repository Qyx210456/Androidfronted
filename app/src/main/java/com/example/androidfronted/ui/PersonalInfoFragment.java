package com.example.androidfronted.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.example.androidfronted.R;
import com.example.androidfronted.ui.base.BaseDetailFragment;
import com.example.androidfronted.ui.personalinformationinfo.CertificateOfIdFragment;
import com.example.androidfronted.ui.personalinformationinfo.CertificateOfJobFragment;
import com.example.androidfronted.ui.personalinformationinfo.CertificateOfPropertyFragment;
import com.example.androidfronted.ui.personalinformationinfo.CertificateOfThirdPartyFragment;
import com.example.androidfronted.viewmodel.auth.PersonalInfoViewModel;
import com.example.androidfronted.viewmodel.base.NavigationEvent;

/**
 * 个人信息认证主页 (二级页面)
 * 逻辑：进入时检查各项资料的上传状态 -> 决定跳转到 "未上传引导页" 还是 "已上传列表页"
 */
public class PersonalInfoFragment extends BaseDetailFragment {
    private PersonalInfoViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info_personal_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(PersonalInfoViewModel.class);
        android.util.Log.d("PersonalInfoFragment", "ViewModel created: " + viewModel.hashCode());
        android.util.Log.d("PersonalInfoFragment", "Current navigation event: " + viewModel.getNavigationEvent().getValue());

        setupObservers();
        setupClickListeners(view);
        viewModel.getCertInfo();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && getContext() != null) {
                // Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getHasIdInfo().observe(getViewLifecycleOwner(), hasInfo -> {
            android.util.Log.d("PersonalInfoFragment", "getHasIdInfo changed: " + hasInfo);
            updateStatusText(R.id.tv_id_status, hasInfo);
        });

        viewModel.getHasJobInfo().observe(getViewLifecycleOwner(), hasInfo -> {
            android.util.Log.d("PersonalInfoFragment", "getHasJobInfo changed: " + hasInfo);
            updateStatusText(R.id.tv_job_status, hasInfo);
        });

        viewModel.getHasPropertyInfo().observe(getViewLifecycleOwner(), hasInfo -> {
            android.util.Log.d("PersonalInfoFragment", "getHasPropertyInfo changed: " + hasInfo);
            updateStatusText(R.id.tv_house_status, hasInfo);
        });

        viewModel.getHasThirdPartyInfo().observe(getViewLifecycleOwner(), hasInfo -> {
            android.util.Log.d("PersonalInfoFragment", "getHasThirdPartyInfo changed: " + hasInfo);
            updateStatusText(R.id.tv_other_status, hasInfo);
        });

        viewModel.getNavigationEvent().observe(getViewLifecycleOwner(), event -> {
            android.util.Log.d("PersonalInfoFragment", "getNavigationEvent changed: " + (event != null ? event.getNavigationType() : "null"));
            if (event != null) {
                handleNavigation(event);
            }
        });
    }

    private void setupClickListeners(View view) {
        view.findViewById(R.id.id_info).setOnClickListener(v -> {
            viewModel.navigateToIdCert();
        });

        view.findViewById(R.id.job_info).setOnClickListener(v -> {
            viewModel.navigateToJobCert();
        });

        view.findViewById(R.id.house_info).setOnClickListener(v -> {
            viewModel.navigateToPropertyCert();
        });

        view.findViewById(R.id.other_info).setOnClickListener(v -> {
            viewModel.navigateToThirdPartyCert();
        });
    }

    private void updateStatusText(int statusTextViewId, boolean hasInfo) {
        if (getView() != null) {
            TextView statusText = getView().findViewById(statusTextViewId);
            if (statusText != null) {
                statusText.setText(hasInfo ? "已认证" : "未认证");
                statusText.setTextColor(hasInfo ? 
                    getResources().getColor(R.color.al_info) : 
                    getResources().getColor(R.color.no_info));
            }
        }
    }

    private void handleNavigation(NavigationEvent event) {
        android.util.Log.d("PersonalInfoFragment", "handleNavigation called, navigationType: " + event.getNavigationType());
        
        if (event == null) {
            return;
        }
        
        switch (event.getNavigationType()) {
            case NavigationEvent.NAVIGATE_TO_ID_CERT_UPLOAD:
                android.util.Log.d("PersonalInfoFragment", "Navigating to ID cert upload");
                navigateToDetail(new CertificateOfIdFragment());
                break;
            case NavigationEvent.NAVIGATE_TO_JOB_CERT_UPLOAD:
                navigateToDetail(new CertificateOfJobFragment());
                break;
            case NavigationEvent.NAVIGATE_TO_PROPERTY_CERT_UPLOAD:
                navigateToDetail(new CertificateOfPropertyFragment());
                break;
            case NavigationEvent.NAVIGATE_TO_THIRD_PARTY_CERT_UPLOAD:
                navigateToDetail(new CertificateOfThirdPartyFragment());
                break;
            case NavigationEvent.NAVIGATE_BACK:
                navigateBack();
                break;
        }
    }

    private void navigateToDetail(BaseDetailFragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        android.util.Log.d("PersonalInfoFragment", "onResume called");

        android.util.Log.d("PersonalInfoFragment", "About to call getCertInfo()");
        viewModel.getCertInfo();
        android.util.Log.d("PersonalInfoFragment", "getCertInfo() called");

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setBottomNavigationVisible(false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        android.util.Log.d("PersonalInfoFragment", "onPause called");
    }

    @Override
    public void onStop() {
        super.onStop();
        android.util.Log.d("PersonalInfoFragment", "onStop called");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        android.util.Log.d("PersonalInfoFragment", "onDestroyView called");
    }
}
