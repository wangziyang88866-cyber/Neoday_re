package com.endofdays_re.level.goal;

import com.endofdays_re.level.register.ModMemoryModuleTypes;
import com.endofdays_re.level.register.RegisterEntityAttributes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class BreakerBlockGoal extends Goal {
    private static final double BREAK_REACH_SQR = 16.0D;
    private static final double CLOSE_TO_TARGET_SQR = 12.25D;
    private static final double FAST_BREAK_MULTIPLIER = 8.0D;

    private static final int MOVE_RETRY_TICKS = 10;
    private static final int CLOSE_CLEARANCE_DELAY_TICKS = 40;
    private static final int MAX_CLOSE_CLEARANCE_BLOCKS = 3;
    private static final int FAILED_BLOCK_COOLDOWN_TICKS = 80;
    private static final int MAX_FAILED_BLOCKS = 8;

    private final Mob miner;
    private final double maxDistanceFromTargetSqr;
    private final boolean toolOnly;
    private final boolean properToolOnly;
    private final boolean properToolRequired;
    private final boolean requireLineOfSight;
    private final Collection<String> blacklistedBlocks;
    private final Map<BlockPos, Integer> failedBlocks = new HashMap<>();

    private LivingEntity target;
    private LivingEntity trackedTarget;
    private boolean targetDamaged;
    private int closeWithoutDamageSince = -1;
    private int clearanceBlocksBroken;

    private BlockPos breakingPos;
    private BlockState breakingState;
    private int breakingTicks;
    private int ticksToBreak;
    private int lastBreakProgress = -1;
    private int lastMoveTick = -10_000;

    private Holder<Enchantment> aquaAffinity;
    private Holder<Enchantment> efficiency;

    public BreakerBlockGoal(
            Mob miner,
            double maxDistanceFromTarget,
            boolean toolOnly,
            boolean properToolOnly,
            boolean properToolRequired,
            ItemStack allocationTool,
            boolean thereLineOfSight,
            Collection<String> blacklistedBlocks
    ) {
        this.miner = miner;
        this.maxDistanceFromTargetSqr = maxDistanceFromTarget == 0.0D
                ? 4096.0D
                : maxDistanceFromTarget * maxDistanceFromTarget;
        this.toolOnly = toolOnly;
        this.properToolOnly = properToolOnly;
        this.properToolRequired = properToolRequired;
        this.requireLineOfSight = thereLineOfSight;
        this.blacklistedBlocks = blacklistedBlocks == null
                ? Collections.emptySet()
                : blacklistedBlocks;

        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));

        if (toolOnly
                && allocationTool != null
                && !allocationTool.isEmpty()
                && miner.getOffhandItem().isEmpty()) {
            miner.setItemSlot(EquipmentSlot.OFFHAND, allocationTool);
            miner.setDropChance(EquipmentSlot.OFFHAND, -1.0F);
        }
    }

    public BreakerBlockGoal(
            Mob miner,
            double maxDistanceFromTarget,
            boolean toolOnly,
            boolean properToolOnly,
            boolean properToolRequired,
            boolean thereLineOfSight
    ) {
        this(
                miner,
                maxDistanceFromTarget,
                toolOnly,
                properToolOnly,
                properToolRequired,
                null,
                thereLineOfSight,
                Collections.emptySet()
        );
    }

    @Override
    public boolean canUse() {
        LivingEntity candidate = miner.getTarget();

        if (!canOperateOn(candidate)) {
            return false;
        }

        updateTargetTracking(candidate);

        if (candidate != null) {
            return (miner.getNavigation().isDone()
                    && findDirectObstacle(candidate) != null)
                    || canUseCloseClearance(candidate);
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        LivingEntity candidate = miner.getTarget();

        if (!canOperateOn(candidate) || candidate != target) {
            return false;
        }

        if (breakingPos != null) {
            return true;
        }

        if (candidate != null) {
            return findDirectObstacle(candidate) != null
                    || canUseCloseClearance(candidate);
        }
        return false;
    }

    @Override
    public void start() {
        target = miner.getTarget();

        if (target == null) {
            return;
        }

        updateTargetTracking(target);
        lastMoveTick = miner.tickCount - MOVE_RETRY_TICKS;

        BlockPos obstacle = findDirectObstacle(target);

        if (obstacle != null) {
            beginBreaking(obstacle);
            moveNearBlock(obstacle);
        }
    }

    @Override
    public void tick() {
        if (!canOperateOn(target) || miner.getTarget() != target) {
            return;
        }

        updateTargetTracking(target);

        if (breakingPos == null) {
            BlockPos obstacle = findDirectObstacle(target);

            if (obstacle == null && canUseCloseClearance(target)) {
                obstacle = findCloseClearanceBlock();
            }

            if (obstacle == null) {
                return;
            }

            beginBreaking(obstacle);
        }

        tickBreaking();
    }

    private boolean canOperateOn(LivingEntity candidate) {
        if (miner.getVehicle() != null || hasTargetBlockMemory()) {
            return false;
        }

        if (toolOnly
                && !(miner.getOffhandItem().getItem() instanceof DiggerItem)) {
            return false;
        }

        if (candidate == null || !candidate.isAlive()) {
            return false;
        }

        if (candidate instanceof ServerPlayer player && player.isCreative()) {
            return false;
        }

        return miner.distanceToSqr(candidate) < maxDistanceFromTargetSqr;
    }

    private BlockPos findDirectObstacle(LivingEntity candidate) {
        Vec3 start = miner.getEyePosition();

        BlockPos hit = raycastBreakable(start, candidate.getEyePosition());

        if (hit != null) {
            return hit;
        }

        return raycastBreakable(
                start,
                candidate.position().add(
                        0.0D,
                        candidate.getBbHeight() * 0.5D,
                        0.0D
                )
        );
    }

    private BlockPos raycastBreakable(Vec3 start, Vec3 end) {
        BlockHitResult hit = miner.level().clip(new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                miner
        ));

        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }

        BlockPos pos = hit.getBlockPos();
        BlockState state = miner.level().getBlockState(pos);

        return canBreakState(pos, state) ? pos : null;
    }

    private boolean canUseCloseClearance(LivingEntity candidate) {
        if (targetDamaged
                || clearanceBlocksBroken >= MAX_CLOSE_CLEARANCE_BLOCKS) {
            return false;
        }

        if (miner.distanceToSqr(candidate) > CLOSE_TO_TARGET_SQR
                || !miner.getNavigation().isDone()) {
            return false;
        }

        if (requireLineOfSight && !miner.hasLineOfSight(candidate)) {
            return false;
        }

        return closeWithoutDamageSince >= 0
                && miner.tickCount - closeWithoutDamageSince
                >= CLOSE_CLEARANCE_DELAY_TICKS
                && findCloseClearanceBlock() != null;
    }

    private BlockPos findCloseClearanceBlock() {
        BlockPos base = target.blockPosition();
        BlockPos best = null;
        double bestScore = Double.MAX_VALUE;

        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) {
                        continue;
                    }

                    // 不挖目标脚下的方块，避免制造坑洞。
                    if (x == 0 && z == 0 && y == -1) {
                        continue;
                    }

                    BlockPos pos = base.offset(x, y, z);
                    BlockState state = miner.level().getBlockState(pos);

                    if (!canBreakState(pos, state)) {
                        continue;
                    }

                    double score = miner.distanceToSqr(pos.getCenter())
                            + target.distanceToSqr(pos.getCenter()) * 0.5D;

                    // 优先破坏墙面高度，而不是地板或天花板。
                    if (y != 0) {
                        score += 2.0D;
                    }

                    if (score < bestScore) {
                        bestScore = score;
                        best = pos.immutable();
                    }
                }
            }
        }

        return best;
    }

    private void updateTargetTracking(LivingEntity candidate) {
        if (candidate != trackedTarget) {
            trackedTarget = candidate;
            targetDamaged = candidate.getLastHurtByMob() == miner;
            closeWithoutDamageSince = -1;
            clearanceBlocksBroken = 0;
            failedBlocks.clear();
        }

        if (candidate.getLastHurtByMob() == miner) {
            targetDamaged = true;
        }

        if (!targetDamaged
                && miner.distanceToSqr(candidate) <= CLOSE_TO_TARGET_SQR
                && miner.getNavigation().isDone()) {
            if (closeWithoutDamageSince < 0) {
                closeWithoutDamageSince = miner.tickCount;
            }
        } else {
            closeWithoutDamageSince = -1;
        }
    }

    private void moveNearBlock(BlockPos pos) {
        if (miner.tickCount - lastMoveTick < MOVE_RETRY_TICKS) {
            return;
        }

        lastMoveTick = miner.tickCount;

        Vec3 center = Vec3.atCenterOf(pos);
        Vec3 fromBlock = miner.position().subtract(center);

        if (fromBlock.lengthSqr() < 0.01D) {
            fromBlock = target.position().subtract(center);
        }

        if (fromBlock.lengthSqr() < 0.01D) {
            fromBlock = new Vec3(1.0D, 0.0D, 0.0D);
        }

        Vec3 stand = center.add(fromBlock.normalize().scale(1.5D));

        miner.getNavigation().moveTo(
                stand.x,
                stand.y,
                stand.z,
                1.0D
        );
    }

    private void beginBreaking(BlockPos pos) {
        BlockState state = miner.level().getBlockState(pos);

        if (!canBreakState(pos, state)) {
            return;
        }

        breakingPos = pos.immutable();
        breakingState = state;
        breakingTicks = 0;
        ticksToBreak = computeTicksToBreak(pos, state);
        lastBreakProgress = -1;
    }

    private void tickBreaking() {
        BlockPos pos = breakingPos;
        BlockState currentState = miner.level().getBlockState(pos);

        if (currentState.isAir()
                || currentState != breakingState
                || !canBreakState(pos, currentState)) {
            clearBreaking();
            return;
        }

        if (miner.distanceToSqr(pos.getCenter()) > BREAK_REACH_SQR) {
            moveNearBlock(pos);
            return;
        }

        miner.getLookControl().setLookAt(
                pos.getX() + 0.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D
        );

        breakingTicks++;

        int progress = Mth.clamp(
                (int) ((double) breakingTicks / ticksToBreak * 10.0D),
                0,
                9
        );

        if (progress != lastBreakProgress) {
            lastBreakProgress = progress;
            miner.level().destroyBlockProgress(
                    miner.getId(),
                    pos,
                    progress
            );
        }

        if (breakingTicks % 4 == 0) {
            miner.swing(InteractionHand.MAIN_HAND);

            SoundType sound = currentState.getSoundType(
                    miner.level(),
                    pos,
                    miner
            );

            miner.level().playSound(
                    null,
                    pos,
                    sound.getHitSound(),
                    SoundSource.BLOCKS,
                    (sound.getVolume() + 1.0F) / 8.0F,
                    sound.getPitch() * 0.5F
            );
        }

        if (breakingTicks >= ticksToBreak) {
            destroyCurrentBlock(pos, currentState);
        }
    }

    private void destroyCurrentBlock(BlockPos pos, BlockState state) {
        boolean destroyed = false;

        if (miner.level() instanceof ServerLevel level
                && EventHooks.onEntityDestroyBlock(miner, pos, state)) {
            ItemStack tool = miner.getOffhandItem();

            destroyed = level.destroyBlock(pos, false, miner);

            if (destroyed) {
                state.spawnAfterBreak(level, pos, tool, false);

                state.getDrops(new LootParams.Builder(level)
                                .withParameter(
                                        LootContextParams.ORIGIN,
                                        Vec3.atCenterOf(pos)
                                )
                                .withParameter(
                                        LootContextParams.TOOL,
                                        tool
                                )
                                .withOptionalParameter(
                                        LootContextParams.THIS_ENTITY,
                                        miner
                                ))
                        .forEach(drop -> level.addFreshEntity(
                                new ItemEntity(
                                        level,
                                        pos.getX() + 0.5D,
                                        pos.getY() + 0.5D,
                                        pos.getZ() + 0.5D,
                                        drop
                                )
                        ));
            }
        }

        if (destroyed
                && miner.distanceToSqr(target) <= CLOSE_TO_TARGET_SQR) {
            clearanceBlocksBroken++;
        }

        if (!destroyed) {
            markFailed(pos);
        }

        clearBreaking();
        lastMoveTick = miner.tickCount - MOVE_RETRY_TICKS;
    }

    private boolean canBreakState(BlockPos pos, BlockState state) {
        if (state.isAir()
                || state.hasBlockEntity()
                || state.getDestroySpeed(miner.level(), pos) < 0.0F) {
            return false;
        }

        if (isBlacklisted(state) || isRecentlyFailed(pos)) {
            return false;
        }

        return canBreakBlock(state);
    }

    private boolean canBreakBlock(BlockState state) {
        ItemStack tool = miner.getOffhandItem();

        if (properToolOnly) {
            return !tool.isEmpty() && tool.isCorrectToolForDrops(state);
        }

        return !properToolRequired
                || !state.requiresCorrectToolForDrops()
                || (!tool.isEmpty() && tool.isCorrectToolForDrops(state));
    }

    private int computeTicksToBreak(BlockPos pos, BlockState state) {
        float hardness = state.getDestroySpeed(miner.level(), pos);

        if (hardness <= 0.0F) {
            return 1;
        }

        ItemStack tool = miner.getOffhandItem();

        double damagePerTick = getDigSpeed(state)
                * Math.max(
                0.01D,
                miner.getAttributeValue(
                        RegisterEntityAttributes.BREAKER_SPEED
                )
        )
                * FAST_BREAK_MULTIPLIER
                / hardness
                / (tool.isCorrectToolForDrops(state) ? 30.0D : 100.0D);

        if (!Double.isFinite(damagePerTick) || damagePerTick <= 0.0D) {
            return Integer.MAX_VALUE;
        }

        return Math.max(1, Mth.ceil(1.0D / damagePerTick));
    }

    private float getDigSpeed(BlockState state) {
        ItemStack tool = miner.getOffhandItem();
        float speed = tool.getDestroySpeed(state);

        if (speed > 1.0F) {
            int level = EnchantmentHelper.getItemEnchantmentLevel(
                    efficiency(),
                    tool
            );

            if (level > 0) {
                speed += level * level + 1.0F;
            }
        }

        if (MobEffectUtil.hasDigSpeed(miner)) {
            speed *= 1.0F
                    + (MobEffectUtil.getDigSpeedAmplification(miner) + 1)
                    * 0.2F;
        }

        if (miner.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            var effect = miner.getEffect(MobEffects.DIG_SLOWDOWN);

            if (effect != null) {
                speed *= switch (effect.getAmplifier()) {
                    case 0 -> 0.3F;
                    case 1 -> 0.09F;
                    case 2 -> 0.0027F;
                    default -> 0.00081F;
                };
            }
        }

        if (miner.isEyeInFluid(FluidTags.WATER)
                && !hasAquaAffinity()) {
            speed /= 5.0F;
        }

        if (!miner.onGround()) {
            speed /= 5.0F;
        }

        return Math.max(speed, 0.0F);
    }

    private Holder<Enchantment> efficiency() {
        if (efficiency == null) {
            efficiency = miner.registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(Enchantments.EFFICIENCY);
        }

        return efficiency;
    }

    private boolean hasAquaAffinity() {
        if (aquaAffinity == null) {
            aquaAffinity = miner.registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(Enchantments.AQUA_AFFINITY);
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (EnchantmentHelper.getItemEnchantmentLevel(
                    aquaAffinity,
                    miner.getItemBySlot(slot)
            ) > 0) {
                return true;
            }
        }

        return false;
    }

    private boolean isBlacklisted(BlockState state) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(
                state.getBlock()
        );

        return blacklistedBlocks.contains(id.toString());
    }

    private boolean isRecentlyFailed(BlockPos pos) {
        Integer until = failedBlocks.get(pos);

        return until != null && miner.tickCount < until;
    }

    private void markFailed(BlockPos pos) {
        if (failedBlocks.size() >= MAX_FAILED_BLOCKS) {

            failedBlocks.entrySet().removeIf(blockPosIntegerEntry -> miner.tickCount >= blockPosIntegerEntry.getValue());

            if (failedBlocks.size() >= MAX_FAILED_BLOCKS) {
                failedBlocks.remove(
                        failedBlocks.keySet().iterator().next()
                );
            }
        }

        failedBlocks.put(
                pos.immutable(),
                miner.tickCount + FAILED_BLOCK_COOLDOWN_TICKS
        );
    }

    private boolean hasTargetBlockMemory() {
        return miner.getBrain().hasMemoryValue(
                ModMemoryModuleTypes.TARGET_BLOCK_POS.get()
        ) || miner.getBrain().hasMemoryValue(
                ModMemoryModuleTypes.TARGET_BLOCK_POS_ARRMOR.get()
        ) || miner.getBrain().hasMemoryValue(
                ModMemoryModuleTypes.ATTACK_TARGET_BLOCK_POS.get()
        );
    }

    private void clearBreaking() {
        if (breakingPos != null) {
            miner.level().destroyBlockProgress(
                    miner.getId(),
                    breakingPos,
                    -1
            );
        }

        breakingPos = null;
        breakingState = null;
        breakingTicks = 0;
        ticksToBreak = 0;
        lastBreakProgress = -1;
    }

    @Override
    public void stop() {
        clearBreaking();
        target = null;
        miner.getNavigation().stop();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}