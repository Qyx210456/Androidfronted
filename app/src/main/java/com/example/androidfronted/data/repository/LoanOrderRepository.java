package com.example.androidfronted.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.androidfronted.data.local.entity.LoanOrderDetailEntity;
import com.example.androidfronted.data.local.entity.LoanOrderEntity;
import com.example.androidfronted.data.local.entity.RepaymentPlanEntity;
import com.example.androidfronted.data.model.LoanOrderDetailResponse;
import com.example.androidfronted.data.model.LoanOrderResponse;
import com.example.androidfronted.data.model.RepaymentPlanResponse;
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

    /**
     * 获取还款计划
     * @param orderId 订单ID
     * @param currentTerm 当前已还期数（用于确定还款状态）
     * @param callback 回调
     */
    public void getRepaymentPlan(int orderId, int currentTerm, @NonNull RepaymentPlanCallback callback) {
        String token = tokenManager.getToken();
        Log.d(TAG, "getRepaymentPlan, orderId: " + orderId + ", currentTerm: " + currentTerm);
        remoteDataSource.getRepaymentPlan(token, orderId, new RemoteDataSource.NetworkCallback<RepaymentPlanResponse>() {
            @Override
            public void onSuccess(RepaymentPlanResponse response) {
                if (response != null && response.getData() != null) {
                    Log.d(TAG, "Got " + response.getData().size() + " repayment plan items");
                    
                    List<RepaymentPlanEntity> entities = new ArrayList<>();
                    for (RepaymentPlanResponse.RepaymentPlanItem item : response.getData()) {
                        String status = (item.getTerm() <= currentTerm) ? "已还" : "未还";
                        Log.d(TAG, "Term: " + item.getTerm() + ", currentTerm: " + currentTerm + ", status: " + status);
                        RepaymentPlanEntity entity = new RepaymentPlanEntity(
                            0,  // localId auto-generated
                            orderId,
                            item.getTerm(),
                            item.getPrincipal(),
                            item.getInterest(),
                            item.getTotal(),
                            status
                        );
                        entities.add(entity);
                    }
                    
                    localDataSource.deleteRepaymentPlansByOrderId(orderId, new LocalDataSource.DataSourceCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            localDataSource.saveRepaymentPlans(entities, new LocalDataSource.DataSourceCallback<Void>() {
                                @Override
                                public void onSuccess(Void data) {
                                    callback.onSuccess(entities);
                                }

                                @Override
                                public void onError(String errorMessage) {
                                    callback.onError(errorMessage);
                                }
                            });
                        }

                        @Override
                        public void onError(String errorMessage) {
                            callback.onError(errorMessage);
                        }
                    });
                } else {
                    callback.onError("获取还款计划失败");
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to get repayment plan: " + errorMessage);
                localDataSource.getRepaymentPlansByOrderId(orderId, new LocalDataSource.DataSourceCallback<List<RepaymentPlanEntity>>() {
                    @Override
                    public void onSuccess(List<RepaymentPlanEntity> data) {
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

    /**
     * 更新第一个未还期的状态为已还
     * 用于还款成功后按顺序更新还款计划
     * 如果本地数据库没有数据，直接返回成功（后端已处理还款逻辑）
     */
    public void updateFirstUnpaidToRepaid(int orderId, @NonNull UpdateFirstUnpaidCallback callback) {
        Log.d(TAG, "updateFirstUnpaidToRepaid called for orderId: " + orderId);
        localDataSource.getRepaymentPlansByOrderId(orderId, new LocalDataSource.DataSourceCallback<List<RepaymentPlanEntity>>() {
            @Override
            public void onSuccess(List<RepaymentPlanEntity> plans) {
                Log.d(TAG, "getRepaymentPlansByOrderId returned " + (plans != null ? plans.size() : 0) + " plans");
                if (plans == null || plans.isEmpty()) {
                    Log.d(TAG, "No local data, repayment handled by backend");
                    callback.onSuccess(0);
                    return;
                }

                int firstUnpaidTerm = -1;
                for (RepaymentPlanEntity plan : plans) {
                    Log.d(TAG, "Plan term: " + plan.getTerm() + ", status: " + plan.getStatus());
                    if ("未还".equals(plan.getStatus())) {
                        firstUnpaidTerm = plan.getTerm();
                        break;
                    }
                }

                if (firstUnpaidTerm == -1) {
                    Log.d(TAG, "All terms are already repaid");
                    callback.onAllRepaid();
                    return;
                }

                final int termToUpdate = firstUnpaidTerm;
                Log.d(TAG, "Updating term " + termToUpdate + " to 已还");
                localDataSource.updateRepaymentPlanStatus(orderId, termToUpdate, "已还", new LocalDataSource.DataSourceCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        Log.d(TAG, "Successfully updated term " + termToUpdate + " to 已还");
                        callback.onSuccess(termToUpdate);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.e(TAG, "Failed to update status: " + errorMessage);
                        callback.onError(errorMessage);
                    }
                });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to get repayment plans: " + errorMessage);
                callback.onSuccess(0);
            }
        });
    }

    public interface RepaymentPlanCallback {
        void onSuccess(List<RepaymentPlanEntity> plans);
        void onError(String errorMessage);
    }

    public interface UpdateFirstUnpaidCallback {
        void onSuccess(int updatedTerm);
        void onAllRepaid();
        void onError(String errorMessage);
    }

    public interface UpdateCurrentTermCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    public interface UnpaidStatsCallback {
        void onSuccess(double totalPrincipal, double totalInterest, double totalAmount);
        void onError(String errorMessage);
    }

    public void updateCurrentTerm(int orderId, int newCurrentTerm, @NonNull UpdateCurrentTermCallback callback) {
        Log.d(TAG, "updateCurrentTerm: orderId=" + orderId + ", newCurrentTerm=" + newCurrentTerm);
        localDataSource.updateLoanOrderCurrentTerm(orderId, newCurrentTerm, new LocalDataSource.DataSourceCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d(TAG, "updateCurrentTerm onSuccess");
                callback.onSuccess();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "updateCurrentTerm onError: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }

    public void getAllUnpaidStats(@NonNull UnpaidStatsCallback callback) {
        localDataSource.getAllLoanOrders(new LocalDataSource.DataSourceCallback<List<LoanOrderEntity>>() {
            @Override
            public void onSuccess(List<LoanOrderEntity> orders) {
                if (orders == null || orders.isEmpty()) {
                    callback.onSuccess(0, 0, 0);
                    return;
                }

                final int totalOrders = orders.size();
                final int[] processedOrders = {0};
                final double[] principalSum = {0};
                final double[] interestSum = {0};
                final double[] amountSum = {0};

                for (LoanOrderEntity order : orders) {
                    int orderId = order.getId();
                    int currentTerm = order.getCurrentTerm();
                    
                    getRepaymentPlan(orderId, currentTerm, new RepaymentPlanCallback() {
                        @Override
                        public void onSuccess(List<RepaymentPlanEntity> plans) {
                            if (plans != null) {
                                for (RepaymentPlanEntity plan : plans) {
                                    if ("未还".equals(plan.getStatus())) {
                                        principalSum[0] += plan.getPrincipal();
                                        interestSum[0] += plan.getInterest();
                                        amountSum[0] += plan.getTotal();
                                    }
                                }
                            }
                            
                            processedOrders[0]++;
                            if (processedOrders[0] == totalOrders) {
                                callback.onSuccess(principalSum[0], interestSum[0], amountSum[0]);
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            processedOrders[0]++;
                            if (processedOrders[0] == totalOrders) {
                                callback.onSuccess(principalSum[0], interestSum[0], amountSum[0]);
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }
}
