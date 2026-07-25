package com.endofdays_re.level.register.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class BleedingEffect extends MobEffect {

    public BleedingEffect() {
        // 1.21.1 使用 Properties 构建，0x8B0000 是深红色
        super(MobEffectCategory.HARMFUL, 0x8B0000);
    }

    /**
     * 1.21.1 修正：返回值变为 boolean
     */
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        // 使用实体自身的持久化数据记录触发次数
        int bleedTicks = entity.getPersistentData().getInt("BleedTickCount");
        bleedTicks++;
        entity.getPersistentData().putInt("BleedTickCount", bleedTicks);

        // 计算伤害：基础 1.0 + 等级加成 + 时间成长加成
        // 这里的 1.0f 替代了你之前的成员变量 damage，确保线程安全
        float baseDamage = 1.0f;
        float timeBonus = (bleedTicks / 5.0f);
        float finalDamage = baseDamage + (amplifier * 1.0f) + timeBonus;

        // 应用伤害
        entity.hurt(entity.damageSources().magic(), finalDamage);

        // 视觉效果：在脚部生成一些红色粒子（可选）
        if (entity.level().isClientSide) {
            // 客户端粒子逻辑通常在单独的渲染类或特定的 tick 事件中，
            // 但这里可以简单模拟流血视觉
        }

        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 每 20 tick (1秒) 触发一次 applyEffectTick
        // 注意：在 1.21.1 中，如果持续时间为 0，通常不会再触发
        return duration % 20 == 0;
    }
}