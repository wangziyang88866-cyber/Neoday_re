package com.endofdays_re.event.helper;

import com.endofdays_re.client.config.data.InvasionBuild;
import com.endofdays_re.config.ConfigData;
import com.endofdays_re.event.data.AllSyncValue;
import com.endofdays_re.event.data.LevelDataSava;
import com.endofdays_re.utils.ModUtils;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EventBusSubscriber(modid = ModUtils.MODID)
public enum InvasionManager {
    ;

    private static final List<InvasionTask> ACTIVE_TASKS = new ArrayList<>();
    private static final int CHECK_INTERVAL = 1200; // 60秒检查一次
    private static int checkTimer = 0;

    public static void startInvasionTask(ServerPlayer player, InvasionBuild.InvasionSettings settings, int totalWaves) {
        boolean isAlreadyInInvasion = ACTIVE_TASKS.stream().anyMatch(t -> t.player.getUUID().equals(player.getUUID()));
        if (!isAlreadyInInvasion) {
            ACTIVE_TASKS.add(new InvasionTask(player, settings, totalWaves));
        }
    }

    /**
     * 1.21.1 修正：TickEvent 拆分为多个独立事件
     * 使用 ServerTickEvent.Post 替代 TickEvent.Phase.END
     */
    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event) {
        // --- 逻辑 A: 触发检查 (包含冷却逻辑) ---
        checkTimer++;
        if (checkTimer >= CHECK_INTERVAL) {
            checkTimer = 0;
            var server = event.getServer();
            // 在 1.21.1 中通过 event.getServer() 直接获取服务端实例
            long currentTotalTicks = server.overworld().getGameTime();

            // 校验全局冷却
            if (currentTotalTicks - AllSyncValue.Instance.lastInvasionTick >= ConfigData.InvasionData.max_time) {
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    if (!player.isCreative() && !player.isSpectator()) {
                        if (InvasionHelper.tryExecuteInvasion(player)) {
                            // 触发成功：记录时间戳
                            AllSyncValue.Instance.lastInvasionTick = currentTotalTicks;

                            // 修正：确保 SavedData 被标记为脏数据以保存 NBT
                            LevelDataSava data = LevelDataSava.get(player.serverLevel());
                            data.setDirty();

                            // 触发一场后直接跳出，平衡压力
                            break;
                        }
                    }
                }
            }
        }

        // --- 逻辑 B: 波次任务处理 (异步调度) ---
        if (ACTIVE_TASKS.isEmpty()) return;

        Iterator<InvasionTask> it = ACTIVE_TASKS.iterator();
        while (it.hasNext()) {
            InvasionTask task = it.next();

            // 1.21.1 建议使用此方法检查玩家是否离线
            if (task.player.hasDisconnected()) {
                it.remove();
                continue;
            }

            if (task.ticksUntilNextWave <= 0) {
                task.currentWave++;

                InvasionHelper.executeSingleWave(
                        task.player.serverLevel(),
                        task.player,
                        task.settings,
                        task.currentWave,
                        task.totalWaves
                );

                if (task.currentWave >= task.totalWaves) {
                    it.remove();
                } else {
                    task.ticksUntilNextWave = 600; // 30秒一波
                }
            } else {
                task.ticksUntilNextWave--;
            }
        }
    }

    private static class InvasionTask {
        final ServerPlayer player;
        final InvasionBuild.InvasionSettings settings;
        final int totalWaves;
        int currentWave = 0;
        int ticksUntilNextWave = 0;

        InvasionTask(ServerPlayer player, InvasionBuild.InvasionSettings settings, int totalWaves) {
            this.player = player;
            this.settings = settings;
            this.totalWaves = totalWaves;
        }
    }
}