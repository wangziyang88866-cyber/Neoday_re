package com.endofdays_re.level.goal;

import com.endofdays_re.utils.BlockPacket;
import com.endofdays_re.utils.PathFinder;
import com.endofdays_re.utils.type.BlockKind;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Drowned;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.AbstractCauldronBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.event.EventHooks;

import java.util.EnumSet;

import static com.endofdays_re.level.register.RegisterEntityAttributes.BUILD_SPEED;

public class PathBuildingGoal extends Goal {
    /*
     * 相对标准原版破坏进度的加速倍率。
     * 它不依赖 BUILD_SPEED，以免放置速度配置意外让方块一 tick 消失。
     */
    private static final double FAST_BREAK_MULTIPLIER = 8.0D;
    private static final int MIN_BREAK_TICKS = 2;
    private static final int MAX_WORK_DISTANCE_SQR = 64 * 64;

    private final PathfinderMob zombie;
    private final PathFinder PATH_FINDER;
    private final Block block;

    public long lastStopBuildingTime = 0L;
    public long lastPlaceBlockTime = 0L;
    public long lastHurtTime = 0L;
    public long banPathFinderTime = 0L;
    public boolean canPlaceBlock = true;
    public boolean canBreakBlock = true;

    private boolean isDone = true;
    private boolean isWaitingToBuild;
    private long startBuildingWaitTime = -1000L;
    private BlockPos pendingTargetPos;
    private BlockPos selfPos = BlockPos.ZERO;

    private BlockPos currentBreakingPos;
    private BlockState currentBreakingState;
    private int breakingTicks;
    private int ticksToBreak;
    private int lastBreakProgress = -1;

    public PathBuildingGoal(PathfinderMob zombie, Block block) {
        this.zombie = zombie;
        this.block = block;

        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));

        this.PATH_FINDER = new PathFinder((oldStartPos, newStartPos) -> {
            if (!this.zombie.isAlive() || this.isDone) {
                return;
            }

            this.selfPos = newStartPos.immutable();

            Path path = this.zombie.getNavigation().createPath(newStartPos, 0);
            if (path != null) {
                this.zombie.getNavigation().moveTo(
                        path,
                        getMovementSpeed()
                );
            }
        }, hash -> {
            boolean completed = true;

            for (BlockPacket packet : hash.values()) {
                if (packet == null || packet.isEmpty()) {
                    continue;
                }

                BlockState state = this.zombie.level().getBlockState(packet.blockPos);
                if (!isBlockDone(state, packet.blockKind, packet.blockPos)) {
                    completed = false;
                    break;
                }
            }

            return completed;
        });
    }

    /*
     * 保持原公开接口。
     * 不再先让僵尸随机走开；这会使僵尸离玩家更远，也容易让路径计划失效。
     */
    public void triggerBuildSequence(BlockPos targetPos) {
        if (targetPos == null
                || !isDone
                || isWaitingToBuild
                || !canUsePathBuilder()) {
            return;
        }

        pendingTargetPos = targetPos.immutable();
        isWaitingToBuild = true;
        startBuildingWaitTime = zombie.level().getGameTime();
    }

    public boolean isWorking() {
        return !isDone || isWaitingToBuild;
    }

    public boolean canUsePathBuilder() {
        return banPathFinderTime <= 0L
                && zombie.level().getGameTime() - lastHurtTime >= 30L;
    }

    @Override
    public boolean canUse() {
        if (!isWorking() || !canUsePathBuilder()) {
            return false;
        }

        if (zombie instanceof Drowned drowned
                && !drowned.level().isWaterAt(drowned.blockPosition())
                && !drowned.getNavigation().canFloat()) {
            return false;
        }

        /*
         * 清障行为不依赖持有方块。
         * 因此建造僵尸即使方块耗尽，仍可沿已有路径破坏挡路方块。
         */
        return canBreakBlock || hasBuildBlock();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public void tick() {
        if (banPathFinderTime > 0L) {
            banPathFinderTime--;
        }

        if (!(zombie.level() instanceof ServerLevel level)) {
            stopBuild();
            return;
        }

        if (zombie.getTarget() == null || !zombie.getTarget().isAlive()) {
            stopBuild();
            return;
        }

        if (zombie.distanceToSqr(zombie.getTarget()) > MAX_WORK_DISTANCE_SQR) {
            stopBuild();
            return;
        }

        if (shouldStopBecauseTargetIsReachable()) {
            stopBuild();
            return;
        }

        if (isWaitingToBuild) {
            /*
             * 保留极短的启动延迟，给其他高优先级 Goal 一次切换机会。
             * 2 tick 不会造成可感知的迟滞。
             */
            if (level.getGameTime() - startBuildingWaitTime >= 2L) {
                isWaitingToBuild = false;

                if (pendingTargetPos != null) {
                    startBuild(pendingTargetPos);
                } else {
                    stopBuild();
                }
            }

            return;
        }

        if (currentBreakingPos != null) {
            processBreaking(level);
            return;
        }

        if (isDone || !canUsePathBuilder()) {
            return;
        }

        if (!zombie.getEyePosition().closerThan(Vec3.atCenterOf(selfPos), 10.0D)) {
            stopBuild();
            return;
        }

        processPathStep(level);
    }

    private void processPathStep(ServerLevel level) {
        /*
         * 每 tick 最多跳过 4 个已完成节点；
         * 足以处理连续空气，同时避免大路径在单 tick 内占用太多时间。
         */
        for (int i = 0; i < 4; i++) {
            BlockPacket packet = PATH_FINDER.getBlock();

            if (packet == null || packet.isEmpty()) {
                stopBuild();
                return;
            }

            BlockPos targetPos = packet.blockPos;
            BlockState state = level.getBlockState(targetPos);

            if (isBlockDone(state, packet.blockKind, targetPos)) {
                if (zombie.getEyePosition().closerThan(
                        Vec3.atCenterOf(selfPos),
                        2.5D
                )) {
                    PATH_FINDER.next();
                    continue;
                }

                moveToCurrentPathNode();
                return;
            }

            tryToFinishBlock(level, packet, state);
            return;
        }
    }

    private void moveToCurrentPathNode() {
        if (!zombie.getNavigation().isDone()) {
            return;
        }

        Vec3 nodeCenter = Vec3.atCenterOf(selfPos);
        zombie.getNavigation().moveTo(
                nodeCenter.x,
                nodeCenter.y,
                nodeCenter.z,
                getMovementSpeed()
        );
    }

    private void tryToFinishBlock(
            ServerLevel level,
            BlockPacket packet,
            BlockState currentState
    ) {
        BlockPos targetPos = packet.blockPos;

        if (!zombie.blockPosition().closerThan(targetPos, 4.5D)) {
            moveToCurrentPathNode();
            return;
        }

        if (packet.blockKind == BlockKind.BLOCK) {
            handleRequiredPlacement(level, targetPos, currentState);
            return;
        }

        /*
         * BlockKind.AIR 表示该位置应该被清空。
         * 若它尚未完成，说明这里存在障碍，应优先快速破坏。
         */
        if (canBreakState(currentState, targetPos)) {
            startBreakingBlock(targetPos, currentState);
        } else {
            stopBuild();
        }
    }

    private void handleRequiredPlacement(
            ServerLevel level,
            BlockPos targetPos,
            BlockState currentState
    ) {
        if (isPlaceable(currentState)) {
            /*
             * 有实体在位置内时，只等待，不破坏。
             * 旧版会把这种情况转成破坏操作，容易误伤路径或产生卡循环。
             */
            if (!level.noCollision(null, new AABB(targetPos))) {
                return;
            }

            if (!hasBuildBlock() || !canPlaceBlock) {
                stopBuild();
                return;
            }

            placeBlock(targetPos);
            return;
        }

        if (canBreakState(currentState, targetPos)) {
            startBreakingBlock(targetPos, currentState);
        } else {
            stopBuild();
        }
    }

    private void startBuild(BlockPos targetPos) {
        selfPos = zombie.getOnPos().above().immutable();
        PATH_FINDER.start(selfPos, targetPos.above());

        isDone = false;
        pendingTargetPos = null;
    }

    public void stopBuild() {
        if (!isDone || isWaitingToBuild || currentBreakingPos != null) {
            PATH_FINDER.stop();
            clearBreaking();

            isDone = true;
            isWaitingToBuild = false;
            pendingTargetPos = null;
            lastStopBuildingTime = zombie.level().getGameTime();
        }
    }

    private void startBreakingBlock(BlockPos pos, BlockState state) {
        if (currentBreakingPos != null || !canBreakState(state, pos)) {
            return;
        }

        currentBreakingPos = pos.immutable();
        currentBreakingState = state;
        breakingTicks = 0;
        ticksToBreak = computeTicksToBreak(pos, state);
        lastBreakProgress = -1;
    }

    private void processBreaking(ServerLevel level) {
        BlockPos pos = currentBreakingPos;
        BlockState currentState = level.getBlockState(pos);

        if (currentState.isAir()) {
            clearBreaking();
            PATH_FINDER.next();
            return;
        }

        if (currentState != currentBreakingState
                || !canBreakState(currentState, pos)) {
            clearBreaking();
            return;
        }

        breakingTicks++;

        int progress = Mth.clamp(
                (int) ((float) breakingTicks / ticksToBreak * 10.0F),
                0,
                9
        );

        if (progress != lastBreakProgress) {
            lastBreakProgress = progress;
            level.destroyBlockProgress(zombie.getId(), pos, progress);
        }

        zombie.getLookControl().setLookAt(
                pos.getX() + 0.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D
        );

        if (breakingTicks % 4 == 0) {
            zombie.swing(InteractionHand.MAIN_HAND);

            SoundType soundType = currentState.getSoundType(level, pos, zombie);
            level.playSound(
                    null,
                    pos,
                    soundType.getHitSound(),
                    SoundSource.BLOCKS,
                    (soundType.getVolume() + 1.0F) / 8.0F,
                    soundType.getPitch() * 0.5F
            );
        }

        if (breakingTicks < ticksToBreak) {
            return;
        }

        boolean destroyed = EventHooks.onEntityDestroyBlock(zombie, pos, currentState)
                && level.destroyBlock(pos, false, zombie);

        clearBreaking();

        if (destroyed) {
            zombie.swing(InteractionHand.MAIN_HAND);
            PATH_FINDER.next();
        } else {
            /*
             * Forge 事件取消、领地保护或方块状态变化时立即终止，
             * 避免每 tick 重复尝试同一方块。
             */
            stopBuild();
        }
    }

    private void clearBreaking() {
        if (currentBreakingPos != null) {
            zombie.level().destroyBlockProgress(
                    zombie.getId(),
                    currentBreakingPos,
                    -1
            );
        }

        currentBreakingPos = null;
        currentBreakingState = null;
        breakingTicks = 0;
        ticksToBreak = 0;
        lastBreakProgress = -1;
    }

    private int computeTicksToBreak(BlockPos pos, BlockState state) {
        float hardness = state.getDestroySpeed(zombie.level(), pos);

        if (hardness == 0.0F) {
            return 1;
        }

        if (hardness < 0.0F) {
            return Integer.MAX_VALUE;
        }

        ItemStack tool = getBreakingTool();
        float speed = tool.isEmpty() ? 1.0F : tool.getDestroySpeed(state);

        if (speed > 1.0F && !tool.isEmpty()) {
            var enchantmentRegistry = zombie.registryAccess()
                    .lookupOrThrow(Registries.ENCHANTMENT);

            var efficiency = enchantmentRegistry.getOrThrow(
                    Enchantments.EFFICIENCY
            );

            int efficiencyLevel = EnchantmentHelper.getItemEnchantmentLevel(
                    efficiency,
                    tool
            );

            if (efficiencyLevel > 0) {
                speed += efficiencyLevel * efficiencyLevel + 1.0F;
            }
        }

        boolean correctTool = !tool.isEmpty()
                && tool.isCorrectToolForDrops(state);

        double damagePerTick = speed
                * FAST_BREAK_MULTIPLIER
                / hardness
                / (correctTool ? 30.0D : 100.0D);

        if (!Double.isFinite(damagePerTick) || damagePerTick <= 0.0D) {
            return Integer.MAX_VALUE;
        }

        return Math.max(
                MIN_BREAK_TICKS,
                Mth.ceil(1.0D / damagePerTick)
        );
    }

    private ItemStack getBreakingTool() {
        ItemStack mainHand = zombie.getMainHandItem();
        if (mainHand.getItem() instanceof DiggerItem) {
            return mainHand;
        }

        ItemStack offHand = zombie.getOffhandItem();
        if (offHand.getItem() instanceof DiggerItem) {
            return offHand;
        }

        return ItemStack.EMPTY;
    }

    private void placeBlock(BlockPos pos) {
        ItemStack buildStack = getBuildStack();
        if (buildStack.isEmpty()) {
            return;
        }

        long currentTime = zombie.level().getGameTime();
        if (currentTime - lastPlaceBlockTime < getBuildCooldown()) {
            return;
        }

        BlockState stateToPlace = block.defaultBlockState();

        if (!zombie.level().setBlock(pos, stateToPlace, 3)) {
            return;
        }

        lastPlaceBlockTime = currentTime;
        zombie.swing(InteractionHand.OFF_HAND);

        SoundType soundType = stateToPlace.getSoundType();
        zombie.level().playSound(
                null,
                pos,
                soundType.getPlaceSound(),
                (SoundSource) SoundSource.BLOCKS,
                (soundType.getVolume() + 1.0F) / 2.0F,
                soundType.getPitch() * 0.8F
        );
    }

    private double getBuildCooldown() {
        /*
         * BUILD_SPEED 越高，冷却越低。
         * 默认值为 1 时是 12 tick；数值为 2 时是 6 tick。
         */
        double buildSpeed = zombie.getAttributeValue(BUILD_SPEED);

        if (buildSpeed <= 0.0D) {
            buildSpeed = 1.0D;
        }

        return Math.max(1.0D, 12.0D / buildSpeed);
    }

    private double getMovementSpeed() {
        double movementSpeed = zombie.getAttributeValue(Attributes.MOVEMENT_SPEED);
        return Math.max(0.1D, movementSpeed * 1.15D);
    }

    private boolean hasBuildBlock() {
        return !getBuildStack().isEmpty();
    }

    private ItemStack getBuildStack() {
        ItemStack offHand = zombie.getOffhandItem();
        if (offHand.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() == block) {
            return offHand;
        }

        ItemStack mainHand = zombie.getMainHandItem();
        if (mainHand.getItem() instanceof BlockItem blockItem
                && blockItem.getBlock() == block) {
            return mainHand;
        }

        return ItemStack.EMPTY;
    }

    private boolean canBreakState(BlockState state, BlockPos pos) {
        if (!canBreakBlock
                || state.isAir()
                || state.hasBlockEntity()
                || zombie.level().isOutsideBuildHeight(pos)) {
            return false;
        }

        return state.getDestroySpeed(zombie.level(), pos) >= 0.0F;
    }

    private boolean isPlaceable(BlockState state) {
        return state.isAir()
                || state.canBeReplaced()
                || state.getBlock() instanceof LiquidBlock
                || state.getBlock() instanceof AbstractCauldronBlock;
    }

    private boolean isBlockDone(
            BlockState state,
            BlockKind kind,
            BlockPos pos
    ) {
        if (kind == BlockKind.AIR) {
            return isPlaceable(state)
                    && !state.is(Blocks.POWDER_SNOW_CAULDRON);
        }

        VoxelShape collisionShape = state.getCollisionShape(
                zombie.level(),
                pos,
                CollisionContext.of(zombie)
        );

        return !collisionShape.isEmpty()
                && Block.isShapeFullBlock(collisionShape);
    }

    private boolean shouldStopBecauseTargetIsReachable() {
        Vec3 targetPos = zombie.getTarget().position();
        double horizontalDistanceSqr = zombie.distanceToSqr(
                targetPos.x,
                zombie.getY(),
                targetPos.z
        );

        double verticalDifference = zombie.getY() - targetPos.y;

        return horizontalDistanceSqr < 12.25D
                && verticalDifference >= 0.0D;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}