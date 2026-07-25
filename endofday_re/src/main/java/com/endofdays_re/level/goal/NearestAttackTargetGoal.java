package com.endofdays_re.level.goal;


import com.endofdays_re.client.config.data.CommonBuild;
import com.endofdays_re.level.register.ModMemoryModuleTypes;
import com.endofdays_re.level.register.RegisterEntityAttributes;
import com.endofdays_re.utils.ModUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class NearestAttackTargetGoal<T extends LivingEntity> extends TargetGoal {//攻击选择过滤
    protected final Class<T> targetType;
    protected final int randomInterval;
    private final List<? extends CommonBuild.TargetSelect> AttackList;
    private final java.util.function.Predicate<LivingEntity> originalSelector; // 保存原始选择器
    public TargetingConditions targetEntitySelectorXRay;
    @Nullable
    protected LivingEntity target;
    protected TargetingConditions targetConditions;
    private String RegisterName;

    public NearestAttackTargetGoal(Mob pMob, Class<T> pTargetType, boolean pMustSee, double follow_range, double xaer_range, List<CommonBuild.TargetSelect> Attacklist) {
        this(pMob, pTargetType, 10, pMustSee, false, null, follow_range, xaer_range, Attacklist);
    }

    public NearestAttackTargetGoal(Mob pMob, Class<T> pTargetType, boolean pMustSee, Predicate<LivingEntity> pTargetPredicate, double follow_range, double xaer_range, List<CommonBuild.TargetSelect> Attacklist) {
        this(pMob, pTargetType, 10, pMustSee, false, pTargetPredicate, follow_range, xaer_range, Attacklist);
    }

    public NearestAttackTargetGoal(Mob pMob, Class<T> pTargetType, boolean pMustSee, boolean pMustReach, double follow_range, double xaer_range, List<CommonBuild.TargetSelect> Attacklist) {
        this(pMob, pTargetType, 10, pMustSee, pMustReach, null, follow_range, xaer_range, Attacklist);
    }

    public NearestAttackTargetGoal(Mob pMob, Class<T> pTargetType, int pRandomInterval, boolean pMustSee, boolean pMustReach, @Nullable Predicate<LivingEntity> pTargetPredicate, double follow_range, double xaer_range, List<CommonBuild.TargetSelect> Attacklist) {
        super(pMob, pMustSee, pMustReach);
        this.targetType = pTargetType;
        this.randomInterval = reducedTickDelay(pRandomInterval);
        this.setFlags(EnumSet.of(Flag.TARGET));
        this.originalSelector = pTargetPredicate; // 保存原始选择器
        this.targetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(pTargetPredicate);
        this.targetEntitySelectorXRay = this.targetConditions.copy().ignoreLineOfSight();
        this.AttackList = Attacklist;
        ModUtils.setAttributeBaseValue(pMob, Attributes.FOLLOW_RANGE, follow_range);
        ModUtils.setAttributeBaseValue(pMob, RegisterEntityAttributes.XRAY_FOLLOW_RANGE, xaer_range);

    }

    public boolean canUse() {
        if (this.mob.getTarget() == this.mob) {
            return false;
        }
        if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
            return false;
        } else {
            this.findTarget();
            return this.target != null;
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (mob.getBrain().hasMemoryValue(ModMemoryModuleTypes.TARGET_BLOCK_POS.get())
                || mob.getBrain().hasMemoryValue(ModMemoryModuleTypes.TARGET_BLOCK_POS_ARRMOR.get())) {
            return false;
        }
        return super.canContinueToUse();
    }

    protected AABB getTargetSearchArea(double pTargetDistance) {
        return this.mob.getBoundingBox().inflate(pTargetDistance, pTargetDistance, pTargetDistance);
    }

    protected void findTarget() {
        // 每次查找目标时更新搜索范围，确保与当前属性同步
        this.targetConditions = TargetingConditions.forCombat().range(this.getFollowDistance()).selector(this.originalSelector);
        this.targetEntitySelectorXRay = this.targetConditions.copy().ignoreLineOfSight();

        if (this.targetType != Player.class && this.targetType != ServerPlayer.class) {
            this.target = this.mob.level().getNearestEntity(this.mob.level().getEntitiesOfClass(this.targetType, this.getTargetSearchArea(this.getFollowDistance()), (entity) -> {
                if (entity instanceof Player) {
                    this.RegisterName = "minecraft:player";
                } else {
                    EntityType<?> type = entity.getType();
                    ResourceLocation Entitykey = BuiltInRegistries.ENTITY_TYPE.getKey(type);
                    this.RegisterName = Entitykey.toString();
                }

                return AttackList.stream().anyMatch(targetSelect -> Objects.equals(targetSelect.Target, this.RegisterName));
            }), this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
            if (this.target == null && this.getFollowXRayDistance() > 0.0) {
                this.target = this.mob.level().getNearestEntity(this.mob.level().getEntitiesOfClass(this.targetType, this.getTargetSearchArea(this.getFollowDistance()), (Entity) -> {
                    if (Entity instanceof Player) {
                        this.RegisterName = "minecraft:player";
                    } else {
                        EntityType<?> type = Entity.getType();
                        ResourceLocation Entitykey = BuiltInRegistries.ENTITY_TYPE.getKey(type);
                        this.RegisterName = Entitykey.toString();
                    }


                    return AttackList.stream().anyMatch(targetSelect -> Objects.equals(targetSelect.Target, this.RegisterName));
                }), this.targetEntitySelectorXRay.range(this.getFollowXRayDistance()), this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
            }
        } else {
            this.target = this.mob.level().getNearestPlayer(this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
            if (this.target == null && this.getFollowXRayDistance() > 0.0) {
                this.target = this.mob.level().getNearestPlayer(this.targetEntitySelectorXRay.range(this.getFollowXRayDistance()), this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
            }
        }

    }

    public void start() {
        this.mob.setTarget(this.target);
        super.start();
    }

    public void setTarget(@Nullable LivingEntity pTarget) {
        this.target = pTarget;
    }

    protected double getFollowXRayDistance() {
        return this.mob.getAttributeValue(RegisterEntityAttributes.XRAY_FOLLOW_RANGE);
    }
}
