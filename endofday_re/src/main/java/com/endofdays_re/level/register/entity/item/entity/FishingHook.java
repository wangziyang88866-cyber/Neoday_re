package com.endofdays_re.level.register.entity.item.entity;

import com.endofdays_re.level.register.RegisterEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class FishingHook extends Projectile {
    // 1.21.1 标准定义方式
    private static final EntityDataAccessor<Integer> DATA_HOOKED_ENTITY = SynchedEntityData.defineId(FishingHook.class, EntityDataSerializers.INT);

    private int life;
    @Nullable
    private Entity hookedIn;
    private FishHookState currentState = FishHookState.FLYING;

    public FishingHook(EntityType<? extends FishingHook> entityType, Level level) {
        super(entityType, level);
        this.noCulling = true;
    }

    public FishingHook(Entity owner, Level level) {
        this(RegisterEntity.FISHING_HOOK.get(), level);
        this.setOwner(owner);
        // 1.21.1 建议在构造时就设置初始位置
        this.setPos(owner.getX(), owner.getEyeY(), owner.getZ());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        // 1.21.1 修正：defineSynchedData 现在接收一个 Builder
        builder.define(DATA_HOOKED_ENTITY, 0);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        if (DATA_HOOKED_ENTITY.equals(accessor)) {
            int i = this.getEntityData().get(DATA_HOOKED_ENTITY);
            // 修正：1.21.1 使用 level() 访问
            this.hookedIn = i > 0 ? this.level().getEntity(i - 1) : null;
        }
        super.onSyncedDataUpdated(accessor);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        return pDistance < 4096.0D;
    }

    @Override
    public void tick() {
        super.tick();
        Entity owner = this.getOwner();
        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }

        if (!this.level().isClientSide && this.distanceToSqr(owner) > 1024.0D) {
            this.discard();
            return;
        }

        if (this.onGround()) {
            this.life++;
            if (this.life >= 1200) {
                this.discard();
                return;
            }
        } else {
            this.life = 0;
        }

        BlockPos blockpos = this.blockPosition();
        FluidState fluidstate = this.level().getFluidState(blockpos);
        boolean isUnderwater = fluidstate.is(FluidTags.WATER);

        if (this.currentState == FishHookState.FLYING) {
            if (this.hookedIn != null) {
                this.setDeltaMovement(Vec3.ZERO);
                this.currentState = FishHookState.HOOKED_IN_ENTITY;
                return;
            }

            if (isUnderwater) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.3D, 0.2D, 0.3D));
            }

            this.checkCollision();
        } else if (this.currentState == FishHookState.HOOKED_IN_ENTITY) {
            if (this.hookedIn != null) {
                if (!this.hookedIn.isRemoved() && this.hookedIn.level().dimension() == this.level().dimension()) {
                    this.setPos(this.hookedIn.getX(), this.hookedIn.getY(0.8D), this.hookedIn.getZ());
                } else {
                    this.setHookedEntity(null);
                    this.currentState = FishHookState.FLYING;
                }
            }
            return;
        }

        if (!isUnderwater) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.03D, 0.0D));
        }

        this.move(MoverType.SELF, this.getDeltaMovement());
        this.updateRotation();

        if (this.currentState == FishHookState.FLYING && (this.onGround() || this.horizontalCollision)) {
            this.setDeltaMovement(Vec3.ZERO);
        }

        this.setDeltaMovement(this.getDeltaMovement().scale(0.96D));
        this.reapplyPosition();
    }

    private void checkCollision() {
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        // 修正：使用 NeoForge 的 EventHooks
        if (hitresult.getType() != HitResult.Type.MISS) {
            if (!EventHooks.onProjectileImpact(this, hitresult)) {
                this.onHit(hitresult);
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) || (entity.isAlive() && entity instanceof ItemEntity);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            this.setHookedEntity(result.getEntity());
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        // 1.21.1 碰撞后通常停止运动
        this.setDeltaMovement(Vec3.ZERO);
        this.currentState = FishHookState.ON_GROUND;
    }

    private void setHookedEntity(@Nullable Entity entity) {
        this.hookedIn = entity;
        this.getEntityData().set(DATA_HOOKED_ENTITY, entity == null ? 0 : entity.getId() + 1);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        // 如果需要保存，在这里添加
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        // 如果需要读取，在这里添加
    }

    public void retrieve(boolean isInventoryHooked) {
        Entity owner = this.getOwner();
        if (this.level().isClientSide || owner == null) return;

        if (this.hookedIn != null) {
            if (this.hookedIn instanceof Player player && isInventoryHooked) {
                if (!player.getInventory().isEmpty()) {
                    int slot = this.random.nextInt(36);
                    ItemStack itemStack = player.getInventory().getItem(slot);

                    if (!itemStack.isEmpty()) {
                        ItemEntity itemEntity = new ItemEntity(this.level(), this.hookedIn.getX(), this.hookedIn.getEyeY(), this.hookedIn.getZ(), itemStack.copy());
                        itemEntity.setDefaultPickUpDelay();
                        this.pullEntity(itemEntity);
                        this.level().addFreshEntity(itemEntity);
                        // 1.21.1 移除物品
                        player.getInventory().removeItem(slot, itemStack.getCount());
                    }
                }
            } else {
                this.pullEntity(this.hookedIn);
                // 1.21.1 实体事件广播
                this.level().broadcastEntityEvent(this, (byte) 31);
            }
        }
        this.discard();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 31 && this.level().isClientSide && this.hookedIn != null) {
            this.pullEntity(this.hookedIn);
        }
        super.handleEntityEvent(id);
    }

    protected void pullEntity(Entity entity) {
        Entity owner = this.getOwner();
        if (owner != null) {
            Vec3 vec3 = (new Vec3(owner.getX() - this.getX(), owner.getY() - this.getY(), owner.getZ() - this.getZ())).scale(0.1D);
            entity.setDeltaMovement(entity.getDeltaMovement().add(vec3.scale(entity instanceof LivingEntity ? 3.0D : 1.0D)));
        }
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    public boolean canChangeDimensions(Level p_35237_, Level p_35238_) {
        // 1.21.1 签名变更
        return false;
    }

    // 1.21.1 修正：使用标准的 Packet 重新创建逻辑
    @Override
    public void recreateFromPacket(@NotNull ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
    }

    @Nullable
    public Entity getHookedIn() {
        return this.hookedIn;
    }

    enum FishHookState {
        FLYING,
        HOOKED_IN_ENTITY,
        ON_GROUND
    }
}