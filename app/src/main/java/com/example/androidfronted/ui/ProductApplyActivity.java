package com.example.androidfronted.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.androidfronted.R;
import com.example.androidfronted.data.model.LoanProduct;
import com.example.androidfronted.data.model.ProductApplyRequest;
import com.example.androidfronted.viewmodel.loan.ProductApplyViewModel;
import com.example.androidfronted.viewmodel.base.NavigationEvent;

import java.text.DecimalFormat;

/**
 * 贷款申请确认页面
 * - 展示用户选择的贷款方案
 * - 点击"立即申请"委托 ViewModel 提交申请
 */
public class ProductApplyActivity extends AppCompatActivity {

    private static final String TAG = "ProductApplyActivity";

    private ProductApplyViewModel viewModel;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_apply);

        viewModel = new ViewModelProvider(this).get(ProductApplyViewModel.class);

        setupObservers();
        setupViews();
        bindUI();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (btnSubmit != null) {
                btnSubmit.setEnabled(!isLoading);
            }
        });

        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getSubmitSuccess().observe(this, success -> {
            if (success != null && success) {
                Intent intent = new Intent(ProductApplyActivity.this, ProductApplySuccessActivity.class);
                intent.putExtra("from", getIntent().getStringExtra("from"));
                startActivity(intent);
                setResult(RESULT_OK);
                finish();
            }
        });

        viewModel.getNavigationEvent().observe(this, event -> {
            if (event != null) {
                handleNavigation(event);
            }
        });
    }

    private void setupViews() {
        findViewById(R.id.apply_btn_back).setOnClickListener(v -> viewModel.navigateBack());
        btnSubmit = findViewById(R.id.btn_apply);
        btnSubmit.setOnClickListener(v -> submitApplication());
    }

    private void bindUI() {
        LoanProduct.LoanOption selectedOption = (LoanProduct.LoanOption)
                getIntent().getSerializableExtra("selected_option");
        int selectedTerm = getIntent().getIntExtra("selected_term", -1);
        double selectedAmount = getIntent().getDoubleExtra("selected_amount", -1);
        int productId = getIntent().getIntExtra("product_id", -1);

        if (selectedOption == null || selectedTerm <= 0 || selectedAmount <= 0 || productId == -1) {
            Log.e(TAG, "申请数据异常");
            Toast.makeText(this, "申请数据异常", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String productName = getIntent().getStringExtra("product_name");

        TextView tvLoanName = findViewById(R.id.tvLoanName);
        TextView tvAmount = findViewById(R.id.tvSelectedAmount);
        TextView tvRate = findViewById(R.id.tvSelectedRate);
        TextView tvPeriod = findViewById(R.id.tvSelectedLoanPeriod);
        TextView tvRepay = findViewById(R.id.tvSelectedRepayType);
        TextView tvTerm = findViewById(R.id.tvSelectedTerm);

        if (productName != null && !productName.isEmpty()) {
            tvLoanName.setText(productName);
        } else {
            tvLoanName.setText("贷款申请");
        }

        tvAmount.setText(String.format("¥%,.0f", selectedAmount));
        tvRate.setText(new DecimalFormat("#.##%").format(selectedOption.getInterestRate()));
        tvPeriod.setText(selectedOption.getLoanPeriod() + "个月");
        tvRepay.setText(selectedOption.getRepaidType());
        tvTerm.setText(selectedTerm + "期");
    }

    private void submitApplication() {
        LoanProduct.LoanOption selectedOption = (LoanProduct.LoanOption)
                getIntent().getSerializableExtra("selected_option");
        int selectedTerm = getIntent().getIntExtra("selected_term", -1);
        double selectedAmount = getIntent().getDoubleExtra("selected_amount", -1);
        int productId = getIntent().getIntExtra("product_id", -1);

        if (selectedOption == null || selectedTerm <= 0 || selectedAmount <= 0 || productId == -1) {
            Log.e(TAG, "申请数据异常");
            Toast.makeText(this, "申请数据异常", Toast.LENGTH_SHORT).show();
            return;
        }

        ProductApplyRequest request = new ProductApplyRequest(productId, selectedOption.getOptionId(), selectedTerm, selectedAmount);
        viewModel.submitApplication(request);
    }

    private void handleNavigation(NavigationEvent event) {
        switch (event.getNavigationType()) {
            case NavigationEvent.NAVIGATE_BACK:
                finish();
                break;
        }
    }
}