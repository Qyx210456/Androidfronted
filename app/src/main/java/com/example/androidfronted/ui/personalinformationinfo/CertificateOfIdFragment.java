package com.example.androidfronted.ui.personalinformationinfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import com.example.androidfronted.R;
import com.example.androidfronted.data.model.CertInfoResponse;
import com.example.androidfronted.data.model.CertState;
import com.example.androidfronted.ui.base.BaseDetailFragment;
import com.example.androidfronted.utils.IdCardValidator;
import com.example.androidfronted.viewmodel.auth.IdCertViewModel;
import com.example.androidfronted.viewmodel.base.NavigationEvent;

public class CertificateOfIdFragment extends BaseDetailFragment {
    private IdCertViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_certificate_of_id, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(IdCertViewModel.class);

        setupObservers();
        setupClickListeners(view);
        loadCertInfo();
    }

    private void setupObservers() {
        viewModel.getCertState().observe(getViewLifecycleOwner(), state -> {
            updateUIByState(state);
        });

        viewModel.getCertData().observe(getViewLifecycleOwner(), certData -> {
            if (certData != null && getView() != null) {
                TextView tvName = getView().findViewById(R.id.tv_name);
                TextView tvIdNumber = getView().findViewById(R.id.tv_id_number);

                if (tvName != null) {
                    tvName.setText(certData.getRealName() != null ? certData.getRealName() : "");
                }
                if (tvIdNumber != null) {
                    tvIdNumber.setText(certData.getIdCard() != null ? certData.getIdCard() : "");
                }
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
        view.findViewById(R.id.front_ID).setOnClickListener(v -> {
            Toast.makeText(getContext(), "上传身份证正面", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.back_ID).setOnClickListener(v -> {
            Toast.makeText(getContext(), "上传身份证反面", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.btn_confirm_id_submission).setOnClickListener(v -> {
            android.util.Log.d("CertificateOfIdFragment", "btn_confirm_id_submission clicked");
            EditText etIdNumber = view.findViewById(R.id.et_id_number);
            EditText etName = view.findViewById(R.id.et_name);
            String idNumber = etIdNumber.getText().toString().trim();
            String realName = etName.getText().toString().trim();
            android.util.Log.d("CertificateOfIdFragment", "idNumber: " + idNumber + ", realName: " + realName);
            
            if (realName.isEmpty()) {
                android.util.Log.w("CertificateOfIdFragment", "realName is empty");
                Toast.makeText(getContext(), "请输入真实姓名", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!isValidRealName(realName)) {
                android.util.Log.w("CertificateOfIdFragment", "Invalid realName format");
                Toast.makeText(getContext(), "姓名格式不正确，请输入真实姓名", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (idNumber.isEmpty()) {
                android.util.Log.w("CertificateOfIdFragment", "idNumber is empty");
                Toast.makeText(getContext(), "请输入身份证号", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String errorMessage = IdCardValidator.getErrorMessage(idNumber);
            if (!errorMessage.isEmpty()) {
                android.util.Log.w("CertificateOfIdFragment", "Invalid idCard: " + errorMessage);
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                return;
            }
            
            android.util.Log.d("CertificateOfIdFragment", "calling viewModel.submitCert");
            viewModel.submitCert(idNumber, realName);
        });

        view.findViewById(R.id.btn_id_upload).setOnClickListener(v -> {
            viewModel.startUpload();
        });

        view.findViewById(R.id.btn_modify_id).setOnClickListener(v -> {
            viewModel.startUpload();
        });
    }

    private void loadCertInfo() {
        viewModel.getCertInfo(new IdCertViewModel.CertInfoCallback() {
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
        View btnConfirmSubmission = getView().findViewById(R.id.btn_confirm_id_submission);
        View btnIdUpload = getView().findViewById(R.id.btn_id_upload);
        View btnModifyId = getView().findViewById(R.id.btn_modify_id);

        if (containerNotCertified != null) containerNotCertified.setVisibility(View.GONE);
        if (containerUploading != null) containerUploading.setVisibility(View.GONE);
        if (containerCertified != null) containerCertified.setVisibility(View.GONE);
        if (btnConfirmSubmission != null) btnConfirmSubmission.setVisibility(View.GONE);
        if (btnIdUpload != null) btnIdUpload.setVisibility(View.GONE);
        if (btnModifyId != null) btnModifyId.setVisibility(View.GONE);

        if (state == null) {
            // 状态为 null，不显示任何内容，避免闪烁
            return;
        }

        switch (state) {
            case NOT_CERTIFIED:
                if (containerNotCertified != null) containerNotCertified.setVisibility(View.VISIBLE);
                if (btnIdUpload != null) btnIdUpload.setVisibility(View.VISIBLE);
                break;
            case UPLOADING:
                if (containerUploading != null) containerUploading.setVisibility(View.VISIBLE);
                if (btnConfirmSubmission != null) btnConfirmSubmission.setVisibility(View.VISIBLE);
                break;
            case CERTIFIED:
                if (containerCertified != null) containerCertified.setVisibility(View.VISIBLE);
                if (btnModifyId != null) btnModifyId.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void handleNavigation(NavigationEvent event) {
        switch (event.getNavigationType()) {
            case NavigationEvent.NAVIGATE_BACK:
                navigateBack();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        android.util.Log.d("CertificateOfIdFragment", "onResume called");
        CertState currentState = viewModel.getCertState().getValue();
        if (currentState != CertState.UPLOADING) {
            loadCertInfo();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        android.util.Log.d("CertificateOfIdFragment", "onPause called");
    }

    @Override
    public void onStop() {
        super.onStop();
        android.util.Log.d("CertificateOfIdFragment", "onStop called");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        android.util.Log.d("CertificateOfIdFragment", "onDestroyView called");
    }

    @Override
    protected void navigateBack() {
        CertState currentState = viewModel.getCertState().getValue();
        android.util.Log.d("CertificateOfIdFragment", "navigateBack called, currentState: " + currentState);
        if (currentState == CertState.UPLOADING) {
            android.util.Log.d("CertificateOfIdFragment", "Cancelling upload");
            viewModel.cancelUpload();
        } else {
            android.util.Log.d("CertificateOfIdFragment", "Calling super.navigateBack()");
            super.navigateBack();
        }
    }
    
    /**
     * 验证真实姓名格式是否符合中华人民共和国身份证上的真实姓名格式
     * @param realName 真实姓名
     * @return 是否符合格式
     */
    private boolean isValidRealName(String realName) {
        // 姓名长度：2-6个汉字，可能包含少数特殊字符如"·"
        // 正则表达式：匹配2-6个汉字，中间可能包含一个"·"
        String regex = "^[\\u4e00-\\u9fa5]{2,6}$|^[\\u4e00-\\u9fa5]{1,5}[·][\\u4e00-\\u9fa5]{1,5}$";
        return realName.matches(regex);
    }
}
