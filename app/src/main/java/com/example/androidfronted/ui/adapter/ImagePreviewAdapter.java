package com.example.androidfronted.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.androidfronted.R;
import com.example.androidfronted.utils.ImageLoader;
import com.example.androidfronted.utils.ImageUrlHelper;
import com.github.chrisbanes.photoview.PhotoView;
import java.util.ArrayList;
import java.util.List;

public class ImagePreviewAdapter extends RecyclerView.Adapter<ImagePreviewAdapter.ViewHolder> {

    private List<String> imageUrls = new ArrayList<>();

    public void setImageUrls(List<String> urls) {
        this.imageUrls = urls != null ? urls : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image_preview, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        
        if (imageUrl != null && !imageUrl.isEmpty()) {
            String fullUrl = imageUrl.startsWith("http") ? imageUrl : ImageUrlHelper.getFullImageUrl(imageUrl);
            
            Glide.with(holder.photoView.getContext())
                    .load(fullUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .error(R.drawable.ic_error)
                    .into(holder.photoView);
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        Glide.with(holder.photoView.getContext()).clear(holder.photoView);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        PhotoView photoView;

        ViewHolder(View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.photo_view);
        }
    }
}
