package com.example.androidfronted.ui.personalinformationinfo;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.example.androidfronted.R;
import com.example.androidfronted.data.model.CertInfoResponse;
import com.example.androidfronted.data.model.CertState;
import com.example.androidfronted.ui.InfoConfirmSuccessFragment;
import com.example.androidfronted.ui.adapter.UploadedCertificateAdapter;
import com.example.androidfronted.viewmodel.auth.PropertyCertViewModel;
import com.example.androidfronted.viewmodel.base.NavigationEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CertificateOfPropertyFragment extends BaseCertFragment {
    private PropertyCertViewModel viewModel;

    @Override
    protected Object getViewModel() {
        return viewModel;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_certificate_of_property;
    }

    @Override
    protected int getSuccessType() {
        return InfoConfirmSuccessFragment.TYPE_PROPERTY;
    }

    @Override
    protected List<String> getCertTypes() {
        List<String> certTypes = new ArrayList<>();
        certTypes.add("全部");
        certTypes.add("房产证明");
        certTypes.add("车产证明");
        return certTypes;
    }

    @Override
    protected List<String> getUploadTypes() {
        List<String> types = new ArrayList<>();
        types.add("房产证明");
        types.add("车产证明");
        return types;
    }

    @Override
    protected int getContainerNotCertifiedId() {
        return R.id.container_not_certified;
    }

    @Override
    protected int getContainerUploadingId() {
        return R.id.container_uploading;
    }

    @Override
    protected int getContainerCertifiedId() {
        return R.id.container_certified;
    }

    @Override
    protected int getBtnConfirmUploadId() {
        return R.id.btn_confirm_property_upload;
    }

    @Override
    protected int getBtnAddId() {
        return R.id.btn_add_property;
    }

    @Override
    protected int getBtnStartUploadId() {
        return R.id.btn_property_upload;
    }

    @Override
    protected void onFileSelected(File file, String uploadType) {
        if (uploadType != null && uploadType.contains("房产")) {
            firstFile = file;
            secondFile = null;
        } else if (uploadType != null && uploadType.contains("车产")) {
            secondFile = file;
            firstFile = null;
        }
    }

    @Override
    protected void handleNavigation(int navigationType) {
        switch (navigationType) {
            case NavigationEvent.NAVIGATE_TO_PERSONAL_INFO:
            case NavigationEvent.NAVIGATE_BACK:
                navigateBack();
                break;
            case NavigationEvent.NAVIGATE_TO_PROPERTY_CERT_UPLOAD:
                viewModel.startUpload();
                break;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(PropertyCertViewModel.class);
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void setupObservers() {
        viewModel.getCertState().observe(getViewLifecycleOwner(), this::updateUIByState);

        viewModel.getCertData().observe(getViewLifecycleOwner(), certData -> {
            if (certData != null && getView() != null && adapter != null) {
                filterCertificates();
            }
        });

        viewModel.getSubmitResult().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getCode() == 200) {
                navigateToSuccessPage();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && getContext() != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getNavigationEvent().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                handleNavigation(event.getNavigationType());
            }
        });
    }

    @Override
    protected void loadCertInfo() {
        viewModel.getCertInfo(new PropertyCertViewModel.CertInfoCallback() {
            @Override
            public void onSuccess(CertInfoResponse response) {
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    @Override
    protected void submitCert() {
        viewModel.submitCert(createRequestBody(firstFile), createRequestBody(secondFile));
    }

    @Override
    protected void filterCertificates() {
        if (adapter == null) return;
        
        CertInfoResponse.ImmovablesCert certData = viewModel.getCertData().getValue();
        if (certData == null) return;
        
        List<UploadedCertificateAdapter.CertificateItem> items = new ArrayList<>();
        
        if (currentFilterType.equals("全部") || currentFilterType.equals("房产证明")) {
            if (certData.getPropertyCertPath() != null && !certData.getPropertyCertPath().isEmpty()) {
                items.add(new UploadedCertificateAdapter.CertificateItem("房产证明", certData.getPropertyCertPath()));
            }
        }
        
        if (currentFilterType.equals("全部") || currentFilterType.equals("车产证明")) {
            if (certData.getCarCertPath() != null && !certData.getCarCertPath().isEmpty()) {
                items.add(new UploadedCertificateAdapter.CertificateItem("车产证明", certData.getCarCertPath()));
            }
        }
        
        adapter.setItems(items);
    }

    @Override
    protected void onStartUploadClick() {
        viewModel.startUpload();
    }

    @Override
    protected CertState getCertStateValue() {
        return viewModel.getCertState().getValue();
    }

    @Override
    protected void cancelUpload() {
        viewModel.cancelUpload();
    }

    private void navigateToSuccessPage() {
        InfoConfirmSuccessFragment successFragment = new InfoConfirmSuccessFragment();
        Bundle args = new Bundle();
        args.putInt(InfoConfirmSuccessFragment.ARG_TARGET_FRAGMENT, getSuccessType());
        successFragment.setArguments(args);
        
        getParentFragmentManager().beginTransaction()
                .replace(((ViewGroup) requireView().getParent()).getId(), successFragment)
                .commit();
    }
}
