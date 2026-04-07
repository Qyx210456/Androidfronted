package com.example.androidfronted.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.androidfronted.data.local.entity.ApplicationDetailEntity;
import com.example.androidfronted.data.local.entity.ApplicationEntity;
import com.example.androidfronted.data.model.ApplicationDetailResponse;
import com.example.androidfronted.data.model.ApplicationListResponse;
import com.example.androidfronted.data.model.ProductApplyRequest;
import com.example.androidfronted.data.source.LocalDataSource;
import com.example.androidfronted.data.source.RemoteDataSource;
import com.example.androidfronted.util.TokenManager;
import java.util.ArrayList;
import java.util.List;

public class LoanApplicationRepository {
    private static final String TAG = "LoanApplicationRepo";
    private static LoanApplicationRepository instance;

    private final LocalDataSource localDataSource;
    private final RemoteDataSource remoteDataSource;
    private final TokenManager tokenManager;

    private LoanApplicationRepository(Context context) {
        this.localDataSource = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource(context);
        this.tokenManager = new TokenManager(context);
    }

    public static synchronized LoanApplicationRepository getInstance(Context context) {
        if (instance == null) {
            instance = new LoanApplicationRepository(context.getApplicationContext());
        }
        return instance;
    }

    public void submitApplication(@NonNull ProductApplyRequest request, @NonNull CallbackResult callback) {
        remoteDataSource.submitLoanApplication(request, new RemoteDataSource.NetworkCallback<String>() {
            @Override
            public void onSuccess(String successMessage) {
                Log.d(TAG, "Loan application submitted successfully");
                callback.onSuccess(successMessage);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to submit loan application: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }

    public void getMyApplications(@NonNull ApplicationsCallback callback) {
        String token = tokenManager.getToken();
        Log.d(TAG, "getMyApplications, getToken from TokenManager: " + (token != null ? "not null" : "null"));
        remoteDataSource.getMyApplications(token, new RemoteDataSource.NetworkCallback<ApplicationListResponse>() {
            @Override
            public void onSuccess(ApplicationListResponse response) {
                if (response != null && response.getData() != null) {
                    Log.d(TAG, "Got " + response.getData().size() + " applications");
                    
                    List<ApplicationEntity> entities = new ArrayList<>();
                    for (ApplicationListResponse.ApplicationRecord record : response.getData()) {
                        ApplicationEntity entity = new ApplicationEntity(
                            record.getApplicationId(),
                            record.getProductName(),
                            record.getLoanAmount(),
                            record.getStatus(),
                            record.getApplyTime(),
                            record.getRejectReason()
                        );
                        entities.add(entity);
                    }
                    
                    localDataSource.saveApplications(entities);
                    
                    callback.onSuccess(entities);
                } else {
                    callback.onError("获取申请记录失败");
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to get applications: " + errorMessage);
                localDataSource.getAllApplications(new LocalDataSource.DataSourceCallback<List<ApplicationEntity>>() {
                    @Override
                    public void onSuccess(List<ApplicationEntity> data) {
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

    public void getApplicationsFromLocal(@NonNull ApplicationsCallback callback) {
        localDataSource.getAllApplications(new LocalDataSource.DataSourceCallback<List<ApplicationEntity>>() {
            @Override
            public void onSuccess(List<ApplicationEntity> data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public void getApplicationsByStatusFromLocal(String status, @NonNull ApplicationsCallback callback) {
        localDataSource.getApplicationsByStatus(status, new LocalDataSource.DataSourceCallback<List<ApplicationEntity>>() {
            @Override
            public void onSuccess(List<ApplicationEntity> data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public interface CallbackResult {
        void onSuccess(String successMessage);
        void onError(String errorMessage);
    }

    public interface ApplicationsCallback {
        void onSuccess(List<ApplicationEntity> applications);
        void onError(String errorMessage);
    }

    public interface ApplicationDetailCallback {
        void onSuccess(ApplicationDetailEntity detail);
        void onError(String errorMessage);
    }

    public void getApplicationDetail(int applicationId, @NonNull ApplicationDetailCallback callback) {
        String token = tokenManager.getToken();
        Log.d(TAG, "getApplicationDetail, getToken from TokenManager: " + (token != null ? "not null" : "null"));
        remoteDataSource.getApplicationDetail(token, applicationId, new RemoteDataSource.NetworkCallback<ApplicationDetailResponse>() {
            @Override
            public void onSuccess(ApplicationDetailResponse response) {
                if (response != null && response.getData() != null) {
                    Log.d(TAG, "Got application detail for id: " + applicationId);
                    
                    ApplicationDetailResponse.ApplicationDetail apiDetail = response.getData();
                    ApplicationDetailEntity entity = new ApplicationDetailEntity(
                        apiDetail.getId(),
                        apiDetail.getUserId(),
                        apiDetail.getProductId(),
                        apiDetail.getStatus(),
                        apiDetail.getLoanAmount(),
                        apiDetail.getInterestRate(),
                        apiDetail.getLoanPeriod(),
                        apiDetail.getTerm(),
                        apiDetail.getRepaidType(),
                        apiDetail.getRejectReason(),
                        apiDetail.getApplyTime(),
                        apiDetail.getReviewTime(),
                        ""
                    );
                    
                    localDataSource.saveApplicationDetail(entity);
                    
                    callback.onSuccess(entity);
                } else {
                    callback.onError("获取申请详情失败");
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to get application detail: " + errorMessage);
                localDataSource.getApplicationDetail(applicationId, new LocalDataSource.DataSourceCallback<ApplicationDetailEntity>() {
                    @Override
                    public void onSuccess(ApplicationDetailEntity data) {
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

    public void getApplicationDetailFromLocal(int applicationId, @NonNull ApplicationDetailCallback callback) {
        localDataSource.getApplicationDetail(applicationId, new LocalDataSource.DataSourceCallback<ApplicationDetailEntity>() {
            @Override
            public void onSuccess(ApplicationDetailEntity data) {
                callback.onSuccess(data);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public void withdrawApplication(int applicationId, @NonNull CallbackResult callback) {
        String token = tokenManager.getToken();
        Log.d(TAG, "withdrawApplication, getToken from TokenManager: " + (token != null ? "not null" : "null"));
        remoteDataSource.withdrawApplication(token, applicationId, new RemoteDataSource.NetworkCallback<String>() {
            @Override
            public void onSuccess(String successMessage) {
                Log.d(TAG, "Application withdrawn successfully: " + successMessage);
                callback.onSuccess(successMessage);
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Failed to withdraw application: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }
}
