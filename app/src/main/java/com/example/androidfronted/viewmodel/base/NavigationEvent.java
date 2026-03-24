package com.example.androidfronted.viewmodel.base;

public class NavigationEvent {
    public static final int NAVIGATE_TO_LOGIN = 1;
    public static final int NAVIGATE_TO_REGISTER = 2;
    public static final int NAVIGATE_TO_HOME = 3;
    public static final int NAVIGATE_TO_PROFILE = 4;
    public static final int NAVIGATE_TO_PERSONAL_INFO = 5;
    public static final int NAVIGATE_TO_BANK_CARDS = 6;
    public static final int NAVIGATE_TO_PRODUCT_DETAIL = 7;
    public static final int NAVIGATE_TO_PRODUCT_APPLY = 8;
    public static final int NAVIGATE_TO_BANK_CARD_UPLOAD = 9;
    public static final int NAVIGATE_TO_ID_CERT_UPLOAD = 10;
    public static final int NAVIGATE_TO_JOB_CERT_UPLOAD = 11;
    public static final int NAVIGATE_TO_PROPERTY_CERT_UPLOAD = 12;
    public static final int NAVIGATE_TO_THIRD_PARTY_CERT_UPLOAD = 13;
    public static final int NAVIGATE_BACK = 14;
    public static final int NAVIGATE_TO_REGISTER_STEP_2 = 15;

    private final int navigationType;
    private final Object data;

    public NavigationEvent(int navigationType) {
        this.navigationType = navigationType;
        this.data = null;
    }

    public NavigationEvent(int navigationType, Object data) {
        this.navigationType = navigationType;
        this.data = data;
    }

    public int getNavigationType() {
        return navigationType;
    }

    public Object getData() {
        return data;
    }
}
