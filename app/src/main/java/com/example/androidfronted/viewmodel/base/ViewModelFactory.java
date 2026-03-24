package com.example.androidfronted.viewmodel.base;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import android.app.Application;

public class ViewModelFactory extends ViewModelProvider.AndroidViewModelFactory {
    private final Application application;

    public ViewModelFactory(@NonNull Application application) {
        super(application);
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(com.example.androidfronted.viewmodel.auth.PasswordLoginViewModel.class)) {
            return (T) new com.example.androidfronted.viewmodel.auth.PasswordLoginViewModel(application);
        } else if (modelClass.isAssignableFrom(com.example.androidfronted.viewmodel.auth.PersonalInfoViewModel.class)) {
            return (T) new com.example.androidfronted.viewmodel.auth.PersonalInfoViewModel(application);
        } else if (modelClass.isAssignableFrom(com.example.androidfronted.viewmodel.auth.MyBankCardsViewModel.class)) {
            return (T) new com.example.androidfronted.viewmodel.auth.MyBankCardsViewModel(application);
        } else if (modelClass.isAssignableFrom(com.example.androidfronted.viewmodel.auth.IdCertViewModel.class)) {
            return (T) new com.example.androidfronted.viewmodel.auth.IdCertViewModel(application);
        } else if (modelClass.isAssignableFrom(com.example.androidfronted.viewmodel.auth.JobCertViewModel.class)) {
            return (T) new com.example.androidfronted.viewmodel.auth.JobCertViewModel(application);
        } else if (modelClass.isAssignableFrom(com.example.androidfronted.viewmodel.auth.PropertyCertViewModel.class)) {
            return (T) new com.example.androidfronted.viewmodel.auth.PropertyCertViewModel(application);
        } else if (modelClass.isAssignableFrom(com.example.androidfronted.viewmodel.auth.ThirdPartyCertViewModel.class)) {
            return (T) new com.example.androidfronted.viewmodel.auth.ThirdPartyCertViewModel(application);
        } else if (modelClass.isAssignableFrom(com.example.androidfronted.viewmodel.auth.VerifyCodeLoginViewModel.class)) {
            return (T) new com.example.androidfronted.viewmodel.auth.VerifyCodeLoginViewModel(application);
        } else if (modelClass.isAssignableFrom(com.example.androidfronted.viewmodel.loan.HomeViewModel.class)) {
            return (T) new com.example.androidfronted.viewmodel.loan.HomeViewModel(application);
        } else if (modelClass.isAssignableFrom(com.example.androidfronted.viewmodel.loan.ProductDetailViewModel.class)) {
            return (T) new com.example.androidfronted.viewmodel.loan.ProductDetailViewModel(application);
        } else if (modelClass.isAssignableFrom(com.example.androidfronted.viewmodel.loan.ProductApplyViewModel.class)) {
            return (T) new com.example.androidfronted.viewmodel.loan.ProductApplyViewModel(application);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}
