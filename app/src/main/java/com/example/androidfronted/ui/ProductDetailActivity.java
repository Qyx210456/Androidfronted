package com.example.androidfronted;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 产品详情页面 Activity
 * - 加载 product_detail.xml 布局
 * - 点击左上角返回按钮，关闭当前页面，返回到 MainActivity（主页）
 */
public class ProductDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_detail); // 确保布局文件名为 product_detail.xml

        // 查找“去申请”按钮
        Button btnApply = findViewById(R.id.btn_apply);
        if (btnApply != null) {
            btnApply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 跳转到贷款申请页面
                    Intent intent = new Intent(ProductDetailActivity.this, ProductApplyActivity.class);
                    startActivity(intent);
                }
            });
        }

        // 可选：添加返回按钮逻辑（如果 layout 中有）
        View backBtn = findViewById(R.id.btn_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }
    }
}
