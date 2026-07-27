package com.endofdays_re.event.data;

import com.endofdays_re.config.ConfigData;
import com.endofdays_re.utils.type.ModeEventType;


public class AllSyncValue {
    public static final AllSyncValue Instance = new AllSyncValue();
    public long day = 1;
    public int time;
    public ModeEventType mode = ModeEventType.NONE;
    public boolean isDay;
    public double BProbability;
    public ModeEventType nextNightMode = ModeEventType.NONE;
    // 配置会在 FMLCommonSetupEvent 才加载；不能在静态实例构造期间读取它。
    public float temperature;
    public int lastBloodMoonDay;
    public long lastTotalTick;

    // --- 新增：上一次入侵触发的绝对刻数 ---
    public long lastInvasionTick = 0;

    public int calculateRetentionDays(int interval) {
        if (interval <= 0) throw new IllegalArgumentException("间隔天数必须大于0");
        return (int) (day / interval);
    }


}
