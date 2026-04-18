package com.example.androidfronted.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final String INPUT_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final SimpleDateFormat inputFormat = new SimpleDateFormat(INPUT_FORMAT, Locale.getDefault());
    
    public static String formatDate(String createdAt) {
        try {
            Date date = inputFormat.parse(createdAt);
            if (date == null) {
                return "";
            }
            
            Calendar messageCal = Calendar.getInstance();
            messageCal.setTime(date);
            
            Calendar todayCal = Calendar.getInstance();
            Calendar yesterdayCal = Calendar.getInstance();
            yesterdayCal.add(Calendar.DAY_OF_YEAR, -1);
            
            String weekDay = getWeekDay(messageCal);
            
            if (isSameDay(messageCal, todayCal)) {
                return "今天 " + weekDay;
            } else if (isSameDay(messageCal, yesterdayCal)) {
                return "昨天 " + weekDay;
            } else if (isSameYear(messageCal, todayCal)) {
                int month = messageCal.get(Calendar.MONTH) + 1;
                int day = messageCal.get(Calendar.DAY_OF_MONTH);
                return month + "月" + day + "日 " + weekDay;
            } else {
                int year = messageCal.get(Calendar.YEAR);
                int month = messageCal.get(Calendar.MONTH) + 1;
                int day = messageCal.get(Calendar.DAY_OF_MONTH);
                return year + "年" + month + "月" + day + "日 " + weekDay;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }
    
    public static String formatTime(String createdAt) {
        try {
            Date date = inputFormat.parse(createdAt);
            if (date == null) {
                return "";
            }
            
            Calendar messageCal = Calendar.getInstance();
            messageCal.setTime(date);
            
            Calendar todayCal = Calendar.getInstance();
            
            int hour = messageCal.get(Calendar.HOUR_OF_DAY);
            int minute = messageCal.get(Calendar.MINUTE);
            int second = messageCal.get(Calendar.SECOND);
            
            if (isSameDay(messageCal, todayCal)) {
                return String.format(Locale.getDefault(), "%02d:%02d:%02d", hour, minute, second);
            } else {
                int month = messageCal.get(Calendar.MONTH) + 1;
                int day = messageCal.get(Calendar.DAY_OF_MONTH);
                return month + "月" + day + "日";
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }
    
    public static String formatDateTime(String createdAt) {
        try {
            Date date = inputFormat.parse(createdAt);
            if (date == null) {
                return "";
            }
            
            Calendar messageCal = Calendar.getInstance();
            messageCal.setTime(date);
            
            Calendar todayCal = Calendar.getInstance();
            Calendar yesterdayCal = Calendar.getInstance();
            yesterdayCal.add(Calendar.DAY_OF_YEAR, -1);
            
            int hour = messageCal.get(Calendar.HOUR_OF_DAY);
            int minute = messageCal.get(Calendar.MINUTE);
            String timeStr = String.format(Locale.getDefault(), "%02d:%02d", hour, minute);
            
            if (isSameDay(messageCal, todayCal)) {
                return timeStr;
            } else if (isSameDay(messageCal, yesterdayCal)) {
                return "昨天 " + timeStr;
            } else if (isSameYear(messageCal, todayCal)) {
                int month = messageCal.get(Calendar.MONTH) + 1;
                int day = messageCal.get(Calendar.DAY_OF_MONTH);
                return month + "月" + day + "日 " + timeStr;
            } else {
                int year = messageCal.get(Calendar.YEAR);
                int month = messageCal.get(Calendar.MONTH) + 1;
                int day = messageCal.get(Calendar.DAY_OF_MONTH);
                return year + "年" + month + "月" + day + "日 " + timeStr;
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }
    
    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
               cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
    }
    
    private static boolean isSameYear(Calendar cal1, Calendar cal2) {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR);
    }
    
    private static String getWeekDay(Calendar cal) {
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        return weekDays[dayOfWeek - 1];
    }
}
