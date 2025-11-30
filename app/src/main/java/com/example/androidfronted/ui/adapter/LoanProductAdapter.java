package com.example.androidfronted.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidfronted.R;
import com.example.androidfronted.data.model.LoanProduct;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * 贷款产品列表适配器
 * <p>
 * 用于在首页 RecyclerView 中动态展示贷款产品卡片。
 * </p>
 */
public class LoanProductAdapter extends RecyclerView.Adapter<LoanProductAdapter.ViewHolder> {

    /**
     * 贷款产品数据列表
     */
    private List<LoanProduct> products = new ArrayList<>();

    /**
     * “了解详情”按钮点击事件监听器
     */
    private OnLearnMoreClickListener onLearnMoreClickListener;

    /**
     * 设置贷款产品数据列表并刷新界面
     *
     * @param products 新的贷款产品列表
     */
    public void setProducts(List<LoanProduct> products) {
        this.products = products != null ? products : new ArrayList<>();
        notifyDataSetChanged();
    }

    /**
     * 设置“了解详情”按钮的点击监听器
     *
     * @param listener 点击监听器
     */
    public void setOnLearnMoreClickListener(OnLearnMoreClickListener listener) {
        this.onLearnMoreClickListener = listener;
    }

    /**
     * 创建 ViewHolder 实例，加载 item_loan_product 布局
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_loan_product, parent, false);
        return new ViewHolder(view);
    }

    /**
     * 绑定数据到指定位置的 ViewHolder
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LoanProduct product = products.get(position);
        holder.bind(product);
    }

    /**
     * 返回贷款产品总数
     */
    @Override
    public int getItemCount() {
        return products.size();
    }

    /**
     * 贷款产品项的 ViewHolder，持有所有子控件引用
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        // 产品名称
        TextView tvProductName;
        // 产品描述
        TextView tvDescription;
        // 最高额度
        TextView tvLimitValue;
        // 贷款期限
        TextView tvTermValue;
        // 贷款用途
        TextView tvLoanUsage;
        // “了解详情”按钮
        TextView btnLearnMore;

        /**
         * 构造函数：查找并绑定所有子视图
         */
        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLimitValue = itemView.findViewById(R.id.tvLimitValue);
            tvTermValue = itemView.findViewById(R.id.tvTermValue);
            tvLoanUsage = itemView.findViewById(R.id.tvLoanUsage);
            btnLearnMore = itemView.findViewById(R.id.btnLearnMore);
        }

        /**
         * 将 LoanProduct 数据绑定到 UI 控件
         *
         * @param product 要绑定的贷款产品对象
         */
        void bind(LoanProduct product) {
            // 绑定产品名称
            tvProductName.setText(product.getProductName());

            // 绑定描述
            tvDescription.setText(product.getDescription());

            // 绑定贷款用途
            tvLoanUsage.setText(product.getLoanUsage());

            //  计算最高额度：遍历所有 options，取 loanAmount 最大值
            double maxAmount = 0;
            if (product.getOptions() != null && !product.getOptions().isEmpty()) {
                for (LoanProduct.LoanOption option : product.getOptions()) {
                    if (option.getLoanAmount() > maxAmount) {
                        maxAmount = option.getLoanAmount();
                    }
                }
                String formattedAmount = String.format("¥%,.0f", maxAmount);
                tvLimitValue.setText(formattedAmount);
            } else {
                tvLimitValue.setText("--");
            }

            //展示所有 terms：如 "3,6,12,24"
            if (product.getTerms() != null && !product.getTerms().isEmpty()) {
                String termsText = android.text.TextUtils.join(",", product.getTerms());
                tvTermValue.setText(termsText);
            } else {
                tvTermValue.setText("--");
            }

            // 设置“了解详情”按钮点击事件
            btnLearnMore.setOnClickListener(v -> {
                if (onLearnMoreClickListener != null) {
                    onLearnMoreClickListener.onLearnMoreClick(product);
                }
            });
        }
    }

    /**
     * “了解详情”按钮点击事件回调接口
     */
    public interface OnLearnMoreClickListener {
        /**
         * 当用户点击某产品的“了解详情”按钮时触发
         *
         * @param product 被点击的贷款产品
         */
        void onLearnMoreClick(LoanProduct product);
    }
}


