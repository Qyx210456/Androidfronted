package com.example.androidfronted.data.api;

import com.example.androidfronted.data.model.ApplicationListResponse;
import com.example.androidfronted.data.model.ApplicationDetailResponse;
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

    public void getMyApplications(String token, RemoteDataSource.NetworkCallback<ApplicationListResponse> callback) {
        remoteDataSource.getMyApplications(token, callback);
    }

    public void getApplicationDetail(String token, int applicationId, RemoteDataSource.NetworkCallback<ApplicationDetailResponse> callback) {
        remoteDataSource.getApplicationDetail(token, applicationId, callback);
    }

    public void withdrawApplication(String token, int applicationId, RemoteDataSource.NetworkCallback<String> callback) {
        remoteDataSource.withdrawApplication(token, applicationId, callback);
    }
}
