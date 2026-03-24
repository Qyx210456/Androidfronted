package com.example.androidfronted.data.repository;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import com.example.androidfronted.data.model.ProductApplyRequest;
import com.example.androidfronted.data.source.LocalDataSource;
import com.example.androidfronted.data.source.RemoteDataSource;

public class LoanApplicationRepository {
    private static final String TAG = "LoanApplicationRepo";
    private static LoanApplicationRepository instance;

    private final LocalDataSource localDataSource;
    private final RemoteDataSource remoteDataSource;

    private LoanApplicationRepository(Context context) {
        this.localDataSource = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource(context);
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

    public interface CallbackResult {
        void onSuccess(String successMessage);
        void onError(String errorMessage);
    }
}
