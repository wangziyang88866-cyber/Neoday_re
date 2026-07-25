package com.endofdays_re.api;

import com.endofdays_re.event.data.AllSyncValue;
import com.endofdays_re.utils.type.ModeEventType;

/**
 * EndOfDays 外部调用接口 (1.21.1 适配版)
 */
public enum EodAPI {
    ;

    // --- 环境数据获取 (注意：AllSyncValue 依然作为客户端/服务端共用的简易缓存) ---

    /**
     * 当前天数
     */
    public static long getCurrentDay() {
        return AllSyncValue.Instance.day;
    }

    /**
     * 设置当前天数
     */
    public static void setCurrentDay(long day) {
        AllSyncValue.Instance.day = day;
    }

    /**
     * 当前时间 (0~24000)
     */
    public static int getCurrentTime() {
        return AllSyncValue.Instance.time;
    }

    /**
     * 是否为白天
     */
    public static boolean isDay() {
        return AllSyncValue.Instance.isDay;
    }

    /**
     * 当前事件模式名称
     */
    public static String getMode() {
        ModeEventType mode = AllSyncValue.Instance.mode;
        return mode != null ? mode.name() : "NONE";
    }

    /**
     * 下一个夜晚模式名称
     */
    public static String getNextNightMode() {
        ModeEventType mode = AllSyncValue.Instance.nextNightMode;
        return mode != null ? mode.name() : "NONE";
    }

    /**
     * 获取血月保底概率
     */
    public static double getBloodMoonProbability() {
        return AllSyncValue.Instance.BProbability;
    }

    /**
     * 根据间隔计算天数
     */
    public static int getRetentionDays(int interval) {
        return AllSyncValue.Instance.calculateRetentionDays(interval);
    }


}