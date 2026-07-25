package com.endofdays_re.event.helper;

import com.endofdays_re.client.config.data.CommonBuild;
import com.endofdays_re.client.config.data.InvasionBuild;
import com.endofdays_re.config.ConfigData;
import com.endofdays_re.utils.ModUtils;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


public enum SimpleWeightListHelper {
    ;
    public static SimpleWeightedRandomList.Builder<ItemStack> list = SimpleWeightedRandomList.builder();
    public static SimpleWeightedRandomList.Builder<CommonBuild.TaczData> taczItems = new SimpleWeightedRandomList.Builder<>();
    public static SimpleWeightedRandomList.Builder<DropsHelper.LootSting> loot = new SimpleWeightedRandomList.Builder<>();
    public static SimpleWeightedRandomList.Builder<DropsHelper.LootSting> zombieLoot = new SimpleWeightedRandomList.Builder<>();
    public static SimpleWeightedRandomList.Builder<InvasionBuild.InvasionSettings> invasionSettingsBuilder = new SimpleWeightedRandomList.Builder<>();

    public static void register() {
        // 1. 注册普通掉落物权重列表
        ConfigData.dropConfigData.data.values().forEach(dropInfo -> {
            for (int i = 0; i < dropInfo.items.length; i++) {
                loot.add(new DropsHelper.LootSting(
                                dropInfo.items[i].ItemId,
                                dropInfo.entitys,
                                dropInfo.items[i].min,
                                dropInfo.items[i].max,
                                dropInfo.items[i].tag),
                        dropInfo.items[i].weight);
            }
        });

        // 2. 注册尸体（僵尸）战利品配置
        ConfigData.dropConfigData.zombie_data.forEach((key, dropInfo) -> {
            for (int i = 0; i < dropInfo.items.length; i++) {
                zombieLoot.add(new DropsHelper.LootSting(
                                dropInfo.items[i].ItemId,
                                dropInfo.entitys,
                                dropInfo.items[i].min,
                                dropInfo.items[i].max,
                                dropInfo.items[i].tag),
                        dropInfo.items[i].weight);
            }
        });

        // 3. 注册基础物品权重
        list.add(new ItemStack(Items.TRIDENT), 40)
                .add(new ItemStack(Items.CROSSBOW), 60)
                .add(new ItemStack(Items.BOW), 40);

        // 4. 注册 TACZ 枪械数据 (如果模组存在)
        if (ModUtils.isloadMod("tacz")) {
            ConfigData.commonConfigData.taczData.values().forEach(taczData -> {
                taczItems.add(taczData, taczData.weight);
            });
        }

        // 5. 【补全部分】注册入侵配置权重列表
        // 假设你的单例引用路径为 ConfigData.invasionConfigData
        if (ConfigData.InvasionData != null && ConfigData.InvasionData.invasionSettings != null) {
            ConfigData.InvasionData.invasionSettings.values().forEach(setting -> {
                // 将配置类根据其内部定义的 weight 放入权重随机生成器
                invasionSettingsBuilder.add(setting, setting.weight);
            });
        }
    }

}