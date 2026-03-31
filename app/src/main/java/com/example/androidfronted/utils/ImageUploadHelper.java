package com.example.androidfronted.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageUploadHelper {
    private static final String TAG = "ImageUploadHelper";
    private static final int MAX_WIDTH = 800;
    private static final int MAX_HEIGHT = 800;
    private static final int QUALITY = 80;

    /**
     * 压缩图片
     * @param context 上下文
     * @param uri 图片URI
     * @return 压缩后的图片文件
     */
    public static File compressImage(Context context, Uri uri) throws IOException {
        // 读取图片
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        // 计算压缩比例
        int inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        // 解码图片
        inputStream = context.getContentResolver().openInputStream(uri);
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        // 旋转图片（如果需要）
        bitmap = rotateBitmap(bitmap, getImageRotation(context, uri));

        // 压缩图片
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, bos);

        // 保存压缩后的图片
        File outputDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File outputFile = new File(outputDir, "compressed_" + System.currentTimeMillis() + ".jpg");
        FileOutputStream fos = new FileOutputStream(outputFile);
        fos.write(bos.toByteArray());
        fos.flush();
        fos.close();

        Log.d(TAG, "Compressed image saved to: " + outputFile.getAbsolutePath());
        Log.d(TAG, "Original size: " + (options.outWidth * options.outHeight) + " pixels");
        Log.d(TAG, "Compressed size: " + (bitmap.getWidth() * bitmap.getHeight()) + " pixels");

        return outputFile;
    }

    /**
     * 压缩图片（带回调）
     * @param context 上下文
     * @param uri 图片URI
     * @param callback 回调接口
     */
    public static void compressImage(Context context, Uri uri, ImageUploadCallback callback) {
        try {
            File compressedFile = compressImage(context, uri);
            callback.onSuccess(compressedFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            callback.onError("图片压缩失败");
        }
    }

    /**
     * 计算压缩比例
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    /**
     * 旋转图片
     */
    private static Bitmap rotateBitmap(Bitmap bitmap, int rotation) {
        if (rotation == 0) {
            return bitmap;
        }

        Matrix matrix = new Matrix();
        matrix.postRotate(rotation);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    /**
     * 获取图片旋转角度
     */
    private static int getImageRotation(Context context, Uri uri) {
        // 这里可以通过 ExifInterface 获取图片的旋转角度
        // 简化处理，暂时返回 0
        return 0;
    }

    /**
     * 图片上传回调接口
     */
    public interface ImageUploadCallback {
        void onSuccess(String imagePath);
        void onError(String errorMessage);
        default void onProgress(int progress) {
            // 默认实现，子类可以选择性重写
        }
    }
}