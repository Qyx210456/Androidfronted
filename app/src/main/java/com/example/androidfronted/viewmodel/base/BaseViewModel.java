package com.example.androidfronted.viewmodel.base;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import android.app.Application;

public class BaseViewModel extends AndroidViewModel {
    protected final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    protected final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    protected final SingleLiveEvent<NavigationEvent> navigationEvent = new SingleLiveEvent<>();

    public BaseViewModel(@NonNull Application application) {
        super(application);
    }

    public MutableLiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public SingleLiveEvent<NavigationEvent> getNavigationEvent() {
        return navigationEvent;
    }

    protected void showError(String message) {
        errorMessage.postValue(message);
    }

    protected void showLoading() {
        isLoading.postValue(true);
    }

    protected void hideLoading() {
        isLoading.postValue(false);
    }

    protected void clearError() {
        errorMessage.setValue(null);
    }

    protected void navigate(int navigationType) {
        android.util.Log.d("BaseViewModel", "navigate called, navigationType: " + navigationType);
        android.util.Log.d("BaseViewModel", "navigate called, stack trace:", new Exception());
        navigationEvent.postValue(new NavigationEvent(navigationType));
    }

    protected void navigate(int navigationType, Object data) {
        android.util.Log.d("BaseViewModel", "navigate called, navigationType: " + navigationType + ", data: " + data);
        android.util.Log.d("BaseViewModel", "navigate called, stack trace:", new Exception());
        navigationEvent.postValue(new NavigationEvent(navigationType, data));
    }
}
