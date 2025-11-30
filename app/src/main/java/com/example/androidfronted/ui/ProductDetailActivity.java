package com.example.androidfronted.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidfronted.R;
import com.example.androidfronted.ui.adapter.LoanOptionAdapter;
import com.example.androidfronted.data.model.LoanProduct;
import java.util.List;

public class ProductDetailActivity extends AppCompatActivity {

    private LoanProduct product;
    private int selectedTerm = -1; // 用户在顶部 Spinner 中选择的期数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_detail);

        // 返回按钮
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // 获取产品数据
        product = (LoanProduct) getIntent().getSerializableExtra("loan_product");
        if (product == null) {
            finish();
            return;
        }

        // 绑定产品基本信息
        bindProductInfo();

        // 设置还款期数 Spinner
        setupTermSpinner();

        // 绑定贷款方案列表
        bindOptions();
    }

    private void bindProductInfo() {
        TextView tvProductName = findViewById(R.id.tvProductName);
        TextView tvDescription = findViewById(R.id.tvDescription);
        TextView tvLoanUsage = findViewById(R.id.tvLoanUsage);
        TextView tvPromotionDetails = findViewById(R.id.tvPromotionDetails);

        // 绑定产品名称和描述
        tvProductName.setText(product.getProductName());
        tvDescription.setText(product.getDescription());

        // 绑定贷款用途
        if (product.getLoanUsage() != null && !product.getLoanUsage().isEmpty()) {
            tvLoanUsage.setText(product.getLoanUsage());
        } else {
            tvLoanUsage.setText("暂无说明");
        }

        // 绑定优惠政策
        if (product.getPromotionDetails() != null && !product.getPromotionDetails().isEmpty()) {
            tvPromotionDetails.setText(product.getPromotionDetails());
        } else {
            tvPromotionDetails.setText("暂无优惠");
        }
    }

    private void setupTermSpinner() {
        Spinner spinnerTerm = findViewById(R.id.spinnerTerm);

        if (product.getTerms() != null && !product.getTerms().isEmpty()) {
            // 将整数列表转换为字符串列表
            String[] termArray = new String[product.getTerms().size()];
            for (int i = 0; i < product.getTerms().size(); i++) {
                termArray[i] = product.getTerms().get(i) + "期";
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    termArray
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTerm.setAdapter(adapter);

            // 设置默认选择第一个
            spinnerTerm.setSelection(0);
            selectedTerm = product.getTerms().get(0);

            // 设置选择监听器
            spinnerTerm.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    selectedTerm = product.getTerms().get(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    selectedTerm = -1;
                }
            });
        } else {
            // 如果没有terms数据，显示提示
            String[] noTerms = {"暂无期数可选"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    noTerms
            );
            spinnerTerm.setAdapter(adapter);
            spinnerTerm.setEnabled(false);
        }
    }

    private void bindOptions() {
        RecyclerView recyclerView = findViewById(R.id.rvOptions);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        LoanOptionAdapter adapter = new LoanOptionAdapter(product.getOptions(), option -> {
            if (selectedTerm <= 0) {
                android.widget.Toast.makeText(this, "请选择还款期数", android.widget.Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(ProductDetailActivity.this, ProductApplyActivity.class);
            intent.putExtra("selected_option", option);      // 完整对象
            intent.putExtra("selected_term", selectedTerm);  // 用户选的 term
            intent.putExtra("product_id", product.getProductId());  // 产品 ID（用于校验）
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
    }
}