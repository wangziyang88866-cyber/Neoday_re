package com.endofdays_re.level.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class SwimGoal extends Goal {
    private final Mob zombie;
    private final double speedModifier;

    public SwimGoal(Mob zombie, double speedModifier) {
        this.zombie = zombie;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.zombie.getTarget();
        // 仅在水中且有目标时启动
        return target != null && target.isAlive() && this.zombie.isInWater();
    }

    @Override
    public void tick() {
        LivingEntity target = this.zombie.getTarget();
        if (target == null) return;

        // 1. 始终看向目标
        this.zombie.getLookControl().setLookAt(target, 30.0F, 30.0F);

        // 2. 浮力逻辑：尝试保持头部在水面
        // 检查僵尸眼睛的高度是否低于水面
        if (this.zombie.getEyeInFluidType().isAir() || this.zombie.getFluidHeight(net.minecraft.tags.FluidTags.WATER) > 0.8) {
            // 如果水比较深，向上浮
            this.zombie.setDeltaMovement(this.zombie.getDeltaMovement().add(0, 0.04D, 0));
        } else {
            // 已经在水面附近，维持微小浮力抵消重力
            this.zombie.setDeltaMovement(this.zombie.getDeltaMovement().add(0, 0.01D, 0));
        }

        // 3. 水面推进逻辑 (XZ轴)
        double dx = target.getX() - this.zombie.getX();
        double dz = target.getZ() - this.zombie.getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);

        if (distance > 0.5D) {
            // 计算水平移动向量，不干扰 Y 轴
            Vec3 moveVec = new Vec3(dx, 0, dz).normalize().scale(this.speedModifier * 0.03D);
            this.zombie.setDeltaMovement(this.zombie.getDeltaMovement().add(moveVec));
        }
    }

    @Override
    public void stop() {
        this.zombie.setJumping(false);
    }
}