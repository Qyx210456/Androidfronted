package com.example.androidfronted;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

public class ProductApplyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_apply);

        // 初始化返回按钮
        View backBtn = findViewById(R.id.apply_btn_back);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }

        // 初始化证件类型下拉框
        Spinner spinnerIdType = findViewById(R.id.spinner_id_type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.id_types,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerIdType.setAdapter(adapter);
    }
}