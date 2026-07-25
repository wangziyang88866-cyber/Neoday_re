package com.endofdays_re.utils;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Monster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 性能分析工具 - 用于监控僵尸刷怪系统的性能
 */
public class PerformanceAnalyzer {

    private static final Map<String, Long> timingData = new ConcurrentHashMap<>();
    private static final Map<String, Integer> countData = new ConcurrentHashMap<>();
    private static long lastResetTime = 0;
    private static boolean isMonitoring = false;

    /**
     * 开始监控
     */
    public static void startMonitoring() {
        isMonitoring = true;
        resetData();
    }

    /**
     * 停止监控
     */
    public static void stopMonitoring() {
        isMonitoring = false;
    }

    /**
     * 重置数据
     */
    public static void resetData() {
        timingData.clear();
        countData.clear();
        lastResetTime = System.currentTimeMillis();
    }

    /**
     * 记录执行时间
     */
    public static void recordTime(String operation, long timeMs) {
        if (!isMonitoring) return;

        timingData.merge(operation, timeMs, Long::sum);
        countData.merge(operation, 1, Integer::sum);
    }

    /**
     * 开始计时
     */
    public static long startTime(String operation) {
        return System.nanoTime();
    }

    /**
     * 结束计时并记录
     */
    public static void endTime(String operation, long startTime) {
        long elapsedMs = (System.nanoTime() - startTime) / 1_000_000;
        recordTime(operation, elapsedMs);
    }

    /**
     * 生成性能报告并发送到聊天框
     */
    public static void generateReport(MinecraftServer server) {
        if (server == null) return;

        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - lastResetTime;
        double elapsedSeconds = Math.max(1, elapsedTime) / 1000.0;

        List<Component> messages = new ArrayList<>();

        // 标题
        messages.add(Component.literal("§6========== 性能分析报告 =========="));
        messages.add(Component.literal("§7监控时长: §f" + String.format("%.1f", elapsedSeconds) + " 秒"));
        messages.add(Component.literal(""));

        // 实体统计
        int totalEntities = 0;
        int totalMonsters = 0;
        int totalZombies = 0;

        for (ServerLevel level : server.getAllLevels()) {
            // 使用 level.getAllEntities() 获取所有实体
            for (Entity entity : level.getAllEntities()) {
                totalEntities++;
                if (entity instanceof Monster) {
                    totalMonsters++;
                    if (entity.getType().toString().contains("zombie")) {
                        totalZombies++;
                    }
                }
            }
        }

        messages.add(Component.literal("§b【实体统计】"));
        messages.add(Component.literal("§7  总实体数: §f" + totalEntities));
        messages.add(Component.literal("§7  怪物总数: §f" + totalMonsters));
        messages.add(Component.literal("§7  僵尸数量: §f" + totalZombies));
        messages.add(Component.literal(""));

        // 性能数据
        if (!timingData.isEmpty()) {
            messages.add(Component.literal("§a【操作耗时】"));

            // 按总耗时排序
            List<Map.Entry<String, Long>> sortedEntries = new ArrayList<>(timingData.entrySet());
            sortedEntries.sort(Map.Entry.<String, Long>comparingByValue().reversed());

            for (Map.Entry<String, Long> entry : sortedEntries) {
                String operation = entry.getKey();
                long totalTime = entry.getValue();
                int count = countData.getOrDefault(operation, 1);
                double avgTime = (double) totalTime / count;

                String color = getColorByTime(avgTime);
                messages.add(Component.literal(
                        "§7  " + operation + ": §f" +
                                String.format("%.2f", avgTime) + "ms §7(平均) | " +
                                color + String.format("%.2f", totalTime) + "ms §7(总计) | " +
                                "§f" + count + " §7次"
                ));
            }
            messages.add(Component.literal(""));
        }

        // TPS估算
        messages.add(Component.literal("§e【系统状态】"));
        messages.add(Component.literal("§7  监控状态: " + (isMonitoring ? "§a运行中" : "§c已停止")));
        messages.add(Component.literal("§7  TPS: §f" + getEstimatedTPS(server)));
        messages.add(Component.literal(""));

        // 使用提示
        messages.add(Component.literal("§6================================"));
        messages.add(Component.literal("§7使用 §f/perf start §7开始监控"));
        messages.add(Component.literal("§7使用 §f/perf stop §7停止并查看报告"));
        messages.add(Component.literal("§7使用 §f/perf reset §7重置数据"));

        // 发送消息给所有在线玩家
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            for (Component message : messages) {
                player.sendSystemMessage(message);
            }
        }
    }

    /**
     * 获取阶段标签
     */
    private static String getPhaseLabel(String phaseName) {
        switch (phaseName) {
            case "phase_1_early":
                return "§e阶段1·初期";
            case "phase_2_mid":
                return "§a阶段2·蔓延";
            case "phase_3_outbreak":
                return "§6阶段3·爆发";
            case "phase_4_doom":
                return "§c阶段4·末日";
            case "phase_5_extinction":
                return "§4阶段5·寂灭";
            default:
                return phaseName;
        }
    }

    /**
     * 根据耗时获取颜色
     */
    private static String getColorByTime(double avgTime) {
        if (avgTime < 1) return "§a";      // 绿色：< 1ms
        else if (avgTime < 5) return "§e"; // 黄色：1-5ms
        else if (avgTime < 10) return "§6";// 金色：5-10ms
        else if (avgTime < 50) return "§c";// 红色：10-50ms
        else return "§4";                  // 深红：> 50ms
    }

    /**
     * 估算TPS
     */
    private static String getEstimatedTPS(MinecraftServer server) {
        double mspt = server.getAverageTickTimeNanos() / 1_000_000.0;
        double tps = Math.min(20.0, 1000.0 / mspt);
        return String.format("%.1f", tps);
    }

    /**
     * 获取当前监控状态
     */
    public static boolean isMonitoring() {
        return isMonitoring;
    }
}
