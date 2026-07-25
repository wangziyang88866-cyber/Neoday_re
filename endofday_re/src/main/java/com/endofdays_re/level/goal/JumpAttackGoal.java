package com.endofdays_re.level.goal;

import com.endofdays_re.utils.ModUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class JumpAttackGoal extends Goal {

    private final Mob mob;
    private final Item weapon;
    private final double jumpSpeed;       // 竖直跳跃速度
    private final double horizontalSpeed; // 水平移动速度
    private final double maxDistance;     // 最大触发距离
    private final double attackRange;     // 攻击判定范围
    private final int cooldownTicks;      // 冷却时间
    private LivingEntity target;
    private int cooldown = 0;
    private boolean jumping = false;

    public JumpAttackGoal(Mob mob, Item weapon, double jumpSpeed, double horizontalSpeed,
                          double maxDistance, double attackRange, int cooldownTicks) {
        this.mob = mob;
        this.weapon = weapon;
        this.jumpSpeed = jumpSpeed;
        this.horizontalSpeed = horizontalSpeed;
        this.maxDistance = maxDistance;
        this.attackRange = attackRange;
        this.cooldownTicks = cooldownTicks;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (cooldown > 0) {
            cooldown--;
            return false;
        }

        this.target = mob.getTarget();
        if (target == null || !target.isAlive()) return false;

        ItemStack stack = mob.getMainHandItem();
        if (stack.getItem() != weapon) return false;

        double distanceSq = mob.distanceToSqr(target);
        return mob.onGround() && distanceSq <= maxDistance * maxDistance && mob.getVehicle() == null
                && !mob.getPersistentData().contains(ModUtils.KeyWraps("gigantic"))
                ;
    }

    @Override
    public void start() {
        if (target != null) {
            // 贴身跳，方向指向目标（包含y轴）
            Vec3 direction = target.position().subtract(mob.position()).normalize();
            mob.setDeltaMovement(direction.x * horizontalSpeed, jumpSpeed, direction.z * horizontalSpeed);
            jumping = true;
        }
    }

    @Override
    public boolean canContinueToUse() {
        return jumping && !mob.onGround();
    }

    @Override
    public void tick() {
        if (!jumping || target == null) return;

        // 看向目标
        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // 水平移动保持贴近目标
        Vec3 direction = target.position().subtract(mob.position());
        direction = new Vec3(direction.x, 0, direction.z).normalize();
        mob.setDeltaMovement(direction.x * horizontalSpeed, mob.getDeltaMovement().y, direction.z * horizontalSpeed);

        // 空中攻击判定，只要在攻击范围内即可触发
        if (mob.distanceToSqr(target) <= attackRange * attackRange) {
            // 手臂挥剑动画
            mob.swing(InteractionHand.MAIN_HAND);

            // 普通伤害
            mob.doHurtTarget(target);

            // 模拟暴击伤害（Critical Hit）
            float critExtra = mob.getMainHandItem().getDamageValue() * 0.5f;
            target.hurt(mob.level().damageSources().mobAttack(mob), critExtra);

            // 击退目标
            double dx = target.getX() - mob.getX();
            double dz = target.getZ() - mob.getZ();
            double length = Math.sqrt(dx * dx + dz * dz);
            if (length > 0.0) {
                double strength = 0.5;
                target.push(dx / length * strength, 0.2, dz / length * strength);
            }

            // 暴击粒子效果 - 客户端显示
            mob.level().addParticle(
                    ParticleTypes.CRIT,
                    mob.getX(),
                    mob.getY() + mob.getBbHeight() * 0.5,
                    mob.getZ(),
                    (mob.getRandom().nextDouble() - 0.5) * 0.2,  // 随机X偏移
                    (mob.getRandom().nextDouble() - 0.5) * 0.2,  // 随机Y偏移
                    (mob.getRandom().nextDouble() - 0.5) * 0.2  // 随机Z偏移
            );
            // 结束跳劈并触发冷却
            jumping = false;
            cooldown = cooldownTicks;
        }
    }

    @Override
    public void stop() {
        jumping = false;
    }
}
