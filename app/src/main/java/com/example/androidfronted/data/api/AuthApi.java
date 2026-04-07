package com.example.androidfronted.data.api;

import com.example.androidfronted.data.model.AuthSubmitResponse;
import com.example.androidfronted.data.model.CertInfoResponse;
import com.example.androidfronted.data.model.LoginRequest;
import com.example.androidfronted.data.model.LoginResponse;
import com.example.androidfronted.data.model.RegisterRequest;
import com.example.androidfronted.data.model.RegisterResponse;
import com.example.androidfronted.data.source.RemoteDataSource;
import okhttp3.RequestBody;

public class AuthApi {
    private final RemoteDataSource remoteDataSource;

    public AuthApi(RemoteDataSource remoteDataSource) {
        this.remoteDataSource = remoteDataSource;
    }

    public void login(LoginRequest request, RemoteDataSource.NetworkCallback<LoginResponse> callback) {
        remoteDataSource.login(request, callback);
    }

    public void register(RegisterRequest request, RemoteDataSource.NetworkCallback<RegisterResponse> callback) {
        remoteDataSource.register(request, callback);
    }

    public void getCertInfo(String token, RemoteDataSource.NetworkCallback<CertInfoResponse> callback) {
        remoteDataSource.getCertInfo(token, callback);
    }

    public void submitBasicCert(String token, String idCard, String realName, RemoteDataSource.NetworkCallback<AuthSubmitResponse> callback) {
        remoteDataSource.submitBasicCert(token, idCard, realName, callback);
    }

    public void submitOtherCert(String token, String bankCardId,
                            RequestBody propertyFile, RequestBody carFile,
                            RequestBody employmentFile, RequestBody salaryFile,
                            RequestBody socialSecurityFile, RequestBody creditReportFile,
                            RemoteDataSource.NetworkCallback<AuthSubmitResponse> callback) {
        remoteDataSource.submitOtherCert(token, bankCardId, propertyFile, carFile,
                employmentFile, salaryFile, socialSecurityFile, creditReportFile, callback);
    }

    public void refreshToken(String refreshToken, RemoteDataSource.NetworkCallback<LoginResponse> callback) {
        remoteDataSource.refreshToken(refreshToken, callback);
    }
}
