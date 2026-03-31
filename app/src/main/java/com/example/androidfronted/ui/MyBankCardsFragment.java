package com.example.androidfronted.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidfronted.R;
import com.example.androidfronted.data.model.CertState;
import com.example.androidfronted.ui.adapter.BankCardAdapter;
import com.example.androidfronted.ui.base.BaseDetailFragment;
import com.example.androidfronted.viewmodel.auth.MyBankCardsViewModel;
import com.example.androidfronted.viewmodel.base.NavigationEvent;

public class MyBankCardsFragment extends BaseDetailFragment {
    private Spinner spinnerBank;
    private RecyclerView rvBankCards;
    private EditText etBankCardNumber;
    private EditText etBankName;
    private BankCardAdapter adapter;
    private MyBankCardsViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_bank_cards, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MyBankCardsViewModel.class);
        spinnerBank = view.findViewById(R.id.spinner_filter);
        rvBankCards = view.findViewById(R.id.rv_bank_cards);
        etBankCardNumber = view.findViewById(R.id.et_bank_card_number);
        etBankName = view.findViewById(R.id.et_bank_name);

        setupSpinner();
        setupRecyclerView();
        setupObservers();
        setupClickListeners(view);
        viewModel.getCertInfo();
    }

    private void setupRecyclerView() {
        adapter = new BankCardAdapter();
        rvBankCards.setAdapter(adapter);
    }

    private void setupSpinner() {
        String[] banks = new String[]{"全部", "中国工商银行", "中国建设银行", "中国农业银行", "中国银行"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), 
            android.R.layout.simple_spinner_item, banks);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerBank.setAdapter(adapter);
    }

    private void setupObservers() {
        viewModel.getCertState().observe(getViewLifecycleOwner(), state -> {
            updateUIByState(state);
        });

        viewModel.getBankCardData().observe(getViewLifecycleOwner(), bankCards -> {
            if (bankCards != null && adapter != null) {
                adapter.setItems(bankCards);
            }
        });

        viewModel.getSubmitResult().observe(getViewLifecycleOwner(), response -> {
            if (response != null && response.getCode() == 200) {
                Toast.makeText(getContext(), "提交成功", Toast.LENGTH_SHORT).show();
                viewModel.getCertInfo();
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
        view.findViewById(R.id.btnGoToInfoBankcard).setOnClickListener(v -> {
            viewModel.startUpload();
        });

        view.findViewById(R.id.btn_confirm_bank_card_upload).setOnClickListener(v -> {
            String bankCardNumber = etBankCardNumber.getText().toString().trim();
            String bankName = etBankName.getText().toString().trim();
            
            android.util.Log.d("MyBankCardsFragment", "btn_confirm_bank_card_upload clicked");
            android.util.Log.d("MyBankCardsFragment", "bankCardNumber: " + bankCardNumber);
            android.util.Log.d("MyBankCardsFragment", "bankName: " + bankName);
            
            if (bankCardNumber.isEmpty()) {
                android.util.Log.w("MyBankCardsFragment", "bankCardNumber is empty");
                Toast.makeText(getContext(), "请输入银行卡号", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (bankCardNumber.length() != 16) {
                android.util.Log.w("MyBankCardsFragment", "bankCardNumber length is not 16");
                Toast.makeText(getContext(), "银行卡号必须为16位", Toast.LENGTH_SHORT).show();
                return;
            }
            
            android.util.Log.d("MyBankCardsFragment", "calling viewModel.submitBankCard");
            viewModel.submitBankCard(bankCardNumber, bankName);
        });

        view.findViewById(R.id.btn_add_bank_card).setOnClickListener(v -> {
            viewModel.startUpload();
        });

        view.findViewById(R.id.btn_delete).setOnClickListener(v -> {
            Toast.makeText(getContext(), "管理", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateUIByState(CertState state) {
        if (getView() == null) return;

        View containerNotCertified = getView().findViewById(R.id.container_not_certified);
        View containerUploading = getView().findViewById(R.id.container_uploading);
        View containerCertified = getView().findViewById(R.id.container_certified);
        View btnAddBankCard = getView().findViewById(R.id.btn_add_bank_card);
        View btnConfirmUpload = getView().findViewById(R.id.btn_confirm_bank_card_upload);

        if (containerNotCertified != null) containerNotCertified.setVisibility(View.GONE);
        if (containerUploading != null) containerUploading.setVisibility(View.GONE);
        if (containerCertified != null) containerCertified.setVisibility(View.GONE);
        if (btnAddBankCard != null) btnAddBankCard.setVisibility(View.GONE);
        if (btnConfirmUpload != null) btnConfirmUpload.setVisibility(View.GONE);

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
                if (btnAddBankCard != null) btnAddBankCard.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void handleNavigation(NavigationEvent event) {
        switch (event.getNavigationType()) {
            case NavigationEvent.NAVIGATE_TO_BANK_CARD_UPLOAD:
                viewModel.startUpload();
                break;
            case NavigationEvent.NAVIGATE_BACK:
                navigateBack();
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 移除重复请求，避免页面闪烁
    }

    @Override
    protected void navigateBack() {
        CertState currentState = viewModel.getCertState().getValue();
        android.util.Log.d("MyBankCardsFragment", "navigateBack called, currentState: " + currentState);
        if (currentState == CertState.UPLOADING) {
            android.util.Log.d("MyBankCardsFragment", "Cancelling upload");
            viewModel.cancelUpload();
        } else {
            android.util.Log.d("MyBankCardsFragment", "Calling super.navigateBack()");
            super.navigateBack();
        }
    }
}
