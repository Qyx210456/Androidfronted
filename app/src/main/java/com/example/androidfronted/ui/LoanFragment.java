package com.example.androidfronted.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.androidfronted.R;

public class LoanFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 直接返回布局视图
        return inflater.inflate(R.layout.fragment_loan_manage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // 设置申请记录点击事件
        view.findViewById(R.id.item_application).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new com.example.androidfronted.ui.loan.ApplicationRecordsFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
        
        // 设置其他项目点击事件
        view.findViewById(R.id.item_loans).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new com.example.androidfronted.ui.loan.LoanOrdersFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
        
        view.findViewById(R.id.item_repayment_history).setOnClickListener(v -> {
            android.widget.Toast.makeText(getContext(), "还款记录功能待实现", android.widget.Toast.LENGTH_SHORT).show();
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 恢复显示底部导航栏
        if (getActivity() instanceof com.example.androidfronted.ui.MainActivity) {
            ((com.example.androidfronted.ui.MainActivity) getActivity()).setBottomNavigationVisible(true);
        }
    }
}