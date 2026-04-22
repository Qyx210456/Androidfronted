package com.example.androidfronted.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        holder.tvBankName.setText(item.getBankName());
        holder.tvCardNumber.setText(item.getCardNumber());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBankName;
        TextView tvCardNumber;

        ViewHolder(View itemView) {
            super(itemView);
            tvBankName = itemView.findViewById(R.id.tv_bank_name);
            tvCardNumber = itemView.findViewById(R.id.tv_bank_card_number);
        }
    }
}
