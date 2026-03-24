package com.example.androidfronted.ui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidfronted.R;
import com.example.androidfronted.data.model.LoanProduct;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class HotProductAdapter extends RecyclerView.Adapter<HotProductAdapter.ViewHolder> {

    private static final String TAG = "HotProductAdapter";

    private List<LoanProduct> products = new ArrayList<>();
    private OnApplyClickListener onApplyClickListener;

    public void setProducts(List<LoanProduct> products) {
        this.products = products != null ? products : new ArrayList<>();
        notifyDataSetChanged();
        Log.d(TAG, "设置产品数据: " + this.products.size() + "个");
    }

    public List<LoanProduct> getProducts() {
        return products;
    }

    public void setOnApplyClickListener(OnApplyClickListener listener) {
        this.onApplyClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hot_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (products.isEmpty()) return;

        int actualPosition = position % products.size();
        LoanProduct product = products.get(actualPosition);

        try {
            holder.tvProductName.setText(product.getProductName());

            // 显示最高额度
            double maxAmount = product.getMaxAmount();
            if (maxAmount > 0) {
                String formattedAmount = String.format("¥%,.0f", maxAmount);
                holder.tvLimitValue.setText(formattedAmount);
            } else {
                holder.tvLimitValue.setText("--");
            }

            // 计算最低利率
            double minRate = Double.MAX_VALUE;
            if (product.getOptions() != null && !product.getOptions().isEmpty()) {
                for (LoanProduct.LoanOption option : product.getOptions()) {
                    if (option.getInterestRate() < minRate) {
                        minRate = option.getInterestRate();
                    }
                }
                String rateText = new DecimalFormat("#.##%").format(minRate);
                holder.tvInterestRate.setText(rateText);
            } else {
                holder.tvInterestRate.setText("--");
            }

            // 点击事件
            holder.btnApplyNow.setOnClickListener(v -> {
                if (onApplyClickListener != null) {
                    onApplyClickListener.onApplyClick(product);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "绑定数据失败: " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return products.size() > 1 ? Integer.MAX_VALUE : products.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName;
        TextView tvLimitValue;
        TextView tvInterestRate;
        Button btnApplyNow;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvLimitValue = itemView.findViewById(R.id.tvLimitValue);
            tvInterestRate = itemView.findViewById(R.id.tvInterestRate);
            btnApplyNow = itemView.findViewById(R.id.btnApplyNow);
        }
    }

    public interface OnApplyClickListener {
        void onApplyClick(LoanProduct product);
    }
}