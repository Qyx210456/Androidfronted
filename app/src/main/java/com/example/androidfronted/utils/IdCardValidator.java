package com.example.androidfronted.utils;

import android.util.Log;
import java.util.Calendar;
import java.util.regex.Pattern;

public class IdCardValidator {
    
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^[1-9]\\d{5}\\d{4}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$");
    
    private static final int[] POWER = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    private static final String[] VERIFY_CODE = {"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
    
    public static boolean isValid(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            return false;
        }
        
        idCard = idCard.replaceAll("[^0-9Xx]", "");
        
        if (idCard.length() != 18) {
            return false;
        }
        
        if (!ID_CARD_PATTERN.matcher(idCard).matches()) {
            return false;
        }
        
        if (!isValidDate(idCard)) {
            return false;
        }
        
        return verifyChecksum(idCard);
    }
    
    private static boolean isValidDate(String idCard) {
        int year = Integer.parseInt(idCard.substring(6, 10));
        int month = Integer.parseInt(idCard.substring(10, 12));
        int day = Integer.parseInt(idCard.substring(12, 14));
        
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (year < 1900 || year > currentYear) {
            return false;
        }
        
        if (month < 1 || month > 12) {
            return false;
        }
        
        int maxDay = getMaxDay(year, month);
        if (day < 1 || day > maxDay) {
            return false;
        }
        
        return true;
    }
    
    private static int getMaxDay(int year, int month) {
        switch (month) {
            case 4:
            case 6:
            case 9:
            case 11:
                return 30;
            case 2:
                return isLeapYear(year) ? 29 : 28;
            default:
                return 31;
        }
    }
    
    private static boolean isLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }
    
    private static boolean verifyChecksum(String idCard) {
        String first17Digits = idCard.substring(0, 17);
        System.err.println("=== 身份证校验位验证 ===");
        System.err.println("输入的身份证号: " + idCard);
        System.err.println("输入文字总长度: " + idCard.length());
        System.err.println("前17位数字: " + first17Digits);
        
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            int digit = idCard.charAt(i) - '0';
            int weighted = digit * POWER[i];
            sum += weighted;
            System.err.println("第" + (i + 1) + "位: " + digit + " × " + POWER[i] + " = " + weighted);
        }
        
        int mod = sum % 11;
        String expectedVerifyCode = VERIFY_CODE[mod];
        String actualVerifyCode = String.valueOf(idCard.charAt(17)).toUpperCase();
        
        System.err.println("加权总和: " + sum);
        System.err.println("总和 % 11: " + mod);
        System.err.println("正确的校验码: " + expectedVerifyCode);
        System.err.println("输入的校验码: " + actualVerifyCode);
        System.err.println("校验结果: " + (expectedVerifyCode.equals(actualVerifyCode) ? "通过" : "失败"));
        System.err.println("=======================");
        
        Log.d("IdCardValidator", "=== 身份证校验位验证 ===");
        Log.d("IdCardValidator", "输入的身份证号: " + idCard);
        Log.d("IdCardValidator", "输入文字总长度: " + idCard.length());
        Log.d("IdCardValidator", "前17位数字: " + first17Digits);
        Log.d("IdCardValidator", "加权总和: " + sum);
        Log.d("IdCardValidator", "总和 % 11: " + mod);
        Log.d("IdCardValidator", "正确的校验码: " + expectedVerifyCode);
        Log.d("IdCardValidator", "输入的校验码: " + actualVerifyCode);
        Log.d("IdCardValidator", "校验结果: " + (expectedVerifyCode.equals(actualVerifyCode) ? "通过" : "失败"));
        Log.d("IdCardValidator", "=======================");
        
        return expectedVerifyCode.equals(actualVerifyCode);
    }
    
    public static String getErrorMessage(String idCard) {
        if (idCard == null || idCard.trim().isEmpty()) {
            return "身份证号不能为空";
        }
        
        String cleanId = idCard.replaceAll("[^0-9Xx]", "");
        
        if (cleanId.length() != 18) {
            return "身份证号长度必须为18位";
        }
        
        if (!ID_CARD_PATTERN.matcher(cleanId).matches()) {
            return "身份证号格式不正确";
        }
        
        if (!isValidDate(cleanId)) {
            return "身份证号中的日期无效";
        }
        
        if (!verifyChecksum(cleanId)) {
            return "身份证号校验位错误";
        }
        
        return "";
    }
}
