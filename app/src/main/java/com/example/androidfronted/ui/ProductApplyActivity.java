package com.example.androidfronted.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androidfronted.R;
import com.example.androidfronted.data.model.LoanProduct;
import java.text.DecimalFormat;

public class ProductApplyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_apply);

        // 返回按钮
        findViewById(R.id.apply_btn_back).setOnClickListener(v -> finish());

        // 初始化证件类型下拉框
        Spinner spinnerIdType = findViewById(R.id.spinner_id_type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.id_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIdType.setAdapter(adapter);

        // 获取传递的数据
        LoanProduct.LoanOption selectedOption = (LoanProduct.LoanOption)
                getIntent().getSerializableExtra("selected_option");
        int selectedTerm = getIntent().getIntExtra("selected_term", -1);
        long productId = getIntent().getLongExtra("product_id", -1);

        // 数据校验
        if (selectedOption == null || selectedTerm <= 0 || productId == -1) {
            Toast.makeText(this, "申请数据异常", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 绑定 UI
        bindUI(selectedOption, selectedTerm);

        // 立即申请按钮
        Button btnSubmit = findViewById(R.id.btn_apply);
        btnSubmit.setOnClickListener(v -> submitApplication(productId, selectedOption.getOptionId(), selectedTerm));
    }

    private void bindUI(LoanProduct.LoanOption option, int term) {
        TextView tvAmount = findViewById(R.id.tvSelectedAmount);
        TextView tvRate = findViewById(R.id.tvSelectedRate);
        TextView tvPeriod = findViewById(R.id.tvSelectedLoanPeriod);
        TextView tvRepay = findViewById(R.id.tvSelectedRepayType);
        TextView tvTerm = findViewById(R.id.tvSelectedTerm);

        tvAmount.setText(String.format("¥%,.0f", option.getLoanAmount()));
        tvRate.setText(new DecimalFormat("#.##%").format(option.getInterestRate()));
        tvPeriod.setText(option.getLoanPeriod() + "个月");
        tvRepay.setText(option.getRepaidType());
        tvTerm.setText(term + "期");
    }

    private void submitApplication(long productId, long optionId, int term) {
        // TODO: 实际提交申请（调用 API）
        Toast.makeText(this, "申请提交成功！", Toast.LENGTH_SHORT).show();
    }
}