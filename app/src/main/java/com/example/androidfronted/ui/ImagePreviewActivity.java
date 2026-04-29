package com.example.androidfronted.ui;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.androidfronted.R;
import com.example.androidfronted.ui.adapter.ImagePreviewAdapter;
import java.util.ArrayList;
import java.util.List;

public class ImagePreviewActivity extends AppCompatActivity {

    public static final String EXTRA_IMAGE_URLS = "image_urls";
    public static final String EXTRA_CURRENT_POSITION = "current_position";
    public static final String EXTRA_SINGLE_IMAGE_URL = "single_image_url";

    private ViewPager2 viewPager;
    private ImagePreviewAdapter adapter;
    private TextView tvPageIndicator;
    private ImageButton btnRotateLeft;
    private ImageButton btnRotateRight;
    private List<String> imageUrls;
    private int currentRotation = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        setupFullScreenMode();
        initViews();
        loadImageUrls();
        setupViewPager();
        setupClickListeners();
    }

    private void setupFullScreenMode() {
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    private void initViews() {
        viewPager = findViewById(R.id.view_pager);
        tvPageIndicator = findViewById(R.id.tv_page_indicator);
        btnRotateLeft = findViewById(R.id.btn_rotate_left);
        btnRotateRight = findViewById(R.id.btn_rotate_right);
        adapter = new ImagePreviewAdapter();
    }

    private void loadImageUrls() {
        imageUrls = new ArrayList<>();
        
        String singleImageUrl = getIntent().getStringExtra(EXTRA_SINGLE_IMAGE_URL);
        if (singleImageUrl != null && !singleImageUrl.isEmpty()) {
            imageUrls.add(singleImageUrl);
        } else {
            ArrayList<String> urls = getIntent().getStringArrayListExtra(EXTRA_IMAGE_URLS);
            if (urls != null) {
                imageUrls.addAll(urls);
            }
        }
    }

    private void setupViewPager() {
        viewPager.setAdapter(adapter);
        adapter.setImageUrls(imageUrls);
        
        int currentPosition = getIntent().getIntExtra(EXTRA_CURRENT_POSITION, 0);
        if (currentPosition >= 0 && currentPosition < imageUrls.size()) {
            viewPager.setCurrentItem(currentPosition, false);
        }
        
        updatePageIndicator(currentPosition);
        
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updatePageIndicator(position);
                currentRotation = 0;
            }
        });
    }

    private void updatePageIndicator(int position) {
        if (imageUrls.size() > 1) {
            tvPageIndicator.setVisibility(View.VISIBLE);
            tvPageIndicator.setText(String.format("%d/%d", position + 1, imageUrls.size()));
        } else {
            tvPageIndicator.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        
        btnRotateLeft.setOnClickListener(v -> rotateCurrentImage(-90));
        btnRotateRight.setOnClickListener(v -> rotateCurrentImage(90));
    }

    private void rotateCurrentImage(int degrees) {
        currentRotation = (currentRotation + degrees) % 360;
        
        View currentItem = viewPager.getChildAt(0);
        if (currentItem != null) {
            View photoView = currentItem.findViewById(R.id.photo_view);
            if (photoView != null) {
                photoView.setRotation(currentRotation);
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
