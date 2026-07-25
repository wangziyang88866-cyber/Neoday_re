package com.endofdays_re.level.goal;

import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.List;

public class UltimateDispenserAttackGoal extends Goal {
    private static final SimpleWeightedRandomList<MobEffectInstance> WEIGHTED_EFFECTS =
            SimpleWeightedRandomList.<MobEffectInstance>builder()
                    .add(new MobEffectInstance(MobEffects.POISON, 200, 0), 30)
                    .add(new MobEffectInstance(MobEffects.WEAKNESS, 300, 0), 25)
                    // 1. 尝试使用 MOVEMENT_SLOWDOWN
                    .add(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1), 20)
                    .add(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0), 15)
                    // 2. 尝试使用 HARM (即时伤害)
                    .add(new MobEffectInstance(MobEffects.HARM, 1, 0), 10)
                    // 3. 尝试使用 CONFUSION (反胃)
                    .add(new MobEffectInstance(MobEffects.CONFUSION, 200, 0), 20)
                    // 4. 尝试使用 DIG_SLOWDOWN (挖掘疲劳)
                    .add(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 300, 0), 15)
                    .add(new MobEffectInstance(MobEffects.UNLUCK, 600, 0), 5)
                    .build();
    private static final List<AttackType> ATTACK_TYPES = List.of(
            new AttackType("arrow", 12, 2, 4.0, Items.ARROW, UltimateDispenserAttackGoal::shootArrow),
            new AttackType("snowball", 5, 3, 1.0, Items.SNOWBALL, UltimateDispenserAttackGoal::shootSnowball),
            new AttackType("harming_potion", 1, 0, 6.0, Items.SPLASH_POTION, UltimateDispenserAttackGoal::shootHarmfulPotion),
            new AttackType("splash_potion", 1, 0, 0.0, Items.SPLASH_POTION, UltimateDispenserAttackGoal::shootSplashPotion),
            new AttackType("trident", 1, 0, 8.0, Items.TRIDENT, UltimateDispenserAttackGoal::shootTrident),
            new AttackType("firework", 2, 4, 5.0, Items.FIREWORK_ROCKET, UltimateDispenserAttackGoal::shootFirework),
            new AttackType("egg", 4, 2, 0.0, Items.EGG, UltimateDispenserAttackGoal::shootEgg),
            new AttackType("ender_pearl", 1, 0, 5.0, Items.ENDER_PEARL, UltimateDispenserAttackGoal::shootEnderPearl),
            new AttackType("experience_bottle", 3, 3, 0.0, Items.EXPERIENCE_BOTTLE, UltimateDispenserAttackGoal::shootExperienceBottle),
            new AttackType("shulker_bullet", 1, 0, 4.0, Items.SHULKER_SHELL, UltimateDispenserAttackGoal::shootShulkerBullet),
            new AttackType("small_fireball", 1, 10, 5.0, Items.FIRE_CHARGE, UltimateDispenserAttackGoal::shootSmallFireball),
            new AttackType("dragon_fireball", 1, 30, 15.0, Items.DRAGON_BREATH, UltimateDispenserAttackGoal::shootDragonFireball),
            new AttackType("wither_skull", 1, 25, 12.0, Items.WITHER_SKELETON_SKULL, UltimateDispenserAttackGoal::shootWitherSkull),
            new AttackType("spectral_arrow", 2, 7, 5.0, Items.SPECTRAL_ARROW, UltimateDispenserAttackGoal::shootSpectralArrow),
            new AttackType("large_fireball", 1, 20, 10.0, Items.FIRE_CHARGE, UltimateDispenserAttackGoal::shootLargeFireball),
            new AttackType("tipped_arrow", 3, 6, 4.0, Items.TIPPED_ARROW, UltimateDispenserAttackGoal::shootTippedArrow)
    );
    private final Mob mob;
    private int attackTime;
    private int burstCount;

    // 1.21.1 效果名称修正
    private int burstDelay;
    private AttackType currentAttackType;

    public UltimateDispenserAttackGoal(Mob mob) {
        this.mob = mob;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    private static void shootArrow(UltimateDispenserAttackGoal goal, AttackContext ctx) {
        Arrow arrow = new Arrow(ctx.mob.level(), ctx.mob, new ItemStack(Items.ARROW), null);
        initAndShoot(arrow, ctx, 1.6F);
        arrow.setBaseDamage(ctx.type.damage);
        ctx.mob.level().addFreshEntity(arrow);
    }

    private static void shootSnowball(UltimateDispenserAttackGoal goal, AttackContext ctx) {
        Snowball snowball = new Snowball(ctx.mob.level(), ctx.mob);
        snowball.setItem(new ItemStack(Items.SNOWBALL));
        initAndShoot(snowball, ctx, 1.5F);
        ctx.mob.level().addFreshEntity(snowball);
    }

    private static void shootHarmfulPotion(UltimateDispenserAttackGoal goal, AttackContext ctx) {
        ItemStack potionStack = new ItemStack(Items.SPLASH_POTION);
        potionStack.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.HARMING));
        ThrownPotion potion = new ThrownPotion(ctx.mob.level(), ctx.mob);
        potion.setItem(potionStack);
        initAndShoot(potion, ctx, 0.9F);
        ctx.mob.level().addFreshEntity(potion);
    }

    private static void shootSplashPotion(UltimateDispenserAttackGoal goal, AttackContext ctx) {
        ItemStack potionStack = new ItemStack(Items.SPLASH_POTION);
        var p = ctx.mob.getRandom().nextBoolean() ? Potions.POISON : Potions.SLOWNESS;
        potionStack.set(DataComponents.POTION_CONTENTS, new PotionContents(p));
        ThrownPotion potion = new ThrownPotion(ctx.mob.level(), ctx.mob);
        potion.setItem(potionStack);
        initAndShoot(potion, ctx, 0.9F);
        ctx.mob.level().addFreshEntity(potion);
    }

    // ========== 发射器核心方法 (1.21.1 适配) ==========

    private static void shootTrident(UltimateDispenserAttackGoal goal, AttackContext ctx) {
        ThrownTrident trident = new ThrownTrident(ctx.mob.level(), ctx.mob, new ItemStack(Items.TRIDENT));
        initAndShoot(trident, ctx, 1.6F);
        ctx.mob.level().addFreshEntity(trident);
    }

    private static void shootFirework(UltimateDispenserAttackGoal goal, AttackContext ctx) {
        FireworkRocketEntity rocket = new FireworkRocketEntity(ctx.mob.level(), ctx.mob, ctx.shootPos.x, ctx.shootPos.y, ctx.shootPos.z, new ItemStack(Items.FIREWORK_ROCKET));
        initAndShoot(rocket, ctx, 1.5F);
        ctx.mob.level().addFreshEntity(rocket);
    }

    private static void shootEgg(UltimateDispenserAttackGoal goal, AttackContext ctx) {
        ThrownEgg egg = new ThrownEgg(ctx.mob.level(), ctx.mob);
        egg.setItem(new ItemStack(Items.EGG));
        initAndShoot(egg, ctx, 1.2F);
        ctx.mob.level().addFreshEntity(egg);
    }

    private static void shootEnderPearl(UltimateDispenserAttackGoal goal, AttackContext ctx) {
        ThrownEnderpearl pearl = new ThrownEnderpearl(ctx.mob.level(), ctx.mob);
        pearl.setItem(new ItemStack(Items.ENDER_PEARL));
        initAndShoot(pearl, ctx, 1.2F);
        ctx.mob.level().addFreshEntity(pearl);
    }

    private static void shootExperienceBottle(UltimateDispenserAttackGoal goal, AttackContext ctx) {
        ThrownExperienceBottle bottle = new ThrownExperienceBottle(ctx.mob.level(), ctx.mob);
        bottle.setItem(new ItemStack(Items.EXPERIENCE_BOTTLE));
        initAndShoot(bottle, ctx, 1.1F);
        ctx.mob.level().addFreshEntity(bottle);
    }

    private static void shootShulkerBullet(UltimateDispenserAttackGoal goal, AttackContext ctx) {
        ShulkerBullet bullet = new ShulkerBullet(ctx.mob.level(), ctx.mob, ctx.target, Direction.Axis.Y);
        bullet.setPos(ctx.shootPos.x, ctx.shootPos.y, ctx.shootPos.z);
        ctx.mob.level().addFreshEntity(bullet);
    }

    private static void shootSmallFireball(UltimateDispenserAttackGoal goal, AttackContext ctx) {
        Vec3 dir = ctx.target.getEyePosition().subtract(ctx.shootPos);
        SmallFireball fireball = new SmallFireball(ctx.mob.level(), ctx.mob, dir.normalize());
        fireball.setPos(ctx.shootPos.x, ctx.shootPos.y, ctx.shootPos.z);
        ctx.mob.level().addFreshEntity(fireball);
    }

    private static void shootLargeFireball(UltimateDispenserAttackGoal goal, AttackContext ctx) {
        Vec3 dir = ctx.target.getEyePosition().subtract(ctx.shootPos);
        LargeFireball fireball = new LargeFireball(ctx.mob.level(), ctx.mob, dir.normalize(), 2);
        fireball.setPos(ctx.shootPos.x, ctx.shootPos.y, ctx.shootPos.z);
        ctx.mob.level().addFreshEntity(fireball);
    }

    private static void shootDragonFireball(UltimateDispenserAttackGoal goal, AttackContext ctx) {
        Vec3 dir = ctx.target.getEyePosition().subtract(ctx.shootPos);
        DragonFireball fireball = new DragonFireball(ctx.mob.level(), ctx.mob, dir.normalize());
        fireball.setPos(ctx.shootPos.x, ctx.shootPos.y, ctx.shootPos.z);
        ctx.mob.level().addFreshEntity(fireball);
    }

    private static void shootWitherSkull(UltimateDispenserAttackGoal goal, AttackContext ctx) {
        Vec3 dir = ctx.target.getEyePosition().subtract(ctx.shootPos);
        WitherSkull skull = new WitherSkull(ctx.mob.level(), ctx.mob, dir.normalize());
        skull.setPos(ctx.shootPos.x, ctx.shootPos.y, ctx.shootPos.z);
        skull.setDangerous(ctx.mob.getRandom().nextFloat() < 0.15F);
        ctx.mob.level().addFreshEntity(skull);
    }

    private static void shootSpectralArrow(UltimateDispenserAttackGoal goal, AttackContext ctx) {
        SpectralArrow arrow = new SpectralArrow(ctx.mob.level(), ctx.mob, new ItemStack(Items.SPECTRAL_ARROW), null);
        initAndShoot(arrow, ctx, 1.6F);
        ctx.mob.level().addFreshEntity(arrow);
    }

    private static void shootTippedArrow(UltimateDispenserAttackGoal goal, AttackContext ctx) {
        ItemStack arrowStack = new ItemStack(Items.TIPPED_ARROW);
        Arrow arrow = new Arrow(ctx.mob.level(), ctx.mob, arrowStack, null);
        WEIGHTED_EFFECTS.getRandomValue(ctx.mob.getRandom()).ifPresent(arrow::addEffect);
        initAndShoot(arrow, ctx, 1.6F);
        ctx.mob.level().addFreshEntity(arrow);
    }

    private static void initAndShoot(Projectile p, AttackContext ctx, float speed) {
        p.setPos(ctx.shootPos.x, ctx.shootPos.y, ctx.shootPos.z);
        Vec3 dir = ctx.target.getEyePosition().subtract(ctx.shootPos);
        // 稍微向上补偿一点点重力
        double horizDist = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        p.shoot(dir.x, dir.y + horizDist * 0.1D, dir.z, speed, 1.0F);
    }

    @Override
    public boolean canUse() {
        LivingEntity target = mob.getTarget();
        return target != null && target.isAlive() && this.mob.getSensing().hasLineOfSight(target);
    }

    @Override
    public void tick() {
        LivingEntity target = mob.getTarget();
        if (target == null) return;

        mob.getLookControl().setLookAt(target, 30.0F, 30.0F);

        if (burstDelay > 0) {
            burstDelay--;
            return;
        }

        if (burstCount > 0) {
            performDispenserAttack(target);
            burstCount--;
            if (burstCount > 0) {
                burstDelay = currentAttackType.delay;
            }
            return;
        }

        if (attackTime > 0) {
            attackTime--;
            return;
        }

        if (mob.distanceToSqr(target) <= 400.0D) { // 20格范围
            startBurstAttack(target);
            attackTime = 40 + mob.getRandom().nextInt(20);
        }
    }

    private void startBurstAttack(LivingEntity target) {
        if (mob.level().isClientSide) return;
        currentAttackType = ATTACK_TYPES.get(mob.getRandom().nextInt(ATTACK_TYPES.size()));
        burstCount = currentAttackType.count;
        performDispenserAttack(target);
    }

    private void performDispenserAttack(LivingEntity target) {
        Vec3 shootPos = mob.getEyePosition().add(mob.getLookAngle().scale(0.5));
        currentAttackType.attackFunction.accept(this, new AttackContext(mob, target, shootPos, currentAttackType));
        mob.level().playSound(null, mob.getX(), mob.getY(), mob.getZ(),
                SoundEvents.DISPENSER_LAUNCH, SoundSource.HOSTILE, 1.0F, 1.0F);
    }

    @FunctionalInterface
    private interface AttackFunction {
        void accept(UltimateDispenserAttackGoal goal, AttackContext context);
    }

    private record AttackType(String name, int count, int delay, double damage, Item item,
                              AttackFunction attackFunction) {
    }

    private record AttackContext(Mob mob, LivingEntity target, Vec3 shootPos, AttackType type) {
    }
}