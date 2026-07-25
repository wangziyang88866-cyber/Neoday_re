package com.endofdays_re.client.config;

import com.endofdays_re.client.config.factory.*;
import com.endofdays_re.config.ConfigData;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

public class ConfigScreenBuild {  // ✅ 删除 extends ConfigData

    public static void loadGui() {
        ModLoadingContext.get().registerExtensionPoint(
                IConfigScreenFactory.class,
                () -> (client, parent) -> getConfigBuilder().build()
        );
    }

    public static ConfigBuilder getConfigBuilder() {
        ConfigBuilder builder = ConfigBuilder.create()
                .setTitle(Component.literal("主菜单"));
        builder.setDefaultBackgroundTexture(ResourceLocation.parse("minecraft:textures/block/oak_planks.png"));
        builder.setGlobalized(true);
        builder.setGlobalizedExpanded(false);

        ArrmorConfigFactory.build(builder, ConfigData.ConfigArrmorData.getConfig());
        AttributeConfigFactory.build(builder, ConfigData.ConfigAttribute.getConfig());
        DropsConfigFactory.build(builder, ConfigData.ConfigDataDrop.getConfig());
        CommonConfigFactory.build(builder, ConfigData.ConfigDataCommon.getConfig());
        DayConfigFactory.build(builder, ConfigData.ConfigDataDay.getConfig());
        DimensionConfigFactory.build(builder, ConfigData.ConfigDataDimension.getConfig());
        EnableConfigFactory.build(builder, ConfigData.ConfigDataEnable.getConfig());
        InvasionConfigFactory.build(builder, ConfigData.ConfigInvasionData.getConfig());
        SpawnerConfigFactory.build(builder, ConfigData.ConfigSpawnerData.getConfig());
        ScreenConfigFactory.build(builder, ConfigData.ConfigDataScreen.getConfig());

        builder.setSavingRunnable(ConfigData::build);

        return builder;
    }
}