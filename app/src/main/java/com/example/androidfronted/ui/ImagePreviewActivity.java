package com.example.androidfronted.ui;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.androidfronted.R;

/**
 * 图片预览Activity
 * 支持图片的放大缩小功能
 */
public class ImagePreviewActivity extends AppCompatActivity {
    private ImageView imageView;
    private GestureDetector gestureDetector;
    private float currentScale = 1.0f;
    private float maxScale = 3.0f;
    private float minScale = 0.5f;
    private float lastTouchX;
    private float lastTouchY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        imageView = findViewById(R.id.iv_preview_image);

        // 获取传递过来的图片URI
        String imageUriString = getIntent().getStringExtra("image_uri");
        if (imageUriString != null) {
            // TODO: 加载图片到ImageView
        }

        // 设置手势检测
        setupGestures();

        // 设置返回按钮
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());
    }

    /**
     * 设置手势检测
     */
    private void setupGestures() {
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // 双击放大到最大或恢复到原始大小
                if (currentScale == 1.0f) {
                    zoomTo(maxScale, e.getX(), e.getY());
                } else {
                    zoomTo(1.0f, imageView.getWidth() / 2f, imageView.getHeight() / 2f);
                }
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                // 处理拖动
                if (currentScale > 1.0f) {
                    imageView.scrollBy((int) distanceX, (int) distanceY);
                }
                return true;
            }
        });

        imageView.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);

            // 处理缩放手势
            if (event.getPointerCount() == 2) {
                handlePinchZoom(event);
            }

            return true;
        });
    }

    /**
     * 处理双指缩放
     */
    private void handlePinchZoom(MotionEvent event) {
        float x0 = event.getX(0);
        float y0 = event.getY(0);
        float x1 = event.getX(1);
        float y1 = event.getY(1);

        float distance = (float) Math.sqrt(Math.pow(x1 - x0, 2) + Math.pow(y1 - y0, 2));

        if (event.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
            lastTouchX = distance;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float scale = distance / lastTouchX;
            float newScale = currentScale * scale;

            // 限制缩放范围
            if (newScale >= minScale && newScale <= maxScale) {
                currentScale = newScale;
                applyScale();
            }

            lastTouchX = distance;
        }
    }

    /**
     * 缩放到指定比例
     */
    private void zoomTo(float scale, float centerX, float centerY) {
        currentScale = scale;
        applyScale();
    }

    /**
     * 应用缩放
     */
    private void applyScale() {
        imageView.setScaleX(currentScale);
        imageView.setScaleY(currentScale);
    }

    /**
     * 重置缩放
     */
    private void resetZoom() {
        currentScale = 1.0f;
        applyScale();
        imageView.scrollTo(0, 0);
    }
}
