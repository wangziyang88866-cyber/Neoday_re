package com.endofdays_re.client.config.factory;


import com.endofdays_re.client.config.data.Daybuild;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

public class DayConfigFactory {
    public static void build(ConfigBuilder builder, Daybuild config) {
        ConfigEntryBuilder eb = builder.entryBuilder();
        // 获取或创建“天数设置”主分类
        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("config.endofdays_re.category.day_settings"));
        config.data.forEach((key, dayData) -> {
            // 使用 dayData.lang 作为子类别的标题，实现本地化展示
            var sub = eb.startSubCategory(Component.translatable(dayData.lang));
            // 1. 生效天数
            sub.add(eb.startIntField(Component.translatable("config.endofdays_re.day.start"), dayData.day)
                    .setSaveConsumer(v -> dayData.day = v)
                    .setDefaultValue(dayData.DefaultValue) // 使用数据里的 DefaultValue 作为重置目标
                    .build());
            // 2. 失效天数
            sub.add(eb.startIntField(Component.translatable("config.endofdays_re.day.end"), dayData.endDay)
                    .setSaveConsumer(v -> dayData.endDay = v)
                    .setDefaultValue(100)
                    .build());
            // 3. 翻译键（如果不需要玩家改，可以删掉这行或者设为只读）
            sub.add(eb.startStrField(Component.translatable("config.endofdays_re.day.lang_key"), dayData.lang)
                    .setSaveConsumer(v -> dayData.lang = v)
                    .build());
            // 4. 默认值（显示该功能的基准参考值）
            sub.add(eb.startIntField(Component.translatable("config.endofdays_re.day.default_value"), dayData.DefaultValue)
                    .setSaveConsumer(v -> dayData.DefaultValue = v)
                    .build());
            // 将构建好的子类别添加到主分类中
            category.addEntry(sub.build());
        });
    }
}
