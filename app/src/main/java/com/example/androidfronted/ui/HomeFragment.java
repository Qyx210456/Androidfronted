package com.example.androidfronted.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.util.Log;
import android.content.Intent;

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


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        repository = new LoanProductRepository(requireContext());
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

        //了解详情监听器
        adapter.setOnLearnMoreClickListener(product -> {
            Log.d("HomeFragment", "点击产品: " + product.getProductName());

            Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
            intent.putExtra("loan_product", product);
            startActivity(intent);
        });
        loadLoanProducts();
    }

    // HomeFragment调试信息 - 添加加载状态
    private void loadLoanProducts() {
        Log.d("HomeFragment", "开始加载贷款产品...");

        // 显示加载状态
        if (getView() != null) {
            getView().findViewById(R.id.loading_indicator).setVisibility(View.VISIBLE);
        }

        repository.getLoanProducts(new LoanProductRepository.AuthCallback<>() {
            @Override
            public void onSuccess(LoanProductResponse response) {
                Log.d("HomeFragment", "成功加载 " + response.getData().size() + " 个产品");
                adapter.setProducts(response.getData());

                // 隐藏加载状态
                if (getView() != null) {
                    getView().findViewById(R.id.loading_indicator).setVisibility(View.GONE);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e("HomeFragment", "加载失败: " + errorMessage);
                Toast.makeText(getContext(), "加载失败: " + errorMessage, Toast.LENGTH_SHORT).show();

                // 隐藏加载状态
                if (getView() != null) {
                    getView().findViewById(R.id.loading_indicator).setVisibility(View.GONE);
                }
            }
        });
    }
}