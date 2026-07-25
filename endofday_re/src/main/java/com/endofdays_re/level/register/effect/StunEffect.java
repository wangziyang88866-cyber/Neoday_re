package com.endofdays_re.level.register.effect;

import com.endofdays_re.utils.ModUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class StunEffect extends MobEffect {
    public static final String STUN_ACTIVE_TAG = "is_stunned";

    private static final ResourceLocation STUN_SPEED_ID = ResourceLocation.fromNamespaceAndPath(ModUtils.MODID, "effect.stun_speed");
    private static final ResourceLocation STUN_ATTACK_SPEED_ID = ResourceLocation.fromNamespaceAndPath(ModUtils.MODID, "effect.stun_attack_speed");

    public StunEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFFF00);
        // 属性修改器会自动处理，不需要手动在 removeAttributeModifiers 里拿实体
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, STUN_SPEED_ID, -1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        this.addAttributeModifier(Attributes.ATTACK_SPEED, STUN_ATTACK_SPEED_ID, -1.0D, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }


    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 如果你只是想实现控制，不需要每 tick 跑逻辑，返回 false 性能最好
        return false;
    }


}