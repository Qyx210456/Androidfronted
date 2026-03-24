package com.example.androidfronted.ui.adapter;

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
import java.util.Collections;
import java.util.List;

/**
 * 贷款产品列表适配器
 * - 展示：产品名、描述、最高额度、最低利率、期数范围
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
        TextView tvProductName, tvDescription, tvLimitValue, tvTermValue, tvMinRateValue;
        Button btnLearnMore;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvLimitValue = itemView.findViewById(R.id.tvLimitValue);
            tvTermValue = itemView.findViewById(R.id.tvTermValue);
            tvMinRateValue = itemView.findViewById(R.id.tvMinInterestRate);
            btnLearnMore = itemView.findViewById(R.id.btnLearnMore);
        }

        void bind(LoanProduct product) {
            tvProductName.setText(product.getProductName());
            tvDescription.setText(product.getDescription());

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

            btnLearnMore.setOnClickListener(v -> {
                if (onLearnMoreClickListener != null) {
                    onLearnMoreClickListener.onLearnMoreClick(product);
                }
            });
        }
    }

    public interface OnLearnMoreClickListener {
        void onLearnMoreClick(LoanProduct product);
    }
}