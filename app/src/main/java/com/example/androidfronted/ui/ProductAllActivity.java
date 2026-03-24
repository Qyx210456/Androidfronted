package com.example.androidfronted.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.androidfronted.R;
import com.example.androidfronted.data.model.LoanProduct;
import com.example.androidfronted.ui.adapter.LoanProductAdapter;
import com.example.androidfronted.viewmodel.loan.ProductAllViewModel;
import com.example.androidfronted.viewmodel.base.NavigationEvent;

import java.util.List;

/**
 * 全部贷款产品页面
 * - 支持按利率（最低）、额度（最高）、期限（最短 term）排序
 * - 排序通过点击顶部图标切换方向（↑↓）
 * - item 不显示贷款用途，改为显示最低利率
 */
public class ProductAllActivity extends AppCompatActivity {

    private ProductAllViewModel viewModel;
    private LoanProductAdapter adapter;
    private RecyclerView rvProducts;

    // 排序控件
    private ImageView ivSortRateAsc, ivSortRateDesc;
    private ImageView ivSortAmountAsc, ivSortAmountDesc;
    private ImageView ivSortTermAsc, ivSortTermDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.product_all);

        viewModel = new ViewModelProvider(this).get(ProductAllViewModel.class);

        setupObservers();
        initViews();
        setupSortListeners();
        loadProducts();
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
            // 可以显示加载状态
        });

        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null) {
                Toast.makeText(this, "加载失败: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getProducts().observe(this, productList -> {
            if (productList != null) {
                adapter.setProducts(productList);
            }
        });

        viewModel.getCurrentSortField().observe(this, field -> {
            updateSortIcons();
        });

        viewModel.getIsAscending().observe(this, ascending -> {
            updateSortIcons();
        });

        viewModel.getNavigationEvent().observe(this, event -> {
            if (event != null) {
                handleNavigation(event);
            }
        });
    }

    private void initViews() {
        rvProducts = findViewById(R.id.rvProducts);
        rvProducts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LoanProductAdapter();
        rvProducts.setAdapter(adapter);

        ivSortRateAsc = findViewById(R.id.ivSortRateAsc);
        ivSortRateDesc = findViewById(R.id.ivSortRateDesc);
        ivSortAmountAsc = findViewById(R.id.ivSortAmountAsc);
        ivSortAmountDesc = findViewById(R.id.ivSortAmountDesc);
        ivSortTermAsc = findViewById(R.id.ivSortTermAsc);
        ivSortTermDesc = findViewById(R.id.ivSortTermDesc);

        findViewById(R.id.apply_btn_back).setOnClickListener(v -> finish());

        adapter.setOnLearnMoreClickListener(product -> {
            viewModel.selectProduct(product);
        });
    }

    private void setupSortListeners() {
        View.OnClickListener rateListener = v -> viewModel.sortProducts("rate");
        View.OnClickListener amountListener = v -> viewModel.sortProducts("amount");
        View.OnClickListener termListener = v -> viewModel.sortProducts("term");

        ivSortRateAsc.setOnClickListener(rateListener);
        ivSortRateDesc.setOnClickListener(rateListener);
        ivSortAmountAsc.setOnClickListener(amountListener);
        ivSortAmountDesc.setOnClickListener(amountListener);
        ivSortTermAsc.setOnClickListener(termListener);
        ivSortTermDesc.setOnClickListener(termListener);
    }

    private void updateSortIcons() {
        String currentSortField = viewModel.getCurrentSortField().getValue();
        Boolean isAscending = viewModel.getIsAscending().getValue();

        if (currentSortField == null || isAscending == null) {
            return;
        }

        int defaultColor = getResources().getColor(R.color.text_quaternary, getTheme());
        int activeColor = getResources().getColor(R.color.main_blue, getTheme());

        // 重置所有图标
        ivSortRateAsc.setColorFilter(defaultColor);
        ivSortRateDesc.setColorFilter(defaultColor);
        ivSortAmountAsc.setColorFilter(defaultColor);
        ivSortAmountDesc.setColorFilter(defaultColor);
        ivSortTermAsc.setColorFilter(defaultColor);
        ivSortTermDesc.setColorFilter(defaultColor);

        // 高亮当前排序
        switch (currentSortField) {
            case "rate":
                (isAscending ? ivSortRateAsc : ivSortRateDesc).setColorFilter(activeColor);
                break;
            case "amount":
                (isAscending ? ivSortAmountAsc : ivSortAmountDesc).setColorFilter(activeColor);
                break;
            case "term":
                (isAscending ? ivSortTermAsc : ivSortTermDesc).setColorFilter(activeColor);
                break;
        }
    }

    private void loadProducts() {
        viewModel.loadProducts();
    }

    private void handleNavigation(NavigationEvent event) {
        switch (event.getNavigationType()) {
            case NavigationEvent.NAVIGATE_TO_PRODUCT_DETAIL:
                LoanProduct product = (LoanProduct) event.getData();
                if (product != null) {
                    Intent intent = new Intent(ProductAllActivity.this, ProductDetailActivity.class);
                    intent.putExtra("loan_product", product);
                    intent.putExtra("from", "all");
                    startActivity(intent);
                }
                break;
            case NavigationEvent.NAVIGATE_BACK:
                finish();
                break;
        }
    }

    /**
     * 启动本页面（可携带来源）
     */
    public static void startFrom(Context context, String from) {
        Intent intent = new Intent(context, ProductAllActivity.class);
        intent.putExtra("from", from);
        context.startActivity(intent);
    }
}