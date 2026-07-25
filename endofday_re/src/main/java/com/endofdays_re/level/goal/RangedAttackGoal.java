package com.endofdays_re.level.goal;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.EnumSet;

public class RangedAttackGoal extends Goal {

    private static final int RELOAD_DURATION = 25;
    private final Mob mob;
    private final double speedModifier;
    private final int attackInterval;
    private final float attackRadius;
    private int attackTime;
    private boolean isCharging;
    private boolean isReloading;
    private int reloadTime;
    private InteractionHand weaponHand;
    private RangedWeaponType currentWeaponType;

    public RangedAttackGoal(Mob mob, double speedModifier, int attackInterval, float attackRadius) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.attackInterval = attackInterval;
        this.attackRadius = attackRadius;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.mob.getTarget();
        if (target == null || !target.isAlive()) return false;
        // 视线检查
        if (!this.mob.getSensing().hasLineOfSight(target)) return false;
        return determineWeaponHand() != null;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity target = this.mob.getTarget();
        return target != null && target.isAlive() && determineWeaponHand() != null;
    }

    @Override
    public void tick() {
        LivingEntity target = this.mob.getTarget();
        if (target == null) return;

        handleCombatMovement(target);
        handleWeaponAction(target);
        this.mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
    }

    private InteractionHand determineWeaponHand() {
        ItemStack mainHand = this.mob.getMainHandItem();
        if (isRangedWeapon(mainHand)) {
            this.currentWeaponType = getWeaponType(mainHand);
            this.weaponHand = InteractionHand.MAIN_HAND;
            return InteractionHand.MAIN_HAND;
        }
        ItemStack offHand = this.mob.getOffhandItem();
        if (isRangedWeapon(offHand)) {
            this.currentWeaponType = getWeaponType(offHand);
            this.weaponHand = InteractionHand.OFF_HAND;
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    private boolean isRangedWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item instanceof BowItem || item instanceof CrossbowItem || item instanceof TridentItem ||
                stack.is(Items.SNOWBALL) || stack.is(Items.EGG) ||
                stack.is(Items.EXPERIENCE_BOTTLE) || stack.is(Items.ENDER_PEARL);
    }

    private RangedWeaponType getWeaponType(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof BowItem) return RangedWeaponType.BOW;
        if (item instanceof CrossbowItem) return RangedWeaponType.CROSSBOW;
        if (item instanceof TridentItem) return RangedWeaponType.TRIDENT;
        return RangedWeaponType.THROWABLE;
    }

    private void handleCombatMovement(LivingEntity target) {
        double distanceSqr = this.mob.distanceToSqr(target);
        boolean canSee = this.mob.getSensing().hasLineOfSight(target);

        if (distanceSqr <= (this.attackRadius * this.attackRadius) && canSee) {
            this.mob.getNavigation().stop();
            this.attackTime++;
        } else {
            this.mob.getNavigation().moveTo(target, this.speedModifier);
            this.attackTime = 0;
            resetChargingState();
        }
    }

    private void handleWeaponAction(LivingEntity target) {
        if (isReloading) {
            handleCrossbowReloading();
            return;
        }

        if (this.attackTime >= this.attackInterval) {
            executeRangedAttack(target);
            resetAttackState();
        } else {
            handleWeaponCharging();
        }
    }

    private void executeRangedAttack(LivingEntity target) {
        if (this.weaponHand == null) return;
        switch (this.currentWeaponType) {
            case BOW -> shootArrow(target, 1.0F);
            case CROSSBOW -> shootCrossbow(target);
            case TRIDENT -> throwTrident(target);
            case THROWABLE -> shootThrowable(target);
        }
    }

    // ========== 1.21.1 弩箭逻辑适配 ==========

    private void handleWeaponCharging() {
        ItemStack weapon = this.mob.getItemInHand(this.weaponHand);
        if (this.currentWeaponType == RangedWeaponType.BOW && !this.isCharging) {
            this.mob.startUsingItem(this.weaponHand);
            this.isCharging = true;
        } else if (this.currentWeaponType == RangedWeaponType.CROSSBOW) {
            ChargedProjectiles charged = weapon.get(DataComponents.CHARGED_PROJECTILES);
            if ((charged == null || charged.isEmpty()) && !this.isReloading) {
                startCrossbowReload();
            }
        }
    }

    private void startCrossbowReload() {
        this.isReloading = true;
        this.reloadTime = 0;
        playSound(SoundEvents.CROSSBOW_LOADING_START.value());
    }

    private void handleCrossbowReloading() {
        this.reloadTime++;
        if (this.reloadTime == 10) playSound(SoundEvents.CROSSBOW_LOADING_MIDDLE.value());
        if (this.reloadTime >= RELOAD_DURATION) finishCrossbowReload();
    }

    private void finishCrossbowReload() {
        ItemStack crossbow = this.mob.getItemInHand(this.weaponHand);
        if (crossbow.getItem() instanceof CrossbowItem) {
            // 设置装填组件
            crossbow.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(new ItemStack(Items.ARROW)));
        }
        playSound(SoundEvents.CROSSBOW_LOADING_END.value());
        this.isReloading = false;
    }

    private void shootCrossbow(LivingEntity target) {
        ItemStack crossbow = this.mob.getItemInHand(this.weaponHand);
        // 获取弩箭装填组件
        ChargedProjectiles charged = crossbow.get(DataComponents.CHARGED_PROJECTILES);

        // 1.21.1 修正：判断组件是否存在且不为空
        if (charged != null && !charged.isEmpty()) {
            Level level = this.mob.level();
            if (!level.isClientSide) {
                // 核心修正：在 1.21.1 中，ChargedProjectiles 记录类直接暴露了 items 列表
                // 或者通过调用其提供的访问方法（取决于具体的映射，通常是 items() 或 getItems()）
                for (ItemStack ammo : charged.getItems()) {
                    if (!ammo.isEmpty()) {
                        AbstractArrow arrow = getArrowFromStack(level, ammo, crossbow);
                        shootProjectile(arrow, target, 1.6F);
                    }
                }

                // 发射后清空装填状态
                crossbow.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);

                // 播放弩箭发射音效
                // 注意：CrossbowItem 上的音效通常是 .value()，因为它是 Holder
                playSound(SoundEvents.CROSSBOW_SHOOT);
            }
        }
    }

    private AbstractArrow getArrowFromStack(Level level, ItemStack ammo, ItemStack weapon) {
        // 获取箭矢项目，如果不是箭矢则默认为普通箭
        ArrowItem arrowitem = (ArrowItem) (ammo.getItem() instanceof ArrowItem ? ammo.getItem() : Items.ARROW);

        // 1.21.1 核心：在创建箭矢时传入 weapon (弩)
        // 引擎会自动根据 weapon 的组件处理弩箭逻辑（如音效和特殊属性）
        AbstractArrow arrow = arrowitem.createArrow(level, ammo, this.mob, weapon);

        // 获取附魔注册表
        var enchantmentRegistry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);

        // 获取穿透附魔等级
        int piercing = EnchantmentHelper.getItemEnchantmentLevel(
                enchantmentRegistry.getOrThrow(Enchantments.PIERCING),
                weapon
        );

        if (piercing > 0) {
            arrow.setPierceLevel((byte) piercing);
        }

        // 1.21.1 修正：setShotFromCrossbow 已被移除
        // 箭矢现在通过内部的发射源逻辑自动处理，无需手动标记

        return arrow;
    }

    private void shootThrowable(LivingEntity target) {
        ItemStack stack = this.mob.getItemInHand(this.weaponHand);
        Level level = this.mob.level();
        if (level.isClientSide) return;

        Projectile projectile = null;

        // 1.21.1 核心修正：使用 EntityType.create 或正确的构造函数
        if (stack.is(Items.SNOWBALL)) {
            // 1.21.1 中类名通常就是 Snowball
            Snowball snowball = new Snowball(level, this.mob);
            // 关键：1.21.1 设置渲染物品的方法
            snowball.setItem(stack);
            projectile = snowball;
        } else if (stack.is(Items.EGG)) {
            // 修正：如果 ThrownEgg 找不到，尝试使用 ThrownEgg(EntityType, Level)
            // 或者直接使用 EntityType 注册表创建
            ThrownEgg egg = new ThrownEgg(level, this.mob);
            egg.setItem(stack);
            projectile = egg;
        } else if (stack.is(Items.EXPERIENCE_BOTTLE)) {
            ThrownExperienceBottle bottle = new ThrownExperienceBottle(level, this.mob);
            bottle.setItem(stack);
            projectile = bottle;
        } else if (stack.is(Items.ENDER_PEARL)) {
            ThrownEnderpearl pearl = new ThrownEnderpearl(level, this.mob);
            pearl.setItem(stack);
            projectile = pearl;
        } else {
            projectile = ProjectileUtil.getMobArrow(this.mob, stack, 1.0F, stack);
        }

        if (projectile != null) {
            shootProjectile(projectile, target, 1.2F);

            // 播放对应的投掷音效
            net.minecraft.sounds.SoundEvent sound = stack.is(Items.EXPERIENCE_BOTTLE)
                    ? SoundEvents.EXPERIENCE_BOTTLE_THROW
                    : SoundEvents.SNOWBALL_THROW;
            playSound(sound);
        }
    }

    private void shootArrow(LivingEntity target, float power) {
        ItemStack weapon = this.mob.getItemInHand(this.weaponHand);
        // 1.21.1 修正：ProjectileUtil 参数增加
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this.mob, weapon, power, weapon);
        shootProjectile(arrow, target, 1.6F);
        playSound(SoundEvents.ARROW_SHOOT);
    }

    private void throwTrident(LivingEntity target) {
        ItemStack tridentStack = this.mob.getItemInHand(this.weaponHand);
        ThrownTrident trident = new ThrownTrident(this.mob.level(), this.mob, tridentStack.copy());
        shootProjectile(trident, target, 1.6F);
        playSound(SoundEvents.TRIDENT_THROW.value());
    }

    /**
     * 通用投掷物发射算法修正
     */
    private void shootProjectile(Projectile projectile, LivingEntity target, float velocity) {
        double dX = target.getX() - this.mob.getX();
        double dY = target.getY(0.33D) - projectile.getY();
        double dZ = target.getZ() - this.mob.getZ();
        double horizDist = Math.sqrt(dX * dX + dZ * dZ);

        // 1.21.1 建议使用 shoot 方法，并稍微补偿重力产生的下坠
        projectile.shoot(dX, dY + horizDist * 0.15D, dZ, velocity, 1.0F);
        this.mob.level().addFreshEntity(projectile);
    }

    private void playSound(net.minecraft.sounds.SoundEvent sound) {
        this.mob.level().playSound(null, this.mob.getX(), this.mob.getY(), this.mob.getZ(),
                sound, SoundSource.HOSTILE, 1.0F, 1.0F / (this.mob.getRandom().nextFloat() * 0.4F + 0.8F));
    }

    private void resetAttackState() {
        this.attackTime = 0;
        this.isCharging = false;
        this.isReloading = false;
        this.mob.stopUsingItem();
    }

    private void resetChargingState() {
        this.isCharging = false;
        this.isReloading = false;
        this.mob.stopUsingItem();
    }

    @Override
    public void stop() {
        this.mob.setAggressive(false);
        resetChargingState();
    }

    private enum RangedWeaponType {
        BOW, CROSSBOW, TRIDENT, THROWABLE
    }
}