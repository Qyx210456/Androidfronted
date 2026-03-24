package com.example.androidfronted.data.api;

import com.example.androidfronted.data.model.LoanProductResponse;
import com.example.androidfronted.data.source.RemoteDataSource;

public class LoanProductApi {
    private final RemoteDataSource remoteDataSource;

    public LoanProductApi(RemoteDataSource remoteDataSource) {
        this.remoteDataSource = remoteDataSource;
    }

    public void getLoanProducts(RemoteDataSource.NetworkCallback<LoanProductResponse> callback) {
        remoteDataSource.getLoanProducts(callback);
    }
}
