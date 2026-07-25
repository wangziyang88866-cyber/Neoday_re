package com.endofdays_re.client.config.factory;

import com.endofdays_re.client.config.data.ArrmorBuild;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.MultiElementListEntry;
import me.shedaniel.clothconfig2.gui.entries.NestedListListEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.*;

public class ArrmorConfigFactory {

    public static void build(ConfigBuilder builder, ArrmorBuild config) {
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("config.endofdays_re.category.armor"));

        category.addEntry(entryBuilder.startIntField(Component.translatable("config.endofdays_re.armor_spawn_max"), config.ArrmorSpawnMax)
                .setDefaultValue(4)
                .setSaveConsumer(val -> config.ArrmorSpawnMax = val)
                .build());

        // 核心：保持引用关系的列表
        List<ArrmorBuild.Arrmor> armorList = new ArrayList<>(config.Arrmor.values());

        category.addEntry(new NestedListListEntry<ArrmorBuild.Arrmor, MultiElementListEntry<ArrmorBuild.Arrmor>>(
                Component.translatable("config.endofdays_re.armor_list"),
                armorList,
                false,
                Optional::empty,
                newList -> {
                    // 1. 建立 反向查找表 (Object -> Original Key)
                    Map<ArrmorBuild.Arrmor, String> reverseMap = new HashMap<>();
                    config.Arrmor.forEach((s, arrmor) -> reverseMap.put(arrmor, s));
                    // 2. 清空旧 Map，重新按 newList 顺序和 Key 写入
                    config.Arrmor.clear();
                    for (int i = 0; i < newList.size(); i++) {
                        ArrmorBuild.Arrmor armor = newList.get(i);
                        if (armor == null) continue;
                        // 尝试获取原始 Key，如果找不到(说明是新加的)，生成一个不会冲突的 Key
                        String key = reverseMap.get(armor);
                        if (key == null) {
                            key = (armor.id == null || armor.id.isEmpty()) ? "new_entry_" + i : armor.id + "_" + i;
                        }
                        config.Arrmor.put(key, armor);
                    }
                },
                // 【注意】：Cloth Config 的新项创建器期望返回 List<T> 而不是单个 T
                () -> List.of(new ArrmorBuild.Arrmor()),
                entryBuilder.getResetButtonKey(),
                true,
                true,
                (armor, nestedList) -> {
                    final ArrmorBuild.Arrmor targetArmor = (armor == null) ? new ArrmorBuild.Arrmor() : armor;
                    List<AbstractConfigListEntry<?>> subEntries = new ArrayList<>();

                    // 物品信息
                    subEntries.add(entryBuilder.startStrField(Component.translatable("config.endofdays_re.armor.id"), targetArmor.id)
                            .setSaveConsumer(v -> targetArmor.id = v).build());
                    subEntries.add(entryBuilder.startDoubleField(Component.translatable("config.endofdays_re.armor.chance"), targetArmor.chance)
                            .setSaveConsumer(v -> targetArmor.chance = v).build());
                    subEntries.add(entryBuilder.startEnumSelector(Component.translatable("config.endofdays_re.armor.slot"), EquipmentSlot.class, targetArmor.slot)
                            .setSaveConsumer(v -> targetArmor.slot = v).build());
                    subEntries.add(entryBuilder.startIntField(Component.translatable("config.endofdays_re.armor.day"), targetArmor.day)
                            .setSaveConsumer(v -> targetArmor.day = v).build());
                    subEntries.add(entryBuilder.startIntField(Component.translatable("config.endofdays_re.armor.end_day"), targetArmor.end_day)
                            .setSaveConsumer(v -> targetArmor.end_day = v).build());
                    subEntries.add(entryBuilder.startBooleanToggle(Component.translatable("config.endofdays_re.armor.enchanted"), targetArmor.enchanted)
                            .setSaveConsumer(v -> targetArmor.enchanted = v).build());
                    subEntries.add(entryBuilder.startStrField(Component.translatable("config.endofdays_re.armor.tag"), targetArmor.tag)
                            .setSaveConsumer(v -> targetArmor.tag = v).build());

                    // 附魔列表
                    subEntries.add(createEnchantNestedList(entryBuilder, targetArmor));

                    String header = (targetArmor.id == null || targetArmor.id.isEmpty()) ? "New Entry" : targetArmor.id;
                    return new MultiElementListEntry<>(Component.literal(header), targetArmor, subEntries, true);
                }
        )); // 记得加 .build()
    }

    private static NestedListListEntry<ArrmorBuild.Enchante, MultiElementListEntry<ArrmorBuild.Enchante>> createEnchantNestedList(ConfigEntryBuilder entryBuilder, ArrmorBuild.Arrmor armor) {
        List<ArrmorBuild.Enchante> enchants = (armor.enchantes == null) ? new ArrayList<>() : new ArrayList<>(Arrays.asList(armor.enchantes));

        return new NestedListListEntry<>(
                Component.translatable("config.endofdays_re.armor.enchants"),
                enchants,
                false,
                Optional::empty,
                newList -> {
                    if (newList != null) {
                        armor.enchantes = newList.toArray(new ArrmorBuild.Enchante[0]);
                    }
                },
                () -> List.of(new ArrmorBuild.Enchante()),
                entryBuilder.getResetButtonKey(),
                true,
                true,
                (enchant, nestedList) -> {
                    final ArrmorBuild.Enchante targetEnch = (enchant == null) ? new ArrmorBuild.Enchante() : enchant;
                    if (targetEnch.level == null) targetEnch.level = new ArrmorBuild.EnchanteLevel();

                    List<AbstractConfigListEntry<?>> eEntries = new ArrayList<>();
                    eEntries.add(entryBuilder.startStrField(Component.translatable("config.endofdays_re.enchant.id"), targetEnch.id)
                            .setSaveConsumer(v -> targetEnch.id = v).build());
                    eEntries.add(entryBuilder.startDoubleField(Component.translatable("config.endofdays_re.enchant.chance"), targetEnch.chance)
                            .setSaveConsumer(v -> targetEnch.chance = v).build());
                    eEntries.add(entryBuilder.startIntField(Component.translatable("config.endofdays_re.enchant.min_level"), targetEnch.level.level)
                            .setSaveConsumer(v -> targetEnch.level.level = v).build());
                    eEntries.add(entryBuilder.startIntField(Component.translatable("config.endofdays_re.enchant.max_level"), targetEnch.level.maxLevel)
                            .setSaveConsumer(v -> targetEnch.level.maxLevel = v).build());

                    String header = (targetEnch.id == null || targetEnch.id.isEmpty()) ? "New Enchant" : targetEnch.id;
                    return new MultiElementListEntry<>(Component.literal(header), targetEnch, eEntries, true);
                }
        );
    }
}