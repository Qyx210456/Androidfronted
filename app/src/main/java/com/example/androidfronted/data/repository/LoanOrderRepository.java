package com.example.androidfronted.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.androidfronted.data.local.entity.LoanOrderDetailEntity;
import com.example.androidfronted.data.local.entity.LoanOrderEntity;
import com.example.androidfronted.data.model.LoanOrderDetailResponse;
import com.example.androidfronted.data.model.LoanOrderResponse;
import com.example.androidfronted.data.source.LocalDataSource;
import com.example.androidfronted.data.source.RemoteDataSource;
import com.example.androidfronted.util.TokenManager;
import java.util.ArrayList;
import java.util.List;

public class LoanOrderRepository {
    private static final String TAG = "LoanOrderRepo";
    private static LoanOrderRepository instance;

    private final LocalDataSource localDataSource;
    private final RemoteDataSource remoteDataSource;
    private final TokenManager tokenManager;

    private LoanOrderRepository(Context context) {
        this.localDataSource = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource(context);
        this.tokenManager = new TokenManager(context);
    }

    public static synchronized LoanOrderRepository getInstance(Context context) {
        if (instance == null) {
            instance = new LoanOrderRepository(context.getApplicationContext());
        }
        return instance;
    }

    public void getLoanOrders(@NonNull LoanOrdersCallback callback) {
        String token = tokenManager.getToken();
        Log.d(TAG, "getLoanOrders, getToken from TokenManager: " + (token != null ? "not null" : "null"));
        remoteDataSource.getLoanOrders(token, new RemoteDataSource.NetworkCallback<LoanOrderResponse>() {
            @Override
            public void onSuccess(LoanOrderResponse response) {
                if (response != null && response.getData() != null) {
                    Log.d(TAG, "Got " + response.getData().size() + " loan orders");
                    
                    List<LoanOrderEntity> entities = new ArrayList<>();
                    for (LoanOrderResponse.LoanOrder order : response.getData()) {
                        LoanOrderEntity entity = new LoanOrderEntity(
                            order.getId(),
                            order.getLoanAmount(),
                            order.getStatus(),
                            order.getStartTime(),
                            order.getTerm(),
                            order.getCurrentTerm(),
                            order.getOverdueDays()
                        );
                        entities.add(entity);
                    }
                    
                    localDataSource.saveLoanOrders(entities);
                    
                    callback.onSuccess(entities);
                } else {
                    callback.onError("获取贷款订单失败");
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to get loan orders: " + errorMessage);
                localDataSource.getAllLoanOrders(new LocalDataSource.DataSourceCallback<List<LoanOrderEntity>>() {
                    @Override
                    public void onSuccess(List<LoanOrderEntity> data) {
                        if (data != null && !data.isEmpty()) {
                            callback.onSuccess(data);
                        } else {
                            callback.onError(errorMessage);
                        }
                    }

                    @Override
                    public void onError(String localError) {
                        callback.onError(errorMessage);
                    }
                });
            }
        });
    }

    public void getLoanOrdersFromLocal(@NonNull LoanOrdersCallback callback) {
        localDataSource.getAllLoanOrders(new LocalDataSource.DataSourceCallback<List<LoanOrderEntity>>() {
            @Override
            public void onSuccess(List<LoanOrderEntity> data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public void getLoanOrdersByStatusFromLocal(String status, @NonNull LoanOrdersCallback callback) {
        localDataSource.getLoanOrdersByStatus(status, new LocalDataSource.DataSourceCallback<List<LoanOrderEntity>>() {
            @Override
            public void onSuccess(List<LoanOrderEntity> data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public interface LoanOrdersCallback {
        void onSuccess(List<LoanOrderEntity> orders);
        void onError(String errorMessage);
    }

    public void getLoanOrderDetail(int orderId, @NonNull LoanOrderDetailCallback callback) {
        String token = tokenManager.getToken();
        Log.d(TAG, "getLoanOrderDetail, getToken from TokenManager: " + (token != null ? "not null" : "null"));
        remoteDataSource.getLoanOrderDetail(token, orderId, new RemoteDataSource.NetworkCallback<LoanOrderDetailResponse>() {
            @Override
            public void onSuccess(LoanOrderDetailResponse response) {
                if (response != null && response.getData() != null) {
                    Log.d(TAG, "Got loan order detail for id: " + orderId);
                    
                    LoanOrderDetailResponse.LoanOrderDetailData data = response.getData();
                    LoanOrderDetailResponse.OrderDetail order = data.getOrder();
                    
                    LoanOrderDetailEntity entity = new LoanOrderDetailEntity(
                        order.getId(),
                        order.getUserId(),
                        order.getProductId(),
                        data.getProductName(),
                        order.getStatus(),
                        order.getRepaidAmount(),
                        order.getLoanAmount(),
                        order.getInterestRate(),
                        order.getRepaidType(),
                        order.getLoanPeriod(),
                        order.getTerm(),
                        order.getCurrentTerm(),
                        order.getContract(),
                        order.getOverdueDays(),
                        order.getStartTime()
                    );
                    
                    localDataSource.saveLoanOrderDetail(entity);
                    
                    callback.onSuccess(entity);
                } else {
                    callback.onError("获取订单详情失败");
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to get loan order detail: " + errorMessage);
                localDataSource.getLoanOrderDetail(orderId, new LocalDataSource.DataSourceCallback<LoanOrderDetailEntity>() {
                    @Override
                    public void onSuccess(LoanOrderDetailEntity data) {
                        if (data != null) {
                            callback.onSuccess(data);
                        } else {
                            callback.onError(errorMessage);
                        }
                    }

                    @Override
                    public void onError(String localError) {
                        callback.onError(errorMessage);
                    }
                });
            }
        });
    }

    public void getLoanOrderDetailFromLocal(int orderId, @NonNull LoanOrderDetailCallback callback) {
        localDataSource.getLoanOrderDetail(orderId, new LocalDataSource.DataSourceCallback<LoanOrderDetailEntity>() {
            @Override
            public void onSuccess(LoanOrderDetailEntity data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public interface LoanOrderDetailCallback {
        void onSuccess(LoanOrderDetailEntity detail);
        void onError(String errorMessage);
    }

    public void repayLoanOrder(int orderId, @NonNull RepayCallback callback) {
        String token = tokenManager.getToken();
        Log.d(TAG, "repayLoanOrder, getToken from TokenManager: " + (token != null ? "not null" : "null"));
        remoteDataSource.repayLoanOrder(token, orderId, new RemoteDataSource.NetworkCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "Repay loan order success: " + response);
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to repay loan order: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }

    public interface RepayCallback {
        void onSuccess(String message);
        void onError(String errorMessage);
    }
}
