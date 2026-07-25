package com.endofdays_re.level.register.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class InfectionEffect extends MobEffect {

    public InfectionEffect() {
        // 1.21.1 构造函数：有害分类，亮绿色
        super(MobEffectCategory.HARMFUL, 0x55FF55);
    }

    /**
     * 1.21.1 修正：removeAttributeModifiers 的逻辑
     * 注意：在 1.21 中，getAttributeMap() 变更为使用 AttributeMap 对象
     */
    @Override
    public void onEffectStarted(@NotNull LivingEntity entity, int amplifier) {
        super.onEffectStarted(entity, amplifier);
        // 如果你想在感染开始时做些什么（比如发冷、发抖粒子），写在这里
    }


}