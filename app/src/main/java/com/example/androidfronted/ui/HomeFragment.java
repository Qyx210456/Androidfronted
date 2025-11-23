package com.example.androidfronted.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidfronted.R;
import com.example.androidfronted.data.model.LoanProduct;
import com.example.androidfronted.data.model.LoanProductResponse;
import com.example.androidfronted.data.repository.LoanProductRepository;
import com.example.androidfronted.ui.adapter.LoanProductAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页 Fragment：展示贷款产品列表
 * - 启动时自动加载数据
 * - 使用 LoanProductRepository 获取带 Token 的数据
 */
public class HomeFragment extends Fragment {

    private RecyclerView rvProducts;
    private LoanProductAdapter adapter;
    private LoanProductRepository repository;
    private List<LoanProduct> productList;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new LoanProductRepository(requireContext());
        productList = new ArrayList<>();
        adapter = new LoanProductAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvProducts = view.findViewById(R.id.rv_loan_products); // 确保 ID 一致
        rvProducts.setLayoutManager(new LinearLayoutManager(getContext()));
        rvProducts.setAdapter(adapter);

        loadLoanProducts();
    }

    private void loadLoanProducts() {
        repository.getLoanProducts(new LoanProductRepository.AuthCallback<>() { // 类型推断
            @Override
            public void onSuccess(LoanProductResponse response) {
                productList.clear();
                productList.addAll(response.getData());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), "加载失败: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}