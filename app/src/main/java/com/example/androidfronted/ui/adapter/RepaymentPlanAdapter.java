package com.example.androidfronted.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidfronted.R;
import com.example.androidfronted.data.local.entity.RepaymentPlanEntity;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class RepaymentPlanAdapter extends RecyclerView.Adapter<RepaymentPlanAdapter.ViewHolder> {
    private List<RepaymentPlanEntity> planList;
    private final Context context;

    public RepaymentPlanAdapter(Context context) {
        this.context = context;
        this.planList = new ArrayList<>();
    }

    public void setPlanList(List<RepaymentPlanEntity> planList) {
        this.planList = planList != null ? planList : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_repayment_plan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RepaymentPlanEntity plan = planList.get(position);
        
        holder.tvPeriod.setText("第" + plan.getTerm() + "期");
        
        DecimalFormat df = new DecimalFormat("#,##0.00");
        holder.tvAmount.setText("¥ " + df.format(plan.getTotalAmount()));
        holder.tvPrincipalDue.setText(df.format(plan.getPrincipal()));
        holder.tvInterestDue.setText(df.format(plan.getInterest()));
        
        holder.tvPrincipalRemaining.setText(df.format(plan.getRemainingPrincipal()));
        holder.tvInterestRemaining.setText(df.format(plan.getRemainingInterest()));
        
        if (plan.getDueDate() != null && !plan.getDueDate().isEmpty()) {
            holder.tvRepayDate.setText(plan.getDueDate());
        } else {
            holder.tvRepayDate.setText("--");
        }
        
        if (plan.getActualPayDate() != null && !plan.getActualPayDate().isEmpty() 
                && !"null".equalsIgnoreCase(plan.getActualPayDate())) {
            holder.llActualPayDate.setVisibility(View.VISIBLE);
            holder.tvActualPayDate.setText(plan.getActualPayDate());
        } else {
            holder.llActualPayDate.setVisibility(View.GONE);
        }
        
        bindStatusStyle(holder, plan.getStatus());
    }

    @Override
    public int getItemCount() {
        return planList.size();
    }

    private void bindStatusStyle(ViewHolder holder, String status) {
        if (status == null) {
            status = "未还";
        }
        
        switch (status) {
            case "已还":
                holder.cardContainer.setBackgroundResource(R.drawable.bg_repayment_plan_item_card_repaid);
                holder.tvStatus.setBackgroundResource(R.drawable.bg_repayment_plan_corner_status_repaid);
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.repayment_plan_text_repaid));
                holder.tvAmount.setTextColor(context.getResources().getColor(R.color.repayment_plan_text_repaid));
                break;
            case "逾期":
                holder.cardContainer.setBackgroundResource(R.drawable.bg_repayment_plan_item_card_overdue);
                holder.tvStatus.setBackgroundResource(R.drawable.bg_repayment_plan_corner_status_overdue);
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.repayment_plan_text_overdue));
                holder.tvAmount.setTextColor(context.getResources().getColor(R.color.repayment_plan_text_overdue));
                break;
            case "未还":
            default:
                holder.cardContainer.setBackgroundResource(R.drawable.bg_repayment_plan_item_card_unpaid);
                holder.tvStatus.setBackgroundResource(R.drawable.bg_repayment_plan_corner_status_unpaid);
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.repayment_plan_text_unpaid));
                holder.tvAmount.setTextColor(context.getResources().getColor(R.color.repayment_plan_text_unpaid));
                break;
        }
        holder.tvStatus.setText(status);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout cardContainer;
        TextView tvPeriod;
        TextView tvStatus;
        TextView tvAmount;
        TextView tvRepayDate;
        LinearLayout llActualPayDate;
        TextView tvActualPayDate;
        TextView tvPrincipalDue;
        TextView tvInterestDue;
        TextView tvPrincipalRemaining;
        TextView tvInterestRemaining;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.card_container);
            tvPeriod = itemView.findViewById(R.id.tv_period);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvRepayDate = itemView.findViewById(R.id.tv_repay_date);
            llActualPayDate = itemView.findViewById(R.id.ll_actual_pay_date);
            tvActualPayDate = itemView.findViewById(R.id.tv_actual_pay_date);
            tvPrincipalDue = itemView.findViewById(R.id.tv_principal_due);
            tvInterestDue = itemView.findViewById(R.id.tv_interest_due);
            tvPrincipalRemaining = itemView.findViewById(R.id.tv_principal_remaining);
            tvInterestRemaining = itemView.findViewById(R.id.tv_interest_remaining);
        }
    }
}
