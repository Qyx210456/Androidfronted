package com.example.androidfronted.data.source;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.example.androidfronted.data.local.database.AppDatabase;
import com.example.androidfronted.data.local.dao.ApplicationDao;
import com.example.androidfronted.data.local.dao.ApplicationDetailDao;
import com.example.androidfronted.data.local.dao.AuthTokenDao;
import com.example.androidfronted.data.local.dao.CertificationDao;
import com.example.androidfronted.data.local.dao.LoanOrderDao;
import com.example.androidfronted.data.local.dao.LoanOrderDetailDao;
import com.example.androidfronted.data.local.dao.LoanProductDao;
import com.example.androidfronted.data.local.dao.UserDao;
import com.example.androidfronted.data.local.entity.ApplicationEntity;
import com.example.androidfronted.data.local.entity.ApplicationDetailEntity;
import com.example.androidfronted.data.local.entity.AuthTokenEntity;
import com.example.androidfronted.data.local.entity.CertificationEntity;
import com.example.androidfronted.data.local.entity.LoanOrderEntity;
import com.example.androidfronted.data.local.entity.LoanOrderDetailEntity;
import com.example.androidfronted.data.local.entity.LoanProductEntity;
import com.example.androidfronted.data.local.entity.UserEntity;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LocalDataSource {
    private final UserDao userDao;
    private final AuthTokenDao authTokenDao;
    private final CertificationDao certificationDao;
    private final LoanProductDao loanProductDao;
    private final ApplicationDao applicationDao;
    private final ApplicationDetailDao applicationDetailDao;
    private final LoanOrderDao loanOrderDao;
    private final LoanOrderDetailDao loanOrderDetailDao;
    private final ExecutorService executor;
    private final Handler mainHandler;

    public LocalDataSource(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.userDao = database.userDao();
        this.authTokenDao = database.authTokenDao();
        this.certificationDao = database.certificationDao();
        this.loanProductDao = database.loanProductDao();
        this.applicationDao = database.applicationDao();
        this.applicationDetailDao = database.applicationDetailDao();
        this.loanOrderDao = database.loanOrderDao();
        this.loanOrderDetailDao = database.loanOrderDetailDao();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void saveUser(UserEntity user) {
        executor.execute(() -> userDao.insert(user));
    }

    public void getUser(DataSourceCallback<UserEntity> callback) {
        executor.execute(() -> {
            try {
                UserEntity result = userDao.getUser();
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void getUserById(int userId, DataSourceCallback<UserEntity> callback) {
        executor.execute(() -> {
            try {
                UserEntity result = userDao.getUserById(userId);
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void saveToken(String token, String refreshToken) {
        android.util.Log.d("LocalDataSource", "saveToken called, token: " + (token != null ? "not null" : "null"));
        executor.execute(() -> {
            Integer userId = com.example.androidfronted.util.JWTUtils.getUserIdFromToken(token);
            android.util.Log.d("LocalDataSource", "UserId from token: " + userId);
            AuthTokenEntity entity = new AuthTokenEntity(token, refreshToken, userId);
            authTokenDao.insert(entity);
            android.util.Log.d("LocalDataSource", "Token saved to database");
        });
    }

    public void getToken(DataSourceCallback<AuthTokenEntity> callback) {
        executor.execute(() -> {
            try {
                AuthTokenEntity result = authTokenDao.getToken();
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void clearToken() {
        executor.execute(() -> authTokenDao.deleteToken());
    }

    public void clearAuthData() {
        executor.execute(() -> {
            authTokenDao.deleteToken();
            userDao.deleteAll();
            certificationDao.deleteAll();
        });
    }

    public void saveCertification(CertificationEntity certification) {
        executor.execute(() -> certificationDao.insert(certification));
    }

    public void getCertificationByUserId(int userId, DataSourceCallback<CertificationEntity> callback) {
        executor.execute(() -> {
            try {
                CertificationEntity result = certificationDao.getCertificationByUserId(userId);
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void saveLoanProducts(List<LoanProductEntity> products, DataSourceCallback<Void> callback) {
        executor.execute(() -> {
            try {
                loanProductDao.deleteAllProducts();
                loanProductDao.insertAll(products);
                mainHandler.post(() -> callback.onSuccess(null));
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void getAllLoanProducts(DataSourceCallback<List<LoanProductEntity>> callback) {
        executor.execute(() -> {
            try {
                List<LoanProductEntity> result = loanProductDao.getAllProducts();
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void getLoanProductById(int productId, DataSourceCallback<LoanProductEntity> callback) {
        executor.execute(() -> {
            try {
                LoanProductEntity result = loanProductDao.getProductById(productId);
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void saveApplications(List<ApplicationEntity> applications) {
        executor.execute(() -> {
            applicationDao.deleteAll();
            applicationDao.insertAll(applications);
        });
    }

    public void getAllApplications(DataSourceCallback<List<ApplicationEntity>> callback) {
        executor.execute(() -> {
            try {
                List<ApplicationEntity> result = applicationDao.getAllApplications();
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void getApplicationsByStatus(String status, DataSourceCallback<List<ApplicationEntity>> callback) {
        executor.execute(() -> {
            try {
                List<ApplicationEntity> result = applicationDao.getApplicationsByStatus(status);
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void clearApplications() {
        executor.execute(() -> applicationDao.deleteAll());
    }

    public void saveApplicationDetail(ApplicationDetailEntity detail) {
        executor.execute(() -> applicationDetailDao.insert(detail));
    }

    public void getApplicationDetail(int id, DataSourceCallback<ApplicationDetailEntity> callback) {
        executor.execute(() -> {
            try {
                ApplicationDetailEntity result = applicationDetailDao.getById(id);
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void deleteApplicationDetail(int id) {
        executor.execute(() -> applicationDetailDao.deleteById(id));
    }

    public void clearApplicationDetails() {
        executor.execute(() -> applicationDetailDao.clearAll());
    }

    public void saveLoanOrders(List<LoanOrderEntity> orders) {
        executor.execute(() -> {
            loanOrderDao.deleteAll();
            loanOrderDao.insertAll(orders);
        });
    }

    public void getAllLoanOrders(DataSourceCallback<List<LoanOrderEntity>> callback) {
        executor.execute(() -> {
            try {
                List<LoanOrderEntity> result = loanOrderDao.getAllLoanOrders();
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void getLoanOrdersByStatus(String status, DataSourceCallback<List<LoanOrderEntity>> callback) {
        executor.execute(() -> {
            try {
                List<LoanOrderEntity> result = loanOrderDao.getLoanOrdersByStatus(status);
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void clearLoanOrders() {
        executor.execute(() -> loanOrderDao.deleteAll());
    }

    public void saveLoanOrderDetail(LoanOrderDetailEntity detail) {
        executor.execute(() -> loanOrderDetailDao.insert(detail));
    }

    public void getLoanOrderDetail(int id, DataSourceCallback<LoanOrderDetailEntity> callback) {
        executor.execute(() -> {
            try {
                LoanOrderDetailEntity result = loanOrderDetailDao.getById(id);
                mainHandler.post(() -> callback.onSuccess(result));
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        });
    }

    public void clearLoanOrderDetails() {
        executor.execute(() -> loanOrderDetailDao.deleteAll());
    }

    public interface DataSourceCallback<T> {
        void onSuccess(T data);
        void onError(String errorMessage);
    }
}
