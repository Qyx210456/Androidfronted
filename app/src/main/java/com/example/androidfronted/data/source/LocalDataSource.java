package com.example.androidfronted.data.source;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import com.example.androidfronted.data.local.database.AppDatabase;
import com.example.androidfronted.data.local.dao.AuthTokenDao;
import com.example.androidfronted.data.local.dao.CertificationDao;
import com.example.androidfronted.data.local.dao.LoanProductDao;
import com.example.androidfronted.data.local.dao.UserDao;
import com.example.androidfronted.data.local.entity.AuthTokenEntity;
import com.example.androidfronted.data.local.entity.CertificationEntity;
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
    private final ExecutorService executor;
    private final Handler mainHandler;

    public LocalDataSource(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        this.userDao = database.userDao();
        this.authTokenDao = database.authTokenDao();
        this.certificationDao = database.certificationDao();
        this.loanProductDao = database.loanProductDao();
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void saveUser(UserEntity user) {
        executor.execute(() -> userDao.insert(user));
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
        executor.execute(() -> {
            AuthTokenEntity entity = new AuthTokenEntity(token, refreshToken);
            authTokenDao.insert(entity);
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

    public interface DataSourceCallback<T> {
        void onSuccess(T data);
        void onError(String errorMessage);
    }
}
