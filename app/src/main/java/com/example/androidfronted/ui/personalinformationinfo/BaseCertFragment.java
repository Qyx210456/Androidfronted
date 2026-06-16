package com.example.androidfronted.ui.personalinformationinfo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidfronted.R;
import com.example.androidfronted.data.model.CertState;
import com.example.androidfronted.ui.ImageEditActivity;
import com.example.androidfronted.ui.adapter.UploadedCertificateAdapter;
import com.example.androidfronted.ui.base.BaseDetailFragment;
import com.example.androidfronted.utils.ImageCropHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseCertFragment extends BaseDetailFragment {
    protected Spinner spinnerCertType;
    protected RecyclerView rvCertificates;
    protected UploadedCertificateAdapter adapter;
    protected ActivityResultLauncher<Intent> imagePickerLauncher;
    protected ActivityResultLauncher<Intent> imageEditLauncher;
    protected ImageView ivUploadIcon;
    protected LinearLayout llUploadType;
    protected LinearLayout llFilter;
    protected TextView tvUploadHint;
    protected TextView tvUploadType;
    protected File firstFile;
    protected File secondFile;
    protected String pendingUploadType;
    protected String currentFilterType = "全部";
    protected List<String> uploadTypes;
    protected String firstTypeKeyword;
    protected String secondTypeKeyword;
    private CertState lastState = null;

    protected abstract Object getViewModel();
    protected abstract int getLayoutId();
    protected abstract int getSuccessType();
    protected abstract List<String> getCertTypes();
    protected abstract List<String> getUploadTypes();
    protected abstract void setupObservers();
    protected abstract void loadCertInfo();
    protected abstract void submitCert();
    protected abstract void filterCertificates();
    protected abstract int getContainerNotCertifiedId();
    protected abstract int getContainerUploadingId();
    protected abstract int getContainerCertifiedId();
    protected abstract int getBtnConfirmUploadId();
    protected abstract int getBtnAddId();
    protected abstract int getBtnStartUploadId();
    protected abstract void onFileSelected(File file, String uploadType);
    protected abstract void handleNavigation(int navigationType);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        spinnerCertType = view.findViewById(R.id.spinner_filter);
        rvCertificates = view.findViewById(R.id.rv_uploaded_certificates);
        ivUploadIcon = view.findViewById(R.id.iv_upload_icon);
        llUploadType = view.findViewById(R.id.ll_upload_type);
        llFilter = view.findViewById(R.id.ll_filter);
        tvUploadHint = view.findViewById(R.id.tv_upload_hint);
        tvUploadType = view.findViewById(R.id.tv_upload_type);

        uploadTypes = getUploadTypes();
        List<String> certTypes = getCertTypes();
        if (certTypes.size() >= 2) {
            firstTypeKeyword = certTypes.get(1);
            secondTypeKeyword = certTypes.size() >= 3 ? certTypes.get(2) : certTypes.get(1);
        }

        setupImagePicker();
        setupImageEditLauncher();
        setupSpinner(certTypes);
        setupUploadTypeSelector();
        setupFilterClick();
        setupRecyclerView();
        setupObservers();
        setupClickListeners(view);
        loadCertInfo();
    }

    protected void setupImagePicker() {
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

    protected void setupImageEditLauncher() {
        imageEditLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri resultUri = result.getData().getParcelableExtra(ImageEditActivity.EXTRA_RESULT_URI);
                        if (resultUri != null && ivUploadIcon != null) {
                            ivUploadIcon.setImageURI(resultUri);
                            if (tvUploadHint != null) {
                                tvUploadHint.setVisibility(View.GONE);
                            }
                            Toast.makeText(getContext(), "图片已选择", Toast.LENGTH_SHORT).show();
                            
                            ImageCropHelper.compressCroppedImage(getContext(), resultUri,
                                new ImageCropHelper.CropCallback() {
                                    @Override
                                    public void onSuccess(Uri compressedUri) {
                                        File compressedFile = new File(compressedUri.getPath());
                                        onFileSelected(compressedFile, pendingUploadType);
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

    protected void openImageEdit(Uri imageUri) {
        Intent intent = new Intent(getContext(), ImageEditActivity.class);
        intent.putExtra(ImageEditActivity.EXTRA_IMAGE_URI, imageUri);
        intent.putExtra(ImageEditActivity.EXTRA_CROP_SHAPE, ImageEditActivity.CROP_SHAPE_RECTANGLE);
        imageEditLauncher.launch(intent);
    }

    protected void setupRecyclerView() {
        adapter = new UploadedCertificateAdapter();
        rvCertificates.setAdapter(adapter);
    }

    protected void setupSpinner(List<String> certTypes) {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(requireContext(), 
            R.layout.item_spinner, certTypes);
        spinnerAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerCertType.setAdapter(spinnerAdapter);
        
        spinnerCertType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilterType = certTypes.get(position);
                filterCertificates();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    protected void setupUploadTypeSelector() {
        llUploadType.setOnClickListener(v -> showUploadTypeBottomSheet());
    }

    protected void showUploadTypeBottomSheet() {
        UploadTypeBottomSheet bottomSheet = UploadTypeBottomSheet.newInstance(uploadTypes, -1);
        bottomSheet.setOnTypeSelectedListener((type, position) -> {
            if (pendingUploadType != null && !pendingUploadType.equals(type)) {
                if (firstFile != null || secondFile != null) {
                    File existingFile = firstFile != null ? firstFile : secondFile;
                    onFileSelected(existingFile, type);
                }
            }
            pendingUploadType = type;
            tvUploadType.setText(type);
            tvUploadType.setTextColor(getResources().getColor(R.color.text_secondary));
        });
        bottomSheet.show(getParentFragmentManager(), "UploadTypeBottomSheet");
    }

    protected void setupFilterClick() {
        if (llFilter != null) {
            llFilter.setOnClickListener(v -> {
                if (spinnerCertType != null) {
                    spinnerCertType.performClick();
                }
            });
        }
    }

    protected String getMimeType(String filePath) {
        String type = null;
        String extension = android.webkit.MimeTypeMap.getFileExtensionFromUrl(filePath);
        if (extension != null) {
            type = android.webkit.MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    protected okhttp3.RequestBody createRequestBody(File file) {
        if (file == null) return null;
        String mimeType = getMimeType(file.getAbsolutePath());
        okhttp3.MediaType mediaType = mimeType != null ? okhttp3.MediaType.parse(mimeType) : okhttp3.MediaType.parse("image/*");
        return okhttp3.RequestBody.create(mediaType, file);
    }

    protected void setupClickListeners(View view) {
        view.findViewById(getBtnStartUploadId()).setOnClickListener(v -> onStartUploadClick());
        view.findViewById(getBtnAddId()).setOnClickListener(v -> onStartUploadClick());
        view.findViewById(getBtnConfirmUploadId()).setOnClickListener(v -> onConfirmUploadClick());
        view.findViewById(R.id.iv_upload_icon).setOnClickListener(v -> onUploadIconClick());
        view.findViewById(R.id.btn_delete).setOnClickListener(v -> {
            Toast.makeText(getContext(), "管理", Toast.LENGTH_SHORT).show();
        });
    }

    protected void onStartUploadClick() {
    }

    protected void onConfirmUploadClick() {
        if (pendingUploadType == null || pendingUploadType.isEmpty()) {
            Toast.makeText(getContext(), "请先选择上传资料类型", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (firstFile == null && secondFile == null) {
            Toast.makeText(getContext(), "请先选择要上传的图片", Toast.LENGTH_SHORT).show();
            return;
        }
        
        submitCert();
    }

    protected void onUploadIconClick() {
        if (pendingUploadType == null || pendingUploadType.isEmpty()) {
            Toast.makeText(getContext(), "请先选择上传资料类型", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(intent);
    }

    protected void updateUIByState(CertState state) {
        if (getView() == null) return;

        View containerNotCertified = getView().findViewById(getContainerNotCertifiedId());
        View containerUploading = getView().findViewById(getContainerUploadingId());
        View containerCertified = getView().findViewById(getContainerCertifiedId());
        View btnConfirmUpload = getView().findViewById(getBtnConfirmUploadId());
        View btnAdd = getView().findViewById(getBtnAddId());

        if (containerNotCertified != null) containerNotCertified.setVisibility(View.GONE);
        if (containerUploading != null) containerUploading.setVisibility(View.GONE);
        if (containerCertified != null) containerCertified.setVisibility(View.GONE);
        if (btnConfirmUpload != null) btnConfirmUpload.setVisibility(View.GONE);
        if (btnAdd != null) btnAdd.setVisibility(View.GONE);

        if (state == null) {
            return;
        }

        switch (state) {
            case NOT_CERTIFIED:
                if (containerNotCertified != null) containerNotCertified.setVisibility(View.VISIBLE);
                break;
            case UPLOADING:
                if (containerUploading != null) containerUploading.setVisibility(View.VISIBLE);
                if (btnConfirmUpload != null) btnConfirmUpload.setVisibility(View.VISIBLE);
                if (lastState != CertState.UPLOADING) {
                    resetUploadSpinner();
                }
                break;
            case CERTIFIED:
                if (containerCertified != null) containerCertified.setVisibility(View.VISIBLE);
                if (btnAdd != null) btnAdd.setVisibility(View.VISIBLE);
                break;
        }
        lastState = state;
    }

    @Override
    protected void navigateBack() {
        CertState currentState = getCertStateValue();
        if (currentState == CertState.UPLOADING) {
            cancelUpload();
        } else {
            super.navigateBack();
        }
    }

    protected abstract CertState getCertStateValue();
    protected abstract void cancelUpload();

    @Override
    public void onResume() {
        super.onResume();
        CertState currentState = getCertStateValue();
        if (currentState != CertState.UPLOADING && currentState != null) {
            resetUploadSpinner();
            loadCertInfo();
        }
    }

    protected void resetUploadSpinner() {
        if (tvUploadType != null) {
            pendingUploadType = null;
            tvUploadType.setText("请选择上传资料类型");
            tvUploadType.setTextColor(getResources().getColor(R.color.text_quaternary));
        }
    }
}
