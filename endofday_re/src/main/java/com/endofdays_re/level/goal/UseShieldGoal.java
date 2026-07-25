package com.endofdays_re.level.goal;

import com.endofdays_re.client.config.data.CommonBuild;
import com.endofdays_re.config.ConfigData;
import com.endofdays_re.utils.ModUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class UseShieldGoal extends Goal {
    // 配置参数
    private static final int ATTACK_PREP_TICKS = 2; // 攻击前的反应延迟
    private static final double ATTACK_RANGE_SQR = 6.25; // 2.5 * 2.5
    private static final double DETECTION_RANGE = 12.0;
    private final Mob mob;
    private final List<Item> items = new ArrayList<>();
    private final double distanceSqr;
    private final CommonBuild configData;
    private final Random random = new Random();
    private State currentState = State.DEFENDING;
    private int stateTicks = 0;
    private int shieldCooldown = 0;
    private int globalAttackCooldown = 0;
    private LivingEntity target;
    public UseShieldGoal(Mob mob, double distance, CommonBuild data, List<String> useItemList) {
        this.mob = mob;
        this.distanceSqr = distance * distance;
        this.configData = data;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));

        for (String itemName : useItemList) {
            Item item = ModUtils.getItem(itemName).value();
            items.add(item);
        }
    }

    public UseShieldGoal(Mob mob, double distance, List<String> useItemList) {
        this.mob = mob;
        this.distanceSqr = distance * distance;
        this.configData = ConfigData.commonConfigData;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK, Goal.Flag.MOVE));

        for (String itemName : useItemList) {
            Item item = ModUtils.getItem(itemName).value();
            items.add(item);
        }
    }

    @Override
    public boolean canUse() {
        if (shieldCooldown > 0) {
            shieldCooldown--;
            return false;
        }

        this.target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;
        if (!mob.getOffhandItem().is(Items.SHIELD)) return false;

        double dSqr = mob.distanceToSqr(target);
        if (dSqr > distanceSqr) return false;

        // 触发逻辑：弹射物 > 远程 > 近战频率
        return detectIncomingProjectile() || hasTargetAttack() || isMeleeAttackSituation();
    }

    @Override
    public boolean canContinueToUse() {
        // 只有当目标死亡、盾牌丢失或处于长久冷却时才停止
        return target != null && target.isAlive() && mob.getOffhandItem().is(Items.SHIELD) && shieldCooldown < 20;
    }

    @Override
    public void start() {
        enterState(State.DEFENDING);
    }

    @Override
    public void stop() {
        mob.stopUsingItem();
        this.currentState = State.DEFENDING;
        this.shieldCooldown = 20;
    }

    @Override
    public void tick() {
        if (target == null) return;
        stateTicks++;
        if (globalAttackCooldown > 0) globalAttackCooldown--;

        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        switch (currentState) {
            case DEFENDING -> updateDefending();
            case PREPARING_ATTACK -> updatePreparing();
            case ATTACKING -> updateAttacking();
        }
    }

    private void enterState(State newState) {
        this.currentState = newState;
        this.stateTicks = 0;

        if (newState == State.DEFENDING) {
            mob.startUsingItem(InteractionHand.OFF_HAND);
        } else {
            mob.stopUsingItem();
        }
    }

    private void updateDefending() {
        // 确保举盾
        if (!mob.isUsingItem()) mob.startUsingItem(InteractionHand.OFF_HAND);

        // 移动逻辑：保持距离
        double dSqr = mob.distanceToSqr(target);
        if (dSqr > ATTACK_RANGE_SQR) {
            mob.getNavigation().moveTo(target, getSpeedMod());
        } else if (dSqr < 1.5 * 1.5) {
            // 太近了往后退
            Vec3 back = mob.position().subtract(target.position()).normalize().scale(2);
            mob.getNavigation().moveTo(mob.getX() + back.x, mob.getY(), mob.getZ() + back.z, getSpeedMod());
        }

        // 检查反击机会
        if (globalAttackCooldown <= 0 && dSqr <= ATTACK_RANGE_SQR && isGoodOpportunity()) {
            enterState(State.PREPARING_ATTACK);
        }
    }

    private void updatePreparing() {
        // 等待几个 tick 让 stopUsingItem 生效，防止动作冲突
        if (stateTicks >= ATTACK_PREP_TICKS) {
            enterState(State.ATTACKING);
        }
    }

    private void updateAttacking() {
        if (target != null && mob.hasLineOfSight(target)) {
            mob.swing(InteractionHand.MAIN_HAND);
            mob.doHurtTarget(target);
        }

        this.globalAttackCooldown = 20 + random.nextInt(10);
        enterState(State.DEFENDING);
    }

    private boolean isGoodOpportunity() {
        // 如果对方正在挥刀，举盾不还手；如果对方刚打完，立即还手
        return !target.swinging && !detectIncomingProjectile();
    }

    // --- 辅助逻辑 ---

    private double getSpeedMod() {
        return mob.getAttributeValue(Attributes.MOVEMENT_SPEED) * (currentState == State.DEFENDING ? 0.6 : 1.0);
    }

    private boolean detectIncomingProjectile() {
        // 性能优化：每 2 tick 检测一次
        if (mob.tickCount % 2 != 0) return false;

        return !mob.level().getEntitiesOfClass(Projectile.class,
                mob.getBoundingBox().inflate(DETECTION_RANGE),
                p -> {
                    if (p.getOwner() != target) return false;
                    Vec3 vel = p.getDeltaMovement().normalize();
                    Vec3 toMob = mob.getEyePosition().subtract(p.position()).normalize();
                    return vel.dot(toMob) > 0.85;
                }
        ).isEmpty();
    }

    private boolean hasTargetAttack() {
        return target.isUsingItem() && items.contains(target.getMainHandItem().getItem());
    }

    private boolean isMeleeAttackSituation() {
        return target.swinging && mob.distanceToSqr(target) <= 9.0;
    }

    private enum State {DEFENDING, PREPARING_ATTACK, ATTACKING}
}