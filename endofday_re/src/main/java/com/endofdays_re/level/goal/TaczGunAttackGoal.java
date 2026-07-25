package com.endofdays_re.level.goal;

import com.endofdays_re.client.config.data.CommonBuild;
import com.endofdays_re.config.ConfigData;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class TaczGunAttackGoal<T extends Mob> extends Goal {

    private final T mob;
    private final CommonBuild fullConfig;
    private CommonBuild.TaczData currentGunConfig;

    private GunState gunState = GunState.UNCHARGED;
    private int seeTime;
    private int attackDelay;
    private double attackCount;
    private int ammoCount;

    // 走位控制
    private int strafeTime = -1;
    private boolean strafeLeft;
    private boolean strafeBack;

    public TaczGunAttackGoal(T mob, CommonBuild config) {
        this.mob = mob;
        this.fullConfig = config;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public TaczGunAttackGoal(T mob) {
        this.mob = mob;
        this.fullConfig = ConfigData.commonConfigData;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    private void updateCurrentGunConfig() {
        ItemStack stack = this.mob.getMainHandItem();
        IGun iGun = IGun.getIGunOrNull(stack);
        if (iGun != null) {
            String path = iGun.getGunId(stack).getPath();
            this.currentGunConfig = this.fullConfig.taczData.getOrDefault(path,
                    new CommonBuild.TaczData(path, "SEMI", true, 40, 24, 1.0f, 15));
        }
    }

    @Override
    public boolean canUse() {
        updateCurrentGunConfig();
        return isHoldingGun() && (isValidTarget() || (!hasAmmo() && canReload()));
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void start() {
        this.gunState = GunState.UNCHARGED;
        this.seeTime = 0;
        this.strafeTime = -1;
        IGunOperator.fromLivingEntity(this.mob).draw(this.mob::getMainHandItem);
    }

    @Override
    public void stop() {
        this.mob.setAggressive(false);
        stopFiring(IGunOperator.fromLivingEntity(this.mob));
        this.mob.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (this.currentGunConfig == null) return;

        LivingEntity target = this.mob.getTarget();
        if (target == null) return;

        ItemStack itemStack = this.mob.getMainHandItem();
        IGunOperator gunOperator = IGunOperator.fromLivingEntity(this.mob);

        double radius = this.currentGunConfig.radius;
        double radiusSqr = radius * radius;
        double distSqr = this.mob.distanceToSqr(target);
        boolean isExplosive = isExplosiveWeapon(itemStack);

        // 视线逻辑
        boolean canSee = this.mob.getSensing().hasLineOfSight(target);
        this.seeTime = canSee ? this.seeTime + 1 : 0;

        // 1. 战术走位与移动
        if (isValidTarget()) {
            this.mob.setAggressive(true);
            // 在射程内且 (看得见 或 爆炸武器盲射) 开启走位
            if (distSqr <= radiusSqr && (canSee || isExplosive)) {
                this.mob.getNavigation().stop();
                updateStrafing(distSqr, radiusSqr);
            } else {
                this.strafeTime = -1;
                this.mob.getNavigation().moveTo(target, this.currentGunConfig.move_sped);
            }
            this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
        }

        // 2. 射击判定
        if (isHoldingGun()) {
            if (isValidTarget() && canMeleeAttack(gunOperator, target)) {
                gunOperator.melee();
                return;
            }

            // 严格射程限制
            if (distSqr <= radiusSqr && (this.seeTime > 5 || isExplosive)) {
                handleGunStateMachine(gunOperator, itemStack, target);
            } else {
                stopFiring(gunOperator);
            }
        }
    }

    private void handleGunStateMachine(IGunOperator gunOperator, ItemStack stack, LivingEntity target) {
        if (getGun().isOverheatLocked(stack)) {
            this.gunState = GunState.UNCHARGED;
            return;
        }

        boolean hasAmmo = hasAmmo();
        if (this.gunState == GunState.UNCHARGED && hasAmmo) {
            this.gunState = GunState.CHARGED;
            this.ammoCount = getAmmoCount(stack);
        } else if (this.gunState == GunState.CHARGED && !hasAmmo) {
            this.gunState = GunState.UNCHARGED;
        }

        switch (this.gunState) {
            case UNCHARGED -> {
                if (canReload()) {
                    gunOperator.reload();
                    this.gunState = GunState.CHARGING;
                }
            }
            case CHARGING -> {
                if (!gunOperator.getDataHolder().reloadStateType.isReloading()) {
                    if (hasAmmo()) {
                        this.gunState = GunState.CHARGED;
                        this.attackDelay = 5 + this.mob.getRandom().nextInt(Math.max(1, this.currentGunConfig.attadk_speed));
                        this.ammoCount = getAmmoCount(stack);
                    } else {
                        this.gunState = GunState.UNCHARGED;
                    }
                }
            }
            case CHARGED -> {
                if (--this.attackDelay <= 0) {
                    gunOperator.aim(true);
                    this.gunState = GunState.READY_TO_ATTACK;
                }
            }
            case READY_TO_ATTACK -> {
                if (isRightAngle(target)) {
                    processFiring(gunOperator, stack, target);
                }
            }
        }
    }

    private void processFiring(IGunOperator gunOperator, ItemStack stack, LivingEntity target) {
        IGun iGun = getGun();
        if (iGun == null) return;

        String mode = this.currentGunConfig.FireMode.toUpperCase();

        // 情况 A: 自动武器 (Minigun/AK) - 每一 Tick 持续调用，处理预热和连发
        if ("AUTO".equals(mode)) {
            if (hasAmmo()) {
                gunOperator.shoot(this.mob::getXRot, this.mob::getYHeadRot);
                this.ammoCount = iGun.getCurrentAmmoCount(stack);
                if (this.ammoCount <= 0) {
                    this.gunState = GunState.UNCHARGED;
                }
            }
        }
        // 情况 B: 半自动/栓动/爆炸武器 - 使用 attackCount 计数器控制节奏
        else {
            double rpm = iGun.getRPM(stack);
            double intervalFactor = 1.0 + (this.currentGunConfig.attadk_speed / 10.0);
            attackCount += (rpm / 2400D) / intervalFactor;

            for (; attackCount >= 1; attackCount--) {
                if (hasAmmo()) {
                    gunOperator.shoot(this.mob::getXRot, this.mob::getYHeadRot);

                    // 自动拉栓
                    TimelessAPI.getCommonGunIndex(iGun.getGunId(stack)).ifPresent(index -> {
                        if (index.getGunData().getBolt() == Bolt.MANUAL_ACTION) gunOperator.bolt();
                    });

                    if (this.ammoCount > 0) this.ammoCount--;
                    if (this.ammoCount <= 0) {
                        this.gunState = GunState.UNCHARGED;
                        break;
                    }
                }
            }
        }
    }

    private boolean isSafePosition(Vec3 pos) {
        BlockPos blockPos = BlockPos.containing(pos);
        Level level = this.mob.level();

        // 1. 检测是否有立足点（防止掉下悬崖）
        // 检查脚下及下方2格是否有固体方块
        boolean hasFloor = false;
        for (int i = 0; i <= 2; i++) {
            if (level.getBlockState(blockPos.below(i)).isSolid()) {
                hasFloor = true;
                break;
            }
        }
        if (!hasFloor) return false;

        // 2. 检测当前位置是否是危险方块
        BlockState state = level.getBlockState(blockPos);
        FluidState fluid = level.getFluidState(blockPos);

        // 禁止岩浆
        if (fluid.is(net.minecraft.tags.FluidTags.LAVA)) return false;

        // 禁止火源、营火、仙人掌等伤害方块
        if (state.is(Blocks.FIRE) ||
                state.is(Blocks.SOUL_FIRE) ||
                state.is(Blocks.CAMPFIRE) ||
                state.is(Blocks.SOUL_CAMPFIRE) ||
                state.is(Blocks.CACTUS) ||
                state.is(Blocks.SWEET_BERRY_BUSH)) {
            return false;
        }

        // 3. 检测是否会卡在墙里
        return !state.isSuffocating(level, blockPos);
    }

    private void stopFiring(IGunOperator op) {
        op.aim(false);
        if (this.gunState == GunState.READY_TO_ATTACK) {
            this.gunState = GunState.CHARGED;
        }
    }

    private boolean isExplosiveWeapon(ItemStack stack) {
        IGun iGun = IGun.getIGunOrNull(stack);
        if (iGun == null) return false;
        return TimelessAPI.getCommonGunIndex(iGun.getGunId(stack)).map(index -> {
            var bulletData = index.getGunData().getBulletData();
            if (bulletData == null) return false;
            var explosionData = bulletData.getExplosionData();
            return explosionData != null && explosionData.isExplode();
        }).orElse(false);
    }

    private void updateStrafing(double distSqr, double radiusSqr) {
        this.strafeTime++;
        if (this.strafeTime >= 20) {
            if (this.mob.getRandom().nextFloat() < 0.3) this.strafeLeft = !this.strafeLeft;
            if (this.mob.getRandom().nextFloat() < 0.3) this.strafeBack = !this.strafeBack;
            this.strafeTime = 0;
        }

        if (distSqr > radiusSqr * 0.8) this.strafeBack = false;
        else if (distSqr < radiusSqr * 0.3) this.strafeBack = true;

        float forward = this.strafeBack ? -0.45F : 0.45F;
        float side = this.strafeLeft ? 0.5F : -0.5F;
        // --- 安全检测开始 ---
        // 计算预测位置：当前位置 + 方向向量
        Vec3 lookVec = this.mob.getViewVector(1.0F);
        Vec3 sideVec = lookVec.yRot((float) Math.PI / 2f);

        // 预测位移向量
        Vec3 moveVec = lookVec.scale(forward).add(sideVec.scale(side));
        Vec3 predictedPos = this.mob.position().add(moveVec.scale(1.5)); // 预测稍远一点的位置

        if (!isSafePosition(predictedPos)) {
            // 如果预测位置不安全，尝试切换左右方向
            this.strafeLeft = !this.strafeLeft;
            // 如果换了方向还不安全，停止侧移，改为原地待命（或者让原有的导航接管）
            if (!isSafePosition(this.mob.position().add(lookVec.scale(forward).add(sideVec.scale(this.strafeLeft ? 0.5F : -0.5F))))) {
                this.mob.getMoveControl().strafe(0, 0);
                return;
            }
        }
        this.mob.getMoveControl().strafe(forward, side);
    }

    // --- 基础工具 (保持不变) ---
    private boolean isHoldingGun() {
        return getGun() != null;
    }

    private IGun getGun() {
        return IGun.getIGunOrNull(this.mob.getMainHandItem());
    }

    private boolean hasAmmo() {
        return getAmmoCount(this.mob.getMainHandItem()) > 0;
    }

    private int getAmmoCount(ItemStack stack) {
        IGun iGun = IGun.getIGunOrNull(stack);
        if (iGun == null) return 0;
        return TimelessAPI.getCommonGunIndex(iGun.getGunId(stack)).map(index -> {
            GunData data = index.getGunData();
            return iGun.getCurrentAmmoCount(stack) + (iGun.hasBulletInBarrel(stack) && data.getBolt() != Bolt.OPEN_BOLT ? 1 : 0);
        }).orElse(0);
    }

    private boolean canReload() {
        return getGun() instanceof AbstractGunItem gunItem && gunItem.canReload(this.mob, this.mob.getMainHandItem());
    }

    private boolean isValidTarget() {
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive();
    }

    private boolean isRightAngle(LivingEntity target) {
        Vec3 lookVec = this.mob.getViewVector(1.0F);
        Vec3 targetVec = target.getEyePosition().subtract(this.mob.getEyePosition());
        double cos = lookVec.dot(targetVec) / (lookVec.length() * targetVec.length());
        return Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, cos)))) < 10 + Math.max(0, 64 - this.mob.distanceToSqr(target));
    }

    private boolean canMeleeAttack(IGunOperator op, LivingEntity target) {
        return System.currentTimeMillis() - op.getDataHolder().meleeTimestamp > 3000 && this.mob.distanceToSqr(target) < 4.0;
    }

    enum GunState {UNCHARGED, CHARGING, CHARGED, READY_TO_ATTACK}
}