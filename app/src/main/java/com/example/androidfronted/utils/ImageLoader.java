package com.example.androidfronted.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.androidfronted.R;

/**
 * 图片加载工具类
 * 使用Glide进行图片加载
 * 特性：
 * 1. 三级缓存：内存缓存 → 磁盘缓存 → 网络请求
 * 2. 占位图和错误图处理
 * 3. 平滑过渡动画
 * 4. 自动优化图片尺寸
 * 5. RecyclerView优化
 */
public class ImageLoader {
    private static final String TAG = "ImageLoader";

    /**
     * 加载图片（使用默认占位图和错误图）
     * @param context 上下文
     * @param imageUrl 图片URL
     * @param imageView 目标ImageView
     */
    public static void loadImage(Context context, String imageUrl, ImageView imageView) {
        loadImage(context, imageUrl, imageView, R.drawable.ic_placeholder, R.drawable.ic_error);
    }

    /**
     * 加载图片（自定义占位图）
     * @param context 上下文
     * @param imageUrl 图片URL
     * @param imageView 目标ImageView
     * @param placeholderResId 占位图资源ID
     */
    public static void loadImage(Context context, String imageUrl, ImageView imageView, int placeholderResId) {
        loadImage(context, imageUrl, imageView, placeholderResId, R.drawable.ic_error);
    }

    /**
     * 加载图片（完整参数）
     * @param context 上下文
     * @param imageUrl 图片URL
     * @param imageView 目标ImageView
     * @param placeholderResId 占位图资源ID
     * @param errorResId 错误图资源ID
     */
    public static void loadImage(Context context, String imageUrl, ImageView imageView, 
                           int placeholderResId, int errorResId) {
        if (context == null || imageView == null) {
            Log.w(TAG, "Context or ImageView is null");
            return;
        }

        if (imageUrl == null || imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(placeholderResId)
                    .into(imageView);
            return;
        }

        try {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(placeholderResId)
                    .error(errorResId)
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // 缓存所有版本
                    .transition(DrawableTransitionOptions.withCrossFade(300)) // 300ms淡入动画
                    .into(imageView);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load image: " + imageUrl, e);
            Glide.with(context)
                    .load(errorResId)
                    .into(imageView);
        }
    }

    /**
     * 加载圆形图片（用于头像）
     * @param context 上下文
     * @param imageUrl 图片URL
     * @param imageView 目标ImageView
     */
    public static void loadCircleImage(Context context, String imageUrl, ImageView imageView) {
        loadCircleImage(context, imageUrl, imageView, R.drawable.ic_avatar_placeholder, R.drawable.ic_avatar_error);
    }

    /**
     * 加载圆形图片（自定义占位图）
     * @param context 上下文
     * @param imageUrl 图片URL
     * @param imageView 目标ImageView
     * @param placeholderResId 占位图资源ID
     * @param errorResId 错误图资源ID
     */
    public static void loadCircleImage(Context context, String imageUrl, ImageView imageView, 
                                   int placeholderResId, int errorResId) {
        if (context == null || imageView == null) {
            Log.w(TAG, "Context or ImageView is null");
            return;
        }

        if (imageUrl == null || imageUrl.isEmpty()) {
            Glide.with(context)
                    .load(placeholderResId)
                    .circleCrop()
                    .into(imageView);
            return;
        }

        try {
            Glide.with(context)
                    .load(imageUrl)
                    .placeholder(placeholderResId)
                    .error(errorResId)
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .transition(DrawableTransitionOptions.withCrossFade(300))
                    .into(imageView);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load circle image: " + imageUrl, e);
            Glide.with(context)
                    .load(errorResId)
                    .circleCrop()
                    .into(imageView);
        }
    }

    /**
     * 清除内存缓存
     * @param context 上下文
     */
    public static void clearMemoryCache(Context context) {
        if (context != null) {
            Glide.get(context).clearMemory();
        }
    }

    /**
     * 清除磁盘缓存（异步）
     * @param context 上下文
     */
    public static void clearDiskCache(Context context) {
        if (context != null) {
            Glide.get(context).clearDiskCache();
        }
    }

    /**
     * 暂停所有图片加载（用于优化性能）
     * @param context 上下文
     */
    public static void pauseRequests(Context context) {
        if (context != null) {
            Glide.with(context).pauseRequests();
        }
    }

    /**
     * 恢复图片加载
     * @param context 上下文
     */
    public static void resumeRequests(Context context) {
        if (context != null) {
            Glide.with(context).resumeRequests();
        }
    }
}
