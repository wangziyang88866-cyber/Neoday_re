package com.endofdays_re.level.goal;


import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.EnumSet;

public class EatGoldenAppleGoal extends Goal {
    private final Mob mob;
    private final int EAT_DURATION = 32; // 标准食用时间
    private int eatTick = 0;

    public EatGoldenAppleGoal(Mob mob) {
        this.mob = mob;
        // 占据移动和看向标志，吃东西时会专注动作
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // 修改后的判定条件：血量低于最大血量的 20%
        float healthPercent = this.mob.getHealth() / this.mob.getMaxHealth();
        return healthPercent <= 0.2F && getAppleHand() != null;
    }

    private InteractionHand getAppleHand() {
        // 检查主手或副手是否有金苹果或附魔金苹果
        if (isGoldenApple(this.mob.getItemInHand(InteractionHand.MAIN_HAND))) {
            return InteractionHand.MAIN_HAND;
        }
        if (isGoldenApple(this.mob.getItemInHand(InteractionHand.OFF_HAND))) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    private boolean isGoldenApple(ItemStack stack) {
        return stack.is(Items.GOLDEN_APPLE) || stack.is(Items.ENCHANTED_GOLDEN_APPLE);
    }

    @Override
    public void start() {
        this.eatTick = 0;
        InteractionHand hand = getAppleHand();
        if (hand != null) {
            // 启动原版使用动作（会出现食物碎屑粒子）
            this.mob.startUsingItem(hand);
        }
    }

    @Override
    public void tick() {
        this.eatTick++;

        // 每 4 tick 播放一次啃食声
        if (this.eatTick % 4 == 0) {
            this.mob.playSound(SoundEvents.GENERIC_EAT, 0.5F, 1.0F);
        }

        // 食用完成
        if (this.eatTick >= EAT_DURATION) {
            applyEffects();
        }
    }

    private void applyEffects() {
        InteractionHand hand = getAppleHand();
        if (hand == null) return;

        ItemStack stack = this.mob.getItemInHand(hand);
        boolean isEnchanted = stack.is(Items.ENCHANTED_GOLDEN_APPLE);

        // 1. 执行瞬间治疗 (heal)
        if (isEnchanted) {
            // 附魔金苹果：瞬间恢复 8 颗心
            this.mob.heal(16.0F);

            // 依然保留辅助 Buff (抗性、抗火等)
            this.mob.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 6000, 0));
            this.mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0));
            // 伤害吸收 IV (瞬间提供 8 点金心)
            this.mob.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3));
        } else {
            // 普通金苹果：瞬间恢复 2 颗心
            this.mob.heal(4.0F);

            // 伤害吸收 I (瞬间提供 2 点金心)
            this.mob.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 0));
        }

        // 2. 消耗物品
        stack.shrink(1);

        // 3. 视觉与听觉反馈
        this.mob.playSound(SoundEvents.PLAYER_BURP, 0.5F, 1.0F);
        // 手动添加一些治愈粒子，让瞬间回血更有反馈感
        if (this.mob.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                    this.mob.getX(), this.mob.getY() + 1.0D, this.mob.getZ(),
                    10, 0.3, 0.5, 0.3, 0.05);
        }

        // 4. 清理状态
        this.mob.stopUsingItem();
        this.stop();
    }

    @Override
    public boolean canContinueToUse() {
        // 如果吃的时候果子没了（比如被打掉了），则停止
        return getAppleHand() != null && this.eatTick < EAT_DURATION;
    }

    @Override
    public void stop() {
        this.mob.stopUsingItem();
        super.stop();
    }
}