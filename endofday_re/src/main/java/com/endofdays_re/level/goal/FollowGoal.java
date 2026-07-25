package com.endofdays_re.level.goal;

import com.endofdays_re.level.register.ModMemoryModuleTypes;
import com.endofdays_re.utils.ModUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class FollowGoal extends Goal {
    // 叠罗汉相关标签键名
    private static final String STACK_LEVEL_TAG = "StackLevel";
    private static final String IS_MOUNT_TAG = "IsMount";
    private static final String HAS_RIDER_TAG = "HasRider";
    private static final String SHARED_TARGET_ID_TAG = "SharedTargetId";
    //相互跟随
    protected final Level level;
    protected final Mob mob;
    private final Class<? extends Mob> partnerClass;
    private final double speedModifier;
    private final java.util.function.Predicate<LivingEntity> partnerSelector; // 保存选择器
    @Nullable
    protected Mob partner;

    public FollowGoal(Mob mob, double speedModifier) {
        this(mob, speedModifier, mob.getClass());
    }

    public FollowGoal(Mob mob, double speedModifier, Class<? extends Mob> partnerClass) {
        this(mob, speedModifier, partnerClass, null);
    }

    public FollowGoal(Mob mob, double speedModifier, Class<? extends Mob> partnerClass, @Nullable Predicate<LivingEntity> en) {
        this.level = mob.level();
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.partnerClass = partnerClass;
        this.partnerSelector = en; // 保存选择器
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    public boolean canUse() {
        // 如果处于叠罗汉状态，不启用跟随目标
        if (isInStack(mob)
                || mob.getBrain().hasMemoryValue(ModMemoryModuleTypes.TARGET_BLOCK_POS.get())
                || mob.getBrain().hasMemoryValue(ModMemoryModuleTypes.TARGET_BLOCK_POS_ARRMOR.get())
                || this.mob.getBrain().hasMemoryValue(ModMemoryModuleTypes.ATTACK_TARGET_BLOCK_POS.get())
        ) {
            return false;
        }

        if (this.mob.getTarget() != null || this.mob.getPersistentData().contains(ModUtils.KeyWraps("gigantic"))) {
            return false;
        } else if (this.mob.getRandom().nextInt(15) == 0 && this.getFreePartner() == null
        ) {
            return false;
        } else {
            if (this.mob.getRandom().nextInt(5) == 1) {
                this.partner = this.getFreePartner();
            }

            return this.partnerClass != null && this.partner != null;
        }
    }

    public boolean canContinueToUse() {
        if (this.mob.getBrain().hasMemoryValue(ModMemoryModuleTypes.ATTACK_TARGET_BLOCK_POS.get())
                || mob.getBrain().hasMemoryValue(ModMemoryModuleTypes.TARGET_BLOCK_POS.get())
                || mob.getBrain().hasMemoryValue(ModMemoryModuleTypes.TARGET_BLOCK_POS_ARRMOR.get())) {
            return false;
        }
        if (this.partner == null || !this.partner.isAlive()) {
            return false;
        } else {
            // 如果自己或伙伴进入叠罗汉状态，停止跟随
            if (isInStack(mob) || isInStack(partner)) {
                return false;
            }

            // 检查距离，如果太远也停止跟随
            double distance = mob.distanceToSqr(partner);
            // 16格以外的距离
            return !(distance > 256.0);
        }
    }

    public void stop() {
        this.partner = null;
        this.mob.getNavigation().stop();
        this.mob.setAggressive(false);
    }

    public void tick() {
        if (this.partner != null && this.partner.isAlive()) {
            // 检查叠罗汉状态
            if (isInStack(mob) || isInStack(partner)) {
                this.stop();
                return;
            }

            double distance = mob.distanceToSqr(partner);

            // 根据距离调整移动策略
            if (distance > 16.0) { // 4格以外，快速接近
                this.mob.getNavigation().moveTo(this.partner, this.speedModifier * 1.2);
            } else if (distance > 4.0) { // 2-4格，正常速度
                this.mob.getNavigation().moveTo(this.partner, this.speedModifier);
            } else { // 2格以内，停止移动但保持面向
                this.mob.getNavigation().stop();
                this.mob.getLookControl().setLookAt(this.partner);
            }

            // 看向伙伴
            this.mob.getLookControl().setLookAt(this.partner);
        }
    }

    @Nullable
    private PathfinderMob getFreePartner() {
        // 每次查找伙伴时动态创建TargetingConditions，使用最新的FOLLOW_RANGE值
        TargetingConditions partnerTargeting = TargetingConditions.forNonCombat()
                .range(this.mob.getAttributeValue(Attributes.FOLLOW_RANGE))
                .selector(this.partnerSelector);

        List<? extends Mob> list = this.level.getNearbyEntities(this.partnerClass, partnerTargeting, this.mob, this.mob.getBoundingBox().inflate(this.mob.getAttributeValue(Attributes.FOLLOW_RANGE)));
        double d0 = Double.MAX_VALUE;
        PathfinderMob inf = null;

        for (Mob candidate : list) {
            // 排除处于叠罗汉状态的候选者
            if (isInStack(candidate)) {
                continue;
            }

            // 排除已经有目标的候选者
            if (candidate.getTarget() != null) {
                continue;
            }

            // 排除正在被骑乘或骑乘他人的候选者
            if (candidate.isPassenger() || candidate.isVehicle()) {
                continue;
            }

            double distance = this.mob.distanceToSqr(candidate);
            if (distance < d0) {
                inf = (PathfinderMob) candidate;
                d0 = distance;
            }
        }

        return inf;
    }

    // 检查实体是否处于叠罗汉状态
    private boolean isInStack(Mob entity) {
        // 检查是否被骑乘或骑乘他人
        if (entity.isPassenger() || entity.isVehicle()) {
            return true;
        }

        // 检查叠罗汉相关的持久化数据
        if (entity.getPersistentData().getBoolean(IS_MOUNT_TAG) ||
                entity.getPersistentData().getBoolean(HAS_RIDER_TAG) ||
                entity.getPersistentData().contains(SHARED_TARGET_ID_TAG)) {
            return true;
        }

        // 检查堆叠层级
        int stackLevel = entity.getPersistentData().getInt(STACK_LEVEL_TAG);
        return stackLevel > 0;
    }

    // 获取实体在堆叠中的层级
    private int getStackLevel(Mob entity) {
        return entity.getPersistentData().getInt(STACK_LEVEL_TAG);
    }

    // 检查实体是否是坐骑
    private boolean isMount(Mob entity) {
        return entity.getPersistentData().getBoolean(IS_MOUNT_TAG);
    }

    // 检查实体是否有骑乘者
    private boolean hasRider(Mob entity) {
        return entity.getPersistentData().getBoolean(HAS_RIDER_TAG);
    }

    // 获取堆叠中的顶部实体
    private Mob getTopEntity(Mob entity) {
        Mob top = entity;
        while (top.isPassenger() && top.getVehicle() instanceof Mob mount) {
            top = mount;
        }
        return top;
    }

    // 检查两个实体是否在同一个堆叠中
    private boolean isInSameStack(Mob entity1, Mob entity2) {
        Mob top1 = getTopEntity(entity1);
        Mob top2 = getTopEntity(entity2);
        return top1 == top2;
    }
}