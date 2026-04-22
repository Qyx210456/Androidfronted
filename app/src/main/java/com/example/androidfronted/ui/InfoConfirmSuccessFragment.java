package com.example.androidfronted.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.androidfronted.R;
import com.example.androidfronted.ui.base.BaseDetailFragment;
import com.example.androidfronted.ui.personalinformationinfo.CertificateOfIdFragment;
import com.example.androidfronted.ui.personalinformationinfo.CertificateOfJobFragment;
import com.example.androidfronted.ui.personalinformationinfo.CertificateOfPropertyFragment;
import com.example.androidfronted.ui.personalinformationinfo.CertificateOfThirdPartyFragment;

/**
 * 通用认证成功/提交成功页面
 * 对应布局: info_confirm_success.xml
 */
public class InfoConfirmSuccessFragment extends BaseDetailFragment {

    public static final String ARG_TARGET_FRAGMENT = "target_fragment";
    public static final int TYPE_ID = 1;
    public static final int TYPE_JOB = 2;
    public static final int TYPE_PROPERTY = 3;
    public static final int TYPE_THIRD_PARTY = 4;
    public static final int TYPE_BANK_CARD = 5;

    private int targetType = TYPE_ID;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            targetType = getArguments().getInt(ARG_TARGET_FRAGMENT, TYPE_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.info_confirm_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 绑定返回/完成按钮，根据类型跳转回对应的列表页
        view.findViewById(R.id.btnBackToInfo).setOnClickListener(v -> {
            navigateToListPage();
        });

        // 如果布局中有其他按钮（如返回首页），可在此添加
    }

    private void navigateToListPage() {
        BaseDetailFragment targetFragment = null;
        switch (targetType) {
            case TYPE_ID:
                targetFragment = new CertificateOfIdFragment();
                break;
            case TYPE_JOB:
                targetFragment = new CertificateOfJobFragment();
                break;
            case TYPE_PROPERTY:
                targetFragment = new CertificateOfPropertyFragment();
                break;
            case TYPE_THIRD_PARTY:
                targetFragment = new CertificateOfThirdPartyFragment();
                break;
            case TYPE_BANK_CARD:
                targetFragment = new MyBankCardsFragment();
                break;
        }

        if (targetFragment != null) {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.container, targetFragment)
                    .commit();
        } else {
            getParentFragmentManager().popBackStack();
        }
    }
}
