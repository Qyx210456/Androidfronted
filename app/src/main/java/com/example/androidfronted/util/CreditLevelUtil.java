package com.example.androidfronted.util;

public class CreditLevelUtil {
    public static final int LEVEL_NULL = 0;
    public static final int LEVEL_BASIC = 1;
    public static final int LEVEL_STEADY = 2;
    public static final int LEVEL_GOOD = 3;
    public static final int LEVEL_EXCELLENT = 4;
    public static final int LEVEL_ELITE = 5;

    public static int getLevel(int creditScore) {
        if (creditScore < 0) return LEVEL_BASIC;
        if (creditScore <= 350) return LEVEL_BASIC;
        if (creditScore <= 480) return LEVEL_STEADY;
        if (creditScore <= 600) return LEVEL_GOOD;
        if (creditScore <= 690) return LEVEL_EXCELLENT;
        return LEVEL_ELITE;
    }

    public static int getLevelMax(int level) {
        switch (level) {
            case LEVEL_BASIC:
                return 350;
            case LEVEL_STEADY:
                return 480;
            case LEVEL_GOOD:
                return 600;
            case LEVEL_EXCELLENT:
                return 690;
            case LEVEL_ELITE:
                return 750;
            default:
                return 0;
        }
    }

    public static int getLevelMin(int level) {
        switch (level) {
            case LEVEL_BASIC:
                return 0;
            case LEVEL_STEADY:
                return 351;
            case LEVEL_GOOD:
                return 481;
            case LEVEL_EXCELLENT:
                return 601;
            case LEVEL_ELITE:
                return 691;
            default:
                return 0;
        }
    }

    public static String getLevelName(int level) {
        switch (level) {
            case LEVEL_BASIC:
                return "基础信誉";
            case LEVEL_STEADY:
                return "稳健信誉";
            case LEVEL_GOOD:
                return "良好信誉";
            case LEVEL_EXCELLENT:
                return "优秀信誉";
            case LEVEL_ELITE:
                return "卓越信誉";
            default:
                return "未评级";
        }
    }
}
