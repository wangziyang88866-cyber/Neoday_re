package com.endofdays_re.config;

import com.endofdays_re.client.config.data.*;
import com.endofdays_re.event.data.AllSyncValue;
import com.endofdays_re.utils.ModUtils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;


public enum ConfigData {
    ;
    // Config Holders
    public static ConfigHolder<Enablebuild> ConfigDataEnable;
    public static ConfigHolder<Daybuild> ConfigDataDay;
    public static ConfigHolder<CommonBuild> ConfigDataCommon;
    public static ConfigHolder<Dimensionbuild> ConfigDataDimension;
    public static ConfigHolder<DropsBuild> ConfigDataDrop;
    public static ConfigHolder<LanguageBuild> ConfigDataLanguageMsg;
    public static ConfigHolder<ScreenBuild> ConfigDataScreen;
    public static ConfigHolder<AttributeBuild> ConfigAttribute;
    public static ConfigHolder<ArrmorBuild> ConfigArrmorData;
    public static ConfigHolder<InvasionBuild> ConfigInvasionData;
    public static ConfigHolder<SpawnerBuild> ConfigSpawnerData;


    // Static Config Instances (Cache)
    public static ScreenBuild ScreenConfigData;
    public static Daybuild dayConfigData;
    public static CommonBuild commonConfigData;
    public static Enablebuild enableConfigData;
    public static Dimensionbuild dimensionConfigData;
    public static DropsBuild dropConfigData;
    public static LanguageBuild languageMsgConfigData;
    public static AttributeBuild AttributeConfigData;
    public static ArrmorBuild arrmorData;
    public static InvasionBuild InvasionData;
    public static SpawnerBuild SpawnerConfigData;


    /**
     * 初始化并注册所有配置
     */
    public static void commonInit() {
        ConfigDataEnable = AutoConfig.register(Enablebuild.class, JanksonConfigSerializer::new);
        ConfigDataDay = AutoConfig.register(Daybuild.class, JanksonConfigSerializer::new);
        ConfigDataCommon = AutoConfig.register(CommonBuild.class, JanksonConfigSerializer::new);
        ConfigDataDimension = AutoConfig.register(Dimensionbuild.class, JanksonConfigSerializer::new);
        ConfigDataDrop = AutoConfig.register(DropsBuild.class, JanksonConfigSerializer::new);
        ConfigDataLanguageMsg = AutoConfig.register(LanguageBuild.class, JanksonConfigSerializer::new);
        ConfigDataScreen = AutoConfig.register(ScreenBuild.class, JanksonConfigSerializer::new);
        ConfigAttribute = AutoConfig.register(AttributeBuild.class, JanksonConfigSerializer::new);
        ConfigArrmorData = AutoConfig.register(ArrmorBuild.class, JanksonConfigSerializer::new);
        ConfigInvasionData = AutoConfig.register(InvasionBuild.class, JanksonConfigSerializer::new);
        ConfigSpawnerData = AutoConfig.register(SpawnerBuild.class, JanksonConfigSerializer::new);

        // 注册完成后读取一次数据
        readData();
    }

    /**
     * 刷新内存中的配置缓存
     */
    public static void readData() {
        ScreenConfigData = ConfigDataScreen.get();
        dayConfigData = ConfigDataDay.get();
        commonConfigData = ConfigDataCommon.get();
        enableConfigData = ConfigDataEnable.get();
        dimensionConfigData = ConfigDataDimension.get();
        dropConfigData = ConfigDataDrop.get();
        languageMsgConfigData = ConfigDataLanguageMsg.get();
        AttributeConfigData = ConfigAttribute.get();
        arrmorData = ConfigArrmorData.get();
        InvasionData = ConfigInvasionData.get();
        SpawnerConfigData = ConfigSpawnerData.get();

    }

    /**
     * 保存并重载（通常在GUI修改后或指令触发时调用）
     * 修正：没必要先save再load，AutoConfig的save会自动同步内存
     */
    public static void build() {
        ConfigDataEnable.save();
        ConfigDataDay.save();
        ConfigDataCommon.save();
        ConfigDataDimension.save();
        ConfigDataDrop.save();
        ConfigDataLanguageMsg.save();
        ConfigDataScreen.save();
        ConfigAttribute.save();
        ConfigArrmorData.save();
        ConfigInvasionData.save();
        ConfigSpawnerData.save();

        // 保存后刷新缓存
        readData();
    }

    /**
     * 修正：增加包含判断，防止 ID 不存在时 NullPointerException
     */
    public static boolean isDayEnable(String id) {
        if (dayConfigData != null && dayConfigData.data.containsKey(id)) {
            var entry = dayConfigData.data.get(id);
            long currentDay = AllSyncValue.Instance.day;
            return currentDay >= entry.day && currentDay <= entry.endDay;
        }
        return false;
    }

    /**
     * 修正：增加包含判断
     */
    public static boolean isModeEnable(String id) {
        if (enableConfigData != null && enableConfigData.Data.containsKey(id)) {
            return enableConfigData.Data.get(id).enable;
        }
        return false;
    }

    /**
     * 修正：增加包含判断及随机值安全检查
     */
    public static boolean CheckProbabilityFloat(String id) {
        if (commonConfigData != null && commonConfigData.commonFloat.containsKey(id)) {
            var data = commonConfigData.commonFloat.get(id);
            // 确保 min < max, 否则 nextFloat 会报错
            float min = Math.min(data.min_value, data.max_value);
            float max = Math.max(data.min_value, data.max_value);

            float rolled = (min == max) ? min : ModUtils.random.nextFloat(min, max);
            return rolled < data.value;
        }
        return false;
    }

}