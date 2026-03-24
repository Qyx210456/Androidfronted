package com.example.androidfronted.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidfronted.R;

public class UploadedCertificateAdapter extends RecyclerView.Adapter<UploadedCertificateAdapter.ViewHolder> {
    
    public static class CertificateItem {
        private String type;
        private String imagePath;

        public CertificateItem(String type, String imagePath) {
            this.type = type;
            this.imagePath = imagePath;
        }

        public String getType() {
            return type;
        }

        public String getImagePath() {
            return imagePath;
        }
    }

    private java.util.List<CertificateItem> items;

    public UploadedCertificateAdapter() {
        this.items = new java.util.ArrayList<>();
    }

    public void setItems(java.util.List<CertificateItem> items) {
        this.items = items != null ? items : new java.util.ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_uploaded_certificate, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CertificateItem item = items.get(position);
        holder.tvType.setText(item.getType());
        
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            holder.ivImage.setImageResource(R.drawable.info_upload_picture);
            holder.ivImage.setAlpha(1.0f);
        } else {
            holder.ivImage.setImageResource(R.drawable.info_upload_picture);
            holder.ivImage.setAlpha(0.3f);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvType;

        ViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_uploaded_image);
            tvType = itemView.findViewById(R.id.tv_certificate_type);
        }
    }
}
