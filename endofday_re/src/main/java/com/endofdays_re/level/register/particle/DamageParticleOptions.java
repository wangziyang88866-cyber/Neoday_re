package com.endofdays_re.level.register.particle;

import com.endofdays_re.level.register.RegistryParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;

/**
 * 伤害粒子参数类 - 适配 1.21.1 NeoForge
 */
public record DamageParticleOptions(
        float damage,
        int color,
        boolean isCrit,
        boolean damageBold,
        String prefix,
        int prefixColor,
        boolean prefixBold,
        String suffix,
        int suffixColor,
        boolean suffixBold
) implements ParticleOptions {

    // 1. 定义 MapCodec (用于 JSON 数据包和命令解析)
    public static final MapCodec<DamageParticleOptions> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.FLOAT.fieldOf("damage").forGetter(DamageParticleOptions::damage),
                    Codec.INT.fieldOf("color").forGetter(DamageParticleOptions::color),
                    Codec.BOOL.fieldOf("is_crit").forGetter(DamageParticleOptions::isCrit),
                    Codec.BOOL.fieldOf("damage_bold").forGetter(DamageParticleOptions::damageBold),
                    Codec.STRING.fieldOf("prefix").forGetter(DamageParticleOptions::prefix),
                    Codec.INT.fieldOf("prefix_color").forGetter(DamageParticleOptions::prefixColor),
                    Codec.BOOL.fieldOf("prefix_bold").forGetter(DamageParticleOptions::prefixBold),
                    Codec.STRING.fieldOf("suffix").forGetter(DamageParticleOptions::suffix),
                    Codec.INT.fieldOf("suffix_color").forGetter(DamageParticleOptions::suffixColor),
                    Codec.BOOL.fieldOf("suffix_bold").forGetter(DamageParticleOptions::suffixBold)
            ).apply(instance, DamageParticleOptions::new)
    );

    // 2. 定义 StreamCodec (用于网络同步)
    // 因为字段超过 8 个，使用 StreamCodec.of 手动读写以绕过 composite 限制
    public static final StreamCodec<RegistryFriendlyByteBuf, DamageParticleOptions> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> {
                buffer.writeFloat(value.damage);
                buffer.writeInt(value.color);
                buffer.writeBoolean(value.isCrit);
                buffer.writeBoolean(value.damageBold);
                buffer.writeUtf(value.prefix);
                buffer.writeInt(value.prefixColor);
                buffer.writeBoolean(value.prefixBold);
                buffer.writeUtf(value.suffix);
                buffer.writeInt(value.suffixColor);
                buffer.writeBoolean(value.suffixBold);
            },
            (buffer) -> new DamageParticleOptions(
                    buffer.readFloat(),
                    buffer.readInt(),
                    buffer.readBoolean(),
                    buffer.readBoolean(),
                    buffer.readUtf(),
                    buffer.readInt(),
                    buffer.readBoolean(),
                    buffer.readUtf(),
                    buffer.readInt(),
                    buffer.readBoolean()
            )
    );

    @Override
    @NotNull
    public ParticleType<DamageParticleOptions> getType() {
        return RegistryParticles.DAMAGE_NUMBER.get();
    }
}