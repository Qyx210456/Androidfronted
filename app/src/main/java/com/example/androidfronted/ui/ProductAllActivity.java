package com.example.androidfronted.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidfronted.R;
import com.example.androidfronted.data.local.entity.NotificationEntity;
import com.example.androidfronted.data.model.LoanProduct;
import com.example.androidfronted.event.NotificationEvent;
import com.example.androidfronted.ui.adapter.LoanProductAdapter;
import com.example.androidfronted.util.InAppNotificationManager;
import com.example.androidfronted.util.NotificationStateManager;
import com.example.androidfronted.viewmodel.base.NavigationEvent;
import com.example.androidfronted.viewmodel.loan.ProductAllViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class ProductAllActivity extends AppCompatActivity {

    private ProductAllViewModel viewModel;
    private LoanProductAdapter adapter;
    private RecyclerView rvProducts;

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

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        InAppNotificationManager.getInstance().onActivityResumed(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        InAppNotificationManager.getInstance().onActivityDestroyed(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNewNotification(NotificationEvent.NewNotification event) {
        if (NotificationStateManager.getInstance().isAppInForeground()) {
            InAppNotificationManager.getInstance().showNotification(event.getNotification());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOfflineNotificationSummary(NotificationEvent.OfflineNotificationSummary event) {
        if (event.getCount() > 0 && event.getLatestNotification() != null) {
            InAppNotificationManager.getInstance().showOfflineNotificationSummary(event.getCount(), event.getLatestNotification());
        }
    }

    private void setupObservers() {
        viewModel.getIsLoading().observe(this, isLoading -> {
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

        ivSortRateAsc.setColorFilter(defaultColor);
        ivSortRateDesc.setColorFilter(defaultColor);
        ivSortAmountAsc.setColorFilter(defaultColor);
        ivSortAmountDesc.setColorFilter(defaultColor);
        ivSortTermAsc.setColorFilter(defaultColor);
        ivSortTermDesc.setColorFilter(defaultColor);

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

    public static void startFrom(Context context, String from) {
        Intent intent = new Intent(context, ProductAllActivity.class);
        intent.putExtra("from", from);
        context.startActivity(intent);
    }
}
