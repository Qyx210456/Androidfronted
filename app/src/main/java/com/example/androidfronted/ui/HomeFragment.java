package com.example.androidfronted.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.example.androidfronted.R;
import com.example.androidfronted.data.model.LoanProduct;
import com.example.androidfronted.ui.adapter.HotProductAdapter;
import com.example.androidfronted.viewmodel.loan.HomeViewModel;
import com.example.androidfronted.viewmodel.base.NavigationEvent;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private ViewPager2 viewPagerProducts;
    private LinearLayout productIndicatorLayout;
    private TextView tvSystemNotice;
    private HotProductAdapter hotProductAdapter;
    private HomeViewModel viewModel;
    private final Handler autoScrollHandler = new Handler();
    private boolean isAutoScrolling = false;
    private ViewPager2.OnPageChangeCallback pageChangeCallback;

    //  正确的 Runnable：作为成员变量，不是 Fragment 本身
    private final Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (viewPagerProducts == null || !isAdded()) return;

            List<LoanProduct> allItems = hotProductAdapter.getProducts();
            if (allItems == null || allItems.size() <= 3) return;

            int current = viewPagerProducts.getCurrentItem();
            int total = allItems.size();     // 7: [E, A, B, C, D, E, A]
            int realCount = total - 2;       // 5

            // 当前显示的是最后一个真实产品（E，位置 = total - 2）
            if (current == total - 2) {
                // 1. 停留在 E 3 秒（已完成）
                // 2. 无动画跳到第一个真实项 A（位置 1）
                viewPagerProducts.setCurrentItem(1, false);
                updateIndicators(0); // 指示器指向 A

                // 3. 等待一小段时间（可选），再继续滚动
                // 这里直接调度下一次滚动（从 A 开始）
                autoScrollHandler.postDelayed(this, 3000);
                return;
            }

            // 正常滚动：A→B→C→D→E
            int next = current + 1;
            viewPagerProducts.setCurrentItem(next, true); // 带动画

            // 更新指示器
            int indicatorIndex = next - 1; // 因为真实数据从 index=1 开始
            if (indicatorIndex >= 0 && indicatorIndex < realCount) {
                updateIndicators(indicatorIndex);
            }

            autoScrollHandler.postDelayed(this, 3000);
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        hotProductAdapter = new HotProductAdapter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewPagerProducts = view.findViewById(R.id.viewPagerProducts);
        productIndicatorLayout = view.findViewById(R.id.productIndicatorLayout);
        tvSystemNotice = view.findViewById(R.id.tvSystemNotice);

        ImageView ivAdImage = view.findViewById(R.id.ivAdImage);
        ivAdImage.setImageResource(R.drawable.bg_home_advertisement);

        if (tvSystemNotice != null) {
            tvSystemNotice.setSelected(true); // 启动跑马灯
        }

        setupClickListeners(view);
        initViewPager(); // 必须在加载数据前设置好布局参数
        setupObservers();
        loadData();
    }


    private void setupClickListeners(View view) {
        view.findViewById(R.id.cardCreditQuery).setOnClickListener(v ->
                Toast.makeText(getContext(), "额度查询", Toast.LENGTH_SHORT).show()
        );
        view.findViewById(R.id.layoutSystemNotice).setOnClickListener(v ->
                Toast.makeText(getContext(), "系统公告", Toast.LENGTH_SHORT).show()
        );
        view.findViewById(R.id.cardExploreMore).setOnClickListener(v -> {
            startActivity(new Intent(getContext(), ProductAllActivity.class));
        });
    }

    private void initViewPager() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float density = dm.density;

        // 关键：设置 padding 让左右卡片露出
        int sidePadding = (int) (90 * density); // 左右各 64dp
        viewPagerProducts.setPadding(sidePadding, 0, sidePadding, 0);
        viewPagerProducts.setClipToPadding(false);
        viewPagerProducts.setClipChildren(false);
        viewPagerProducts.setOffscreenPageLimit(3);

        // 确保内部 RecyclerView 也不裁剪
        if (viewPagerProducts.getChildCount() > 0) {
            ViewGroup parent = (ViewGroup) viewPagerProducts.getChildAt(0);
            if (parent != null) {
                parent.setClipChildren(false);
            }
        }

        viewPagerProducts.setAdapter(hotProductAdapter);

        // PageTransformer：实现中间大、两边小 + 透明度
        viewPagerProducts.setPageTransformer(new ViewPager2.PageTransformer() {
            private static final float MIN_SCALE = 0.85f;
            private static final float MIN_ALPHA = 0.6f;

            @Override
            public void transformPage(@NonNull View page, float position) {
                if (position < -1 || position > 1) {
                    page.setScaleX(MIN_SCALE);
                    page.setScaleY(MIN_SCALE);
                    page.setAlpha(MIN_ALPHA);
                } else if (position <= 0) { // [-1, 0]
                    float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 + position);
                    float alpha = MIN_ALPHA + (1 - MIN_ALPHA) * (1 + position);
                    page.setScaleX(scaleFactor);
                    page.setScaleY(scaleFactor);
                    page.setAlpha(alpha);
                } else { // (0, 1]
                    float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - position);
                    float alpha = MIN_ALPHA + (1 - MIN_ALPHA) * (1 - position);
                    page.setScaleX(scaleFactor);
                    page.setScaleY(scaleFactor);
                    page.setAlpha(alpha);
                }
            }
        });

        //  页面变化监听器（处理手动滑动到首尾）
        pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                List<LoanProduct> allItems = hotProductAdapter.getProducts();
                if (allItems == null || allItems.size() <= 3) return;

                int total = allItems.size();
                int realCount = total - 2;

                if (position == 0) {
                    // 用户手动滑到开头虚拟项（E）→ 跳到最后一个真实项（E）
                    viewPagerProducts.setCurrentItem(realCount, false);
                    updateIndicators(realCount - 1);
                } else if (position == total - 1) {
                    // 用户手动滑到末尾虚拟项（A）→ 跳到第一个真实项（A）
                    viewPagerProducts.setCurrentItem(1, false);
                    updateIndicators(0);
                } else {
                    int originalIndex = position - 1;
                    if (originalIndex >= 0 && originalIndex < realCount) {
                        updateIndicators(originalIndex);
                    }
                }
            }
        };
        viewPagerProducts.registerOnPageChangeCallback(pageChangeCallback);

        hotProductAdapter.setOnApplyClickListener(product -> {
            viewModel.navigateToProductDetail(product);
        });
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && getContext() != null) {
                Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getLoanProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null && !products.isEmpty()) {
                processProducts(products);
            }
        });

        viewModel.getNavigationEvent().observe(getViewLifecycleOwner(), event -> {
            if (event != null) {
                handleNavigation(event);
            }
        });
    }

    private void loadData() {
        viewModel.loadLoanProducts();
    }

    private void processProducts(List<LoanProduct> products) {
        if (products == null || products.isEmpty()) {
            Toast.makeText(getContext(), "暂无产品数据", Toast.LENGTH_SHORT).show();
            return;
        }

        // 只取最新的 5 个产品（按 ID 排序，倒序取）
        // ID 越大越新
        products.sort((a, b) -> Integer.compare(b.getProductId(), a.getProductId()));
        List<LoanProduct> latestProducts = new ArrayList<>();
        for (int i = 0; i < Math.min(5, products.size()); i++) {
            latestProducts.add(products.get(i));
        }

        // 现在 latestProducts 只有 5 个，且是最新添加的
        if (latestProducts.isEmpty()) {
            Toast.makeText(getContext(), "暂无产品", Toast.LENGTH_SHORT).show();
            return;
        }

        // 构造循环列表：[last] + [original] + [first]
        List<LoanProduct> circularList = new ArrayList<>();
        circularList.add(latestProducts.get(latestProducts.size() - 1)); // 最后一个
        circularList.addAll(latestProducts);                             // 所有
        circularList.add(latestProducts.get(0));                         // 第一个

        hotProductAdapter.setProducts(circularList);

        int initialPosition = 1; // 指向真实第一个产品
        viewPagerProducts.setCurrentItem(initialPosition, false);

        setupIndicators(latestProducts.size()); // 指示器数量为 5

        startAutoScroll();
    }

    private void handleNavigation(NavigationEvent event) {
        switch (event.getNavigationType()) {
            case NavigationEvent.NAVIGATE_TO_PRODUCT_DETAIL:
                LoanProduct product = (LoanProduct) event.getData();
                if (product != null) {
                    Intent intent = new Intent(getContext(), ProductDetailActivity.class);
                    intent.putExtra("loan_product", product);
                    intent.putExtra("from", "home");
                    startActivity(intent);
                }
                break;
        }
    }

    private void setupIndicators(int count) {
        productIndicatorLayout.removeAllViews();
        for (int i = 0; i < count; i++) {
            View dot = new View(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) (i == 0 ? 6 * getResources().getDisplayMetrics().density : 4 * getResources().getDisplayMetrics().density),
                    (int) (i == 0 ?6 * getResources().getDisplayMetrics().density : 4 * getResources().getDisplayMetrics().density)
            );
            params.setMargins((int) (2 * getResources().getDisplayMetrics().density), 0,
                    (int) (2 * getResources().getDisplayMetrics().density), 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.bg_indicator_dot_custom);
            dot.setSelected(i == 0);
            productIndicatorLayout.addView(dot);
        }
    }

    private void updateIndicators(int position) {
        for (int i = 0; i < productIndicatorLayout.getChildCount(); i++) {
            View dot = productIndicatorLayout.getChildAt(i);
            boolean isSelected = (i == position);
            dot.setSelected(isSelected);

            // 动态更新尺寸（可选，也可全靠 drawable）
            DisplayMetrics dm = getResources().getDisplayMetrics();
            int size = (int) (isSelected ? 6 * dm.density : 4 * dm.density);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) dot.getLayoutParams();
            params.width = size;
            params.height = size;
            dot.setLayoutParams(params);
        }
    }

    private void startAutoScroll() {
        if (isAutoScrolling || !isAdded()) return;
        isAutoScrolling = true;
        autoScrollHandler.postDelayed(autoScrollRunnable, 3000); //  正确传入 Runnable
    }

    @Override
    public void onPause() {
        super.onPause();
        autoScrollHandler.removeCallbacksAndMessages(null);
        isAutoScrolling = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isAdded() && hotProductAdapter.getProducts() != null && hotProductAdapter.getProducts().size() > 1) {
            startAutoScroll();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (viewPagerProducts != null && pageChangeCallback != null) {
            viewPagerProducts.unregisterOnPageChangeCallback(pageChangeCallback);
        }
        autoScrollHandler.removeCallbacksAndMessages(null);
        isAutoScrolling = false;
    }
}