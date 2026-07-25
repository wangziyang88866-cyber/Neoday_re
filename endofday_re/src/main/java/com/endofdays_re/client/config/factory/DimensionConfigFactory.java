package com.endofdays_re.client.config.factory;

import com.endofdays_re.client.config.data.Dimensionbuild;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

public class DimensionConfigFactory {
    public static void build(ConfigBuilder builder, Dimensionbuild config) {
        ConfigEntryBuilder eb = builder.entryBuilder();
        // --- 1. 血月核心配置 (Blood Moon) ---
        ConfigCategory bloodMoon = builder.getOrCreateCategory(Component.translatable("config.endofdays_re.category.bloodmoon"));
        // 启用血月
        bloodMoon.addEntry(eb.startBooleanToggle(Component.translatable("config.endofdays_re.bloodmoon.enable"), config.enable)
                .setSaveConsumer(v -> config.enable = v)
                .setDefaultValue(true)
                .build());
        // 血月计算调整值 (weight)
        bloodMoon.addEntry(eb.startDoubleField(Component.translatable("config.endofdays_re.bloodmoon.weight"), config.weight)
                .setSaveConsumer(v -> config.weight = v)
                .setDefaultValue(0.01)
                .setTooltip(Component.translatable("config.endofdays_re.bloodmoon.weight.tooltip"))
                .build());
        // 血月概率递增值 (bloodMoonProbability)
        bloodMoon.addEntry(eb.startDoubleField(Component.translatable("config.endofdays_re.bloodmoon.probability"), config.bloodMoonProbability)
                .setSaveConsumer(v -> config.bloodMoonProbability = v)
                .setDefaultValue(0.05)
                .build());
        // 聊天框显示
        bloodMoon.addEntry(eb.startBooleanToggle(Component.translatable("config.endofdays_re.bloodmoon.chat_show"), config.chat_show)
                .setSaveConsumer(v -> config.chat_show = v)
                .setDefaultValue(true)
                .build());
        // 是否允许睡觉
        bloodMoon.addEntry(eb.startBooleanToggle(Component.translatable("config.endofdays_re.bloodmoon.sleep"), config.sleep)
                .setSaveConsumer(v -> config.sleep = v)
                .setDefaultValue(false)
                .build());
        // 刷怪倍率 (spawn_weight)
        bloodMoon.addEntry(eb.startFloatField(Component.translatable("config.endofdays_re.bloodmoon.spawn_weight"), config.spawn_weight)
                .setSaveConsumer(v -> config.spawn_weight = v)
                .setDefaultValue(1.35f)
                .setTooltip(Component.translatable("config.endofdays_re.bloodmoon.spawn_weight.tooltip"))
                .build());
    }
}