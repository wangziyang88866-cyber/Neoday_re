package com.endofdays_re.level.goal.brains;

import com.endofdays_re.config.ConfigData;
import com.endofdays_re.level.register.ModMemoryModuleTypes;
import com.endofdays_re.utils.ModUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.endofdays_re.config.ConfigData.isDayEnable;
import static com.endofdays_re.config.ConfigData.isModeEnable;

public class FollowBreakBlockBehavior extends Behavior<Mob> {
    // --- 静态优化：全局锁定机制 ---
    // 使用全局弱引用/定时清理的锁，防止多个怪物同时盯上同一个方块或箱子，避免hasEquipMemory的全表扫描
    private static final Map<BlockPos, Long> GLOBAL_BLOCK_LOCKS = new ConcurrentHashMap<>();
    private static final int LOCK_TIMEOUT_TICKS = 200; // 10秒后锁自动失效

    private final int searchRadius;
    private final double speed;
    // 优化：将List改为HashSet，查询复杂度从 O(n) 降至 O(1)
    private final Set<String> targetBlockSet = new HashSet<>();
    private final Map<BlockPos, Long> recentFailedTargets = new HashMap<>();
    private final Map<BlockPos, Integer> cachedBreakingTicks = new HashMap<>();
    private long lastScanTime = 0;
    private BlockPos targetPos;
    private int breakingTick = 0;
    private int tickToBreak = 0;
    private int prevBreakProgress = -1;
    private BlockState blockState;

    // 路径计算节流：不要每tick都重新计算路径
    private int pathRetryTimer = 0;

    public FollowBreakBlockBehavior(int searchRadius, double speed, String... targetBlockIDs) {
        super(Map.of(ModMemoryModuleTypes.TARGET_BLOCK_POS.get(), MemoryStatus.REGISTERED));
        this.searchRadius = searchRadius;
        this.speed = speed;

        for (String targetBlockID : targetBlockIDs) {
            targetBlockSet.addAll(ModUtils.getBlocksByIDOrRegex(targetBlockID));
        }
    }

    public static boolean isLootChest(ServerLevel world, BlockPos pos) {
        BlockEntity tileEntity = world.getBlockEntity(pos);
        if (tileEntity instanceof RandomizableContainerBlockEntity lootable) {
            return lootable.getLootTable() != null;
        }

        return false;
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, Mob mob) {
        if (!isDayEnable("equip") && !isModeEnable("equip_enable")) return false;

        // 1. 分帧处理：分散不同怪物的扫描压力，避免所有机器人在同一刻扫描方块
        if (level.getGameTime() % 5 != mob.getId() % 5) return false;

        if (!mob.getPassengers().isEmpty() || mob.isVehicle()) return false;

        // 优先处理装备目标
        if (mob.getBrain().hasMemoryValue(ModMemoryModuleTypes.TARGET_BLOCK_POS_ARRMOR.get())) return false;

        // 检查现有内存目标
        Optional<BlockPos> brainMemory = mob.getBrain().getMemory(ModMemoryModuleTypes.TARGET_BLOCK_POS.get());
        if (brainMemory.isPresent()) {
            BlockPos pos = brainMemory.get();
            return !isPositionLockedByOthers(pos, mob);
        }

        // 扫描冷却
        if (ConfigData.commonConfigData == null) return false;
        long gameTime = level.getGameTime();
        int scanInterval = ConfigData.commonConfigData.follow_block_scan_interval * 20;
        if (gameTime - lastScanTime < scanInterval) return false;
        lastScanTime = gameTime;

        // 2. 优化后的方块查找
        Optional<BlockPos> target = BlockPos.findClosestMatch(
                mob.blockPosition(),
                searchRadius,
                Math.min(4, (int) mob.getBbHeight()), // 减小垂直搜索范围
                pos -> {
                    // 基础过滤：是否在黑名单或已被全局锁定
                    if (gameTime - recentFailedTargets.getOrDefault(pos, 0L) < 600) return false; // 路径失败黑名单30秒
                    if (isPositionLockedByOthers(pos, mob)) return false;

                    BlockState state = level.getBlockState(pos);
                    if (state.isAir()) return false;

                    // O(1) 快速匹配目标方块
                    if (!targetBlockSet.contains(ModUtils.getBlockID(state.getBlock()))) return false;

                    // 战利品箱特殊检查
                    if (state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST)) {
                        return !isLootChest(level, pos);
                    }

                    return true;
                }
        );

        target.ifPresent(pos -> {
            mob.getBrain().setMemory(ModMemoryModuleTypes.TARGET_BLOCK_POS.get(), pos.immutable());
            GLOBAL_BLOCK_LOCKS.put(pos.immutable(), gameTime); // 锁定目标
        });

        return target.isPresent() && isDayEnable("break_target_block") && isModeEnable("break_target_block");
    }

    @Override
    protected boolean canStillUse(@NotNull ServerLevel level, Mob mob, long gameTime) {
        if (targetPos == null) return false;
        if (isPositionLockedByOthers(targetPos, mob)) return false;
        return mob.getBrain().getMemory(ModMemoryModuleTypes.TARGET_BLOCK_POS.get()).isPresent()
                && isDayEnable("break_target_block") && isModeEnable("break_target_block");
    }

    @Override
    protected void start(@NotNull ServerLevel level, Mob mob, long gameTime) {
        mob.getBrain().getMemory(ModMemoryModuleTypes.TARGET_BLOCK_POS.get())
                .ifPresent(pos -> {
                    this.targetPos = pos;
                    initBlockBreak(level, mob, pos);
                });
    }

    // --- 核心工具方法优化 ---

    @Override
    protected void tick(@NotNull ServerLevel level, @NotNull Mob mob, long gameTime) {
        if (targetPos == null) return;

        if (level.isEmptyBlock(targetPos)) {
            clearCurrentTarget(mob);
            return;
        }

        // 3. 性能优化：距离判断与路径节流
        double reachSq = getReachDistanceSq(mob);
        double distSq = mob.distanceToSqr(Vec3.atCenterOf(targetPos));

        if (distSq > reachSq) {
            // 每 10 ticks 重新尝试寻路，而不是每 tick
            if (--pathRetryTimer <= 0) {
                pathRetryTimer = 10;
                if (!mob.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, speed)) {
                    // 寻路失败，加入长期黑名单
                    recentFailedTargets.put(targetPos.immutable(), gameTime);
                    clearCurrentTarget(mob);
                    return;
                }
            }
            mob.getLookControl().setLookAt(Vec3.atCenterOf(targetPos).add(0, 0.5, 0));
            return;
        }

        // 到达位置，开始破坏逻辑
        mob.getLookControl().setLookAt(Vec3.atCenterOf(targetPos).add(0, 0.5, 0));

        if (blockState == null) {
            blockState = level.getBlockState(targetPos);
            tickToBreak = computeTickToBreak(level, mob, targetPos, blockState);
            breakingTick = cachedBreakingTicks.getOrDefault(targetPos, 0);
        }

        breakingTick++;
        int progress = (int) ((float) breakingTick / tickToBreak * 10);
        if (progress != prevBreakProgress) {
            prevBreakProgress = progress;
            level.destroyBlockProgress(mob.getId(), targetPos, progress);
        }

        if (breakingTick % 6 == 0) mob.swing(InteractionHand.MAIN_HAND);

        if (breakingTick >= tickToBreak) {
            if (EventHooks.onEntityDestroyBlock(mob, targetPos, blockState) && level.destroyBlock(targetPos, false, mob)) {
                dropBlockLoot(level, mob);
            }
            clearCurrentTarget(mob);
        }
    }

    private void clearCurrentTarget(Mob mob) {
        if (targetPos != null) {
            GLOBAL_BLOCK_LOCKS.remove(targetPos);
            cachedBreakingTicks.remove(targetPos);
        }
        mob.getBrain().eraseMemory(ModMemoryModuleTypes.TARGET_BLOCK_POS.get());
        saveAndResetProgress();
    }

    private boolean isPositionLockedByOthers(BlockPos pos, Mob mob) {
        Long lockTime = GLOBAL_BLOCK_LOCKS.get(pos);
        if (lockTime == null) return false;
        // 如果锁已经过期（比如锁定它的怪物死了），自动释放
        if (mob.level().getGameTime() - lockTime > LOCK_TIMEOUT_TICKS) {
            GLOBAL_BLOCK_LOCKS.remove(pos);
            return false;
        }
        return true;
    }

    private double getReachDistanceSq(Mob mob) {
        // 使用 getAttribute 检查是否存在该属性实例
        var entityReach = mob.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        var blockReach = mob.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);

        // 如果属性存在则取其值，否则提供默认回退值 (4.5 是生存模式默认交互距离)
        double reachVal = entityReach != null ? entityReach.getValue() : 4.5;
        double blockVal = blockReach != null ? blockReach.getValue() : 4.5;

        double maxReach = Math.max(reachVal, blockVal);
        return maxReach * maxReach;
    }

    private void initBlockBreak(ServerLevel level, Mob mob, BlockPos pos) {
        this.blockState = level.getBlockState(pos);
        this.tickToBreak = computeTickToBreak(level, mob, pos, blockState);
        this.breakingTick = cachedBreakingTicks.getOrDefault(pos, 0);
        this.prevBreakProgress = -1;
        this.pathRetryTimer = 0;
    }

    private void saveAndResetProgress() {
        targetPos = null;
        blockState = null;
        breakingTick = 0;
        tickToBreak = 0;
        prevBreakProgress = -1;
    }

    private int computeTickToBreak(ServerLevel level, Mob mob, BlockPos pos, BlockState state) {
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness <= 0) return 1;
        float digSpeed = getDigSpeed(mob, state);
        return Math.max(1, (int) (hardness / digSpeed * 20));
    }

    private float getDigSpeed(Mob mob, BlockState state) {
        ItemStack tool = mob.getMainHandItem();
        float speed = tool.getDestroySpeed(state);
        int effLevel = EnchantmentHelper.getItemEnchantmentLevel(
                mob.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
                        .getOrThrow(Enchantments.EFFICIENCY),
                tool
        );

        // 3. 计算加成 (逻辑同原版：等级^2 + 1)
        if (effLevel > 0 && !tool.isEmpty()) {
            speed += (float) (effLevel * effLevel + 1);
        }

        if (mob.hasEffect(MobEffects.DIG_SPEED)) {
            speed *= 1.0F + (float) (Objects.requireNonNull(mob.getEffect(MobEffects.DIG_SPEED)).getAmplifier() + 1) * 0.2F;
        }

        return speed;
    }

    private void dropBlockLoot(ServerLevel level, Mob mob) {
        if (blockState == null || targetPos == null) return;
        blockState.getDrops(new LootParams.Builder(level)
                        .withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(targetPos))
                        .withParameter(LootContextParams.TOOL, mob.getMainHandItem())
                        .withOptionalParameter(LootContextParams.THIS_ENTITY, mob))
                .forEach(stack -> level.addFreshEntity(new ItemEntity(level, targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, stack)));
    }
}
