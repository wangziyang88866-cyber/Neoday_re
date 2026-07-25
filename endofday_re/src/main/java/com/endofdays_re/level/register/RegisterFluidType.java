package com.endofdays_re.level.register;

import com.endofdays_re.level.register.fluid.QuicksandFluidType;
import com.endofdays_re.utils.ModUtils;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public enum RegisterFluidType {
    ;
    // 1. 使用 NeoForgeRegistries.Keys.FLUID_TYPES
    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, ModUtils.MODID);

    // 2. 使用 DeferredHolder
    public static final DeferredHolder<FluidType, FluidType> QUICK_SAND_TYPE =
            FLUID_TYPES.register("quicksand",
                    () -> new QuicksandFluidType(FluidType.Properties.create()
                            .density(4000)      // 高密度：物体下沉更慢，阻力更大
                            .viscosity(8000)    // 高粘度：流动性极差
                            .motionScale(0.02D) // 移动缩放：进入后几乎无法移动
                            .temperature(300)   // 常温
                            .canSwim(false)     // 禁止像在水中那样游泳
                            .canDrown(true)     // 头部没入时会窒息
                    ));
}