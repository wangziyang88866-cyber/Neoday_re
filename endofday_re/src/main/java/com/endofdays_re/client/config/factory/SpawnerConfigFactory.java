package com.endofdays_re.client.config.factory;

import com.endofdays_re.client.config.data.SpawnerBuild;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.MultiElementListEntry;
import me.shedaniel.clothconfig2.gui.entries.NestedListListEntry;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class SpawnerConfigFactory {

    public static void build(ConfigBuilder builder, SpawnerBuild config) {
        ConfigEntryBuilder eb = builder.entryBuilder();
        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("config.endofdays_re.category.spawner"));

        // 1. 基础配置
        category.addEntry(eb.startBooleanToggle(Component.translatable("config.endofdays_re.spawner.enable"), config.enable)
                .setSaveConsumer(v -> config.enable = v).build());

        category.addEntry(eb.startIntField(Component.translatable("config.endofdays_re.spawner.max_total_entities"), config.max_total_entities)
                .setSaveConsumer(v -> config.max_total_entities = v).build());

        // 2. 维度白名单
        category.addEntry(eb.startStrList(Component.translatable("config.endofdays_re.spawner.allowed_dimensions"), Arrays.asList(config.allowed_dimensions))
                .setSaveConsumer(v -> config.allowed_dimensions = v.toArray(new String[0]))
                .build());

        // 5. 阶段配置列表（嵌套）
        List<StageWrapper> sWrappers = new ArrayList<>();
        if (config.stage_configs != null) {
            config.stage_configs.forEach((k, v) -> sWrappers.add(new StageWrapper(k, v)));
        }

        category.addEntry(new NestedListListEntry<>(
                Component.translatable("config.endofdays_re.spawner.stage_configs"),
                sWrappers,
                false,
                Optional::empty,
                newList -> {
                    config.stage_configs.clear();
                    if (newList != null) {
                        for (StageWrapper w : newList) {
                            String finalKey = (w.key == null || w.key.trim().isEmpty())
                                    ? "stage_" + System.currentTimeMillis()
                                    : w.key;
                            config.stage_configs.put(finalKey, w.data);
                        }
                    }
                },
                () -> List.of(new StageWrapper("new_stage_" + System.currentTimeMillis(), new SpawnerBuild.StageConfig())),
                eb.getResetButtonKey(),
                true,
                true,
                (stageOrigin, n1) -> {
                    final AtomicReference<StageWrapper> stageRef = new AtomicReference<>((stageOrigin != null) ? stageOrigin : new StageWrapper("unknown", new SpawnerBuild.StageConfig()));
                    List<AbstractConfigListEntry<?>> stageEntries = new ArrayList<>();
                    SpawnerBuild.StageConfig stage = stageRef.get().data;

                    // 阶段基础配置
                    stageEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.spawner.stage.key"), stageRef.get().key)
                            .setSaveConsumer(v -> stageRef.get().key = v).build());

                    stageEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.spawner.stage.description"), stage.description)
                            .setSaveConsumer(v -> stage.description = v).build());

                    stageEntries.add(eb.startIntField(Component.translatable("config.endofdays_re.spawner.stage.start_day"), stage.start_day)
                            .setSaveConsumer(v -> stage.start_day = v).build());

                    stageEntries.add(eb.startIntField(Component.translatable("config.endofdays_re.spawner.stage.end_day"), stage.end_day)
                            .setSaveConsumer(v -> stage.end_day = v).build());

                    stageEntries.add(eb.startIntField(Component.translatable("config.endofdays_re.spawner.stage.max_groups"), stage.max_groups)
                            .setSaveConsumer(v -> stage.max_groups = v).build());

                    stageEntries.add(eb.startIntField(Component.translatable("config.endofdays_re.spawner.stage.max_per_group"), stage.max_per_group)
                            .setSaveConsumer(v -> stage.max_per_group = v).build());

                    stageEntries.add(eb.startIntField(Component.translatable("config.endofdays_re.spawner.stage.check_interval"), stage.check_interval)
                            .setTooltip(Component.translatable("config.endofdays_re.spawner.stage.check_interval.tooltip"))
                            .setSaveConsumer(v -> stage.check_interval = v).build());

                    // 阶段刷怪范围配置
                    stageEntries.add(eb.startIntField(Component.translatable("config.endofdays_re.spawner.stage.spawn_range_min"), stage.spawn_range_min)
                            .setSaveConsumer(v -> stage.spawn_range_min = v).build());

                    stageEntries.add(eb.startIntField(Component.translatable("config.endofdays_re.spawner.stage.spawn_range_max"), stage.spawn_range_max)
                            .setSaveConsumer(v -> stage.spawn_range_max = v).build());

                    // 阶段垂直范围配置
                    stageEntries.add(eb.startIntField(Component.translatable("config.endofdays_re.spawner.stage.vertical_range_min"), stage.vertical_range_min)
                            .setSaveConsumer(v -> stage.vertical_range_min = v).build());

                    stageEntries.add(eb.startIntField(Component.translatable("config.endofdays_re.spawner.stage.vertical_range_max"), stage.vertical_range_max)
                            .setSaveConsumer(v -> stage.vertical_range_max = v).build());

                    // 阶段夜间配置
                    stageEntries.add(eb.startBooleanToggle(Component.translatable("config.endofdays_re.spawner.stage.only_spawn_at_night"), stage.only_spawn_at_night)
                            .setTooltip(Component.translatable("config.endofdays_re.spawner.stage.only_spawn_at_night.tooltip"))
                            .setSaveConsumer(v -> stage.only_spawn_at_night = v).build());

                    // 阶段光照检查配置
                    stageEntries.add(eb.startBooleanToggle(Component.translatable("config.endofdays_re.spawner.stage.check_light_level"), stage.check_light_level)
                            .setTooltip(Component.translatable("config.endofdays_re.spawner.stage.check_light_level.tooltip"))
                            .setSaveConsumer(v -> stage.check_light_level = v).build());

                    return new MultiElementListEntry<>(Component.literal("Stage: " + stageRef.get().key), stageRef.get(), stageEntries, true);
                }
        ));
    }

    public static class StageWrapper {
        public String key;
        public SpawnerBuild.StageConfig data;

        public StageWrapper(String key, SpawnerBuild.StageConfig data) {
            this.key = (key != null) ? key : "";
            this.data = (data != null) ? data : new SpawnerBuild.StageConfig();
        }
    }
}
