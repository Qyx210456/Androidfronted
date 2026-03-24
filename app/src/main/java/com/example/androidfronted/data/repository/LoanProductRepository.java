package com.example.androidfronted.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.androidfronted.data.mapper.LoanProductMapper;
import com.example.androidfronted.data.model.LoanProduct;
import com.example.androidfronted.data.model.LoanProductResponse;
import com.example.androidfronted.data.source.LocalDataSource;
import com.example.androidfronted.data.source.RemoteDataSource;

import java.util.ArrayList;
import java.util.List;

public class LoanProductRepository {
    private static final String TAG = "LoanProductRepo";
    private static LoanProductRepository instance;

    private final LocalDataSource localDataSource;
    private final RemoteDataSource remoteDataSource;

    private LoanProductRepository(Context context) {
        this.localDataSource = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource(context);
    }

    public static synchronized LoanProductRepository getInstance(Context context) {
        if (instance == null) {
            instance = new LoanProductRepository(context.getApplicationContext());
        }
        return instance;
    }

    public void getLoanProducts(@NonNull AuthCallback<List<LoanProduct>> callback) {
        remoteDataSource.getLoanProducts(new RemoteDataSource.NetworkCallback<LoanProductResponse>() {
            @Override
            public void onSuccess(LoanProductResponse response) {
                if (response != null && response.getData() != null) {
                    List<LoanProduct> products = response.getData();
                    saveLoanProductsToLocal(products);
                    callback.onSuccess(products);
                } else {
                    callback.onError("获取贷款产品失败");
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Get loan products from remote failed: " + errorMessage);
                loadLoanProductsFromLocal(callback);
            }
        });
    }

    private void saveLoanProductsToLocal(List<LoanProduct> products) {
        List<com.example.androidfronted.data.local.entity.LoanProductEntity> entities = new ArrayList<>();
        for (LoanProduct product : products) {
            entities.add(LoanProductMapper.toEntity(product));
        }
        localDataSource.saveLoanProducts(entities, new LocalDataSource.DataSourceCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d(TAG, "Loan products saved to local database");
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to save loan products: " + errorMessage);
            }
        });
    }

    private void loadLoanProductsFromLocal(@NonNull AuthCallback<List<LoanProduct>> callback) {
        localDataSource.getAllLoanProducts(new LocalDataSource.DataSourceCallback<List<com.example.androidfronted.data.local.entity.LoanProductEntity>>() {
            @Override
            public void onSuccess(List<com.example.androidfronted.data.local.entity.LoanProductEntity> entities) {
                if (entities != null && !entities.isEmpty()) {
                    List<LoanProduct> products = new ArrayList<>();
                    for (com.example.androidfronted.data.local.entity.LoanProductEntity entity : entities) {
                        products.add(LoanProductMapper.fromEntity(entity));
                    }
                    callback.onSuccess(products);
                } else {
                    callback.onError("无可用数据");
                }
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public interface AuthCallback<T> {
        void onSuccess(T response);
        void onError(String errorMessage);
    }
}
