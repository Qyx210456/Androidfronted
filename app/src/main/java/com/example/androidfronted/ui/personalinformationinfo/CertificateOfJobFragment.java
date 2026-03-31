package com.example.androidfronted.ui.personalinformationinfo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidfronted.R;
import com.example.androidfronted.data.model.CertInfoResponse;
import com.example.androidfronted.data.model.CertState;
import com.example.androidfronted.ui.adapter.UploadedCertificateAdapter;
import com.example.androidfronted.ui.base.BaseDetailFragment;
import com.example.androidfronted.utils.ImageUploadHelper;
import com.example.androidfronted.viewmodel.auth.JobCertViewModel;
import com.example.androidfronted.viewmodel.base.NavigationEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CertificateOfJobFragment extends BaseDetailFragment {
    private Spinner spinnerCertType;
    private Spinner spinnerUploadType;
    private RecyclerView rvCertificates;
    private UploadedCertificateAdapter adapter;
    private JobCertViewModel viewModel;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageView ivUploadIcon;
    private TextView tvUploadHint;
    private File employmentFile;
    private File salaryFile;

    private String getMimeType(String filePath) {
        String type = null;
        String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(filePath);
        if (extension != null) {
            type = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_certificate_of_job, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(JobCertViewModel.class);
        spinnerCertType = view.findViewById(R.id.spinner_filter);
        spinnerUploadType = view.findViewById(R.id.spinner_employment_type);
        rvCertificates = view.findViewById(R.id.rv_uploaded_certificates);
        ivUploadIcon = view.findViewById(R.id.iv_upload_icon);
        tvUploadHint = view.findViewById(R.id.tv_upload_hint);

        setupImagePicker();
        setupSpinner();
        setupUploadSpinner();
        setupRecyclerView();
        setupObservers();
        setupClickListeners(view);
        loadCertInfo();
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null && ivUploadIcon != null) {
                            ivUploadIcon.setImageURI(selectedImageUri);
                            if (tvUploadHint != null) {
                                tvUploadHint.setVisibility(View.GONE);
                            }
                            Toast.makeText(getContext(), "已选择图片", Toast.LENGTH_SHORT).show();
                            
                            String uploadType = spinnerUploadType.getSelectedItem() != null ? 
                                spinnerUploadType.getSelectedItem().toString() : "";
                            
                            ImageUploadHelper.compressImage(getContext(), selectedImageUri, 
                                new ImageUploadHelper.ImageUploadCallback() {
                                    @Override
                                    public void onSuccess(String imagePath) {
                                        File compressedFile = new File(imagePath);
                                        if (uploadType.contains("工作证明")) {
                                            employmentFile = compressedFile;
                                        } else if (uploadType.contains("工资")) {
                                            salaryFile = compressedFile;
                                        }
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

    private void setupRecyclerView() {
        adapter = new UploadedCertificateAdapter();
        rvCertificates.setAdapter(adapter);
    }

    private void setupSpinner() {
        String[] certTypes = new String[]{"工作证明图片", "工资流水截图或银行流水"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), 
            android.R.layout.simple_spinner_item, certTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCertType.setAdapter(adapter);
    }

    private void setupUploadSpinner() {
        String[] uploadTypes = new String[]{"工作证明图片", "工资流水截图或银行流水"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), 
            android.R.layout.simple_spinner_item, uploadTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUploadType.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getCertState().observe(getViewLifecycleOwner(), state -> {
            updateUIByState(state);
        });

        viewModel.getCertData().observe(getViewLifecycleOwner(), certData -> {
            if (certData != null && getView() != null && adapter != null) {
                List<UploadedCertificateAdapter.CertificateItem> items = new ArrayList<>();
                
                if (certData.getEmploymentCertPath() != null && !certData.getEmploymentCertPath().isEmpty()) {
                    items.add(new UploadedCertificateAdapter.CertificateItem("工作证明图片", certData.getEmploymentCertPath()));
                }
                
                if (certData.getSalaryCertPath() != null && !certData.getSalaryCertPath().isEmpty()) {
                    items.add(new UploadedCertificateAdapter.CertificateItem("工资流水截图或银行流水", certData.getSalaryCertPath()));
                }
                
                adapter.setItems(items);
            }
        });

        viewModel.getSubmitResult().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getCode() == 200) {
                Toast.makeText(getContext(), "提交成功", Toast.LENGTH_SHORT).show();
                loadCertInfo();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && getContext() != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getNavigationEvent().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                handleNavigation(event);
            }
        });
    }

    private void setupClickListeners(View view) {
        view.findViewById(R.id.btn_job_upload).setOnClickListener(v -> {
            viewModel.startUpload();
        });

        view.findViewById(R.id.btn_add_job).setOnClickListener(v -> {
            viewModel.startUpload();
        });

        view.findViewById(R.id.btn_confirm_employment_upload).setOnClickListener(v -> {
            android.util.Log.d("CertificateOfJobFragment", "btn_confirm_employment_upload clicked");
            
            okhttp3.RequestBody employmentRequestBody = null;
            okhttp3.RequestBody salaryRequestBody = null;
            
            if (employmentFile != null) {
                String mimeType = getMimeType(employmentFile.getAbsolutePath());
                okhttp3.MediaType mediaType = mimeType != null ? okhttp3.MediaType.parse(mimeType) : okhttp3.MediaType.parse("image/*");
                employmentRequestBody = okhttp3.RequestBody.create(
                    mediaType,
                    employmentFile
                );
            }
            
            if (salaryFile != null) {
                String mimeType = getMimeType(salaryFile.getAbsolutePath());
                okhttp3.MediaType mediaType = mimeType != null ? okhttp3.MediaType.parse(mimeType) : okhttp3.MediaType.parse("image/*");
                salaryRequestBody = okhttp3.RequestBody.create(
                    mediaType,
                    salaryFile
                );
            }
            
            android.util.Log.d("CertificateOfJobFragment", "calling viewModel.submitCert");
            android.util.Log.d("CertificateOfJobFragment", "employmentFile: " + (employmentFile != null ? employmentFile.getAbsolutePath() : "null"));
            android.util.Log.d("CertificateOfJobFragment", "salaryFile: " + (salaryFile != null ? salaryFile.getAbsolutePath() : "null"));
            viewModel.submitCert(employmentRequestBody, salaryRequestBody);
        });

        view.findViewById(R.id.iv_upload_icon).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            imagePickerLauncher.launch(intent);
        });

        view.findViewById(R.id.btn_delete).setOnClickListener(v -> {
            Toast.makeText(getContext(), "管理", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadCertInfo() {
        viewModel.getCertInfo(new JobCertViewModel.CertInfoCallback() {
            @Override
            public void onSuccess(CertInfoResponse response) {
            }

            @Override
            public void onError(String errorMessage) {
            }
        });
    }

    private void updateUIByState(CertState state) {
        if (getView() == null) return;

        View containerNotCertified = getView().findViewById(R.id.container_not_certified);
        View containerUploading = getView().findViewById(R.id.container_uploading);
        View containerCertified = getView().findViewById(R.id.container_certified);
        View btnConfirmUpload = getView().findViewById(R.id.btn_confirm_employment_upload);
        View btnAddJob = getView().findViewById(R.id.btn_add_job);

        if (containerNotCertified != null) containerNotCertified.setVisibility(View.GONE);
        if (containerUploading != null) containerUploading.setVisibility(View.GONE);
        if (containerCertified != null) containerCertified.setVisibility(View.GONE);
        if (btnConfirmUpload != null) btnConfirmUpload.setVisibility(View.GONE);
        if (btnAddJob != null) btnAddJob.setVisibility(View.GONE);

        if (state == null) {
            // 状态为 null，不显示任何内容，避免闪烁
            return;
        }

        switch (state) {
            case NOT_CERTIFIED:
                if (containerNotCertified != null) containerNotCertified.setVisibility(View.VISIBLE);
                break;
            case UPLOADING:
                if (containerUploading != null) containerUploading.setVisibility(View.VISIBLE);
                if (btnConfirmUpload != null) btnConfirmUpload.setVisibility(View.VISIBLE);
                break;
            case CERTIFIED:
                if (containerCertified != null) containerCertified.setVisibility(View.VISIBLE);
                if (btnAddJob != null) btnAddJob.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void handleNavigation(NavigationEvent event) {
        switch (event.getNavigationType()) {
            case NavigationEvent.NAVIGATE_TO_PERSONAL_INFO:
                navigateBack();
                break;
            case NavigationEvent.NAVIGATE_BACK:
                navigateBack();
                break;
            case NavigationEvent.NAVIGATE_TO_JOB_CERT_UPLOAD:
                viewModel.startUpload();
                break;
        }
    }

    @Override
    protected void navigateBack() {
        CertState currentState = viewModel.getCertState().getValue();
        if (currentState == CertState.UPLOADING) {
            viewModel.cancelUpload();
        } else {
            super.navigateBack();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        CertState currentState = viewModel.getCertState().getValue();
        if (currentState != CertState.UPLOADING) {
            loadCertInfo();
        }
    }
}
