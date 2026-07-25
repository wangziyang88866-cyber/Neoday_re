package com.endofdays_re.level.register;

import com.endofdays_re.level.register.feature.BeZombieFeature;
import com.endofdays_re.utils.ModUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public class RegistryFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(BuiltInRegistries.FEATURE, ModUtils.MODID);

    public static final DeferredHolder<Feature<?>, Feature<NoneFeatureConfiguration>> BE_ZOMBIE_FEATURE =
            FEATURES.register("be_zombie_feature", () -> new BeZombieFeature(NoneFeatureConfiguration.CODEC));
}
