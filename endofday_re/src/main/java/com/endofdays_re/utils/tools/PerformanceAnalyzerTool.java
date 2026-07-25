package com.endofdays_re.utils.tools;

import com.endofdays_re.utils.type.PhaseType;

public class PerformanceAnalyzerTool {
    private static final int TICK_INTERVAL = 10; // 每10刻（0.5秒）计算一次
    private static final double TICK_INTERVAL_MS = 500.0; // 0.5秒对应的毫秒数
    private final PerformanceData performanceData;
    private long tickStartTime;
    private long lastTickTime;
    private int tickCounter;
    private long totalMspt;

    public PerformanceAnalyzerTool() {
        this.performanceData = new PerformanceData();
        this.tickCounter = 0;
        this.totalMspt = 0;
        this.lastTickTime = 0;
    }


    public void tick(PhaseType phase) {
        if (phase == PhaseType.START) {
            handleTickStart();
        } else if (phase == PhaseType.END) {
            handleTickEnd();
        }
    }

    private void handleTickStart() {
        tickStartTime = System.currentTimeMillis();
    }

    private void handleTickEnd() {
        long tickEndTime = System.currentTimeMillis();
        long mspt = tickEndTime - tickStartTime;

        totalMspt += mspt;
        tickCounter++;

        if (tickCounter >= TICK_INTERVAL) {
            calculatePerformanceMetrics();
        }
    }

    private void calculatePerformanceMetrics() {
        // 计算平均 MSPT
        performanceData.setMspt(totalMspt / TICK_INTERVAL);

        // 计算 TPS
        long currentTime = System.currentTimeMillis();
        if (lastTickTime != 0) {
            long timeDiff = currentTime - lastTickTime;
            double tps = Math.min(20.0, TICK_INTERVAL_MS * 20 / timeDiff);
            performanceData.setTps(Math.round(tps * 100.0) / 100.0);
        }

        lastTickTime = currentTime;

        // 重置计数器和累计值
        tickCounter = 0;
        totalMspt = 0;
    }

    public PerformanceData getPerformanceData() {
        return performanceData;
    }

    // 性能数据持有类
    public static class PerformanceData {
        private double tps;
        private long mspt;

        public PerformanceData() {
            this.tps = 20.0;
            this.mspt = 0;
        }

        public double getTps() {
            return tps;
        }

        public void setTps(double tps) {
            this.tps = tps;
        }

        public long getMspt() {
            return mspt;
        }

        public void setMspt(long mspt) {
            this.mspt = mspt;
        }

        @Override
        public String toString() {
            return String.format("TPS: %.2f, MSPT: %dms", tps, mspt);
        }
    }
}