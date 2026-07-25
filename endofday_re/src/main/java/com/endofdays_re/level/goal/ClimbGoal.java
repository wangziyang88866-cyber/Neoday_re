package com.endofdays_re.level.goal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class ClimbGoal extends Goal {
    private static final double CLIMB_SPEED = 0.24;
    private static final double DETECTION_RANGE = 0.2; // 碰撞预判余量
    private final Mob zombie;

    public ClimbGoal(Mob zombie) {
        this.zombie = zombie;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        // 1. 目标检查：如果没有目标，或者目标不在上方，不触发攀爬
        if (this.zombie.getTarget() == null || this.zombie.getTarget().getY() <= this.zombie.getY()) {
            return false;
        }

        // 2. 判定点扩展：不再仅仅依赖系统的 horizontalCollision
        // 检查僵尸前方一小段距离是否有方块，或者是否撞到了其他僵尸
        return isCollidingWithStructure() || isStuckOnZombies();
    }

    /**
     * 判定是否撞墙（主动探测）
     */
    private boolean isCollidingWithStructure() {
        if (this.zombie.horizontalCollision) return true;

        // 向看向的方向偏移一点点，构建一个稍微大一点的判定盒
        AABB expandedBox = this.zombie.getBoundingBox().expandTowards(this.zombie.getLookAngle().x * DETECTION_RANGE, 0, this.zombie.getLookAngle().z * DETECTION_RANGE);
        return !this.zombie.level().noCollision(this.zombie, expandedBox);
    }

    /**
     * 判定是否因为同类阻塞而无法前进（尸堆逻辑）
     */
    private boolean isStuckOnZombies() {
        // 搜索僵尸正前方极小范围内的其他僵尸
        AABB frontBox = this.zombie.getBoundingBox().inflate(0.1, 0, 0.1);
        List<Zombie> nearbyZombies = this.zombie.level().getEntitiesOfClass(Zombie.class, frontBox, (z) -> z != this.zombie);

        // 如果前面有 2 个以上僵尸，且自己正在尝试移动却动不了，就开启攀爬模式“踩着爬”
        return nearbyZombies.size() >= 2 && this.zombie.getDeltaMovement().horizontalDistanceSqr() < 0.001;
    }

    @Override
    public void tick() {
        Vec3 motion = this.zombie.getDeltaMovement();

        // 增加贴墙力：微弱地将僵尸推向它的目标 XZ 方向，防止在攀爬时向后滑落
        Vec3 look = this.zombie.getLookAngle();
        double stickX = look.x * 0.05;
        double stickZ = look.z * 0.05;

        // 向上攀爬
        this.zombie.setDeltaMovement(motion.x + stickX, CLIMB_SPEED, motion.z + stickZ);
        this.zombie.fallDistance = 0;

        // 智能翻越：当眼睛高度已经没有方块阻挡时，大幅增加向前的推力，帮助僵尸越过边缘
        BlockPos headPos = this.zombie.blockPosition().above(2);
        if (this.zombie.level().isEmptyBlock(headPos)) {
            // 翻越瞬间给予一个明显的冲刺
            this.zombie.setDeltaMovement(this.zombie.getDeltaMovement().add(look.x * 0.15, 0.1, look.z * 0.15));
        }
    }
}