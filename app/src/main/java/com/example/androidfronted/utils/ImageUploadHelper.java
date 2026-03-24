package com.example.androidfronted.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 图片上传辅助类
 * 处理图片选择、裁剪、压缩和预览功能
 */
public class ImageUploadHelper {
    private static final String TAG = "ImageUploadHelper";
    private static final int MAX_IMAGE_SIZE = 1024; // 最大图片尺寸（宽或高）
    private static final int MAX_IMAGE_QUALITY = 85; // 图片压缩质量
    private static final int MAX_IMAGE_FILE_SIZE = 2 * 1024 * 1024; // 最大文件大小 2MB

    public interface ImageUploadCallback {
        void onImageSelected(Uri imageUri);
        void onImageCompressed(File compressedFile);
        void onError(String errorMessage);
    }

    /**
     * 打开图片选择器
     * 支持从相册选择和拍照
     */
    public static void openImagePicker(Fragment fragment, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        fragment.startActivityForResult(Intent.createChooser(intent, "选择图片"), requestCode);
    }

    /**
     * 打开相机拍照
     */
    public static void openCamera(Fragment fragment, int requestCode) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * 压缩图片
     * @param context 上下文
     * @param imageUri 图片URI
     * @param callback 回调
     */
    public static void compressImage(Context context, Uri imageUri, ImageUploadCallback callback) {
        try {
            // 获取图片的原始尺寸
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageUri), null, options);

            int originalWidth = options.outWidth;
            int originalHeight = options.outHeight;

            Log.d(TAG, "原始图片尺寸: " + originalWidth + "x" + originalHeight);

            // 计算缩放比例
            int scale = 1;
            if (originalWidth > MAX_IMAGE_SIZE || originalHeight > MAX_IMAGE_SIZE) {
                scale = Math.min(
                    MAX_IMAGE_SIZE / originalWidth,
                    MAX_IMAGE_SIZE / originalHeight
                );
            }

            // 重新加载图片，应用缩放
            options.inJustDecodeBounds = false;
            options.inSampleSize = calculateInSampleSize(originalWidth, originalHeight, MAX_IMAGE_SIZE, MAX_IMAGE_SIZE);

            Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(imageUri), null, options);

            if (bitmap == null) {
                callback.onError("图片加载失败");
                return;
            }

            // 如果需要进一步缩放
            if (bitmap.getWidth() > MAX_IMAGE_SIZE || bitmap.getHeight() > MAX_IMAGE_SIZE) {
                float ratio = Math.min(
                    (float) MAX_IMAGE_SIZE / bitmap.getWidth(),
                    (float) MAX_IMAGE_SIZE / bitmap.getHeight()
                );
                int newWidth = Math.round(bitmap.getWidth() * ratio);
                int newHeight = Math.round(bitmap.getHeight() * ratio);
                bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
            }

            Log.d(TAG, "压缩后图片尺寸: " + bitmap.getWidth() + "x" + bitmap.getHeight());

            // 保存压缩后的图片
            File compressedFile = createImageFile(context);
            if (compressedFile != null) {
                FileOutputStream fos = new FileOutputStream(compressedFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, MAX_IMAGE_QUALITY, fos);
                fos.flush();
                fos.close();

                Log.d(TAG, "压缩后文件大小: " + compressedFile.length() / 1024 + "KB");

                // 如果文件仍然过大，继续压缩
                if (compressedFile.length() > MAX_IMAGE_FILE_SIZE) {
                    Log.d(TAG, "文件仍然过大，继续压缩");
                    compressFurther(compressedFile, bitmap);
                }

                callback.onImageCompressed(compressedFile);
            }

            bitmap.recycle();
            callback.onImageSelected(imageUri);

        } catch (IOException e) {
            Log.e(TAG, "图片压缩失败", e);
            callback.onError("图片压缩失败: " + e.getMessage());
        }
    }

    /**
     * 计算采样率
     */
    private static int calculateInSampleSize(int reqWidth, int reqHeight, int maxWidth, int maxHeight) {
        int inSampleSize = 1;

        if (reqHeight > maxHeight || reqWidth > maxWidth) {
            final int halfHeight = reqHeight / 2;
            final int halfWidth = reqWidth / 2;

            while ((halfHeight / inSampleSize) >= maxHeight
                    && (halfWidth / inSampleSize) >= maxWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * 进一步压缩图片
     */
    private static void compressFurther(File file, Bitmap bitmap) throws IOException {
        int quality = MAX_IMAGE_QUALITY;
        while (file.length() > MAX_IMAGE_FILE_SIZE && quality > 50) {
            quality -= 10;
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.flush();
            fos.close();
            Log.d(TAG, "继续压缩，质量: " + quality + ", 大小: " + file.length() / 1024 + "KB");
        }
    }

    /**
     * 创建临时图片文件
     */
    private static File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir != null && !storageDir.exists()) {
            storageDir.mkdirs();
        }
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    /**
     * 删除临时文件
     */
    public static void deleteTempFile(File file) {
        if (file != null && file.exists()) {
            file.delete();
            Log.d(TAG, "删除临时文件: " + file.getAbsolutePath());
        }
    }
}
