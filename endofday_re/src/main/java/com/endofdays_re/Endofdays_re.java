package com.endofdays_re;

import com.endofdays_re.config.ConfigData;
import com.endofdays_re.event.data.AllSyncValue;
import com.endofdays_re.level.register.*;
import com.endofdays_re.utils.ModUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(ModUtils.MODID)
public class Endofdays_re {

    public Endofdays_re(IEventBus modEventBus) {
        RegisterEntity.ENTITIES.register(modEventBus);
        RegisterFluidType.FLUID_TYPES.register(modEventBus);
        RegistryParticles.PARTICLE_TYPES.register(modEventBus);
        RegistryFeatures.FEATURES.register(modEventBus);
        RegisterrFluid.FLUIDS.register(modEventBus);
        RgisterBlock.BLOCKS.register(modEventBus);
        RegisterBlockEntityTypes.BLOCK_ENTITIES.register(modEventBus);
        RegisterEntityAttributes.ATTRIBUTES.register(modEventBus);
        RegisterEffect.EFFECTS.register(modEventBus);
        RegisterTab.CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
        ModMemoryModuleTypes.MEMORY_MODULES.register(modEventBus);
        RegisterBiomes.BIOMES.register(modEventBus);

        // ========== 🆕 条件注册 GunHurtEvent（依赖 tacz） ==========
        if (ModList.get().isLoaded("tacz")) {
            try {
                Class<?> clazz = Class.forName("com.endofdays_re.event.GunHurtServerEvent");
                NeoForge.EVENT_BUS.register(clazz);
                // 可选：打印日志
                // ModUtils.LOGGER.info("GunHurtEvent registered because TACZ is present.");
            } catch (ClassNotFoundException ignored) {
                // 正常情况下不会发生，安全忽略
            }
        }
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        ConfigData.commonInit();
        AllSyncValue.Instance.temperature = ConfigData.commonConfigData.temperature;
        if (ModUtils.isloadMod("epicfight") && ConfigData.isModeEnable("ride_enable")) {
            throw new RuntimeException("检测到模组冲突：当安装了 Epic Fight 时，必须在配置文件中关闭 '实体堆叠'！否则将引起崩溃");
        }
    }

}
