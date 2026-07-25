package com.endofdays_re.level.register.effect;

import com.endofdays_re.utils.ModUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

public class FractureEffect extends MobEffect {

    // 1.21.1 使用 ResourceLocation 代替 UUID
    private static final ResourceLocation SPEED_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(ModUtils.MODID, "effect.fracture_speed");

    public FractureEffect() {
        // 1.21.1 构造函数：分类与颜色
        super(MobEffectCategory.HARMFUL, 0x8B4513);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED,
                SPEED_MODIFIER_ID,
                -0.3D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    /**
     * 修正：1.21.1 的正确方法名是 shouldApplyEffectTickThisTick
     */
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 每一 tick 都执行（为了实时拦截疾跑和跳跃压制）
        return true;
    }

    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        // 核心逻辑：禁止疾跑
        if (entity.isSprinting()) {
            entity.setSprinting(false);
        }

        // 等级加深逻辑：模拟骨折无法向上发力
        if (entity.getDeltaMovement().y > 0 && !entity.onGround()) {
            // 等级越高 (amplifier)，向下的压制力越大
            double suppression = 0.05D * (amplifier + 1);
            entity.setDeltaMovement(entity.getDeltaMovement().add(0, -suppression, 0));
        }

        return true;
    }


}