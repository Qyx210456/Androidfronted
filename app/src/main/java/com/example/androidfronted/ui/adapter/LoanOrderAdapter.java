package com.example.androidfronted.ui.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidfronted.R;
import com.example.androidfronted.data.local.entity.LoanOrderEntity;
import java.text.DecimalFormat;
import java.util.List;

public class LoanOrderAdapter extends RecyclerView.Adapter<LoanOrderAdapter.LoanOrderViewHolder> {
    private List<LoanOrderEntity> orderList;
    private final Context context;
    private final OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(LoanOrderEntity order);
    }

    public LoanOrderAdapter(Context context, OnOrderClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setOrderList(List<LoanOrderEntity> orderList) {
        this.orderList = orderList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LoanOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_loan_order, parent, false);
        return new LoanOrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LoanOrderViewHolder holder, int position) {
        LoanOrderEntity order = orderList.get(position);
        
        // 设置产品名称（从缓存获取，如果没有则使用默认名称）
        String productName = order.getProductName();
        if (productName != null && !productName.isEmpty()) {
            holder.tvProductName.setText(productName);
        } else {
            holder.tvProductName.setText("贷款产品名称");  // 默认名称
        }
        
        // 设置贷款金额
        DecimalFormat decimalFormat = new DecimalFormat("#,##0");
        String formattedAmount = "¥" + decimalFormat.format(order.getLoanAmount());
        holder.tvLoanAmountValue.setText(formattedAmount);
        
        // 生成订单编号
        String orderNumber = generateOrderNumber(order);
        holder.tvOrderNumberValue.setText(orderNumber);
        
        // 设置订单生成时间
        holder.tvOrderTimeValue.setText(order.getStartTime());
        
        // 根据订单状态设置样式
        setOrderStatus(holder, order);
        
        // 设置点击事件 - 点击整个卡片
        holder.rootView.setOnClickListener(v -> listener.onOrderClick(order));
    }

    @Override
    public int getItemCount() {
        return orderList != null ? orderList.size() : 0;
    }

    private String generateOrderNumber(LoanOrderEntity order) {
        // 从startTime中提取日期和小时，格式为YYYYMMDDHH
        String startTime = order.getStartTime();
        String dateTimePart = startTime.replaceAll("[^0-9]", "").substring(0, 10);
        // 组合订单编号：LN + 日期时间 + 订单ID
        return "LN" + dateTimePart + order.getId();
    }

    private void setOrderStatus(LoanOrderViewHolder holder, LoanOrderEntity order) {
        String status = order.getStatus();
        int term = order.getTerm();
        int currentTerm = order.getCurrentTerm();
        int overdueDays = order.getOverdueDays();
        String nextRepaymentDate = order.getNextRepaymentDate();
        
        switch (status) {
            case "正常":
                // 还款中状态
                holder.ivStatusIcon.setVisibility(View.GONE);  // 还款中不显示图标
                holder.tvOrderStatus.setText("已还 " + currentTerm + "/" + term + " 期");
                holder.statusContainer.setBackgroundResource(R.drawable.bg_loan_order_corner_status_normal);
                holder.tvOrderStatus.setTextColor(context.getResources().getColor(R.color.order_status_normal_text));
                
                // 设置日历图标
                holder.ivTimeIcon.setImageResource(R.drawable.ic_loan_order_date_upcoming);
                
                // 设置时间标签和值（从缓存获取，如果没有则使用默认值）
                holder.tvTimeLabel.setText("下次还款日期：");
                if (nextRepaymentDate != null && !nextRepaymentDate.isEmpty()) {
                    holder.tvTimeValue.setText(nextRepaymentDate);
                } else {
                    holder.tvTimeValue.setText("加载中...");  // 默认值
                }
                holder.tvTimeValue.setTextColor(0xFF2196F3); // #2196F3
                
                // 贷款金额颜色
                holder.tvLoanAmountValue.setTextColor(0xFF4EACFB);
                break;
                
            case "已逾期":
                // 已逾期状态
                holder.ivStatusIcon.setVisibility(View.VISIBLE);  // 显示警告图标
                holder.ivStatusIcon.setImageResource(R.drawable.ic_loan_order_warning_overdue);
                holder.tvOrderStatus.setText("逾期 " + overdueDays + " 天");
                holder.statusContainer.setBackgroundResource(R.drawable.bg_loan_order_corner_status_overdue);
                holder.tvOrderStatus.setTextColor(context.getResources().getColor(R.color.order_status_overdue_text));
                
                // 设置日历图标
                holder.ivTimeIcon.setImageResource(R.drawable.ic_loan_order_calendar_overdue);
                
                // 设置时间标签和值（从缓存获取，如果没有则使用默认值）
                holder.tvTimeLabel.setText("下次还款日期：");
                if (nextRepaymentDate != null && !nextRepaymentDate.isEmpty()) {
                    holder.tvTimeValue.setText(nextRepaymentDate + " (已逾期)");
                } else {
                    holder.tvTimeValue.setText("已逾期");
                }
                holder.tvTimeValue.setTextColor(0xFFF44336); // #F44336
                
                // 贷款金额颜色
                holder.tvLoanAmountValue.setTextColor(0xFFF44336);
                break;
                
            case "已完成":
                // 已结清状态
                holder.ivStatusIcon.setVisibility(View.VISIBLE);  // 显示对勾图标
                holder.ivStatusIcon.setImageResource(R.drawable.ic_loan_order_tick_cleared);
                holder.tvOrderStatus.setText("已结清");
                holder.statusContainer.setBackgroundResource(R.drawable.bg_loan_order_corner_status_completed);
                holder.tvOrderStatus.setTextColor(context.getResources().getColor(R.color.order_status_completed_text));
                
                // 设置日历图标
                holder.ivTimeIcon.setImageResource(R.drawable.ic_loan_order_schedule_done);
                
                // 设置时间标签和值（从缓存获取，如果没有则使用默认值）
                holder.tvTimeLabel.setText("结清日期：");
                if (nextRepaymentDate != null && !nextRepaymentDate.isEmpty()) {
                    holder.tvTimeValue.setText(nextRepaymentDate);
                } else {
                    holder.tvTimeValue.setText("已结清");
                }
                holder.tvTimeValue.setTextColor(0xFF4CAF50); // #4CAF50
                
                // 贷款金额颜色
                holder.tvLoanAmountValue.setTextColor(0xFF4CAF50);
                break;
                
            default:
                // 默认状态
                holder.ivStatusIcon.setVisibility(View.GONE);  // 默认不显示图标
                holder.tvOrderStatus.setText(status);
                holder.statusContainer.setBackgroundResource(R.drawable.bg_loan_order_corner_status_normal);
                holder.tvOrderStatus.setTextColor(context.getResources().getColor(R.color.order_status_normal_text));
                
                holder.ivTimeIcon.setImageResource(R.drawable.ic_loan_order_date_upcoming);
                holder.tvTimeLabel.setText("下次还款：");
                if (nextRepaymentDate != null && !nextRepaymentDate.isEmpty()) {
                    holder.tvTimeValue.setText(nextRepaymentDate);
                } else {
                    holder.tvTimeValue.setText("加载中...");
                }
                holder.tvTimeValue.setTextColor(0xFF2196F3);
                holder.tvLoanAmountValue.setTextColor(0xFF4EACFB);
        }
    }

    static class LoanOrderViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout rootView;
        ImageView ivProductIcon;
        TextView tvProductName;
        LinearLayout statusContainer;
        ImageView ivStatusIcon;
        TextView tvOrderStatus;
        TextView tvLoanAmountValue;
        ImageView ivTimeIcon;
        TextView tvTimeLabel;
        TextView tvTimeValue;
        TextView tvOrderNumberValue;
        TextView tvOrderTimeValue;

        LoanOrderViewHolder(@NonNull View itemView) {
            super(itemView);
            rootView = (ConstraintLayout) itemView;
            ivProductIcon = itemView.findViewById(R.id.iv_product_icon);
            tvProductName = itemView.findViewById(R.id.tv_product_name);
            statusContainer = itemView.findViewById(R.id.status_container);
            ivStatusIcon = itemView.findViewById(R.id.iv_status_icon);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
            tvLoanAmountValue = itemView.findViewById(R.id.tv_loan_amount_value);
            ivTimeIcon = itemView.findViewById(R.id.iv_time_icon);
            tvTimeLabel = itemView.findViewById(R.id.tv_time_label);
            tvTimeValue = itemView.findViewById(R.id.tv_time_value);
            tvOrderNumberValue = itemView.findViewById(R.id.tv_order_number_value);
            tvOrderTimeValue = itemView.findViewById(R.id.tv_order_time_value);
        }
    }
}