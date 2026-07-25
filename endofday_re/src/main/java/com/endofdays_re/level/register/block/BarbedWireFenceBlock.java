package com.endofdays_re.level.register.block;

import com.endofdays_re.level.register.RegisterEffect;
import com.endofdays_re.utils.ModUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BarbedWireFenceBlock extends FenceBlock {
    public BarbedWireFenceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity livingEntity) {
            if (livingEntity instanceof ServerPlayer player && player.isCreative()) return;
            // 2. 核心优化：检查无敌帧 (hurtTime)，防止每 Tick 都扣血导致瞬间暴毙
            if (livingEntity.hurtTime <= 0) {
                float maxHealth = livingEntity.getMaxHealth();
                // 计算伤害：最大生命值的 5%，最小不低于 0.5 (1/4 颗心)
                float damageAmount = Math.max(maxHealth * 0.05F, 0.5F);
                int newAmplifier = 2; // 默认等级 I (amplifier 为 0)
                int duration = 200;   // 持续时间，比如 5 秒 (100 ticks)
                // 3. 造成伤害：使用 cactus (仙人掌) 类型的伤害源，
                // 这样会有受击红光反馈和清脆的音效，比 genericKill 更有手感
                livingEntity.hurt(level.damageSources().genericKill(), damageAmount);
                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, 2, false, false));
                // 效果 B：流血
                livingEntity.addEffect(new MobEffectInstance(RegisterEffect.BLEEDING, duration, newAmplifier, false, false));
                if (ModUtils.safeRandom.nextDouble() <= 0.2) {
                    livingEntity.addEffect(new MobEffectInstance(RegisterEffect.LACERATE, duration, newAmplifier, false, false));
                }
            }
        }
    }
}
