package com.example.androidfronted.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.example.androidfronted.R;
import com.example.androidfronted.ui.smartcustomerservice.SmartCustomerServiceFragment;

public class SmartCustomerServiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_smart_customer_service);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, SmartCustomerServiceFragment.Companion.newInstance(false))
                .commit();
        }
    }
}
