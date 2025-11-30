package com.example.androidfronted.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
        TextView tvProductName = findViewById(R.id.tvProductName);
        TextView tvDescription = findViewById(R.id.tvDescription);
        tvProductName.setText(product.getProductName());
        tvDescription.setText(product.getDescription());

        // 设置还款期数 Spinner
        setupTermSpinner();

        // 绑定贷款方案列表
        bindOptions();
    }

    private void setupTermSpinner() {

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