package com.example.androidfronted.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidfronted.R;
import com.example.androidfronted.data.model.LoanProduct;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 贷款产品列表适配器
 * - 展示：产品名、类型标签、描述、最高额度、最低利率、期数范围
 * - 卡片背景/图标/标签/额度数值颜色随 productType（个人消费/农业生产/企业经营）切换
 * - 点击“了解详情”跳转到详情页
 */
public class LoanProductAdapter extends RecyclerView.Adapter<LoanProductAdapter.ViewHolder> {

    private List<LoanProduct> products = new ArrayList<>();
    private OnLearnMoreClickListener onLearnMoreClickListener;

    public void setProducts(List<LoanProduct> products) {
        this.products = products != null ? products : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setOnLearnMoreClickListener(OnLearnMoreClickListener listener) {
        this.onLearnMoreClickListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_loan_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LoanProduct product = products.get(position);
        holder.bind(product);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

     class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvProductName, tvDescription, tvLimitValue, tvTermValue, tvMinRateValue, tvProductTag;
        ImageView ivProductIcon;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLimitValue = itemView.findViewById(R.id.tvLimitValue);
            tvTermValue = itemView.findViewById(R.id.tvTermValue);
            tvMinRateValue = itemView.findViewById(R.id.tvMinInterestRate);
            tvProductTag = itemView.findViewById(R.id.tvProductTag);
            ivProductIcon = itemView.findViewById(R.id.ivProductIcon);
        }

        void bind(LoanProduct product) {
            tvProductName.setText(product.getProductName());
            tvDescription.setText(product.getDescription());
            applyTypeStyle(itemView.getContext(), product);

            // 显示最高额度
            double maxAmount = product.getMaxAmount();
            tvLimitValue.setText(maxAmount > 0 ? String.format("¥%,.0f", maxAmount) : "--");

            // 计算最低利率
            double minRate = Double.MAX_VALUE;
            if (product.getOptions() != null) {
                for (LoanProduct.LoanOption opt : product.getOptions()) {
                    minRate = Math.min(minRate, opt.getInterestRate());
                }
            }
            tvMinRateValue.setText(
                    minRate < Double.MAX_VALUE ? new DecimalFormat("#.##%").format(minRate) : "--"
            );

            // 构建期数范围文本
            String termsText = "--";
            List<Integer> terms = product.getTerms();
            if (terms != null && !terms.isEmpty()) {
                int min = Collections.min(terms);
                int max = Collections.max(terms);
                termsText = (min == max) ? String.valueOf(min) : min + "-" + max;
            }
            tvTermValue.setText(termsText);

            itemView.setOnClickListener(v -> {
                if (onLearnMoreClickListener != null) {
                    onLearnMoreClickListener.onLearnMoreClick(product);
                }
            });
        }

        /**
         * 按 productType 应用类型样式（0=个人消费 1=农业生产 2=企业经营，默认个人消费）
         */
        private void applyTypeStyle(Context context, LoanProduct product) {
            int type = resolveType(product.getProductType());
            int cardBgRes, iconRes, tagBgRes, tagTextRes, amountRes;
            String tagText;
            if (type == 1) {
                cardBgRes = R.drawable.bg_loan_product_item_agriculture;
                iconRes = R.drawable.ic_item_product_sort_agriculture;
                tagBgRes = R.drawable.bg_product_tag_agriculture;
                tagTextRes = R.color.tag_agriculture_text;
                amountRes = R.color.amount_agriculture;
                tagText = "农业生产";
            } else if (type == 2) {
                cardBgRes = R.drawable.bg_loan_product_item_business;
                iconRes = R.drawable.ic_item_product_sort_business;
                tagBgRes = R.drawable.bg_product_tag_business;
                tagTextRes = R.color.tag_business_text;
                amountRes = R.color.amount_business;
                tagText = "企业经营";
            } else {
                cardBgRes = R.drawable.bg_loan_product_item_personal;
                iconRes = R.drawable.ic_item_product_sort_personal;
                tagBgRes = R.drawable.bg_product_tag_personal;
                tagTextRes = R.color.tag_personal_text;
                amountRes = R.color.amount_personal;
                tagText = "个人消费";
            }
            itemView.setBackgroundResource(cardBgRes);
            ivProductIcon.setImageResource(iconRes);
            tvProductTag.setBackgroundResource(tagBgRes);
            tvProductTag.setTextColor(ContextCompat.getColor(context, tagTextRes));
            tvProductTag.setText(tagText);
            tvLimitValue.setTextColor(ContextCompat.getColor(context, amountRes));
        }
    }

    /**
     * 将后端 productType 映射为样式索引；null 或未知类型按个人消费处理
     */
    public static int resolveType(String productType) {
        if (productType == null) {
            return 0;
        }
        switch (productType) {
            case "农业生产":
                return 1;
            case "企业经营":
                return 2;
            default:
                return 0;
        }
    }

    public interface OnLearnMoreClickListener {
        void onLearnMoreClick(LoanProduct product);
    }
}