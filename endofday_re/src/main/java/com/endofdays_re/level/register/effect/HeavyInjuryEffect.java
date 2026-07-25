package com.endofdays_re.level.register.effect;

import com.endofdays_re.utils.ModUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.NotNull;

public class HeavyInjuryEffect extends MobEffect {
    // 1.21.1 必须使用 ResourceLocation 定义属性修改器 ID
    private static final ResourceLocation HEAVY_INJURY_SPEED_ID = ResourceLocation.fromNamespaceAndPath(ModUtils.MODID, "effect.heavy_injury_speed");

    public HeavyInjuryEffect() {
        // 1.21.1 构造函数保持 (分类, 颜色)
        super(MobEffectCategory.HARMFUL, 0x330000);

        // 示例：重伤通常伴随减速（每级额外减 15%）
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED,
                HEAVY_INJURY_SPEED_ID,
                -0.15D,
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    /**
     * 1.21.1 修正：方法名变更为 shouldApplyEffectTickThisTick
     */
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        // 返回 true 表示每 tick 都会调用 applyEffectTick
        return true;
    }

    /**
     * 1.21.1 修正：返回值变为 boolean
     */
    @Override
    public boolean applyEffectTick(@NotNull LivingEntity entity, int amplifier) {
        // 这里可以放置自定义逻辑，例如禁止自然回血或周期性产生血粒子

        // 返回 true 表示效果应用成功
        return true;
    }
}