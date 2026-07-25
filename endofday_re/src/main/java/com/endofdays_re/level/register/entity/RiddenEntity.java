package com.endofdays_re.level.register.entity;

import com.endofdays_re.utils.EntityFlags;
import com.google.common.collect.Iterables;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class RiddenEntity extends Mob {
    private boolean clearedAI;
    private Entity scheduledRide;
    private boolean scheduledDismount;

    public RiddenEntity(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        if (!level.isClientSide) {
            AttributeInstance health = this.getAttribute(Attributes.MAX_HEALTH);
            if (health != null) {
                health.setBaseValue(5);
            }
        }
    }

    public static AABB riddenAABB(AABB thisAABB, AABB other) {
        double d = Math.min(thisAABB.minX, other.minX);
        double e = thisAABB.minY;
        double f = Math.min(thisAABB.minZ, other.minZ);
        double g = Math.max(thisAABB.maxX, other.maxX);
        double h = Math.max(thisAABB.maxY, other.maxY);
        double i = Math.max(thisAABB.maxZ, other.maxZ);
        return new AABB(d, e, f, g, h, i);
    }

    public void scheduledRide(Entity passenger) {
        this.scheduledRide = passenger;
    }

    public void scheduledDismount() {
        this.scheduledDismount = true;
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        return spawnData;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.getPassengers().contains(source.getEntity()))
            return false;
        return super.hurt(source, amount);
    }

    @Override
    public boolean doHurtTarget(@NotNull Entity entity) {
        return false;
    }

    @Override
    public boolean canBeSeenAsEnemy() {
        return false;
    }

    public boolean hasEffect(@NotNull MobEffect potion) {
        return false;
    }

    @Nullable

    public MobEffectInstance getEffect(@NotNull MobEffect potion) {
        return null;
    }

    @Override
    public boolean addEffect(@NotNull MobEffectInstance effectInstance, @Nullable Entity entity) {
        return false;
    }

    @Override
    public boolean canBeAffected(@NotNull MobEffectInstance potioneffect) {
        return false;
    }

    @Override
    public void forceAddEffect(@NotNull MobEffectInstance mobEffectInstance, @Nullable Entity entity) {
    }

    protected void dropAllDeathLoot(@NotNull DamageSource damageSource) {
    }

    @Override
    public @NotNull ItemStack getItemBySlot(@NotNull EquipmentSlot slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(@NotNull EquipmentSlot slot, @NotNull ItemStack stack) {
    }

    // 删除 getDimensions 方法，因为父类中已是 final

    @Override
    public @NotNull SlotAccess getSlot(int slot) {
        return SlotAccess.NULL;
    }

    protected EntityDimensions originDimension(Pose pose) {
        return this.getType().getDimensions();
    }

    @Override
    public @NotNull Iterable<ItemStack> getAllSlots() {
        return Iterables.concat();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            if (!this.clearedAI) {
                this.clearedAI = true;
                // 移除 getRunningGoals，改用 getAvailableGoals 并手动停止正在运行的目标
                for (WrappedGoal goal : this.goalSelector.getAvailableGoals()) {
                    if (goal.isRunning()) {
                        goal.stop();
                    }
                }
                this.removeFreeWill();
            }
            if (this.scheduledRide != null) {
                this.scheduledRide.startRiding(this);
                this.scheduledRide = null;
            }
            if (this.scheduledDismount && this.getFirstPassenger() != null) {
                this.getFirstPassenger().stopRiding();
                this.scheduledDismount = false;
            }
            if (!this.isVehicle())
                this.remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected @NotNull Component getTypeName() {
        return this.getType().getDescription();
    }

    @Override
    protected void addPassenger(@NotNull Entity passenger) {
        super.addPassenger(passenger);
        this.refreshDimensions();
    }

    @Override
    public @NotNull CompoundTag saveWithoutId(@NotNull CompoundTag compound) {
        CompoundTag tag = super.saveWithoutId(compound);
        tag.getCompound(EntityFlags.TAG_ID).putString(EntityFlags.SERVER_ENTITY_TAG_ID, this.serverSideID().toString());
        return tag;
    }

    @Override
    protected ResourceKey<LootTable> getDefaultLootTable() {
        return BuiltInLootTables.EMPTY;
    }

    public abstract ResourceLocation serverSideID();

    public boolean doesntCollideWithRidden(Entity rider) {
        return this.level().noCollision(this, riddenAABB(this.getBoundingBox(), rider.getBoundingBox()));
    }
}