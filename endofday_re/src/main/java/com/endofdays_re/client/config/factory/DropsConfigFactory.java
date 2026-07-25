package com.endofdays_re.client.config.factory;

import com.endofdays_re.client.config.data.DropsBuild;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.MultiElementListEntry;
import me.shedaniel.clothconfig2.gui.entries.NestedListListEntry;
import net.minecraft.network.chat.Component;

import java.util.*;

public class DropsConfigFactory {

    public static void build(ConfigBuilder builder, DropsBuild config) {
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // 分别创建两个分类：生物掉落 和 尸体战利品
        createDropCategory(builder, entryBuilder, "config.endofdays_re.category.drop_living", config.data, "data");
        createDropCategory(builder, entryBuilder, "config.endofdays_re.category.drop_corpse", config.zombie_data, "zombie_data");
    }

    private static void createDropCategory(ConfigBuilder builder, ConfigEntryBuilder entryBuilder, String categoryKey, Map<String, DropsBuild.DropInfo> sourceMap, String fieldName) {
        ConfigCategory category = builder.getOrCreateCategory(Component.translatable(categoryKey));

        // 转换 Map 为 List 用于显示
        List<DropsBuild.DropInfo> infoList = new ArrayList<>(sourceMap.values());

        category.addEntry(new NestedListListEntry<DropsBuild.DropInfo, MultiElementListEntry<DropsBuild.DropInfo>>(
                Component.translatable("config.endofdays_re.drop_list"),
                infoList,
                false,
                Optional::empty,
                newList -> {
                    // 反向查找 Key 以保持原始键名 (各归各)
                    Map<DropsBuild.DropInfo, String> reverseMap = new HashMap<>();
                    sourceMap.forEach((k, v) -> reverseMap.put(v, k));

                    sourceMap.clear();
                    for (int i = 0; i < newList.size(); i++) {
                        DropsBuild.DropInfo info = newList.get(i);
                        if (info == null) continue;
                        String key = reverseMap.get(info);
                        if (key == null) key = fieldName + "_key_" + i;
                        sourceMap.put(key, info);
                    }
                },
                () -> List.of(new DropsBuild.DropInfo()),
                entryBuilder.getResetButtonKey(),
                true,
                true,
                (info, nestedList) -> {
                    final DropsBuild.DropInfo target = (info == null) ? new DropsBuild.DropInfo() : info;
                    List<AbstractConfigListEntry<?>> subEntries = new ArrayList<>();

                    // 1. 语言显示名/分类名
                    subEntries.add(entryBuilder.startStrField(Component.translatable("config.endofdays_re.drop.lang"), target.Language)
                            .setSaveConsumer(v -> target.Language = v).build());

                    // 2. 实体列表 (String 数组转 List 操作)
                    List<String> entityList = new ArrayList<>(Arrays.asList(target.entitys));
                    subEntries.add(entryBuilder.startStrList(Component.translatable("config.endofdays_re.drop.entities"), entityList)
                            .setSaveConsumer(newList -> target.entitys = newList.toArray(new String[0]))
                            .build());

                    // 3. 生效时间范围
                    subEntries.add(entryBuilder.startIntField(Component.translatable("config.endofdays_re.drop.day"), target.day)
                            .setSaveConsumer(v -> target.day = v).build());
                    subEntries.add(entryBuilder.startIntField(Component.translatable("config.endofdays_re.drop.end"), target.end)
                            .setSaveConsumer(v -> target.end = v).build());

                    // 4. 核心：掉落物品列表 (二次嵌套)
                    subEntries.add(createItemNestedList(entryBuilder, target));

                    String header = (target.Language == null || target.Language.isEmpty()) ? "New Drop Info" : target.Language;
                    return new MultiElementListEntry<>(Component.literal(header), target, subEntries, true);
                }
        ));
    }

    private static NestedListListEntry<DropsBuild.ItemInfo, MultiElementListEntry<DropsBuild.ItemInfo>> createItemNestedList(ConfigEntryBuilder entryBuilder, DropsBuild.DropInfo parent) {
        List<DropsBuild.ItemInfo> itemList = (parent.items == null) ? new ArrayList<>() : new ArrayList<>(Arrays.asList(parent.items));

        return new NestedListListEntry<>(
                Component.translatable("config.endofdays_re.drop.items"),
                itemList,
                false,
                Optional::empty,
                newList -> parent.items = newList.toArray(new DropsBuild.ItemInfo[0]),
                () -> List.of(new DropsBuild.ItemInfo()),
                entryBuilder.getResetButtonKey(),
                true,
                true,
                (item, nestedList) -> {
                    final DropsBuild.ItemInfo targetItem = (item == null) ? new DropsBuild.ItemInfo() : item;
                    List<AbstractConfigListEntry<?>> eEntries = new ArrayList<>();

                    // 物品基本字段
                    eEntries.add(entryBuilder.startStrField(Component.translatable("config.endofdays_re.item.lang"), targetItem.Language)
                            .setSaveConsumer(v -> targetItem.Language = v).build());
                    eEntries.add(entryBuilder.startStrField(Component.translatable("config.endofdays_re.item.id"), targetItem.ItemId)
                            .setSaveConsumer(v -> targetItem.ItemId = v).build());
                    eEntries.add(entryBuilder.startIntField(Component.translatable("config.endofdays_re.item.weight"), targetItem.weight)
                            .setSaveConsumer(v -> targetItem.weight = v).build());
                    eEntries.add(entryBuilder.startIntField(Component.translatable("config.endofdays_re.item.min"), targetItem.min)
                            .setSaveConsumer(v -> targetItem.min = v).build());
                    eEntries.add(entryBuilder.startIntField(Component.translatable("config.endofdays_re.item.max"), targetItem.max)
                            .setSaveConsumer(v -> targetItem.max = v).build());
                    eEntries.add(entryBuilder.startDoubleField(Component.translatable("config.endofdays_re.item.chance"), targetItem.chance)
                            .setSaveConsumer(v -> targetItem.chance = v).build());
                    eEntries.add(entryBuilder.startStrField(Component.translatable("config.endofdays_re.item.tag"), targetItem.tag)
                            .setSaveConsumer(v -> targetItem.tag = v).build());

                    String header = (targetItem.Language == null || targetItem.Language.isEmpty()) ? targetItem.ItemId : targetItem.Language;
                    return new MultiElementListEntry<>(Component.literal(header), targetItem, eEntries, true);
                }
        );
    }
}