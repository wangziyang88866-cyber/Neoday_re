package com.endofdays_re.level.goal;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class PotionThrowGoal extends Goal {
    private static final int MAX_COOLDOWN = 15 * 20; // 调整为15秒
    private static final int EXTENDED_COOLDOWN = 40;
    private final Mob zombie;
    private final boolean showParticles;
    private final SimpleWeightedRandomList<ItemStack> potionList;

    public PotionThrowGoal(Mob zombie, boolean showParticles) {
        this.zombie = zombie;
        this.showParticles = showParticles;
        this.potionList = buildPotionList();
    }

    private SimpleWeightedRandomList<ItemStack> buildPotionList() {
        return SimpleWeightedRandomList.<ItemStack>builder()
                .add(createCustomPotion(Potions.SWIFTNESS), 40)
                .add(createCustomPotion(Potions.STRONG_STRENGTH), 30)
                .add(createCustomPotion(Potions.INVISIBILITY), 20)
                .add(createCustomPotion(Potions.FIRE_RESISTANCE), 15)
                .add(createCustomPotion(Potions.LONG_FIRE_RESISTANCE), 10)
                .add(createCustomPotion(Potions.LONG_SLOW_FALLING), 10)
                .build();
    }

    /**
     * 1.21.1 核心修正：使用 Data Components 设置药水
     */
    private ItemStack createCustomPotion(Holder<Potion> potion) {
        ItemStack stack = new ItemStack(Items.SPLASH_POTION);

        // 使用 PotionContents 构建药水内容
        PotionContents contents = new PotionContents(potion);

        // 如果需要隐藏粒子，使用自定义构造逻辑
        // 注意：1.21 隐藏粒子通常在 EffectInstance 级别，
        // 但如果只是想标记，可以自定义组件或在渲染层处理。
        // 这里演示标准的药水设置：
        stack.set(DataComponents.POTION_CONTENTS, contents);

        return stack;
    }

    @Override
    public boolean canUse() {
        if (zombie.level().isClientSide) return false;

        int currentCooldown = getCooldown();
        if (currentCooldown > 0) {
            setCooldown(currentCooldown - 1);
            return false;
        }

        if (hasNearbyZombieJustThrown()) {
            setCooldown(EXTENDED_COOLDOWN);
            return false;
        }

        LivingEntity target = zombie.getTarget();
        if (target == null || !target.isAlive()) return false;

        return zombie.getRandom().nextFloat() < 0.25f;
    }

    @Override
    public void start() {
        setCooldown(MAX_COOLDOWN);
        setJustThrown(true);
        executeThrowLogic();
    }

    private void executeThrowLogic() {
        LivingEntity target = zombie.getTarget();
        if (target == null) return;

        // 1. 治疗逻辑
        LivingEntity healingTarget = findInjuredZombie();
        if (healingTarget != null) {
            throwPotionWrapper(Potions.LONG_REGENERATION, healingTarget, 0.8F);
            return;
        }

        // 2. 增益逻辑
        LivingEntity swiftnessTarget = findFrontlineZombie();
        if (swiftnessTarget != null) {
            // 1.21.1 获取随机值的方式
            ItemStack randomPotion = potionList.getRandomValue(zombie.getRandom())
                    .orElse(createCustomPotion(Potions.SWIFTNESS));

            float pitch = 1.0f;
            PotionContents contents = randomPotion.get(DataComponents.POTION_CONTENTS);
            if (contents != null && contents.potion().isPresent()) {
                pitch = getPitchByPotion(contents.potion().get().value());
            }

            throwPotionAtTarget(randomPotion, swiftnessTarget, pitch);
            return;
        }

        // 3. 减益逻辑
        LivingEntity enemyTarget = findEnemyTarget();
        if (enemyTarget != null) {
            throwPotionWrapper(Potions.SLOWNESS, enemyTarget, 0.6F);
        }
    }

    /**
     * 寻找敌对目标：
     * 逻辑：检查僵尸当前的目标（Target）是否有效。
     * 判定条件：目标不为空、目标存活、僵尸拥有目标的视线。
     */
    private LivingEntity findEnemyTarget() {
        LivingEntity target = zombie.getTarget();

        // 1.21.1 标准判定：
        // 使用 canAttack 判定可以自动过滤掉创造模式玩家和一些隐身状态（取决于具体的判定逻辑）
        if (target != null && target.isAlive() && zombie.canAttack(target)) {
            // 视线检查：确保药水不会砸在墙上
            if (zombie.hasLineOfSight(target)) {
                return target;
            }
        }

        return null;
    }

    private void throwPotionWrapper(Holder<Potion> potion, LivingEntity target, float soundPitch) {
        throwPotionAtTarget(createCustomPotion(potion), target, soundPitch);
    }

    private void throwPotionAtTarget(ItemStack potionStack, LivingEntity target, float soundPitch) {
        Level level = zombie.level();
        zombie.swing(InteractionHand.MAIN_HAND);

        ThrownPotion potionEntity = new ThrownPotion(level, zombie);
        potionEntity.setItem(potionStack);

        // 1.21.1 投掷逻辑：计算向量
        Vec3 targetPos = target.position();
        double dX = targetPos.x - zombie.getX();
        double dY = target.getEyeY() - 1.1 - zombie.getY();
        double dZ = targetPos.z - zombie.getZ();
        double horDist = Math.sqrt(dX * dX + dZ * dZ);

        potionEntity.shoot(dX, dY + horDist * 0.2, dZ, 0.75F, 1.0F);

        level.playSound(null, zombie.getX(), zombie.getY(), zombie.getZ(),
                SoundEvents.WITCH_THROW, SoundSource.HOSTILE, 0.5F, soundPitch);
        level.addFreshEntity(potionEntity);
    }

    // --- 辅助逻辑修正 ---

    private float getPitchByPotion(Potion potion) {
        // 使用内置比较或资源位置比较
        if (potion == Potions.SWIFTNESS.value()) return 1.0F;
        if (potion == Potions.STRONG_STRENGTH.value()) return 0.8F;
        if (potion == Potions.INVISIBILITY.value()) return 1.2F;
        return 1.0F;
    }

    private int getCooldown() {
        return zombie.getPersistentData().getInt("PotionThrowCooldown");
    }

    private void setCooldown(int cooldown) {
        zombie.getPersistentData().putInt("PotionThrowCooldown", cooldown);
    }

    private boolean hasNearbyZombieJustThrown() {
        return !zombie.level().getEntitiesOfClass(Zombie.class, zombie.getBoundingBox().inflate(8.0),
                other -> other != zombie && other.getPersistentData().getInt("JustThrownPotion") > 0).isEmpty();
    }

    private void setJustThrown(boolean justThrown) {
        if (justThrown) zombie.getPersistentData().putInt("JustThrownPotion", 20);
        else zombie.getPersistentData().remove("JustThrownPotion");
    }

    @Override
    public void tick() {
        if (!zombie.level().isClientSide) {
            int timer = zombie.getPersistentData().getInt("JustThrownPotion");
            if (timer > 0) zombie.getPersistentData().putInt("JustThrownPotion", timer - 1);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return zombie.getTarget() != null && zombie.getTarget().isAlive();
    }

    /**
     * 寻找“前线”僵尸：
     * 逻辑：在僵尸周围 15 格内，寻找一个距离敌人最近、且能看到敌人的队友僵尸。
     * 这通常用于给正在交战的队友投掷增益药水（如力量、迅捷）。
     */
    private LivingEntity findFrontlineZombie() {
        LivingEntity enemy = zombie.getTarget();
        if (enemy == null || !enemy.isAlive()) return null;

        // 获取半径 15 格内的所有僵尸
        List<Zombie> nearbyZombies = zombie.level().getEntitiesOfClass(
                Zombie.class,
                zombie.getBoundingBox().inflate(15.0),
                // 过滤条件：不是自己，且该队友能看到敌人
                other -> other != zombie && other.isAlive() && other.hasLineOfSight(enemy)
        );

        // 在符合条件的僵尸中，找到距离敌人最近的那一个
        return nearbyZombies.stream()
                .min(Comparator.comparingDouble(other -> other.distanceToSqr(enemy)))
                .orElse(null);
    }

    /**
     * 寻找受伤的僵尸：
     * 逻辑：寻找 10 格内生命值低于 50% 的队友。
     */
    private LivingEntity findInjuredZombie() {
        List<Zombie> allies = zombie.level().getEntitiesOfClass(
                Zombie.class,
                zombie.getBoundingBox().inflate(10.0),
                // 过滤条件：不是自己，且血量低于一半
                other -> other != zombie && other.isAlive() && other.getHealth() < other.getMaxHealth() * 0.5f
        );

        // 返回距离自己最近的受伤队友
        return allies.stream()
                .min(Comparator.comparingDouble(zombie::distanceToSqr))
                .orElse(null);
    }
}