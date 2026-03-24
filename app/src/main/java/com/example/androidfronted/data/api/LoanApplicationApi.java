package com.example.androidfronted.data.api;

import com.example.androidfronted.data.model.ProductApplyRequest;
import com.example.androidfronted.data.source.RemoteDataSource;

public class LoanApplicationApi {
    private final RemoteDataSource remoteDataSource;

    public LoanApplicationApi(RemoteDataSource remoteDataSource) {
        this.remoteDataSource = remoteDataSource;
    }

    public void submitApplication(ProductApplyRequest request, RemoteDataSource.NetworkCallback<String> callback) {
        remoteDataSource.submitLoanApplication(request, callback);
    }
}
