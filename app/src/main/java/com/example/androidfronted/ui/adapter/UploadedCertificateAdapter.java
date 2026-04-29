package com.example.androidfronted.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.androidfronted.R;
import com.example.androidfronted.ui.ImagePreviewActivity;
import com.example.androidfronted.utils.ImageLoader;
import com.example.androidfronted.utils.ImageUrlHelper;
import java.util.ArrayList;
import java.util.List;

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

    private List<CertificateItem> items;

    public UploadedCertificateAdapter() {
        this.items = new ArrayList<>();
    }

    public void setItems(List<CertificateItem> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    public List<String> getAllImageUrls() {
        List<String> urls = new ArrayList<>();
        for (CertificateItem item : items) {
            if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
                urls.add(ImageUrlHelper.getFullImageUrl(item.getImagePath()));
            }
        }
        return urls;
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
            String imageUrl = ImageUrlHelper.getFullImageUrl(item.getImagePath());
            android.util.Log.d("UploadedCertificateAdapter", "Loading image: " + imageUrl);
            ImageLoader.loadImage(holder.itemView.getContext(), imageUrl, holder.ivImage);
            
            holder.ivImage.setOnClickListener(v -> {
                Context context = holder.itemView.getContext();
                List<String> allUrls = getAllImageUrls();
                int currentPosition = allUrls.indexOf(imageUrl);
                if (currentPosition < 0) currentPosition = 0;
                
                Intent intent = new Intent(context, ImagePreviewActivity.class);
                intent.putStringArrayListExtra(ImagePreviewActivity.EXTRA_IMAGE_URLS, new ArrayList<>(allUrls));
                intent.putExtra(ImagePreviewActivity.EXTRA_CURRENT_POSITION, currentPosition);
                context.startActivity(intent);
            });
        } else {
            android.util.Log.d("UploadedCertificateAdapter", "No image path available");
            holder.ivImage.setImageResource(R.drawable.ic_profile_menu_info_upload_picture);
            holder.ivImage.setAlpha(0.3f);
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.with(holder.itemView.getContext()).clear(holder.ivImage);
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
