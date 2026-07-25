package com.endofdays_re.level.register.entity;

import com.endofdays_re.utils.ModUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("removal")
public class FlyingEntity extends RiddenEntity {
    public static final ResourceLocation SUMMONED_FLYING_ID = ResourceLocation.parse(ModUtils.MODID + ":flying_entity");
    private static final EntityDataAccessor<Integer> DATA_ID_SIZE = SynchedEntityData.defineId(FlyingEntity.class, EntityDataSerializers.INT);

    public FlyingEntity(Level level) {
        super(EntityType.PHANTOM, level);
        if (!level.isClientSide) {
            AttributeInstance speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
            AttributeInstance follow = this.getAttribute(Attributes.FOLLOW_RANGE);
            if (speed != null && follow != null) {
                speed.setBaseValue(0.2);
                follow.setBaseValue(24);
            }
        }
        this.moveControl = new CustomFlyMoveControl(this);
        this.setNoGravity(true);
    }

    public FlyingEntity(EntityType<FlyingEntity> flyingSummonEntityEntityType, Level level) {
        super(EntityType.PHANTOM, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ID_SIZE, -1);
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        FlyingPathNavigation flyingPathNavigation = new FlyingPathNavigation(this, level) {
            @Override
            public boolean isStableDestination(@NotNull BlockPos pos) {
                return true;
            }
        };
        flyingPathNavigation.setCanOpenDoors(false);
        flyingPathNavigation.setCanFloat(true);
        flyingPathNavigation.setCanPassDoors(true);
        return flyingPathNavigation;
    }

    // 尺寸缩放逻辑移到此处，因为父类 getDimensions 已是 final，不能重写，所以需要覆盖 originDimension 或直接修改尺寸？
    // 实际上，我们可以在 tick 中动态调整尺寸，或者使用 Attribute 的 SCALE。这里保留原逻辑但调整方法。
    @Override
    protected EntityDimensions originDimension(Pose pose) {
        EntityDimensions base = super.originDimension(pose);
        int i = this.getEntityData().get(DATA_ID_SIZE);
        float f = (base.width() + 0.2f * (float) i) / base.width();
        return base.scale(f);
    }

    public double getPassengersRidingOffset() {
        return this.getType().getDimensions().height() * 0.35f;
    }

    @Override
    protected void addPassenger(@NotNull Entity passenger) {
        if (this.getPassengers().isEmpty()) {
            float widthPassenger = passenger.getBbWidth();
            int w = (int) ((widthPassenger - 0.8f) / 0.2f);
            this.getEntityData().set(DATA_ID_SIZE, w);
            if (passenger instanceof Mob mob)
                ((FlyingPathNavigation) this.getNavigation()).setCanOpenDoors(mob.getNavigation().getNodeEvaluator().canOpenDoors());
        }
        super.addPassenger(passenger);
    }

    @Override
    public ResourceLocation serverSideID() {
        return SUMMONED_FLYING_ID;
    }

    @Override
    public void onSyncedDataUpdated(@NotNull EntityDataAccessor<?> key) {
        if (DATA_ID_SIZE.equals(key)) {
            this.refreshDimensions();
        }
        super.onSyncedDataUpdated(key);
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float multiplier, @NotNull DamageSource source) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, @NotNull BlockState state, @NotNull BlockPos pos) {
    }

    @Override
    public void travel(@NotNull Vec3 travelVector) {
        if (this.isInWater()) {
            this.moveRelative(0.02f, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.8f));
        } else if (this.isInLava()) {
            this.moveRelative(0.02f, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.5));
        } else {
            float friction = 0.91f;
            this.moveRelative(0.02f, travelVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(friction));
        }
        this.calculateEntityAnimation(false);
    }

    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource damageSource) {
        return SoundEvents.PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PHANTOM_DEATH;
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * 0.8f;
    }

    protected static class CustomFlyMoveControl extends MoveControl {
        private float speed;

        public CustomFlyMoveControl(FlyingEntity mob) {
            super(mob);
            this.speed = 0.1f;
        }

        @Override
        public void tick() {
            if (this.operation != Operation.MOVE_TO || this.mob.getNavigation().isDone()) {
                this.mob.setSpeed(0.0f);
                return;
            }
            if (this.mob.horizontalCollision) {
                this.mob.setYRot(this.mob.getYRot() + 180.0f);
                this.speed = 0.1f;
            }
            Vec3 dir = new Vec3(this.wantedX - this.mob.getX(), this.wantedY - this.mob.getY(), this.wantedZ - this.mob.getZ());
            dir = dir.normalize();
            float rotPre = this.mob.getYRot();

            double horLen = Math.sqrt(dir.x() * dir.x() + dir.z() * dir.z());
            this.mob.setXRot(Mth.wrapDegrees((float) (-(Mth.atan2(dir.y(), horLen) * Mth.RAD_TO_DEG))));
            float newRot = Mth.wrapDegrees((float) (Mth.atan2(dir.z(), dir.x()) * Mth.RAD_TO_DEG));
            this.mob.setYRot(Mth.approachDegrees(rotPre + 90, newRot, 8.0f) - 90.0f);
            this.mob.yBodyRot = this.mob.getYRot();

            float throttleTreshold = 12;
            if (!this.mob.getNavigation().isDone()) {
                if (this.mob.getNavigation().getPath() == null) return;
                BlockPos target = this.mob.getNavigation().getPath().getTarget();
                if (this.mob.distanceToSqr(target.getX() + 0.5, target.getY(), target.getZ() + 0.5) < 4.5)
                    throttleTreshold = 3;
            }
            this.speed = Mth.degreesDifferenceAbs(rotPre, this.mob.getYRot()) < throttleTreshold ? Mth.approach(this.speed, 1.8f, 0.009f * (1.8f / this.speed)) : Mth.approach(this.speed, 0.2f, 0.025f);

            Vec3 moveDir = Vec3.directionFromRotation(this.mob.getXRot(), this.mob.getYRot());
            double xDir = this.speed * moveDir.x() * 0.02;
            double yDir = this.speed * moveDir.y() * 0.02;
            double zDir = this.speed * moveDir.z() * 0.02;

            Vec3 delta = this.mob.getDeltaMovement();
            this.mob.setDeltaMovement(delta.add(xDir, yDir, zDir));
        }
    }
}