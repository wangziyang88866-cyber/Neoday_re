package com.endofdays_re.event.data;

import com.endofdays_re.utils.ModUtils;
import com.endofdays_re.utils.type.ModeEventType;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class LevelDataSava extends SavedData {

    // 1.21.1 必须定义一个 Factory
    private static final SavedData.Factory<LevelDataSava> FACTORY = new SavedData.Factory<>(
            LevelDataSava::new,            // 创建新实例的方法
            LevelDataSava::load,           // 从 NBT 读取的方法
            null                        // DataFixTypes (通常传 null)
    );

    // 提供给 Factory 使用的无参构造
    public LevelDataSava() {
    }

    /**
     * 获取或创建数据实例
     */
    public static LevelDataSava get(ServerLevel level) {
        // 1.21.1 修正：computeIfAbsent 现在接收 Factory 和 文件名
        return level.getDataStorage().computeIfAbsent(FACTORY, "EodGameData");
    }

    /**
     * 1.21.1 修正：load 方法现在必须包含 HolderLookup.Provider 参数
     */
    public static LevelDataSava load(CompoundTag tag, HolderLookup.Provider provider) {
        LevelDataSava data = new LevelDataSava();
        if (tag.contains(ModUtils.MODID + "_level_data")) {
            CompoundTag levelSyncData = tag.getCompound(ModUtils.MODID + "_level_data");

            // 将 NBT 数据恢复到单例/同步类中
            AllSyncValue.Instance.day = levelSyncData.getLong("day");
            AllSyncValue.Instance.time = levelSyncData.getInt("time");
            AllSyncValue.Instance.BProbability = levelSyncData.getDouble("probability");
            AllSyncValue.Instance.lastBloodMoonDay = levelSyncData.getInt("lastBloodMoonDay");
            AllSyncValue.Instance.lastInvasionTick = levelSyncData.getLong("lastInvasionTick");

            String modeName = levelSyncData.getString("level_mode");
            try {
                AllSyncValue.Instance.mode = ModeEventType.valueOf(modeName);
            } catch (Exception e) {
                AllSyncValue.Instance.mode = ModeEventType.NONE;
            }
        }
        return data;
    }

    /**
     * 触发保存
     */
    public void sava() { // 建议重命名为 markDirty 或 saveToFile
        this.setDirty();
    }

    /**
     * 1.21.1 保存逻辑
     */
    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag compoundTag, @NotNull HolderLookup.Provider provider) {
        CompoundTag levelSyncData = new CompoundTag();
        levelSyncData.putLong("day", AllSyncValue.Instance.day);
        levelSyncData.putInt("time", AllSyncValue.Instance.time);
        levelSyncData.putDouble("probability", AllSyncValue.Instance.BProbability);
        levelSyncData.putInt("lastBloodMoonDay", AllSyncValue.Instance.lastBloodMoonDay);
        levelSyncData.putString("level_mode", AllSyncValue.Instance.mode.name());
        levelSyncData.putLong("lastInvasionTick", AllSyncValue.Instance.lastInvasionTick);

        compoundTag.put(ModUtils.MODID + "_level_data", levelSyncData);
        return compoundTag;
    }
}