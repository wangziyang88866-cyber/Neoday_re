package com.endofdays_re.level.register.effect;

import com.endofdays_re.utils.ModUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

public class LacerateEffect extends MobEffect {
    // 1.21.1 必须使用 ResourceLocation 代替 UUID 字符串
    private static final ResourceLocation LACERATE_ARMOR_ID = ResourceLocation.fromNamespaceAndPath(ModUtils.MODID, "effect.lacerate_armor");

    public LacerateEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF6B35); // 橙色

        // 注册属性修改器：削弱护甲
        // 注意：Operation.MULTIPLY_TOTAL 在 1.21.1 对应 ADD_MULTIPLIED_TOTAL
        this.addAttributeModifier(Attributes.ARMOR,
                LACERATE_ARMOR_ID,
                -0.5D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }


    /**
     * 修正：方法名变为 shouldApplyEffectTickThisTick
     */
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 每 20 tick 执行一次 applyEffectTick
        return duration % 20 == 0;
    }

    /**
     * 修正：返回值变为 boolean
     */
    @Override
    public boolean applyEffectTick(@NotNull LivingEntity livingEntity, int amplifier) {
        if (livingEntity.level().isClientSide) {
            // 在这里可以生成血粒子效果
        }
        return true;
    }

}