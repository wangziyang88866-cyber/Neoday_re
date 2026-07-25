package com.endofdays_re.level.register;

import com.endofdays_re.level.register.particle.DamageParticleOptions;
import com.endofdays_re.utils.ModUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

public class RegistryParticles {
    // 1. 使用 Registries.PARTICLE_TYPE
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, ModUtils.MODID);

    public static final DeferredHolder<ParticleType<?>, ParticleType<DamageParticleOptions>> DAMAGE_NUMBER =
            PARTICLE_TYPES.register("damage_number", () ->
                    new ParticleType<>(false) {
                        @Override
                        @NotNull
                        public MapCodec<DamageParticleOptions> codec() {
                            return DamageParticleOptions.CODEC;
                        }

                        @Override
                        @NotNull
                        public StreamCodec<? super RegistryFriendlyByteBuf, DamageParticleOptions> streamCodec() {
                            return DamageParticleOptions.STREAM_CODEC;
                        }
                    }
            );
}