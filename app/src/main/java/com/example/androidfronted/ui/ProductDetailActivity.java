package com.example.androidfronted.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.NumberPicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.androidfronted.R;
import com.example.androidfronted.data.model.LoanProduct;
import com.example.androidfronted.ui.adapter.LoanOptionAdapter;
import com.example.androidfronted.viewmodel.loan.ProductDetailViewModel;
import com.example.androidfronted.viewmodel.base.NavigationEvent;

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
    private TextView tvSelectedAmount;
    private Button btnApplyGlobal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_detail);

        viewModel = new ViewModelProvider(this).get(ProductDetailViewModel.class);

        setupObservers();
        setupViews();
        loadProductData();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                btnApplyGlobal.setEnabled(false);
            } else {
                btnApplyGlobal.setEnabled(true);
            }
        });

        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getValidationError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getNavigationEvent().observe(this, event -> {
            if (event != null) {
                handleNavigation(event);
            }
        });
    }

    private void setupViews() {
        findViewById(R.id.btn_back).setOnClickListener(v -> viewModel.navigateBack());

        tvSelectedTerm = findViewById(R.id.tvSelectedTerm);
        tvSelectedAmount = findViewById(R.id.tvSelectedAmount);
        btnApplyGlobal = findViewById(R.id.btnApplyGlobal);
        View cvTermSelector = findViewById(R.id.cvTermSelector);
        View cvAmountSelector = findViewById(R.id.cvAmountSelector);

        tvSelectedTerm.setText("");
        tvSelectedAmount.setText("");

        cvTermSelector.setOnClickListener(v -> showTermPickerDialog());
        cvAmountSelector.setOnClickListener(v -> showAmountInputDialog());

        btnApplyGlobal.setOnClickListener(v -> viewModel.validateAndNavigateToApply());
    }

    private void loadProductData() {
        product = (LoanProduct) getIntent().getSerializableExtra("loan_product");
        if (product == null) {
            finish();
            return;
        }

        Log.d(TAG, "产品最小金额: " + product.getMinAmount() + ", 最大金额: " + product.getMaxAmount());

        viewModel.setProduct(product);
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

        NumberPicker picker = new NumberPicker(this);
        picker.setMinValue(0);
        picker.setMaxValue(terms.size() - 1);
        picker.setDisplayedValues(terms.stream().map(t -> t + "期").toArray(String[]::new));

        Integer currentTerm = viewModel.getSelectedTerm().getValue();
        if (currentTerm != null && currentTerm > 0 && terms.contains(currentTerm)) {
            picker.setValue(terms.indexOf(currentTerm));
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("选择还款期数")
                .setView(picker)
                .setPositiveButton("确定", (d, w) -> {
                    int selectedTerm = terms.get(picker.getValue());
                    viewModel.setSelectedTerm(selectedTerm);
                    tvSelectedTerm.setText(selectedTerm + "期");
                    tvSelectedTerm.setTextColor(ContextCompat.getColor(ProductDetailActivity.this, R.color.number_amount));
                })
                .setNegativeButton("取消", null)
                .create();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
        }

        dialog.show();
    }

    private void showAmountInputDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("输入贷款金额");
        
        final EditText input = new EditText(this);
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("请输入贷款金额（" + product.getMinAmount() + "-" + product.getMaxAmount() + "）");
        builder.setView(input);
        
        builder.setPositiveButton("确定", null);
        builder.setNegativeButton("取消", (dialog, which) -> {
            Log.d(TAG, "用户点击取消");
            dialog.dismiss();
        });
        
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String amountStr = input.getText().toString();
                Log.d(TAG, "用户输入金额: " + amountStr);
                
                if (amountStr.isEmpty()) {
                    Log.e(TAG, "金额为空");
                    Toast.makeText(this, "请输入贷款金额", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                try {
                    double amount = Double.parseDouble(amountStr);
                    Log.d(TAG, "解析金额: " + amount + ", min: " + product.getMinAmount() + ", max: " + product.getMaxAmount());
                    
                    if (amount <= 0) {
                        Log.e(TAG, "金额必须大于0");
                        Toast.makeText(this, "贷款金额必须大于0", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (amount < product.getMinAmount()) {
                        Log.e(TAG, "金额小于最小值: " + amount + " < " + product.getMinAmount());
                        Toast.makeText(this, "贷款金额不能小于" + product.getMinAmount() + "元", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (amount > product.getMaxAmount()) {
                        Log.e(TAG, "金额大于最大值: " + amount + " > " + product.getMaxAmount());
                        Toast.makeText(this, "贷款金额不能大于" + product.getMaxAmount() + "元", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    Log.d(TAG, "金额验证通过，设置selectedAmount: " + amount);
                    viewModel.setSelectedAmount(amount);
                    tvSelectedAmount.setText(amount + "元");
                    tvSelectedAmount.setTextColor(ContextCompat.getColor(ProductDetailActivity.this, R.color.number_amount));
                    dialog.dismiss();
                } catch (NumberFormatException e) {
                    Log.e(TAG, "数字格式错误: " + e.getMessage());
                    Toast.makeText(this, "请输入有效的数字", Toast.LENGTH_SHORT).show();
                }
            });
        });
        
        dialog.show();
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