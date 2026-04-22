package com.example.androidfronted.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.androidfronted.R;
import com.example.androidfronted.ui.base.BaseDetailFragment;

/**
 * 银行卡绑定成功页面
 * 对应布局: info_confirm_bank_cards_success.xml
 */
public class InfoConfirmBankCardsSuccessFragment extends BaseDetailFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.info_confirm_bank_cards_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btnBackToList).setOnClickListener(v -> {
            navigateToListPage();
        });
    }

    private void navigateToListPage() {
        MyBankCardsFragment targetFragment = new MyBankCardsFragment();
        getParentFragmentManager().beginTransaction()
                .replace(R.id.container, targetFragment)
                .commit();
    }
}
