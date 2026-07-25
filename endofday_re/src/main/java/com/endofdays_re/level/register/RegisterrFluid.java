package com.endofdays_re.level.register;

import com.endofdays_re.level.register.fluid.QuicksandFluid;
import com.endofdays_re.utils.ModUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class RegisterrFluid {
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID, ModUtils.MODID);

    public static final DeferredHolder<Fluid, FlowingFluid> FLOWING_QUICK_SAND =
            FLUIDS.register("flowing_quicksand", QuicksandFluid.Flowing::new);
    public static final DeferredHolder<Fluid, FlowingFluid> SOURCE_QUICK_SAND =
            FLUIDS.register("quicksand",
                    QuicksandFluid.Source::new);

}
