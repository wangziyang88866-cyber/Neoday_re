package com.endofdays_re.level.register;

import com.endofdays_re.utils.ModUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public enum RegisterBiomes {
    ;
    // 1. 使用 Registries.BIOME 代替 ForgeRegistries
    public static final DeferredRegister<Biome> BIOMES =
            DeferredRegister.create(Registries.BIOME, ModUtils.MODID);

    // 2. 使用 DeferredHolder 代替 RegistryObject
    // 荒地群系
    public static final DeferredHolder<Biome, Biome> END_OF_DAYS_WASTELAND =
            BIOMES.register("wasteland", RegisterBiomes::createWastelandBiome);

    // 极寒绝地
    public static final DeferredHolder<Biome, Biome> FROZEN_DEADLAND =
            BIOMES.register("frozen_deadland", RegisterBiomes::createFrozenDeadlandBiome);

    public static Biome createWastelandBiome() {
        MobSpawnSettings spawnSettings = new MobSpawnSettings.Builder()
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.ZOMBIE, 20, 4, 4))
                .build();

        BiomeSpecialEffects effects = new BiomeSpecialEffects.Builder()
                .waterColor(0x3f76e4)
                .waterFogColor(0x050533)
                .fogColor(0xc0d8ff)
                .skyColor(0x77adff)
                .ambientParticle(new AmbientParticleSettings(ParticleTypes.WHITE_ASH, 0.5f))
                .build();

        return new Biome.BiomeBuilder()
                // 3. 1.21.1 中使用 precipitation() 枚举
                .hasPrecipitation(true)
                .temperature(-0.5f)
                .downfall(0.8f)
                .specialEffects(effects)
                .mobSpawnSettings(spawnSettings)
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .build();
    }

    public static Biome createFrozenDeadlandBiome() {
        MobSpawnSettings spawnSettings = new MobSpawnSettings.Builder()
                .addSpawn(MobCategory.MONSTER, new MobSpawnSettings.SpawnerData(EntityType.STRAY, 20, 4, 4))
                .build();

        BiomeSpecialEffects effects = new BiomeSpecialEffects.Builder()
                .waterColor(0x1e366e)
                .waterFogColor(0x0a1124)
                .fogColor(0x95a5ad)
                .skyColor(0x5f6b7d)
                .ambientParticle(new AmbientParticleSettings(ParticleTypes.SNOWFLAKE, 0.75f))
                .build();

        return new Biome.BiomeBuilder()
                .hasPrecipitation(true)
                .temperature(-1.5f)
                .downfall(1.0f)
                .specialEffects(effects)
                .mobSpawnSettings(spawnSettings)
                .generationSettings(BiomeGenerationSettings.EMPTY)
                .build();
    }
}