package com.endofdays_re.client.config.factory;

import com.endofdays_re.client.config.data.CommonBuild;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.MultiElementListEntry;
import me.shedaniel.clothconfig2.gui.entries.NestedListListEntry;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CommonConfigFactory {
    public static void build(ConfigBuilder builder, CommonBuild config) {
        ConfigEntryBuilder eb = builder.entryBuilder();

        // --- 1. 基础设置 (Main & World) ---
        ConfigCategory main = builder.getOrCreateCategory(Component.translatable("config.endofdays_re.category.common_main"));

        main.addEntry(eb.startIntField(Component.translatable("config.endofdays_re.common.scan_interval"), config.follow_block_scan_interval)
                .setSaveConsumer(v -> config.follow_block_scan_interval = v).build());

        main.addEntry(eb.startFloatField(Component.translatable("config.endofdays_re.common.temperature"), config.temperature)
                .setSaveConsumer(v -> config.temperature = v).build());

        main.addEntry(eb.startIntField(Component.translatable("config.endofdays_re.common.sync_interval"), config.max_time)
                .setSaveConsumer(v -> config.max_time = v).build());
        main.addEntry(eb.startBooleanToggle(Component.translatable("config.endofdays_re.common.enable_stuck_day"), config.enable_stuck_day)
                .setSaveConsumer(v -> config.enable_stuck_day = v).build());
        main.addEntry(eb.startLongField(Component.translatable("config.endofdays_re.common.retention_interval"), config.Retention_interval)
                .setSaveConsumer(v -> config.Retention_interval = v).build());
        main.addEntry(eb.startBooleanToggle(Component.translatable("config.endofdays_re.common.enable_corpse"), config.enable_corpse)
                .setSaveConsumer(v -> config.enable_corpse = v).build());
        main.addEntry(eb.startIntField(Component.translatable("config.endofdays_re.common.corpse_max"), config.corpse_max)
                .setSaveConsumer(v -> config.corpse_max = v).build());

        // 析光熔融器黑名单
        main.addEntry(eb.startStrList(Component.translatable("config.endofdays_re.common.smelt_blacklist"), config.SMELT_BLACKLIST)
                .setSaveConsumer(v -> config.SMELT_BLACKLIST = v).build());


        // --- 3. 概率与模块 (commonFloat & commonData) ---
        ConfigCategory probability = builder.getOrCreateCategory(Component.translatable("config.endofdays_re.category.probability"));
        // 处理 commonFloat (概率)
        addFloatMapEntries(eb, probability, config.commonFloat);
        // 处理 commonData (整数型设置，如 follow_range)
        addIntMapEntries(eb, probability, config.commonData);
        ConfigCategory target = builder.getOrCreateCategory(Component.translatable("config.endofdays_re.common.target_list"));
        addTargetListEntry(eb, target, config);
        ConfigCategory replace = builder.getOrCreateCategory(Component.translatable("config.endofdays_re.common.replace_map"));
        addReplaceListEntry(eb, replace, config);
        addBanListEntry(eb, replace, config);
        ConfigCategory ex = builder.getOrCreateCategory(Component.translatable("config.endofdays_re.category.ai"));
        addFollowBlockBreakEntry(eb, ex, config);
        addEquipChestMobEntry(eb, ex, config);
        ConfigCategory taczCat = builder.getOrCreateCategory(Component.translatable("config.endofdays_re.category.tacz"));
        addTaczDataListEntry(eb, taczCat, config);
    }

    private static void addFloatMapEntries(ConfigEntryBuilder eb, ConfigCategory cat, Map<String, CommonBuild.CommonData_Float> map) {
        map.forEach((key, data) -> {
            var sub = eb.startSubCategory(Component.translatable(data.lang));
            sub.add(eb.startFloatField(Component.translatable("config.endofdays_re.common.value"), data.value).setSaveConsumer(v -> data.value = v).build());
            sub.add(eb.startFloatField(Component.translatable("config.endofdays_re.common.min"), data.min_value).setSaveConsumer(v -> data.min_value = v).build());
            sub.add(eb.startFloatField(Component.translatable("config.endofdays_re.common.max"), data.max_value).setSaveConsumer(v -> data.max_value = v).build());
            cat.addEntry(sub.build());
        });
    }

    private static void addIntMapEntries(ConfigEntryBuilder eb, ConfigCategory cat, Map<String, CommonBuild.CommonData_Int> map) {
        map.forEach((key, data) -> {
            var sub = eb.startSubCategory(Component.translatable(data.lang));
            sub.add(eb.startIntField(Component.translatable("config.endofdays_re.common.value"), data.value).setSaveConsumer(v -> data.value = v).build());
            cat.addEntry(sub.build());
        });
    }

    private static void addTargetListEntry(ConfigEntryBuilder eb, ConfigCategory category, CommonBuild config) {
        // 1. 包装类转换
        List<TargetEntryWrapper> targetWrappers = new ArrayList<>();
        config.Target.forEach((k, v) -> targetWrappers.add(new TargetEntryWrapper(k, v)));

        category.addEntry(new NestedListListEntry<TargetEntryWrapper, MultiElementListEntry<TargetEntryWrapper>>(
                Component.translatable("config.endofdays_re.common.target_list"),
                targetWrappers,
                false,
                Optional::empty,
                newList -> {
                    // 保存逻辑：重建 Map
                    config.Target.clear();
                    for (int i = 0; i < newList.size(); i++) {
                        TargetEntryWrapper wrapper = newList.get(i);
                        if (wrapper == null) continue;

                        String finalKey = wrapper.key;
                        if (finalKey == null || finalKey.isEmpty() || finalKey.equals("new_target")) {
                            // 修正：使用 mobId 作为 Key 的生成前缀
                            finalKey = (wrapper.data.mobId != null) ?
                                    wrapper.data.mobId.replace(":", "_") + "_target_" + i :
                                    "target_entry_" + i;
                        }
                        config.Target.put(finalKey, wrapper.data);
                    }
                },
                // 创建新项逻辑：匹配你的无参构造或全参构造
                () -> List.of(new TargetEntryWrapper("new_target", new CommonBuild.TargetSelect("minecraft:zombie", "minecraft:player"))),
                eb.getResetButtonKey(),
                true,
                true,
                (wrapper, nestedList) -> {
                    // 渲染前的 NPE 防护
                    final TargetEntryWrapper wrapperSafe = (wrapper == null)
                            ? new TargetEntryWrapper("new_target", new CommonBuild.TargetSelect()) : wrapper;

                    if (wrapperSafe.data == null) wrapperSafe.data = new CommonBuild.TargetSelect();
                    // 严格对齐字段：mobId 和 Target
                    if (wrapperSafe.data.mobId == null) wrapperSafe.data.mobId = "minecraft:zombie";
                    if (wrapperSafe.data.Target == null) wrapperSafe.data.Target = "minecraft:pig";
                    if (wrapperSafe.key == null) wrapperSafe.key = "new_target";

                    List<AbstractConfigListEntry<?>> subEntries = new ArrayList<>();

                    // 1. Map Key (例如："村民")
                    subEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.common.key"), wrapperSafe.key)
                            .setSaveConsumer(v -> wrapperSafe.key = v)
                            .build());

                    // 2. 攻击者 ID (对应 TargetSelect.mobId)
                    subEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.target.mob"), wrapperSafe.data.mobId)
                            .setSaveConsumer(v -> wrapperSafe.data.mobId = v)
                            .build());

                    // 3. 目标实体 ID (对应 TargetSelect.Target)
                    subEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.target.victim"), wrapperSafe.data.Target)
                            .setSaveConsumer(v -> wrapperSafe.data.Target = v)
                            .build());

                    String header = (wrapperSafe.key.equals("new_target")) ? "New Target Selection" : wrapperSafe.key;
                    return new MultiElementListEntry<>(Component.literal(header), wrapperSafe, subEntries, true);
                }
        ));
    }

    // --- 工具辅助方法 ---

    private static void addReplaceListEntry(ConfigEntryBuilder eb, ConfigCategory category, CommonBuild config) {
        // 1. 将 Map 转换为包装列表
        List<StringPairWrapper> replaceWrappers = new ArrayList<>();
        config.replace_.forEach((k, v) -> replaceWrappers.add(new StringPairWrapper(k, v)));

        // 2. 添加到分类
        category.addEntry(new NestedListListEntry<StringPairWrapper, MultiElementListEntry<StringPairWrapper>>(
                Component.translatable("config.endofdays_re.common.replace_list"), // 需要在 lang 中添加
                replaceWrappers,
                false,
                Optional::empty,
                newList -> {
                    // 保存逻辑：重建 Map
                    config.replace_.clear();
                    for (int i = 0; i < newList.size(); i++) {
                        StringPairWrapper wrapper = newList.get(i);
                        if (wrapper == null || wrapper.key == null || wrapper.key.isEmpty()) continue;

                        // 防止重复 Key 导致覆盖，或者提供默认 Key
                        String finalKey = wrapper.key.equals("new_entry") ? "minecraft:undefined_" + i : wrapper.key;
                        config.replace_.put(finalKey, wrapper.value != null ? wrapper.value : "minecraft:air");
                    }
                },
                // 创建新项时的默认值
                () -> List.of(new StringPairWrapper("new_entry", "minecraft:zombie")),
                eb.getResetButtonKey(),
                true,
                true,
                (wrapper, nestedList) -> {
                    final StringPairWrapper target = (wrapper == null)
                            ? new StringPairWrapper("new_entry", "minecraft:zombie") : wrapper;

                    List<AbstractConfigListEntry<?>> subEntries = new ArrayList<>();

                    // 子项 1: 原始实体 ID (Map Key)
                    subEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.replace.original"), target.key)
                            .setSaveConsumer(v -> target.key = v)
                            .setTooltip(Component.literal("要被替换掉的实体 ID (如 minecraft:skeleton)"))
                            .build());

                    // 子项 2: 目标实体 ID (Map Value)
                    subEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.replace.target"), target.value)
                            .setSaveConsumer(v -> target.value = v)
                            .setTooltip(Component.literal("替换后的新实体 ID (如 minecraft:zombie)"))
                            .build());

                    String header = target.key.equals("new_entry") ? "New Replacement" : target.key + " -> " + target.value;
                    return new MultiElementListEntry<>(Component.literal(header), target, subEntries, true);
                }
        ));
    }

    private static void addBanListEntry(ConfigEntryBuilder eb, ConfigCategory category, CommonBuild config) {
        // 1. 将 Map 转换为包装列表
        List<StringPairWrapper> banWrappers = new ArrayList<>();
        config.banlist.forEach((k, v) -> banWrappers.add(new StringPairWrapper(k, v)));

        // 2. 添加到分类
        category.addEntry(new NestedListListEntry<StringPairWrapper, MultiElementListEntry<StringPairWrapper>>(
                Component.translatable("config.endofdays_re.common.ban_list"),
                banWrappers,
                false,
                Optional::empty,
                newList -> {
                    // 保存逻辑：重建 Map
                    config.banlist.clear();
                    for (int i = 0; i < newList.size(); i++) {
                        StringPairWrapper wrapper = newList.get(i);
                        if (wrapper == null) continue;

                        // 自动修正逻辑：防止 Key 为空或重复
                        String finalKey = wrapper.key;
                        if (finalKey == null || finalKey.isEmpty() || finalKey.equals("new_ban_entry")) {
                            finalKey = (wrapper.value != null && !wrapper.value.isEmpty()) ?
                                    wrapper.value.replace(":", "_") + "_" + i : "ban_item_" + i;
                        }
                        config.banlist.put(finalKey, wrapper.value != null ? wrapper.value : "minecraft:air");
                    }
                },
                // 创建新项时的默认值（点击 + 号生成的内容）
                () -> List.of(new StringPairWrapper("new_ban_entry", "minecraft:obsidian")),
                eb.getResetButtonKey(),
                true,
                true,
                (wrapper, nestedList) -> {
                    // 渲染前的 NPE 防护
                    final StringPairWrapper target = (wrapper == null)
                            ? new StringPairWrapper("new_ban_entry", "minecraft:obsidian") : wrapper;

                    if (target.key == null) target.key = "new_ban_entry";
                    if (target.value == null) target.value = "minecraft:air";

                    List<AbstractConfigListEntry<?>> subEntries = new ArrayList<>();

                    // 子项 1: 方块描述/名称 (Map Key)
                    subEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.ban.name"), target.key)
                            .setSaveConsumer(v -> target.key = v)
                            .setTooltip(Component.literal("用于识别的名称（如：黑曜石）"))
                            .build());

                    // 子项 2: 方块 ID (Map Value)
                    subEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.ban.id"), target.value)
                            .setSaveConsumer(v -> target.value = v)
                            .setTooltip(Component.literal("具体的方块 ID（如：minecraft:obsidian）"))
                            .build());

                    String header = target.key.equals("new_ban_entry") ? "New Ban Entry" : target.key + " [" + target.value + "]";
                    return new MultiElementListEntry<>(Component.literal(header), target, subEntries, true);
                }
        ));
    }

    private static void addFollowBlockBreakEntry(ConfigEntryBuilder eb, ConfigCategory category, CommonBuild config) {
        List<StringPairWrapper> wrappers = new ArrayList<>();
        config.FollowBlockBreak.forEach((k, v) -> wrappers.add(new StringPairWrapper(k, v)));

        category.addEntry(new NestedListListEntry<StringPairWrapper, MultiElementListEntry<StringPairWrapper>>(
                Component.translatable("config.endofdays_re.common.block_break_list"),
                wrappers,
                false,
                Optional::empty,
                newList -> {
                    config.FollowBlockBreak.clear();
                    for (int i = 0; i < newList.size(); i++) {
                        StringPairWrapper wrapper = newList.get(i);
                        if (wrapper == null || wrapper.key == null || wrapper.key.isEmpty()) continue;
                        config.FollowBlockBreak.put(wrapper.key, wrapper.value != null ? wrapper.value : "minecraft:air");
                    }
                },
                () -> List.of(new StringPairWrapper("新方块条目", "regex:^minecraft:.*$")),
                eb.getResetButtonKey(),
                true,
                true,
                (wrapper, nestedList) -> {
                    final StringPairWrapper target = (wrapper == null) ? new StringPairWrapper("新方块条目", "minecraft:air") : wrapper;
                    List<AbstractConfigListEntry<?>> subEntries = new ArrayList<>();

                    subEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.common.key"), target.key)
                            .setSaveConsumer(v -> target.key = v).build());

                    subEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.common.value"), target.value)
                            .setSaveConsumer(v -> target.value = v)
                            .setTooltip(Component.translatable("config.endofdays_re.common.block_break.tooltip"))
                            .build());

                    return new MultiElementListEntry<>(Component.literal(target.key), target, subEntries, true);
                }
        ));
    }

    private static void addEquipChestMobEntry(ConfigEntryBuilder eb, ConfigCategory category, CommonBuild config) {
        List<StringPairWrapper> wrappers = new ArrayList<>();
        config.EquipChestMob.forEach((k, v) -> wrappers.add(new StringPairWrapper(k, v)));

        category.addEntry(new NestedListListEntry<StringPairWrapper, MultiElementListEntry<StringPairWrapper>>(
                Component.translatable("config.endofdays_re.common.equip_chest_list"),
                wrappers,
                false,
                Optional::empty,
                newList -> {
                    config.EquipChestMob.clear();
                    for (int i = 0; i < newList.size(); i++) {
                        StringPairWrapper wrapper = newList.get(i);
                        if (wrapper == null || wrapper.key == null || wrapper.key.isEmpty()) continue;
                        config.EquipChestMob.put(wrapper.key, wrapper.value != null ? wrapper.value : "");
                    }
                },
                () -> List.of(new StringPairWrapper("新偷盗条目", "items:minecraft:apple")),
                eb.getResetButtonKey(),
                true,
                true,
                (wrapper, nestedList) -> {
                    final StringPairWrapper target = (wrapper == null) ? new StringPairWrapper("新偷盗条目", "") : wrapper;
                    List<AbstractConfigListEntry<?>> subEntries = new ArrayList<>();

                    subEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.common.key"), target.key)
                            .setSaveConsumer(v -> target.key = v).build());

                    subEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.common.value"), target.value)
                            .setSaveConsumer(v -> target.value = v)
                            .setTooltip(Component.translatable("config.endofdays_re.common.equip_chest.tooltip"))
                            .build());

                    return new MultiElementListEntry<>(Component.literal(target.key), target, subEntries, true);
                }
        ));
    }

    private static void addTaczDataListEntry(ConfigEntryBuilder eb, ConfigCategory category, CommonBuild config) {
        // 1. 转换包装列表
        List<TaczDataWrapper> wrappers = new ArrayList<>();
        config.taczData.forEach((k, v) -> wrappers.add(new TaczDataWrapper(k, v)));

        // 2. 添加列表条目
        category.addEntry(new NestedListListEntry<TaczDataWrapper, MultiElementListEntry<TaczDataWrapper>>(
                Component.translatable("config.endofdays_re.category.tacz"),
                wrappers,
                false,
                Optional::empty,
                newList -> {
                    // 保存逻辑
                    config.taczData.clear();
                    for (int i = 0; i < newList.size(); i++) {
                        TaczDataWrapper wrapper = newList.get(i);
                        if (wrapper == null || wrapper.key == null || wrapper.key.isEmpty()) continue;

                        String finalKey = wrapper.key.equals("new_gun") ? "gun_" + i : wrapper.key;
                        config.taczData.put(finalKey, wrapper.data);
                    }
                },
                // 创建新项按钮：默认给一把 AK
                () -> List.of(new TaczDataWrapper("new_gun", new CommonBuild.TaczData("tacz:ak47", "AUTO", true, 40, 24, 1.0f, 5))),
                eb.getResetButtonKey(),
                true,
                true,
                (wrapper, nestedList) -> {
                    // NPE 防护
                    final TaczDataWrapper safe = (wrapper == null)
                            ? new TaczDataWrapper("new_gun", new CommonBuild.TaczData()) : wrapper;

                    // 确保数据内部不为 null
                    if (safe.data == null) safe.data = new CommonBuild.TaczData();
                    if (safe.data.id == null) safe.data.id = "tacz:ak47";
                    if (safe.data.FireMode == null) safe.data.FireMode = "AUTO";

                    List<AbstractConfigListEntry<?>> subEntries = new ArrayList<>();

                    // 子项 1: 条目名称 (Map Key)
                    subEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.common.key"), safe.key)
                            .setSaveConsumer(v -> safe.key = v).build());

                    // 子项 2: 枪械 ID (id)
                    subEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.tacz.id"), safe.data.id)
                            .setSaveConsumer(v -> safe.data.id = v).build());

                    // 子项 3: 射击模式 (FireMode)
                    subEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.tacz.fire_mode"), safe.data.FireMode)
                            .setSaveConsumer(v -> safe.data.FireMode = v).build());

                    // 子项 4: 需要弹药? (AmmoInBarrel)
                    subEntries.add(eb.startBooleanToggle(Component.translatable("config.endofdays_re.tacz.ammo"), safe.data.AmmoInBarrel)
                            .setSaveConsumer(v -> safe.data.AmmoInBarrel = v).build());

                    // 子项 5: 生成权重 (weight)
                    subEntries.add(eb.startIntField(Component.translatable("config.endofdays_re.tacz.weight"), safe.data.weight)
                            .setSaveConsumer(v -> safe.data.weight = v).build());

                    // 子项 6: 攻击半径 (radius)
                    subEntries.add(eb.startIntField(Component.translatable("config.endofdays_re.tacz.radius"), safe.data.radius)
                            .setSaveConsumer(v -> safe.data.radius = v).build());

                    // 子项 7: 移动速度 (注意你的变量名是 move_sped)
                    subEntries.add(eb.startFloatField(Component.translatable("config.endofdays_re.tacz.speed"), safe.data.move_sped)
                            .setSaveConsumer(v -> safe.data.move_sped = v).build());

                    // 子项 8: 攻击间隔 (注意你的变量名是 attadk_speed)
                    subEntries.add(eb.startIntField(Component.translatable("config.endofdays_re.tacz.attack_speed"), safe.data.attadk_speed)
                            .setSaveConsumer(v -> safe.data.attadk_speed = v).build());

                    return new MultiElementListEntry<>(Component.literal(safe.key), safe, subEntries, true);
                }
        ));
    }

    public static class TargetEntryWrapper {
        public String key;
        public CommonBuild.TargetSelect data;

        public TargetEntryWrapper(String key, CommonBuild.TargetSelect data) {
            this.key = key;
            this.data = data;
        }
    }

    public static class StringPairWrapper {
        public String key;
        public String value;

        public StringPairWrapper(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }

    public static class TaczDataWrapper {
        public String key;
        public CommonBuild.TaczData data;

        public TaczDataWrapper(String key, CommonBuild.TaczData data) {
            this.key = key;
            this.data = data;
        }
    }

}