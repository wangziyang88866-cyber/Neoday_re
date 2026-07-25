package com.endofdays_re.event.helper;

import com.endofdays_re.client.config.data.SpawnerBuild;
import com.endofdays_re.config.ConfigData;
import com.endofdays_re.event.data.AllSyncValue;
import com.endofdays_re.utils.ModUtils;
import com.endofdays_re.utils.type.ModeEventType;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventBusSubscriber(modid = ModUtils.MODID)
public enum SpawnerManager {
    ;

    private static final Logger LOGGER = LoggerFactory.getLogger(SpawnerManager.class);

    /*
     * 防止 check_interval = 0 或负数导致每 tick 都进行一次寻点和实体查询。
     * 10 tick 即每秒两次，已足够灵敏。
     */
    private static final int MIN_CHECK_INTERVAL = 10;

    private static int checkTimer;
    private static int currentPlayerIndex;
    private static SpawnerBuild.StageConfig lastStage;

    @SubscribeEvent
    public static void onServerTickPost(ServerTickEvent.Post event) {
        if (!ConfigData.SpawnerConfigData.enable) {
            resetState();
            return;
        }

        SpawnerBuild.StageConfig currentStage =
                SpawnerHelper.getCurrentStage(ConfigData.SpawnerConfigData);

        if (currentStage == null) {
            resetState();

            if (event.getServer().getTickCount() % 200 == 0) {
                LOGGER.warn("[Spawner] 当前天数无有效阶段配置，已暂停刷怪");
            }

            return;
        }

        if (lastStage != currentStage) {
            checkTimer = 0;
            lastStage = currentStage;
        }

        int checkInterval = Math.max(
                MIN_CHECK_INTERVAL,
                currentStage.check_interval
        );

        if (++checkTimer < checkInterval) {
            return;
        }

        checkTimer = 0;

        var players = event.getServer().getPlayerList().getPlayers();
        if (players.isEmpty()) {
            currentPlayerIndex = 0;
            return;
        }

        currentPlayerIndex = Math.floorMod(currentPlayerIndex, players.size());
        ServerPlayer selectedPlayer = players.get(currentPlayerIndex);
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();

        float spawnWeight = 1.0F;
        if (AllSyncValue.Instance.mode == ModeEventType.BLOOD) {
            /*
             * 小于等于 0 时，SpawnerHelper 会直接拒绝刷怪。
             * 这使配置的 0 可以作为“血月关闭额外刷怪”使用。
             */
            spawnWeight = ConfigData.dimensionConfigData.spawn_weight;
        }

        SpawnerHelper.trySpawn(selectedPlayer, spawnWeight);
    }

    private static void resetState() {
        checkTimer = 0;
        currentPlayerIndex = 0;
        lastStage = null;
    }
}