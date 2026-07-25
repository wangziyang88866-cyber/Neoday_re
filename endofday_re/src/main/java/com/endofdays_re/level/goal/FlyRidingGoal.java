package com.endofdays_re.level.goal;


import com.endofdays_re.level.goal.path.FlyNodeEvalRider;
import com.endofdays_re.level.register.ModMemoryModuleTypes;
import com.endofdays_re.level.register.entity.FlyingEntity;
import com.endofdays_re.mixin.IMobEntityMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public class FlyRidingGoal extends Goal {

    protected final Mob living;
    private final PathNavigation flyer;
    private int iddle, pathCheckWait, flyDelay, targetDelay;
    private boolean start;
    private Path lastGroundPath; // 缓存最近的地面路径计算结果
    private BlockPos lastTargetPos = BlockPos.ZERO; // 避免空指针异常

    public FlyRidingGoal(Mob living) {
        this.living = living;
        this.flyer = new FlyingPathNavigation(living, living.level()) {

            @Override
            protected @NotNull PathFinder createPathFinder(int maxVisitedNodes) {
                this.nodeEvaluator = new FlyNodeEvalRider();
                this.nodeEvaluator.setCanPassDoors(true);
                return new PathFinder(this.nodeEvaluator, 512); // 减少最大寻路节点数
            }

            @Nullable
            @Override
            protected Path createPath(@NotNull Set<BlockPos> targets, int regionOffset, boolean offsetUpward, int accuracy, float followRange) {
                return super.createPath(targets, regionOffset, offsetUpward, 4, followRange - 2); // 降低路径精度
            }

            @Override
            public boolean isStableDestination(@NotNull BlockPos blockPos) {
                return true;
            }
        };
    }

    @Override
    public boolean canUse() {
        if (this.living.getVehicle() instanceof FlyingEntity) {
            return true;
        }
        LivingEntity target = this.living.getTarget();
        if (target == null || !target.isAlive() || !this.living.isWithinRestriction(target.blockPosition())
                || this.living.getBrain().hasMemoryValue(ModMemoryModuleTypes.ATTACK_TARGET_BLOCK_POS.get())
                || living.getBrain().hasMemoryValue(ModMemoryModuleTypes.TARGET_BLOCK_POS.get())
                || living.getBrain().hasMemoryValue(ModMemoryModuleTypes.TARGET_BLOCK_POS_ARRMOR.get())
                || this.living.getBrain().hasMemoryValue(ModMemoryModuleTypes.ATTACK_TARGET_BLOCK_POS.get())
        ) {
            this.targetDelay = 0;
            return false;
        }

        // 仅在目标移动超过3格时重新计算路径
        if (!target.blockPosition().equals(lastTargetPos)) {
            lastTargetPos = target.blockPosition();
            lastGroundPath = null; // 目标位置变化时清除缓存
        }

        if (!this.living.isPassenger() && ++this.targetDelay > 40) {
            if (--this.pathCheckWait <= 0) {
                this.pathCheckWait = 25;
                return checkFlying();
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        if (this.living.getBrain().hasMemoryValue(ModMemoryModuleTypes.ATTACK_TARGET_BLOCK_POS.get())
                || living.getBrain().hasMemoryValue(ModMemoryModuleTypes.TARGET_BLOCK_POS.get())
                || living.getBrain().hasMemoryValue(ModMemoryModuleTypes.TARGET_BLOCK_POS_ARRMOR.get())) {
            return false;
        }
        if (this.living.getVehicle() instanceof FlyingEntity) {
            if (this.living.getTarget() == null) {
                this.iddle++;
            } else {
                this.iddle = 0;
            }
            return this.iddle < 100;
        }
        return false;
    }

    @Override
    public void stop() {
        if (this.living.getVehicle() instanceof FlyingEntity mount) {
            mount.scheduledDismount();
        }
        this.living.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 200, 1));
        this.iddle = 0;
        this.targetDelay = 0;
        this.lastGroundPath = null; // 重置缓存
    }

    @Override
    public void start() {
        this.start = true;
    }

    @Override
    public void tick() {
        if (this.start) {
            if (!this.living.isPassenger()) {
                FlyingEntity summon = new FlyingEntity(this.living.level());
                BlockPos pos = this.living.blockPosition();
                summon.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, this.living.getYRot(), this.living.getXRot());
                if (summon.doesntCollideWithRidden(this.living)) {
                    this.living.level().addFreshEntity(summon);
                    summon.scheduledRide(this.living);
                    this.flyDelay = 0;
                }
            }
            this.start = false;
        }

        Entity entity = this.living.getVehicle();
        if (!(entity instanceof FlyingEntity summon) || !summon.isAlive()) {
            return;
        }

        // 优化着陆检测频率
        if (++this.flyDelay >= 40) {
            if (this.isOnLand(entity)) {
                summon.scheduledDismount();
            }
            this.flyDelay = 0; // 重置计数器避免频繁检测
        }
    }

    private boolean checkFlying() {
        // 快速失败条件前置
        if (this.living.isUnderWater() ||
                !this.living.onGround() ||
                Math.abs(this.living.xxa) > 0.005 ||
                Math.abs(this.living.zza) > 0.005) {
            return false;
        }

        if (this.living.getTarget() == null) return false;

        // 使用缓存的地面路径
        if (lastGroundPath == null) {
            lastGroundPath = this.living.getNavigation().createPath(this.living.getTarget(), 1);
        }

        // 地面路径有效则不飞行
        if (lastGroundPath != null && lastGroundPath.canReach()) {
            return false;
        }

        // 计算飞行路径
        Path flyPath = this.flyer.createPath(this.living.getTarget(), 1);
        return flyPath != null && flyPath.canReach();
    }

    private boolean isOnLand(Entity riding) {
        // 优化区块状态获取
        BlockPos ridingPos = riding.blockPosition().below();
        if (riding.level().getBlockState(ridingPos).isSolid()) {
            return true;
        }

        LivingEntity target = this.living.getTarget();
        if (target == null) {
            return false;
        }

        // 使用混合导航器检测
        PathNavigation trueNav = ((IMobEntityMixin) this.living).getTrueNavigator();
        if (BehaviorUtils.isWithinAttackRange(this.living, target, 0)) {
            return riding.level().getBlockState(ridingPos).isSolid();
        }

        // 减少路径计算频率
        if (--this.pathCheckWait > 0) {
            return false;
        }
        this.pathCheckWait = 25;

        Path groundPath = trueNav.createPath(target, 1);
        return groundPath != null && groundPath.canReach();
    }
}