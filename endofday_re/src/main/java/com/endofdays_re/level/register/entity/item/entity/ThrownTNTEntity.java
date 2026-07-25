package com.endofdays_re.level.register.entity.item.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Random;

public class ThrownTNTEntity extends PrimedTnt {
    private static final double GRAVITY = 0.03;
    private static final float INSTANT_EXPLODE_CHANCE = 0.3f; // 30% 概率立即爆炸
    private final Random random = new Random();
    private boolean hasHitEntity = false;

    public ThrownTNTEntity(EntityType<? extends PrimedTnt> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public ThrownTNTEntity(Level world, LivingEntity owner) {
        super(world, owner.getX(), owner.getEyeY() - 0.1, owner.getZ(), owner);
    }

    @Override
    public void tick() {
        super.tick();

        // 检查是否击中实体
        if (!this.level().isClientSide && this.tickCount > 2 && !hasHitEntity) {
            checkCollisionWithEntities();
        }

        // 检查是否击中方块（基于位置碰撞）
        if (!this.level().isClientSide && !hasHitEntity) {
            checkBlockCollision();
        }
    }

    // 实体碰撞检测
    private void checkCollisionWithEntities() {
        Vec3 currentPos = this.position();
        Vec3 prevPos = currentPos.subtract(this.getDeltaMovement());

        AABB movementBox = this.getBoundingBox().expandTowards(this.getDeltaMovement());
        List<Entity> potentialTargets = this.level().getEntities(this, movementBox,
                entity -> entity != this.getOwner() && entity instanceof LivingEntity);

        for (Entity entity : potentialTargets) {
            Vec3 hitPos = entity.getBoundingBox().clip(currentPos, prevPos).orElse(null);
            if (hitPos != null) {
                onHitEntity(entity);
                break;
            }
        }
    }

    // 方块碰撞检测
    private void checkBlockCollision() {
        // 简单的方块碰撞检测：如果TNT嵌入方块中
        AABB boundingBox = this.getBoundingBox();
        if (!this.level().noCollision(this, boundingBox)) {
            onHitBlock();
        }
    }

    // 击中实体时的处理
    private void onHitEntity(Entity entity) {
        if (hasHitEntity) return; // 防止重复触发
        hasHitEntity = true;

        // 有概率立即爆炸
        if (random.nextFloat() < INSTANT_EXPLODE_CHANCE) {
            instantExplode();
        }
        // 如果没有立即爆炸，继续正常飞行
    }


    // 击中方块时的处理
    private void onHitBlock() {
        if (hasHitEntity) return; // 防止重复触发
        hasHitEntity = true;

        // 击中方块时也有小概率立即爆炸
        if (random.nextFloat() < INSTANT_EXPLODE_CHANCE * 0.5f) {
            instantExplode();
        }
        // 如果没有立即爆炸，继续正常飞行
    }

    // 立即爆炸的方法 - 直接调用父类的爆炸逻辑
    private void instantExplode() {
        if (!this.level().isClientSide) {
            // 直接调用PrimedTnt的爆炸逻辑
            this.explode();
        }
    }

    // 重写explode方法以确保立即爆炸后丢弃实体
    @Override
    protected void explode() {
        // 直接调用父类的explode方法，它会处理爆炸效果和伤害
        super.explode();
        // 爆炸后丢弃实体
        this.discard();
    }

    // ==================== 原有的抛投算法 ====================
    public void shootAtTarget(Entity shooter, Entity target) {
        if (target == null) return;

        Vec3 shooterPos = shooter.position().add(0, shooter.getEyeHeight() * 0.7, 0);
        Vec3 targetPos = getOptimalTargetPosition(target);

        // 预测目标移动
        Vec3 predictedPos = predictTargetPosition(target, shooterPos, targetPos);

        // 计算弹道
        calculateAndSetVelocity(shooterPos, predictedPos, target);
    }

    private Vec3 getOptimalTargetPosition(Entity target) {
        AABB targetBox = target.getBoundingBox();
        double centerY = (targetBox.minY + targetBox.maxY) * 0.6;
        return new Vec3(target.getX(), centerY, target.getZ());
    }

    private Vec3 predictTargetPosition(Entity target, Vec3 shooterPos, Vec3 currentTargetPos) {
        if (!(target instanceof LivingEntity)) {
            return currentTargetPos;
        }

        Vec3 targetVelocity = new Vec3(
                target.getX() - target.xOld,
                target.getY() - target.yOld,
                target.getZ() - target.zOld
        );

        double distance = shooterPos.distanceTo(currentTargetPos);
        double flightTime = Math.max(distance / 1.5, 0.5);
        return currentTargetPos.add(targetVelocity.scale(flightTime * 0.8));
    }

    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        // 1. 计算指向目标的向量
        Vec3 vector3d = (new Vec3(x, y, z)).subtract(this.position());
        double horizontalDistance = Math.sqrt(vector3d.x * vector3d.x + vector3d.z * vector3d.z);

        // 2. 仰角调整：由于存在 GRAVITY (0.03)，我们需要向上偏移瞄准点
        // 这里使用一个简单的重力补偿公式：距离越远，向上瞄准越高
        double yAdjustment = horizontalDistance * 0.15D;

        Vec3 finalDir = new Vec3(vector3d.x, vector3d.y + yAdjustment, vector3d.z).normalize();

        // 3. 注入随机误差（模拟手抖）
        if (inaccuracy > 0) {
            finalDir = finalDir.add(
                    this.random.nextGaussian() * 0.0075D * (double) inaccuracy,
                    this.random.nextGaussian() * 0.0075D * (double) inaccuracy,
                    this.random.nextGaussian() * 0.0075D * (double) inaccuracy
            );
        }

        // 4. 应用力度并设置运动向量
        this.setDeltaMovement(finalDir.scale(velocity));

        // 5. 更新实体的旋转角度，使其朝向飞行方向
        double horizontalVel = this.getDeltaMovement().horizontalDistance();
        this.setYRot((float) (Math.atan2(this.getDeltaMovement().x, this.getDeltaMovement().z) * (180D / Math.PI)));
        this.setXRot((float) (Math.atan2(this.getDeltaMovement().y, horizontalVel) * (180D / Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    private void calculateAndSetVelocity(Vec3 startPos, Vec3 targetPos, Entity target) {
        double dx = targetPos.x - startPos.x;
        double dz = targetPos.z - startPos.z;
        double dy = targetPos.y - startPos.y;
        double horizontalDistance = Math.sqrt(dx * dx + dz * dz);

        float speed = calculateOptimalSpeed(horizontalDistance);
        double flightTime = horizontalDistance / speed;
        double verticalVelocity = calculateVerticalVelocity(dy, flightTime);

        Vec3 horizontalDir = new Vec3(dx, 0, dz).normalize();
        Vec3 velocity = horizontalDir.scale(speed).add(0, verticalVelocity, 0);

        if (!willProjectileHitTarget(startPos, velocity, target)) {
            verticalVelocity = adjustForTargetHeight(startPos, targetPos, target, flightTime);
            velocity = horizontalDir.scale(speed).add(0, verticalVelocity, 0);
        }

        this.setDeltaMovement(velocity);
    }

    private float calculateOptimalSpeed(double distance) {
        return (float) Math.min(Math.max(distance / 8.0, 0.8), 2.2);
    }

    private double calculateVerticalVelocity(double heightDiff, double flightTime) {
        return (heightDiff + 0.5 * GRAVITY * flightTime * flightTime) / flightTime;
    }

    private double adjustForTargetHeight(Vec3 startPos, Vec3 targetPos, Entity target, double flightTime) {
        AABB targetBox = target.getBoundingBox();
        double targetTop = targetBox.maxY;

        double maxHeight = startPos.y + calculateVerticalVelocity(targetPos.y - startPos.y, flightTime) * flightTime / 2
                - 0.5 * GRAVITY * Math.pow(flightTime / 2, 2);

        if (maxHeight < targetTop - 0.2) {
            double neededHeight = targetTop - startPos.y + 0.3;
            return (neededHeight + 0.5 * GRAVITY * flightTime * flightTime) / flightTime;
        }

        return calculateVerticalVelocity(targetPos.y - startPos.y, flightTime);
    }

    private boolean willProjectileHitTarget(Vec3 startPos, Vec3 velocity, Entity target) {
        Vec3 currentPos = startPos;
        Vec3 currentVel = velocity;

        for (int i = 0; i < 20; i++) {
            currentPos = currentPos.add(currentVel);
            currentVel = currentVel.add(0, -GRAVITY, 0);

            if (target.getBoundingBox().contains(currentPos)) {
                return true;
            }

            if (currentPos.distanceTo(startPos) > 32) {
                break;
            }
        }

        return false;
    }
}