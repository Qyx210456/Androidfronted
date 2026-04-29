package com.example.androidfronted.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidfronted.R;
import com.example.androidfronted.viewmodel.auth.MyBankCardsViewModel;

public class BankCardAdapter extends RecyclerView.Adapter<BankCardAdapter.ViewHolder> {

    private java.util.List<MyBankCardsViewModel.BankCardItem> items;

    public BankCardAdapter() {
        this.items = new java.util.ArrayList<>();
    }

    public void setItems(java.util.List<MyBankCardsViewModel.BankCardItem> items) {
        this.items = items != null ? items : new java.util.ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bank_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MyBankCardsViewModel.BankCardItem item = items.get(position);
        
        String cardNumber = item.getCardNumber();
        if (cardNumber != null && cardNumber.length() >= 16) {
            cardNumber = cardNumber.replaceAll("\\s", "");
            holder.tvCardNum1.setText(cardNumber.substring(0, 4));
            holder.tvCardNum2.setText(cardNumber.substring(4, 8));
            holder.tvCardNum3.setText(cardNumber.substring(8, 12));
            holder.tvCardNum4.setText(cardNumber.substring(12, 16));
        } else if (cardNumber != null) {
            holder.tvCardNum1.setText("****");
            holder.tvCardNum2.setText("****");
            holder.tvCardNum3.setText("****");
            holder.tvCardNum4.setText(cardNumber.length() > 4 ? cardNumber.substring(cardNumber.length() - 4) : "****");
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCardNum1;
        TextView tvCardNum2;
        TextView tvCardNum3;
        TextView tvCardNum4;
        TextView tvCardType;
        ImageView ivBankLogo;
        ImageView ivChip;
        TextView tvCardholderName;
        TextView tvExpiryDate;

        ViewHolder(View itemView) {
            super(itemView);
            tvCardNum1 = itemView.findViewById(R.id.tv_card_num_1);
            tvCardNum2 = itemView.findViewById(R.id.tv_card_num_2);
            tvCardNum3 = itemView.findViewById(R.id.tv_card_num_3);
            tvCardNum4 = itemView.findViewById(R.id.tv_card_num_4);
            tvCardType = itemView.findViewById(R.id.tv_card_type);
            ivBankLogo = itemView.findViewById(R.id.iv_bank_logo);
            ivChip = itemView.findViewById(R.id.iv_chip);
            tvCardholderName = itemView.findViewById(R.id.tv_cardholder_name);
            tvExpiryDate = itemView.findViewById(R.id.tv_expiry_date);
        }
    }
}
