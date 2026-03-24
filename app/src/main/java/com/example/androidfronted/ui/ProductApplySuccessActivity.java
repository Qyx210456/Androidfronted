package com.example.androidfronted.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androidfronted.R;

/**
 * 贷款申请成功页面
 * - 提供两个返回选项：
 *   1. 返回产品列表（根据来源决定是首页还是全部产品页）
 *   2. 返回首页
 */
public class ProductApplySuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_apply_success);

        findViewById(R.id.btnBackToList).setOnClickListener(v -> goBackToList());
    }

    private void goBackToList() {
        String from = getIntent().getStringExtra("from");
        Intent intent;
        if ("home".equals(from)) {
            // 从首页进入的，返回首页
            intent = new Intent(this, MainActivity.class);
        } else {
            // 从产品列表进入的，返回全部产品页
            intent = new Intent(this, ProductAllActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}