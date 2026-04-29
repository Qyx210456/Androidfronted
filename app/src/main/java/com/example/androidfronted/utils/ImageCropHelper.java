package com.example.androidfronted.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import com.canhub.cropper.CropImageContractOptions;
import com.canhub.cropper.CropImageOptions;
import com.canhub.cropper.CropImageView;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageCropHelper {
    private static final String TAG = "ImageCropHelper";
    private static final int MAX_WIDTH = 800;
    private static final int MAX_HEIGHT = 800;
    private static final int QUALITY = 80;

    public static final int CROP_SHAPE_RECTANGLE = 0;
    public static final int CROP_SHAPE_OVAL = 1;

    public interface CropCallback {
        void onSuccess(Uri croppedUri);
        void onError(String errorMessage);
    }

    public static CropImageContractOptions getCircleCropOptions(Uri sourceUri) {
        CropImageOptions options = new CropImageOptions();
        options.imageSourceIncludeGallery = true;
        options.imageSourceIncludeCamera = false;
        options.cropShape = CropImageView.CropShape.OVAL;
        options.fixAspectRatio = true;
        options.aspectRatioX = 1;
        options.aspectRatioY = 1;
        options.guidelines = CropImageView.Guidelines.ON;
        options.scaleType = CropImageView.ScaleType.FIT_CENTER;
        options.autoZoomEnabled = true;
        options.multiTouchEnabled = true;
        options.showCropOverlay = true;
        options.allowFlipping = true;
        options.allowRotation = true;
        
        return new CropImageContractOptions(sourceUri, options);
    }

    public static CropImageContractOptions getRectangleCropOptions(Uri sourceUri) {
        CropImageOptions options = new CropImageOptions();
        options.imageSourceIncludeGallery = true;
        options.imageSourceIncludeCamera = false;
        options.cropShape = CropImageView.CropShape.RECTANGLE;
        options.fixAspectRatio = false;
        options.guidelines = CropImageView.Guidelines.ON;
        options.scaleType = CropImageView.ScaleType.FIT_CENTER;
        options.autoZoomEnabled = true;
        options.multiTouchEnabled = true;
        options.showCropOverlay = true;
        options.allowFlipping = true;
        options.allowRotation = true;
        
        return new CropImageContractOptions(sourceUri, options);
    }

    public static CropImageContractOptions getCropOptions(Uri sourceUri, int cropShape) {
        if (cropShape == CROP_SHAPE_OVAL) {
            return getCircleCropOptions(sourceUri);
        } else {
            return getRectangleCropOptions(sourceUri);
        }
    }

    public static File compressCroppedImage(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        int inSampleSize = calculateInSampleSize(options, MAX_WIDTH, MAX_HEIGHT);

        inputStream = context.getContentResolver().openInputStream(uri);
        options.inJustDecodeBounds = false;
        options.inSampleSize = inSampleSize;
        Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
        inputStream.close();

        if (bitmap == null) {
            throw new IOException("Failed to decode bitmap from uri");
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, QUALITY, bos);

        File outputDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File outputFile = new File(outputDir, "cropped_" + System.currentTimeMillis() + ".jpg");
        FileOutputStream fos = new FileOutputStream(outputFile);
        fos.write(bos.toByteArray());
        fos.flush();
        fos.close();

        Log.d(TAG, "Compressed cropped image saved to: " + outputFile.getAbsolutePath());

        return outputFile;
    }

    public static void compressCroppedImage(Context context, Uri uri, CropCallback callback) {
        try {
            File compressedFile = compressCroppedImage(context, uri);
            callback.onSuccess(Uri.fromFile(compressedFile));
        } catch (IOException e) {
            Log.e(TAG, "Failed to compress cropped image", e);
            callback.onError("图片压缩失败: " + e.getMessage());
        }
    }

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
}
