package com.endofdays_re.client.config.factory;

import com.endofdays_re.client.config.data.AttributeBuild;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.MultiElementListEntry;
import me.shedaniel.clothconfig2.gui.entries.NestedListListEntry;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AttributeConfigFactory {

    public static void build(ConfigBuilder builder, AttributeBuild config) {
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("config.endofdays_re.category.attribute"));

        // 1. 转换为包装列表
        List<AttributeEntryWrapper> wrapperList = new ArrayList<>();
        config.attributes.forEach((k, v) -> wrapperList.add(new AttributeEntryWrapper(k, v)));

        // 2. 创建嵌套列表
        category.addEntry(new NestedListListEntry<AttributeEntryWrapper, MultiElementListEntry<AttributeEntryWrapper>>(
                Component.translatable("config.endofdays_re.attribute_list"),
                wrapperList,
                false,
                Optional::empty,
                newList -> {
                    // 保存逻辑：写回原始 Map
                    config.attributes.clear();
                    for (int i = 0; i < newList.size(); i++) {
                        AttributeEntryWrapper wrapper = newList.get(i);
                        if (wrapper == null) continue;

                        String finalKey = wrapper.key;
                        if (finalKey == null || finalKey.isBlank() || finalKey.equals("new_entry")) {
                            finalKey = "未命名属性_" + i;
                        } else {
                            finalKey = finalKey.trim();
                        }

                        // 避免重命名后与已有 key 冲突导致静默覆盖
                        String uniqueKey = finalKey;
                        int suffix = 1;
                        while (config.attributes.containsKey(uniqueKey)) {
                            uniqueKey = finalKey + "_" + suffix++;
                        }

                        config.attributes.put(uniqueKey, wrapper.data);
                    }
                },
                // 创建新项时返回包装类
                () -> List.of(new AttributeEntryWrapper("new_entry", new AttributeBuild.AttributeData())),
                entryBuilder.getResetButtonKey(),
                true,
                true,
                (wrapper, nestedList) -> {
                    // 兜底处理
                    final AttributeEntryWrapper targetWrapper = (wrapper == null)
                            ? new AttributeEntryWrapper("new_entry", new AttributeBuild.AttributeData())
                            : wrapper;

                    List<AbstractConfigListEntry<?>> subEntries = new ArrayList<>();

                    // 配置项名称 (Map Key)
                    subEntries.add(entryBuilder.startStrField(Component.translatable("config.endofdays_re.attr.key"), targetWrapper.key)
                            .setSaveConsumer(v -> targetWrapper.key = (v == null || v.isBlank()) ? targetWrapper.key : v.trim())
                            .build());

                    // 属性 ID
                    subEntries.add(entryBuilder.startStrField(Component.translatable("config.endofdays_re.attr.id"), targetWrapper.data.id)
                            .setSaveConsumer(v -> targetWrapper.data.id = v)
                            .build());

                    // 实体 ID
                    subEntries.add(entryBuilder.startStrField(Component.translatable("config.endofdays_re.attr.entity_id"), targetWrapper.data.EntityID)
                            .setSaveConsumer(v -> targetWrapper.data.EntityID = v)
                            .build());

                    // 增益表达式
                    subEntries.add(entryBuilder.startStrField(Component.translatable("config.endofdays_re.attr.value"), targetWrapper.data.value)
                            .setSaveConsumer(v -> targetWrapper.data.value = v)
                            .setTooltip(Component.translatable("config.endofdays_re.attr.value.tooltip"))
                            .build());

                    // 生效天数
                    subEntries.add(entryBuilder.startIntField(Component.translatable("config.endofdays_re.attr.start"), targetWrapper.data.start)
                            .setSaveConsumer(v -> targetWrapper.data.start = v)
                            .build());
                    subEntries.add(entryBuilder.startIntField(Component.translatable("config.endofdays_re.attr.end"), targetWrapper.data.end)
                            .setSaveConsumer(v -> targetWrapper.data.end = v)
                            .build());

                    // 上限
                    subEntries.add(entryBuilder.startFloatField(Component.translatable("config.endofdays_re.attr.max_limit"), targetWrapper.data.max)
                            .setSaveConsumer(v -> targetWrapper.data.max = v)
                            .build());

                    String header = (targetWrapper.key.equals("new_entry")) ? "New Attribute" : targetWrapper.key;
                    return new MultiElementListEntry<>(Component.literal(header), targetWrapper, subEntries, true);
                }
        ));
    }

    // 定义一个简单的包装类，避免 Map.Entry 的不可变 Key 问题
    public static class AttributeEntryWrapper {
        public String key;
        public AttributeBuild.AttributeData data;

        public AttributeEntryWrapper(String key, AttributeBuild.AttributeData data) {
            this.key = key;
            this.data = data;
        }
    }
}