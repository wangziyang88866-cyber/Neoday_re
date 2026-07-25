package com.endofdays_re.level.goal;

import com.endofdays_re.level.register.ModMemoryModuleTypes;
import com.endofdays_re.utils.ModUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

/**
 * 修复版 RideTargetGoal
 * 解决了 ConcurrentModificationException 崩溃问题
 */
public class RideTargetGoal extends Goal {
    private static final String STACK_LEVEL_TAG = "StackLevel";
    private static final String IS_MOUNT_TAG = "IsMount";
    private static final String HAS_RIDER_TAG = "HasRider";
    private static final String SHARED_TARGET_ID_TAG = "SharedTargetId";
    private static final int MAX_STACK_HEIGHT = 6;
    private final Mob mob;
    private final int range;
    private final float distance;
    private final double speed;
    private LivingEntity target;
    private int cooldown;

    public RideTargetGoal(Mob mob, float distance, int range, double speed) {
        this.mob = mob;
        this.range = range;
        this.distance = distance;
        this.speed = speed;
        this.cooldown = 0;
        this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));

    }

    @Override
    public boolean canUse() {
        if (mob.level().isClientSide) return false;

        // 冷却处理：只在不能使用时递减，减少性能损耗
        if (cooldown > 0) {
            cooldown--;
            return false;
        }

        if (mob.isPassenger()) return false;

        // 1. 基础条件检查（巨人僵尸不参与堆叠）
        if (mob.getPersistentData().contains(ModUtils.KeyWraps("gigantic"))) return false;

        // 2. 检查现有的共享目标（修复：仅读取，不写入）
        UUID sharedTargetId = getSharedTargetId(mob);
        if (sharedTargetId != null && mob.level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(sharedTargetId);
            if (entity instanceof LivingEntity living && isValidTarget(living)) {
                this.target = living;
                return true;
            }
        }

        // 3. 扫描周围候选者
        List<LivingEntity> candidates = mob.level().getEntitiesOfClass(
                LivingEntity.class,
                mob.getBoundingBox().inflate(range),
                this::isValidTarget
        );

        if (!candidates.isEmpty()) {
            LivingEntity bestTarget = null;
            int highestLevel = -1;

            for (LivingEntity e : candidates) {
                int level = getStackLevel(e);
                if (level > highestLevel) {
                    highestLevel = level;
                    bestTarget = e;
                }
            }

            if (bestTarget != null) {
                this.target = bestTarget;
                // 注意：不要在这里调用 setSharedTargetId，移动到 start() 中
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        // 如果已经骑乘，根据 shouldDismount 决定是否继续
        if (mob.isPassenger()) {
            return !shouldDismount();
        }
        // 如果未骑乘，检查目标有效性
        return target != null && target.isAlive() && isValidTarget(target);
    }

    @Override
    public void start() {
        // 修复点：在 AI 正式开始执行时才写入 NBT，避免在 GoalSelector 迭代时触发崩溃
        if (this.target != null) {
            setSharedTargetId(mob, this.target.getUUID());
        }
    }

    @Override
    public void stop() {
        // 只有在非成功骑乘导致的停止时才清理
        if (!mob.isPassenger()) {
            this.target = null;
        }
    }

    @Override
    public void tick() {
        if (shouldDismount()) {
            dismount();
            return;
        }

        if (!mob.isPassenger()) {
            // 追踪阶段
            if (target == null || !target.isAlive()) return;

            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distSqr = mob.distanceToSqr(target);

            // 导航
            if (distSqr > 2.0) {
                mob.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), speed);
            }

            // 尝试骑乘
            if (distSqr < 4.0) {
                attemptMount(target);
            }
        } else {
            // 骑乘阶段控制逻辑
            if (mob.getVehicle() instanceof Mob mount) {
                controlMount(mount);
            }
        }
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (entity == mob || !entity.isAlive()) return false;

        // 排除巨人
        if (entity.getPersistentData().contains(ModUtils.KeyWraps("gigantic"))) return false;

        // 距离检查
        if (mob.distanceToSqr(entity) > (double) range * range) return false;

        // 排除玩家
        if (entity instanceof Player || mob.getTarget() instanceof Player) return false;

        // 检查是否已有骑手或超过高度
        if (hasRider(entity) || getStackLevel(entity) >= MAX_STACK_HEIGHT) return false;

        return canPhysicallyMount(entity);
    }

    private boolean canPhysicallyMount(LivingEntity target) {
        return target.getBbWidth() >= mob.getBbWidth() * 0.5f;
    }

    private void attemptMount(LivingEntity target) {
        if (mob.startRiding(target, true)) {
            int targetLevel = getStackLevel(target);
            setStackLevel(mob, targetLevel + 1);
            setIsMount(target, true);
            setHasRider(target, true);

            this.cooldown = 60; // 成功后进入长冷却
            this.target = null;
            setSharedTargetId(mob, null); // 骑乘成功后清除目标锁定
        }
    }

    private void controlMount(Mob mount) {
        // 同步视角
        mount.setYRot(mob.getYRot());
        mount.setYHeadRot(mob.getYHeadRot());

        // 输入传递
        Vec3 moveVec = new Vec3(mob.xxa, 0, mob.zza).scale(speed);
        if (moveVec.lengthSqr() > 0.01) {
            mount.getNavigation().moveTo(mob.getX() + moveVec.x, mob.getY(), mob.getZ() + moveVec.z, speed);
        } else {
            mount.getNavigation().stop();
        }
    }

    private boolean shouldDismount() {
        if (!mob.isPassenger()) return false;

        // 1. 目标高度差过大
        LivingEntity hostile = mob.getTarget();
        if (hostile != null && Math.abs(mob.getY() - hostile.getY()) > 5.0) return true;

        // 2. AI 强制要求移动到特定方块
        return mob.getBrain().hasMemoryValue(ModMemoryModuleTypes.TARGET_BLOCK_POS.get());
    }

    private void dismount() {
        mob.stopRiding();
        cleanupStackData(mob);
        this.cooldown = 100;
        this.target = null;
    }

    // --- 数据辅助方法 ---

    private int getStackLevel(LivingEntity e) {
        return e.getPersistentData().getInt(STACK_LEVEL_TAG);
    }

    private void setStackLevel(LivingEntity e, int level) {
        e.getPersistentData().putInt(STACK_LEVEL_TAG, level);
    }

    private void setIsMount(LivingEntity e, boolean b) {
        e.getPersistentData().putBoolean(IS_MOUNT_TAG, b);
    }

    private boolean hasRider(LivingEntity e) {
        return !e.getPassengers().isEmpty() || e.getPersistentData().getBoolean(HAS_RIDER_TAG);
    }

    private void setHasRider(LivingEntity e, boolean b) {
        e.getPersistentData().putBoolean(HAS_RIDER_TAG, b);
    }

    private UUID getSharedTargetId(LivingEntity e) {
        CompoundTag nbt = e.getPersistentData();
        return nbt.hasUUID(SHARED_TARGET_ID_TAG) ? nbt.getUUID(SHARED_TARGET_ID_TAG) : null;
    }

    private void setSharedTargetId(LivingEntity e, UUID id) {
        if (id == null) {
            e.getPersistentData().remove(SHARED_TARGET_ID_TAG);
        } else {
            e.getPersistentData().putUUID(SHARED_TARGET_ID_TAG, id);
        }
    }

    private void cleanupStackData(LivingEntity e) {
        e.getPersistentData().remove(STACK_LEVEL_TAG);
        e.getPersistentData().remove(IS_MOUNT_TAG);
        e.getPersistentData().remove(HAS_RIDER_TAG);
        e.getPersistentData().remove(SHARED_TARGET_ID_TAG);
    }
}