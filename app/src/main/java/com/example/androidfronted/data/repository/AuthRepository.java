package com.example.androidfronted.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.androidfronted.data.model.LoginRequest;
import com.example.androidfronted.data.model.LoginResponse;
import com.example.androidfronted.data.model.RegisterRequest;
import com.example.androidfronted.data.model.RegisterResponse;
import com.example.androidfronted.data.model.AuthSubmitResponse;
import com.example.androidfronted.data.model.CertInfoResponse;
import com.example.androidfronted.data.source.LocalDataSource;
import com.example.androidfronted.data.source.RemoteDataSource;

public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private static AuthRepository instance;

    private final LocalDataSource localDataSource;
    private final RemoteDataSource remoteDataSource;

    private AuthRepository(Context context) {
        this.localDataSource = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource(context);
    }

    public static synchronized AuthRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AuthRepository(context.getApplicationContext());
        }
        return instance;
    }

    public void login(@NonNull LoginRequest request, @NonNull AuthCallback<LoginResponse> callback) {
        remoteDataSource.login(request, new RemoteDataSource.NetworkCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse response) {
                Log.d(TAG, "Login onSuccess, response: " + (response != null ? "not null" : "null"));
                if (response != null && response.getData() != null) {
                    String token = response.getData().getToken();
                    Log.d(TAG, "Token from response: " + (token != null ? "not null" : "null"));
                    if (token != null) {
                        Log.d(TAG, "Saving token to local database");
                        localDataSource.saveToken(token, null);
                    }
                }
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errorMessage) {
                Log.d(TAG, "Login onError: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }

    public void register(@NonNull RegisterRequest request, @NonNull AuthCallback<RegisterResponse> callback) {
        remoteDataSource.register(request, new RemoteDataSource.NetworkCallback<RegisterResponse>() {
            @Override
            public void onSuccess(RegisterResponse response) {
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public void refreshToken(String refreshToken, @NonNull AuthCallback<LoginResponse> callback) {
        remoteDataSource.refreshToken(refreshToken, new RemoteDataSource.NetworkCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse response) {
                if (response != null && response.getData() != null) {
                    String token = response.getData().getToken();
                    if (token != null) {
                        localDataSource.saveToken(token, refreshToken);
                    }
                }
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errorMessage) {
                callback.onError(errorMessage);
            }
        });
    }

    public void getCertInfo(@NonNull AuthCallback<CertInfoResponse> callback) {
        Log.d(TAG, "getCertInfo called");
        localDataSource.getToken(new LocalDataSource.DataSourceCallback<com.example.androidfronted.data.local.entity.AuthTokenEntity>() {
            @Override
            public void onSuccess(com.example.androidfronted.data.local.entity.AuthTokenEntity tokenEntity) {
                Log.d(TAG, "getToken onSuccess, tokenEntity: " + (tokenEntity != null ? "not null" : "null"));
                if (tokenEntity == null || tokenEntity.getToken() == null || tokenEntity.getToken().isEmpty()) {
                    Log.d(TAG, "Token is null or empty");
                    callback.onError("未登录，请先登录");
                    return;
                }
                
                // 直接请求网络获取最新数据，本地数据已在 ViewModel 构造函数中加载
                Integer userId = tokenEntity.getUserId();
                Log.d(TAG, "UserId from token: " + userId);
                Log.d(TAG, "Now fetching latest cert info from network");
                fetchRemoteCertInfo(tokenEntity.getToken(), userId, callback);
            }

            @Override
            public void onError(String errorMessage) {
                Log.d(TAG, "getToken onError: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }
    
    private void fetchRemoteCertInfo(String token, Integer userId, @NonNull AuthCallback<CertInfoResponse> callback) {
        Log.d(TAG, "fetchRemoteCertInfo called, userId: " + userId);
        remoteDataSource.getCertInfo(token, new RemoteDataSource.NetworkCallback<CertInfoResponse>() {
            @Override
            public void onSuccess(CertInfoResponse response) {
                Log.d(TAG, "Remote cert info fetch success, response: " + (response != null ? "not null" : "null"));
                // 网络请求成功后，更新本地数据
                if (response != null && response.getData() != null && userId != null) {
                    Log.d(TAG, "Updating local certification data for userId: " + userId);
                    CertInfoResponse.CertInfoData data = response.getData();
                    CertInfoResponse.UserCert userCert = data.getUserCert();
                    CertInfoResponse.WorkCert workCert = data.getWorkCert();
                    CertInfoResponse.TriCert triCert = data.getTriCert();
                    CertInfoResponse.ImmovablesCert immovablesCert = data.getImmovablesCert();
                    
                    if (userCert != null) {
                        Log.d(TAG, "Creating certification entity for userId: " + userId);
                        com.example.androidfronted.data.local.entity.CertificationEntity certification = new com.example.androidfronted.data.local.entity.CertificationEntity(
                            userId,
                            userCert.getIdCard(),
                            userCert.getCreditScore(),
                            userCert.getBankCardId(),
                            userCert.getWorkCertId(),
                            workCert != null ? workCert.getEmploymentCertPath() : null,
                            workCert != null ? workCert.getSalaryCertPath() : null,
                            userCert.getTriCertId(),
                            triCert != null ? triCert.getSocialSecurityPath() : null,
                            triCert != null ? triCert.getCreditReportPath() : null,
                            userCert.getImmovableCertId(),
                            immovablesCert != null ? immovablesCert.getPropertyCertPath() : null,
                            immovablesCert != null ? immovablesCert.getCarCertPath() : null
                        );
                        Log.d(TAG, "Saving certification to local database");
                        localDataSource.saveCertification(certification);
                    }
                }
                callback.onSuccess(response);
            }

            @Override
            public void onError(String errorMessage) {
                Log.d(TAG, "Remote cert info fetch error: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }

    public void submitBasicCert(String idCard, @NonNull AuthCallback<AuthSubmitResponse> callback) {
        Log.d(TAG, "submitBasicCert called, idCard: " + idCard);
        localDataSource.getToken(new LocalDataSource.DataSourceCallback<com.example.androidfronted.data.local.entity.AuthTokenEntity>() {
            @Override
            public void onSuccess(com.example.androidfronted.data.local.entity.AuthTokenEntity tokenEntity) {
                if (tokenEntity == null || tokenEntity.getToken() == null || tokenEntity.getToken().isEmpty()) {
                    Log.e(TAG, "submitBasicCert, token is null or empty");
                    callback.onError("未登录，请先登录");
                    return;
                }
                Log.d(TAG, "submitBasicCert, token retrieved successfully");
                remoteDataSource.submitBasicCert(tokenEntity.getToken(), idCard,
                        new RemoteDataSource.NetworkCallback<AuthSubmitResponse>() {
                            @Override
                            public void onSuccess(AuthSubmitResponse response) {
                                Log.d(TAG, "submitBasicCert, onSuccess");
                                // 上传成功后，刷新本地认证信息
                                if (tokenEntity.getUserId() != null) {
                                    fetchRemoteCertInfo(tokenEntity.getToken(), tokenEntity.getUserId(), new AuthCallback<CertInfoResponse>() {
                                        @Override
                                        public void onSuccess(CertInfoResponse certResponse) {
                                            // 本地数据已在 fetchRemoteCertInfo 中更新
                                        }

                                        @Override
                                        public void onError(String errorMessage) {
                                            // 刷新失败，不影响主流程
                                        }
                                    });
                                }
                                callback.onSuccess(response);
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Log.e(TAG, "submitBasicCert, onError: " + errorMessage);
                                callback.onError(errorMessage);
                            }
                        });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "submitBasicCert, getToken error: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }

    public void submitOtherCert(String bankCardId,
                          okhttp3.RequestBody propertyFile, okhttp3.RequestBody carFile,
                          okhttp3.RequestBody employmentFile, okhttp3.RequestBody salaryFile,
                          okhttp3.RequestBody socialSecurityFile, okhttp3.RequestBody creditReportFile,
                          @NonNull AuthCallback<AuthSubmitResponse> callback) {
        Log.d(TAG, "submitOtherCert called, bankCardId: " + bankCardId);
        localDataSource.getToken(new LocalDataSource.DataSourceCallback<com.example.androidfronted.data.local.entity.AuthTokenEntity>() {
            @Override
            public void onSuccess(com.example.androidfronted.data.local.entity.AuthTokenEntity tokenEntity) {
                if (tokenEntity == null || tokenEntity.getToken() == null || tokenEntity.getToken().isEmpty()) {
                    Log.e(TAG, "submitOtherCert, token is null or empty");
                    callback.onError("未登录，请先登录");
                    return;
                }
                Log.d(TAG, "submitOtherCert, token retrieved successfully");
                remoteDataSource.submitOtherCert(tokenEntity.getToken(), bankCardId, propertyFile, carFile,
                        employmentFile, salaryFile, socialSecurityFile, creditReportFile,
                        new RemoteDataSource.NetworkCallback<AuthSubmitResponse>() {
                            @Override
                            public void onSuccess(AuthSubmitResponse response) {
                                Log.d(TAG, "submitOtherCert, onSuccess");
                                // 上传成功后，刷新本地认证信息
                                if (tokenEntity.getUserId() != null) {
                                    fetchRemoteCertInfo(tokenEntity.getToken(), tokenEntity.getUserId(), new AuthCallback<CertInfoResponse>() {
                                        @Override
                                        public void onSuccess(CertInfoResponse certResponse) {
                                            // 本地数据已在 fetchRemoteCertInfo 中更新
                                        }

                                        @Override
                                        public void onError(String errorMessage) {
                                            // 刷新失败，不影响主流程
                                        }
                                    });
                                }
                                callback.onSuccess(response);
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Log.e(TAG, "submitOtherCert, onError: " + errorMessage);
                                callback.onError(errorMessage);
                            }
                        });
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "submitOtherCert, getToken error: " + errorMessage);
                callback.onError(errorMessage);
            }
        });
    }

    public void logout() {
        localDataSource.clearToken();
    }

    public void getToken(@NonNull LocalDataSource.DataSourceCallback<com.example.androidfronted.data.local.entity.AuthTokenEntity> callback) {
        localDataSource.getToken(callback);
    }

    public void getLocalIdCertState(@NonNull AuthCallback<com.example.androidfronted.data.model.CertState> callback) {
        Log.d(TAG, "getLocalIdCertState called");
        getLocalCertStateByType(callback, certification -> 
            certification != null && certification.getIdCard() != null && !certification.getIdCard().isEmpty()
        );
    }

    public void getLocalJobCertState(@NonNull AuthCallback<com.example.androidfronted.data.model.CertState> callback) {
        Log.d(TAG, "getLocalJobCertState called");
        getLocalCertStateByType(callback, certification -> 
            certification != null && (certification.getWorkCertId() > 0 ||
                    (certification.getEmploymentCertPath() != null && !certification.getEmploymentCertPath().isEmpty()) ||
                    (certification.getSalaryCertPath() != null && !certification.getSalaryCertPath().isEmpty()))
        );
    }

    public void getLocalPropertyCertState(@NonNull AuthCallback<com.example.androidfronted.data.model.CertState> callback) {
        Log.d(TAG, "getLocalPropertyCertState called");
        getLocalCertStateByType(callback, certification -> 
            certification != null && (certification.getImmovableCertId() > 0 ||
                    (certification.getPropertyCertPath() != null && !certification.getPropertyCertPath().isEmpty()) ||
                    (certification.getCarCertPath() != null && !certification.getCarCertPath().isEmpty()))
        );
    }

    public void getLocalThirdPartyCertState(@NonNull AuthCallback<com.example.androidfronted.data.model.CertState> callback) {
        Log.d(TAG, "getLocalThirdPartyCertState called");
        getLocalCertStateByType(callback, certification -> 
            certification != null && (certification.getTriCertId() > 0 ||
                    (certification.getSocialSecurityPath() != null && !certification.getSocialSecurityPath().isEmpty()) ||
                    (certification.getCreditReportPath() != null && !certification.getCreditReportPath().isEmpty()))
        );
    }

    public void getLocalBankCardState(@NonNull AuthCallback<com.example.androidfronted.data.model.CertState> callback) {
        Log.d(TAG, "getLocalBankCardState called");
        getLocalCertStateByType(callback, certification -> 
            certification != null && certification.getBankCardId() != null && !certification.getBankCardId().isEmpty()
        );
    }

    public void getLocalBankCardData(@NonNull AuthCallback<String> callback) {
        Log.d(TAG, "getLocalBankCardData called");
        localDataSource.getToken(new LocalDataSource.DataSourceCallback<com.example.androidfronted.data.local.entity.AuthTokenEntity>() {
            @Override
            public void onSuccess(com.example.androidfronted.data.local.entity.AuthTokenEntity tokenEntity) {
                Log.d(TAG, "getLocalBankCardData, getToken onSuccess, tokenEntity: " + (tokenEntity != null ? "not null" : "null"));
                if (tokenEntity != null && tokenEntity.getUserId() != null) {
                    localDataSource.getCertificationByUserId(tokenEntity.getUserId(), new LocalDataSource.DataSourceCallback<com.example.androidfronted.data.local.entity.CertificationEntity>() {
                        @Override
                        public void onSuccess(com.example.androidfronted.data.local.entity.CertificationEntity certification) {
                            Log.d(TAG, "getLocalBankCardData, local certification found: " + (certification != null ? "not null" : "null"));
                            if (certification != null && certification.getBankCardId() != null && !certification.getBankCardId().isEmpty()) {
                                callback.onSuccess(certification.getBankCardId());
                                Log.d(TAG, "getLocalBankCardData, bankCardId: " + certification.getBankCardId());
                            } else {
                                callback.onSuccess(null);
                                Log.d(TAG, "getLocalBankCardData, no bankCardId found");
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.d(TAG, "getLocalBankCardData, getCertificationByUserId error: " + errorMessage);
                            callback.onSuccess(null);
                        }
                    });
                } else {
                    Log.d(TAG, "getLocalBankCardData, no token or userId found");
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.d(TAG, "getLocalBankCardData, getToken error: " + errorMessage);
                callback.onSuccess(null);
            }
        });
    }

    public void getLocalJobCertData(@NonNull AuthCallback<com.example.androidfronted.data.model.CertInfoResponse.WorkCert> callback) {
        Log.d(TAG, "getLocalJobCertData called");
        localDataSource.getToken(new LocalDataSource.DataSourceCallback<com.example.androidfronted.data.local.entity.AuthTokenEntity>() {
            @Override
            public void onSuccess(com.example.androidfronted.data.local.entity.AuthTokenEntity tokenEntity) {
                Log.d(TAG, "getLocalJobCertData, getToken onSuccess, tokenEntity: " + (tokenEntity != null ? "not null" : "null"));
                if (tokenEntity != null && tokenEntity.getUserId() != null) {
                    localDataSource.getCertificationByUserId(tokenEntity.getUserId(), new LocalDataSource.DataSourceCallback<com.example.androidfronted.data.local.entity.CertificationEntity>() {
                        @Override
                        public void onSuccess(com.example.androidfronted.data.local.entity.CertificationEntity certification) {
                            Log.d(TAG, "getLocalJobCertData, local certification found: " + (certification != null ? "not null" : "null"));
                            if (certification != null) {
                                com.example.androidfronted.data.model.CertInfoResponse.WorkCert workCert = new com.example.androidfronted.data.model.CertInfoResponse.WorkCert();
                                workCert.setWorkCertId(certification.getWorkCertId());
                                workCert.setEmploymentCertPath(certification.getEmploymentCertPath());
                                workCert.setSalaryCertPath(certification.getSalaryCertPath());
                                callback.onSuccess(workCert);
                                Log.d(TAG, "getLocalJobCertData, workCert: " + workCert);
                            } else {
                                callback.onSuccess(null);
                                Log.d(TAG, "getLocalJobCertData, no workCert found");
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.d(TAG, "getLocalJobCertData, getCertificationByUserId error: " + errorMessage);
                            callback.onSuccess(null);
                        }
                    });
                } else {
                    Log.d(TAG, "getLocalJobCertData, no token or userId found");
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.d(TAG, "getLocalJobCertData, getToken error: " + errorMessage);
                callback.onSuccess(null);
            }
        });
    }

    public void getLocalCertState(@NonNull AuthCallback<com.example.androidfronted.data.model.CertState> callback) {
        Log.d(TAG, "getLocalCertState called");
        getLocalCertStateByType(callback, certification -> 
            certification != null && (
                (certification.getIdCard() != null && !certification.getIdCard().isEmpty()) ||
                (certification.getBankCardId() != null && !certification.getBankCardId().isEmpty()) ||
                (certification.getWorkCertId() > 0 ||
                        (certification.getEmploymentCertPath() != null && !certification.getEmploymentCertPath().isEmpty()) ||
                        (certification.getSalaryCertPath() != null && !certification.getSalaryCertPath().isEmpty())) ||
                (certification.getTriCertId() > 0 ||
                        (certification.getSocialSecurityPath() != null && !certification.getSocialSecurityPath().isEmpty()) ||
                        (certification.getCreditReportPath() != null && !certification.getCreditReportPath().isEmpty())) ||
                (certification.getImmovableCertId() > 0 ||
                        (certification.getPropertyCertPath() != null && !certification.getPropertyCertPath().isEmpty()) ||
                        (certification.getCarCertPath() != null && !certification.getCarCertPath().isEmpty()))
            )
        );
    }

    private void getLocalCertStateByType(@NonNull AuthCallback<com.example.androidfronted.data.model.CertState> callback, 
                                        java.util.function.Function<com.example.androidfronted.data.local.entity.CertificationEntity, Boolean> isCertified) {
        localDataSource.getToken(new LocalDataSource.DataSourceCallback<com.example.androidfronted.data.local.entity.AuthTokenEntity>() {
            @Override
            public void onSuccess(com.example.androidfronted.data.local.entity.AuthTokenEntity tokenEntity) {
                Log.d(TAG, "getLocalCertStateByType, getToken onSuccess, tokenEntity: " + (tokenEntity != null ? "not null" : "null"));
                if (tokenEntity != null && tokenEntity.getUserId() != null) {
                    localDataSource.getCertificationByUserId(tokenEntity.getUserId(), new LocalDataSource.DataSourceCallback<com.example.androidfronted.data.local.entity.CertificationEntity>() {
                        @Override
                        public void onSuccess(com.example.androidfronted.data.local.entity.CertificationEntity certification) {
                            Log.d(TAG, "getLocalCertStateByType, local certification found: " + (certification != null ? "not null" : "null"));
                            if (isCertified.apply(certification)) {
                                callback.onSuccess(com.example.androidfronted.data.model.CertState.CERTIFIED);
                                Log.d(TAG, "getLocalCertStateByType, setting state to CERTIFIED");
                            } else {
                                callback.onSuccess(com.example.androidfronted.data.model.CertState.NOT_CERTIFIED);
                                Log.d(TAG, "getLocalCertStateByType, setting state to NOT_CERTIFIED");
                            }
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.d(TAG, "getLocalCertStateByType, getCertificationByUserId error: " + errorMessage);
                            callback.onSuccess(com.example.androidfronted.data.model.CertState.NOT_CERTIFIED);
                        }
                    });
                } else {
                    Log.d(TAG, "getLocalCertStateByType, no token or userId found, setting state to NOT_CERTIFIED");
                    callback.onSuccess(com.example.androidfronted.data.model.CertState.NOT_CERTIFIED);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.d(TAG, "getLocalCertStateByType, getToken error: " + errorMessage);
                callback.onSuccess(com.example.androidfronted.data.model.CertState.NOT_CERTIFIED);
            }
        });
    }

    public void getLocalCertInfo(@NonNull AuthCallback<com.example.androidfronted.data.local.entity.CertificationEntity> callback) {
        Log.d(TAG, "getLocalCertInfo called");
        localDataSource.getToken(new LocalDataSource.DataSourceCallback<com.example.androidfronted.data.local.entity.AuthTokenEntity>() {
            @Override
            public void onSuccess(com.example.androidfronted.data.local.entity.AuthTokenEntity tokenEntity) {
                Log.d(TAG, "getLocalCertInfo, getToken onSuccess, tokenEntity: " + (tokenEntity != null ? "not null" : "null"));
                if (tokenEntity != null && tokenEntity.getUserId() != null) {
                    localDataSource.getCertificationByUserId(tokenEntity.getUserId(), new LocalDataSource.DataSourceCallback<com.example.androidfronted.data.local.entity.CertificationEntity>() {
                        @Override
                        public void onSuccess(com.example.androidfronted.data.local.entity.CertificationEntity certification) {
                            Log.d(TAG, "getLocalCertInfo, local certification found: " + (certification != null ? "not null" : "null"));
                            callback.onSuccess(certification);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            Log.d(TAG, "getLocalCertInfo, getCertificationByUserId error: " + errorMessage);
                            callback.onSuccess(null);
                        }
                    });
                } else {
                    Log.d(TAG, "getLocalCertInfo, no token or userId found");
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onError(String errorMessage) {
                Log.d(TAG, "getLocalCertInfo, getToken error: " + errorMessage);
                callback.onSuccess(null);
            }
        });
    }

    public interface AuthCallback<T> {
        void onSuccess(T response);
        void onError(String errorMessage);
    }
}
