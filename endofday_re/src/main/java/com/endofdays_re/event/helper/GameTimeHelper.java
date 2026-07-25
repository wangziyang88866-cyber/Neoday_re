package com.endofdays_re.event.helper;

import com.endofdays_re.config.ConfigData;
import com.endofdays_re.event.data.AllSyncValue;
import com.endofdays_re.network.Network;
import com.endofdays_re.network.packer.s2c.SyncValueDataPacker;
import com.endofdays_re.utils.type.LevelTimeType;
import com.endofdays_re.utils.type.ModeEventType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;

import java.util.Optional;

public enum GameTimeHelper {
    ;

    public static void update(
            MinecraftServer server,
            ServerLevel level,
            long day,
            LevelTimeType time,
            boolean isDay,
            ModeEventType mode,
            double bpblood
    ) {

        if (time == LevelTimeType.NONE) {
            return;
        }

        ConfigData.languageMsgConfigData.data.values().forEach(languageMsgData -> {

            if (day < languageMsgData.day
                    || day > languageMsgData.endDay
                    || time != languageMsgData.time
                    || !languageMsgData.eventMode.equals(mode)) {
                return;
            }


            Network.sendToALLClient(new SyncValueDataPacker(
                    AllSyncValue.Instance.temperature,
                    Optional.of(languageMsgData.msg),
                    AllSyncValue.Instance.day,
                    AllSyncValue.Instance.time,
                    AllSyncValue.Instance.mode,
                    AllSyncValue.Instance.isDay,
                    AllSyncValue.Instance.BProbability,
                    true,
                    AllSyncValue.Instance.nextNightMode
            ));
        });
    }
}