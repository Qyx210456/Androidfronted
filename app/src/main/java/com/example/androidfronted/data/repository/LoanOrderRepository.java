package com.example.androidfronted.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.androidfronted.data.local.entity.LoanOrderDetailEntity;
import com.example.androidfronted.data.local.entity.LoanOrderEntity;
import com.example.androidfronted.data.local.entity.RepaymentPlanEntity;
import com.example.androidfronted.data.model.LoanOrderDetailResponse;
import com.example.androidfronted.data.model.LoanOrderResponse;
import com.example.androidfronted.data.model.OrderStatisticsResponse;
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
                            order.getStatus() != null ? order.getStatus() : "",
                            order.getStartTime() != null ? order.getStartTime() : "",
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
                        data.getProductName() != null ? data.getProductName() : "",
                        order.getStatus() != null ? order.getStatus() : "",
                        order.getRepaidAmount(),
                        order.getLoanAmount(),
                        order.getInterestRate(),
                        order.getRepaidType() != null ? order.getRepaidType() : "",
                        order.getLoanPeriod(),
                        order.getTerm(),
                        order.getCurrentTerm(),
                        order.getContract() != null ? order.getContract() : "",
                        order.getOverdueDays(),
                        order.getStartTime() != null ? order.getStartTime() : ""
                    );

                    // 后端统计派生金额（null = 后端未提供，保持 -1 标记由 ViewModel 回退本地计算）
                    if (data.getTotalAmountDue() != null) {
                        entity.setTotalAmountDue(data.getTotalAmountDue());
                    }
                    if (data.getOutstandingAmount() != null) {
                        entity.setOutstandingAmount(data.getOutstandingAmount());
                    }
                    if (data.getOutstandingPrincipal() != null) {
                        entity.setOutstandingPrincipal(data.getOutstandingPrincipal());
                    }
                    if (data.getOutstandingInterest() != null) {
                        entity.setOutstandingInterest(data.getOutstandingInterest());
                    }

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
     * 从本地缓存读取还款计划（离线兜底/先展示后刷新）
     */
    public void getRepaymentPlanFromLocal(int orderId, @NonNull RepaymentPlanCallback callback) {
        localDataSource.getRepaymentPlansByOrderId(orderId, new LocalDataSource.DataSourceCallback<List<RepaymentPlanEntity>>() {
            @Override
            public void onSuccess(List<RepaymentPlanEntity> data) {
                if (data != null && !data.isEmpty()) {
                    callback.onSuccess(data);
                } else {
                    callback.onError("本地无还款计划缓存");
                }
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * 获取还款计划（每期状态由后端直接返回，无需本地根据 currentTerm 推算）
     * @param orderId 订单ID
     * @param callback 回调
     */
    public void getRepaymentPlan(int orderId, @NonNull RepaymentPlanCallback callback) {
        String token = tokenManager.getToken();
        Log.d(TAG, "getRepaymentPlan, orderId: " + orderId);
        remoteDataSource.getRepaymentPlan(token, orderId, new RemoteDataSource.NetworkCallback<RepaymentPlanResponse>() {
            @Override
            public void onSuccess(RepaymentPlanResponse response) {
                if (response != null && response.getData() != null) {
                    Log.d(TAG, "Got " + response.getData().size() + " repayment plan items");
                    
                    List<RepaymentPlanEntity> entities = new ArrayList<>();
                    for (RepaymentPlanResponse.RepaymentPlanItem item : response.getData()) {
                        String status = item.getStatus();
                        if (status == null || status.isEmpty()) {
                            status = "未还";
                        }
                        Log.d(TAG, "Term: " + item.getTerm() + ", status from backend: " + status);
                        RepaymentPlanEntity entity = new RepaymentPlanEntity(
                            0,
                            item.getId(),
                            item.getOrderId(),
                            item.getTerm(),
                            item.getPrincipal(),
                            item.getInterest(),
                            item.getTotalAmount(),
                            status,
                            item.getRemainingPrincipal(),
                            item.getRemainingInterest(),
                            item.getDueDate() != null ? item.getDueDate() : "",
                            item.getActualPayDate() != null ? item.getActualPayDate() : ""
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

    public void updateLoanOrderProductInfo(int orderId, String productName, String nextRepaymentDate, @NonNull UpdateProductInfoCallback callback) {
        Log.d(TAG, "updateLoanOrderProductInfo: orderId=" + orderId + ", productName=" + productName + ", nextRepaymentDate=" + nextRepaymentDate);
        localDataSource.updateLoanOrderProductInfo(orderId, productName, nextRepaymentDate, new LocalDataSource.DataSourceCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Log.d(TAG, "updateLoanOrderProductInfo onSuccess");
                callback.onSuccess();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "updateLoanOrderProductInfo onError: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }

    public interface UpdateProductInfoCallback {
        void onSuccess();
        void onError(String errorMessage);
    }

    /**
     * 订单统计（GET /orders/statistics，后端聚合）：
     * 返回当前用户所有订单未还期数的待还总额/本金/利息，取代原逐单逐期本地 N+1 聚合
     */
    public void getOrderStatistics(@NonNull UnpaidStatsCallback callback) {
        String token = tokenManager.getToken();
        Log.d(TAG, "getOrderStatistics: fetching from backend");
        remoteDataSource.getUserOrderStatistics(token, new RemoteDataSource.NetworkCallback<OrderStatisticsResponse>() {
            @Override
            public void onSuccess(OrderStatisticsResponse response) {
                if (response != null && response.getCode() == 200 && response.getData() != null) {
                    OrderStatisticsResponse.StatisticsData data = response.getData();
                    double principal = data.getTotalOutstandingPrincipal() != null ? data.getTotalOutstandingPrincipal() : 0;
                    double interest = data.getTotalOutstandingInterest() != null ? data.getTotalOutstandingInterest() : 0;
                    double amount = data.getTotalOutstandingAmount() != null ? data.getTotalOutstandingAmount() : 0;
                    Log.d(TAG, "getOrderStatistics: principal=" + principal + ", interest=" + interest + ", amount=" + amount);
                    callback.onSuccess(principal, interest, amount);
                } else {
                    callback.onError(response != null ? response.getMessage() : "获取订单统计失败");
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "getOrderStatistics error: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }

    /**
     * 提前还款
     * @param orderId 订单ID
     * @param periods 提前还款期数（null=一次性结清剩余全部期数，1~剩余期数=提前偿还接下来N期）
     * @param callback 回调
     */
    public void earlyRepay(int orderId, Integer periods, @NonNull EarlyRepayCallback callback) {
        String token = tokenManager.getToken();
        Log.d(TAG, "earlyRepay, orderId: " + orderId + ", periods: " + periods);
        remoteDataSource.earlyRepay(token, orderId, periods, new RemoteDataSource.NetworkCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "Early repay success: " + response);
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to early repay: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }

    public void applyPostpone(int orderId, @NonNull PostponeCallback callback) {
        String token = tokenManager.getToken();
        Log.d(TAG, "applyPostpone, orderId: " + orderId);
        remoteDataSource.applyPostpone(token, orderId, new RemoteDataSource.NetworkCallback<String>() {
            @Override
            public void onSuccess(String response) {
                Log.d(TAG, "Apply postpone success: " + response);
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to apply postpone: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }

    public interface EarlyRepayCallback {
        void onSuccess(String message);
        void onError(String errorMessage);
    }

    public interface PostponeCallback {
        void onSuccess(String message);
        void onError(String errorMessage);
    }
}
