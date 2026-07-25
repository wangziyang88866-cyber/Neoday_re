package com.endofdays_re.level.goal;

import com.endofdays_re.level.register.entity.item.entity.ThrownTNTEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Random;

public class PlaceUseTntGoal extends Goal {
    private final Mob mob;
    private final double distance;
    private final int interval;
    private final Random random = new Random();
    private int Time;
    // 新增：用于记录检测到的传送门位置
    private BlockPos portalPos = null;

    public PlaceUseTntGoal(Mob mob, float distance, int interval) {
        this.mob = mob;
        this.distance = distance;
        this.interval = interval;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        // 首先必须持有 TNT
        if (!hasTNT()) return false;

        // 1. 优先检查视野内是否有传送门
        this.portalPos = findPortalInLineOfSight();
        if (this.portalPos != null) {
            return true;
        }

        // 2. 如果没看到传送门，检查是否有生物目标
        LivingEntity target = this.mob.getTarget();
        if (target != null && target.isAlive()) {
            double currentDistance = mob.distanceToSqr(target);
            // 距离判定逻辑
            if (currentDistance <= distance) {
                return random.nextFloat() < 0.75f;
            } else {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return hasTNT() && (this.portalPos != null || (this.mob.getTarget() != null && this.mob.getTarget().isAlive()));
    }

    @Override
    public void start() {
        this.Time = this.interval;
        this.mob.setAggressive(true);
    }

    @Override
    public void stop() {
        this.mob.setAggressive(false);
        this.Time = 0;
        this.portalPos = null;
    }

    @Override
    public void tick() {
        this.Time--;

        // 每一帧尝试更新一下传送门位置（防止传送门已经碎了）
        BlockPos currentPortal = findPortalInLineOfSight();
        if (currentPortal != null) this.portalPos = currentPortal;

        if (this.portalPos != null) {
            // --- 攻击传送门逻辑 ---
            this.mob.getNavigation().stop();
            // 盯住传送门前方的地面（1-2格）
            Vec3 targetVec = Vec3.atCenterOf(this.portalPos);
            this.mob.getLookControl().setLookAt(targetVec.x, targetVec.y, targetVec.z, 30.0F, 30.0F);

            if (this.Time <= 0) {
                performTntThrow(targetVec);
                this.Time = interval;
            }
        } else if (this.mob.getTarget() != null) {
            // --- 攻击生物逻辑 ---
            LivingEntity target = this.mob.getTarget();
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            this.mob.getNavigation().stop();

            if (this.Time <= 0) {
                performTntThrow(target.position());
                this.Time = interval;
            }
        }
    }

    private void performTntThrow(Vec3 targetPos) {
        Level world = this.mob.level();
        if (world.isClientSide) return;

        ThrownTNTEntity tnt = new ThrownTNTEntity(world, this.mob);
        Vec3 armPosition = calculateArmPosition(this.mob);
        tnt.setPos(armPosition.x, armPosition.y, armPosition.z);

        // 计算抛物线速度，使其落在目标位置
        double d0 = targetPos.x - tnt.getX();
        double d1 = targetPos.y - tnt.getY();
        double d2 = targetPos.z - tnt.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        tnt.shoot(d0, d1 + d3 * 0.2D, d2, 0.75F, 8.0F); // 这里的 0.75F 控制投掷力度

        world.addFreshEntity(tnt);
        consumeTNT();
    }

    /**
     * 在僵尸视线范围内（16格）搜索传送门方块
     */
    private BlockPos findPortalInLineOfSight() {
        Vec3 eyePos = this.mob.getEyePosition();
        Vec3 viewVec = this.mob.getViewVector(1.0F);
        // 搜索距离 16 格
        Vec3 reachVec = eyePos.add(viewVec.x * 16, viewVec.y * 16, viewVec.z * 16);

        BlockHitResult hitResult = this.mob.level().clip(new ClipContext(
                eyePos, reachVec,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                this.mob));

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos hitPos = hitResult.getBlockPos();
            if (this.mob.level().getBlockState(hitPos).is(Blocks.NETHER_PORTAL)) {
                return hitPos;
            }
        }
        return null;
    }

    private boolean hasTNT() {
        return this.mob.getMainHandItem().is(Items.TNT) || this.mob.getOffhandItem().is(Items.TNT);
    }

    private void consumeTNT() {
        ItemStack mainHand = this.mob.getMainHandItem();
        ItemStack offHand = this.mob.getOffhandItem();
        if (mainHand.is(Items.TNT)) {
            mainHand.shrink(1);
        } else if (offHand.is(Items.TNT)) {
            offHand.shrink(1);
        }
    }

    private Vec3 calculateArmPosition(LivingEntity entity) {
        float yaw = entity.getYRot() * ((float) Math.PI / 180F);
        double armOffsetX = -Math.sin(yaw) * 0.4;
        double armOffsetY = entity.getEyeHeight() * 0.9;
        double armOffsetZ = Math.cos(yaw) * 0.4;
        return new Vec3(entity.getX() + armOffsetX, entity.getY() + armOffsetY, entity.getZ() + armOffsetZ);
    }
}