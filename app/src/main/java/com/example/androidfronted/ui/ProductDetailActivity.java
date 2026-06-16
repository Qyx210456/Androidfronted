package com.example.androidfronted.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.androidfronted.R;
import com.example.androidfronted.data.model.LoanProduct;
import com.example.androidfronted.ui.adapter.LoanOptionAdapter;
import com.example.androidfronted.ui.loan.TermPickerBottomSheet;
import com.example.androidfronted.viewmodel.loan.ProductDetailViewModel;
import com.example.androidfronted.viewmodel.base.NavigationEvent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetailActivity";
    private static final int REQUEST_APPLY = 1001;

    private ProductDetailViewModel viewModel;
    private LoanProduct product;
    private LoanOptionAdapter optionAdapter;
    private TextView tvSelectedTerm;
    private EditText etAmount;
    private TextView tvAmountRange;
    private TextView tvAmountError;
    private CardView cvAmountSelector;
    private Button btnApplyGlobal;
    private DecimalFormat decimalFormat;
    private LinearLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_detail);

        decimalFormat = new DecimalFormat("#,###");
        viewModel = new ViewModelProvider(this).get(ProductDetailViewModel.class);

        setupObservers();
        setupViews();
        loadProductData();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            btnApplyGlobal.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getNavigationEvent().observe(this, event -> {
            if (event != null) {
                handleNavigation(event);
            }
        });
    }

    private void setupViews() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        rootLayout = findViewById(R.id.root_layout);
        tvSelectedTerm = findViewById(R.id.tvSelectedTerm);
        etAmount = findViewById(R.id.etAmount);
        tvAmountRange = findViewById(R.id.tvAmountRange);
        tvAmountError = findViewById(R.id.tvAmountError);
        cvAmountSelector = findViewById(R.id.cvAmountSelector);
        btnApplyGlobal = findViewById(R.id.btnApplyGlobal);
        View cvTermSelector = findViewById(R.id.cvTermSelector);

        tvSelectedTerm.setText("");

        cvTermSelector.setOnClickListener(v -> showTermPickerDialog());

        etAmount.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateAmountOnFocusLost();
            }
        });

        etAmount.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                etAmount.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etAmount.getWindowToken(), 0);
                return true;
            }
            return false;
        });

        btnApplyGlobal.setOnClickListener(v -> validateAndSubmit());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                int[] location = new int[2];
                v.getLocationOnScreen(location);
                float x = ev.getRawX() + v.getLeft() - location[0];
                float y = ev.getRawY() + v.getTop() - location[1];
                
                if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom()) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void loadProductData() {
        product = (LoanProduct) getIntent().getSerializableExtra("loan_product");
        if (product == null) {
            finish();
            return;
        }

        Log.d(TAG, "产品最小金额: " + product.getMinAmount() + ", 最大金额: " + product.getMaxAmount());

        viewModel.setProduct(product);
        
        String rangeText = "贷款金额范围: " + decimalFormat.format(product.getMinAmount()) + " ~ " + decimalFormat.format(product.getMaxAmount()) + " 元";
        tvAmountRange.setText(rangeText);
        
        bindProductInfo();
        bindOptions();
    }

    private void bindProductInfo() {
        TextView tvProductName = findViewById(R.id.tvProductName);
        TextView tvDescription = findViewById(R.id.tvDescription);
        TextView tvLoanUsage = findViewById(R.id.tvLoanUsage);
        TextView tvPromotionDetails = findViewById(R.id.tvPromotionDetails);

        tvProductName.setText(product.getProductName());
        tvDescription.setText(product.getDescription());

        if (product.getLoanUsage() != null && !product.getLoanUsage().isEmpty()) {
            tvLoanUsage.setText(product.getLoanUsage());
        } else {
            tvLoanUsage.setText("暂无说明");
        }

        if (product.getPromotionDetails() != null && !product.getPromotionDetails().isEmpty()) {
            tvPromotionDetails.setText(product.getPromotionDetails());
        } else {
            tvPromotionDetails.setText("暂无优惠");
        }
    }

    private void showTermPickerDialog() {
        if (product.getTerms() == null || product.getTerms().isEmpty()) return;

        List<Integer> terms = new ArrayList<>(product.getTerms());
        Collections.sort(terms);

        Integer currentTerm = viewModel.getSelectedTerm().getValue();
        int selectedTerm = currentTerm != null ? currentTerm : 0;

        TermPickerBottomSheet bottomSheet = TermPickerBottomSheet.newInstance(terms, selectedTerm);
        bottomSheet.setOnTermSelectedListener(term -> {
            viewModel.setSelectedTerm(term);
            tvSelectedTerm.setText(term + "期");
            tvSelectedTerm.setTextColor(ContextCompat.getColor(ProductDetailActivity.this, R.color.number_amount));
        });
        bottomSheet.show(getSupportFragmentManager(), "TermPickerBottomSheet");
    }

    private void validateAmountOnFocusLost() {
        String amountStr = etAmount.getText().toString().trim();
        
        if (amountStr.isEmpty()) {
            clearAmountError();
            viewModel.setSelectedAmount(0);
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            String error = validateAmount(amount);
            
            if (error != null) {
                showAmountError(error);
            } else {
                clearAmountError();
                viewModel.setSelectedAmount(amount);
            }
        } catch (NumberFormatException e) {
            showAmountError("请输入有效的数字");
        }
    }

    private String validateAmount(double amount) {
        if (amount <= 0) {
            return "贷款金额必须大于0";
        }
        if (amount < product.getMinAmount()) {
            return "金额不能低于 " + decimalFormat.format(product.getMinAmount()) + " 元";
        }
        if (amount > product.getMaxAmount()) {
            return "金额不能高于 " + decimalFormat.format(product.getMaxAmount()) + " 元";
        }
        return null;
    }

    private void showAmountError(String error) {
        tvAmountError.setText(error);
        tvAmountError.setVisibility(View.VISIBLE);
        FrameLayout frameLayout = (FrameLayout) cvAmountSelector.getChildAt(0);
        frameLayout.setBackgroundResource(R.drawable.bg_amount_error);
        etAmount.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
    }

    private void clearAmountError() {
        tvAmountError.setVisibility(View.GONE);
        FrameLayout frameLayout = (FrameLayout) cvAmountSelector.getChildAt(0);
        frameLayout.setBackgroundResource(R.drawable.bg_term_selector);
        etAmount.setTextColor(ContextCompat.getColor(this, R.color.number_amount));
    }

    private void validateAndSubmit() {
        String amountStr = etAmount.getText().toString().trim();
        
        if (amountStr.isEmpty()) {
            showAmountError("请输入贷款金额");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            String error = validateAmount(amount);
            
            if (error != null) {
                showAmountError(error);
                return;
            }
            
            clearAmountError();
            viewModel.setSelectedAmount(amount);
            
            Integer selectedTerm = viewModel.getSelectedTerm().getValue();
            if (selectedTerm == null) {
                Toast.makeText(this, "请选择还款期数", Toast.LENGTH_SHORT).show();
                return;
            }
            
            LoanProduct.LoanOption selectedOption = viewModel.getSelectedOption().getValue();
            if (selectedOption == null) {
                Toast.makeText(this, "请选择方案", Toast.LENGTH_SHORT).show();
                return;
            }
            
            viewModel.validateAndNavigateToApply();
            
        } catch (NumberFormatException e) {
            showAmountError("请输入有效的数字");
        }
    }

    private void bindOptions() {
        androidx.recyclerview.widget.RecyclerView recyclerView = findViewById(R.id.rvOptions);
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(this));
        optionAdapter = new LoanOptionAdapter(product.getOptions(), option -> {
            viewModel.setSelectedOption(option);
        });
        recyclerView.setAdapter(optionAdapter);
    }

    private void handleNavigation(NavigationEvent event) {
        switch (event.getNavigationType()) {
            case NavigationEvent.NAVIGATE_TO_PRODUCT_APPLY:
                navigateToApply();
                break;
            case NavigationEvent.NAVIGATE_BACK:
                finish();
                break;
        }
    }

    private void navigateToApply() {
        LoanProduct.LoanOption selectedOption = viewModel.getSelectedOption().getValue();
        Integer selectedTerm = viewModel.getSelectedTerm().getValue();
        Double selectedAmount = viewModel.getSelectedAmount().getValue();

        if (selectedOption == null || selectedTerm == null || selectedAmount == null) {
            return;
        }

        Intent intent = new Intent(ProductDetailActivity.this, ProductApplyActivity.class);
        intent.putExtra("selected_option", selectedOption);
        intent.putExtra("selected_term", selectedTerm);
        intent.putExtra("selected_amount", selectedAmount);
        intent.putExtra("product_id", product.getProductId());
        intent.putExtra("product_name", product.getProductName());
        intent.putExtra("from", getIntent().getStringExtra("from"));
        startActivityForResult(intent, REQUEST_APPLY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_APPLY && resultCode == RESULT_OK) {
            finish();
        }
    }
}
