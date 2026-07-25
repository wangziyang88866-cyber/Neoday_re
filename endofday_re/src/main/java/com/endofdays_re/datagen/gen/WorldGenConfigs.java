package com.endofdays_re.datagen.gen;

import com.endofdays_re.level.register.RegistryFeatures;
import com.endofdays_re.utils.ModUtils;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistrySetBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.*;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.List;

public class WorldGenConfigs {

    // --- Resource Keys ---
    public static final ResourceKey<ConfiguredFeature<?, ?>> BE_ZOMBIE_CONFIGURED =
            ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(ModUtils.MODID, "be_zombie"));
    public static final ResourceKey<PlacedFeature> BE_ZOMBIE_PLACED =
            ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(ModUtils.MODID, "be_zombie_placed"));
    // 修正：使用 NeoForgeRegistries.Keys
    public static final ResourceKey<BiomeModifier> ADD_BE_ZOMBIE =
            ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ResourceLocation.fromNamespaceAndPath(ModUtils.MODID, "add_be_zombie"));
    // 1.21.1 修正：使用 NeoForgeRegistries.Keys.BIOME_MODIFIERS 替代 ForgeRegistries
    public static final RegistrySetBuilder BUILDER = new RegistrySetBuilder()
            .add(Registries.CONFIGURED_FEATURE, WorldGenConfigs::bootstrapConfigured)
            .add(Registries.PLACED_FEATURE, WorldGenConfigs::bootstrapPlaced)
            .add(NeoForgeRegistries.Keys.BIOME_MODIFIERS, WorldGenConfigs::bootstrapBiomeModifiers);

    // --- Bootstrap 逻辑 ---

    // 修正参数名拼写 Bootstap -> Bootstrap
    public static void bootstrapConfigured(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        context.register(BE_ZOMBIE_CONFIGURED, new ConfiguredFeature<>(RegistryFeatures.BE_ZOMBIE_FEATURE.get(), NoneFeatureConfiguration.INSTANCE));
    }

    public static void bootstrapPlaced(BootstrapContext<PlacedFeature> context) {
        var holder = context.lookup(Registries.CONFIGURED_FEATURE).getOrThrow(BE_ZOMBIE_CONFIGURED);

        context.register(BE_ZOMBIE_PLACED, new PlacedFeature(holder, List.of(
                RarityFilter.onAverageOnceEvery(16),
                InSquarePlacement.spread(),
                HeightmapPlacement.onHeightmap(Heightmap.Types.WORLD_SURFACE_WG),
                BiomeFilter.biome()
        )));
    }

    public static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);

        // 修正：使用 BiomeModifiers.AddFeaturesBiomeModifier (NeoForge 1.21.1)
        context.register(ADD_BE_ZOMBIE, new net.neoforged.neoforge.common.world.BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(BE_ZOMBIE_PLACED)),
                GenerationStep.Decoration.TOP_LAYER_MODIFICATION
        ));
    }
}