package com.endofdays_re.level.goal;

import com.endofdays_re.level.register.ModMemoryModuleTypes;
import com.endofdays_re.level.register.entity.item.entity.ThrownTNTEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;
import java.util.Random;

public class JumpGoal extends Goal {
    private final Random random = new Random();
    protected LivingEntity target;
    protected Mob goalOwner;
    protected int ticksWithoutPath;

    public JumpGoal(Mob goalOwner) {
        this.goalOwner = goalOwner;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        // 检查是否在地面上
        if (!this.goalOwner.onGround()
                || this.goalOwner.getBrain().hasMemoryValue(ModMemoryModuleTypes.TARGET_BLOCK_POS.get())
                || this.goalOwner.getBrain().hasMemoryValue(ModMemoryModuleTypes.TARGET_BLOCK_POS_ARRMOR.get())
                || this.goalOwner.getBrain().hasMemoryValue(ModMemoryModuleTypes.ATTACK_TARGET_BLOCK_POS.get())) {
            return false;
        }

        // 检查是否有激活的TNT实体
        if (isTNTNearby()) {
            return true; // 如果有TNT在附近，允许跳跃
        }

        // 检查是否有弹射物
        if (shouldDodgeProjectiles()) {
            return true; // 如果有弹射物在附近，允许跳跃
        }

        // 当前目标
        this.target = this.goalOwner.getTarget();
        if (this.target == null) {
            return false;
        } else if (!this.goalOwner.getNavigation().isDone() && !this.goalOwner.getNavigation().isStuck()) {
            this.ticksWithoutPath = 0;
            return false;
        } else {
            ++this.ticksWithoutPath;
            return this.goalOwner.distanceToSqr(this.target) < 100.0 && this.ticksWithoutPath > this.adjustedTickDelay(15);
        }
    }

    // 检查周围是否有激活的TNT实体
    private boolean isTNTNearby() {
        List<ThrownTNTEntity> tntEntities = this.goalOwner.level().getEntitiesOfClass(ThrownTNTEntity.class, this.goalOwner.getBoundingBox().inflate(5.0D)); // 5.0D是检查的半径
        for (ThrownTNTEntity tnt : tntEntities) {
            if (tnt.isAlive() && tnt.getFuse() <= 0) { // 确保TNT实体是激活状态
                return true; // 如果找到激活的TNT实体，返回true
            }
        }
        return false; // 如果没有找到
    }

    // 检查是否在附近有弹射物，且有30%的概率躲避
    private boolean shouldDodgeProjectiles() {
        List<ThrowableProjectile> projectiles = this.goalOwner.level().getEntitiesOfClass(ThrowableProjectile.class, this.goalOwner.getBoundingBox().inflate(5.0D)); // 5.0D是检查的半径
        if (!projectiles.isEmpty()) {
            // 随机概率判断
            return random.nextInt(100) < 30; // 30%的概率返回true
        }
        return false; // 如果没有弹射物则返回false
    }

    @Override
    public void stop() {
        this.ticksWithoutPath = 0;
    }

    @Override
    public void start() {
        this.goalOwner.setJumping(true);
        double distanceY = this.target != null ? this.target.getY() - this.goalOwner.getY() : 0;
        double distanceX = this.target != null ? this.target.getX() - this.goalOwner.getX() : 0;
        double distanceZ = this.target != null ? this.target.getZ() - this.goalOwner.getZ() : 0;
        this.goalOwner.setDeltaMovement((new Vec3(distanceX, distanceY, distanceZ)).normalize().add(0.0, 0.2, 0.0));
        this.stop();
    }
}
