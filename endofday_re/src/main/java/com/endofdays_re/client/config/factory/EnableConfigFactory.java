package com.endofdays_re.client.config.factory;

import com.endofdays_re.client.config.data.Enablebuild;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

public class EnableConfigFactory {
    public static void build(ConfigBuilder builder, Enablebuild config) {
        ConfigEntryBuilder eb = builder.entryBuilder();
        // 获取或创建“模块开关”主分类
        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("config.endofdays_re.category.module_enable"));

        // 遍历 Data Map，将每一个 EnableData 渲染为一个子类别 (SubCategory)
        config.Data.forEach((key, enableData) -> {
            // 使用 enableData.lang 作为标题进行本地化展示
            var sub = eb.startSubCategory(Component.translatable(enableData.lang));

            // 1. 核心开关：是否启用该功能
            sub.add(eb.startBooleanToggle(Component.translatable("config.endofdays_re.common.enable"), enableData.enable)
                    .setSaveConsumer(v -> enableData.enable = v)
                    .setDefaultValue(enableData.DefaultValue) // 使用数据里的默认值
                    .build());

            // 2. 翻译键展示 (可选，设置为不可编辑，仅供参考或调试)
            sub.add(eb.startStrField(Component.translatable("config.endofdays_re.common.lang_key"), enableData.lang)
                    .setSaveConsumer(v -> enableData.lang = v)
                    .build());

            // 3. 默认值显示 (只读，让玩家知道模组推荐的初始状态)
            sub.add(eb.startBooleanToggle(Component.translatable("config.endofdays_re.common.default_value"), enableData.DefaultValue)
                    .setSaveConsumer(v -> {
                    }) // 不允许修改默认值字段本身
                    .build());

            // 将子类别添加到主页面
            category.addEntry(sub.build());
        });
    }
}