package com.endofdays_re.level.register.block;

import com.endofdays_re.event.data.AllSyncValue;
import com.endofdays_re.level.register.RgisterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

public class GhostSpongeWaterAirBlock extends AirBlock {
    private static final Direction[] ALL_DIRECTIONS = Direction.values();
    // 占位符存活时间（Tick），给水流物理计算留出足够的静止时间
    private static final int TICK_DELAY = 60;

    public GhostSpongeWaterAirBlock(BlockBehaviour.Properties pProperties) {
        // air() 标记为空气属性减少开销，noLootTable() 保证不掉落
        super(pProperties.air().noCollission().noOcclusion().noLootTable());
    }

    // --- 核心逻辑控制 ---

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            // 1. 立即尝试吸一次周围的水
            this.tryAbsorb(level, pos);
            // 2. 计划在一段时间后自我销毁（解决提前销毁导致的崩溃）
            level.scheduleTick(pos, this, TICK_DELAY);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            // 周围方块变动（如水流回流）时再次尝试吸水
            this.tryAbsorb(level, pos);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isClientSide) {
            // 检查半径 1 内是否有水或流动水
            if (isWaterNearby(level, pos)) {
                // 如果还有水，继续尝试吸收并再次计划 Tick，保持“幽灵”状态
                this.tryAbsorb(level, pos);
                level.scheduleTick(pos, this, TICK_DELAY);
            } else {
                // 只有周围彻底干涸，才变回普通空气
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    /**
     * 检查 3x3x3 范围内是否仍有水源或流动水
     */
    private boolean isWaterNearby(Level level, BlockPos pos) {
        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            // 跳过自身位置
            if (checkPos.equals(pos)) continue;

            FluidState fluidState = level.getFluidState(checkPos);
            if (!fluidState.isEmpty()) {
                return true;
            }
        }
        if (AllSyncValue.Instance.temperature <= 30) return false;
        return false;
    }

    private void tryAbsorb(Level level, BlockPos pos) {
        // 检查自定义配置/同步值
        if (AllSyncValue.Instance.temperature <= 30) return;

        // 1.21.1 推荐的 BFS 遍历方式
        // 参数含义：起始位置, 深度限制, 最大方块数, 相邻逻辑, 测试逻辑
        BlockPos.breadthFirstTraversal(pos, 3, 9, (currentPos, consumer) -> {
            for (Direction direction : Direction.values()) { // 建议直接用内置的 values()
                consumer.accept(currentPos.relative(direction));
            }
        }, (testPos) -> {
            if (testPos.equals(pos)) return true;

            BlockState blockstate = level.getBlockState(testPos);
            FluidState fluidstate = level.getFluidState(testPos);

            // 如果该位置没有任何流体，直接跳过
            if (fluidstate.isEmpty()) return false;

            // --- 逻辑 A：处理含水方块 (Waterlogged Blocks) ---
            // 1.21.1 中 BucketPickup 依然可用，但建议传入玩家作为参数（如果是实体触发）
            if (blockstate.getBlock() instanceof BucketPickup bucket) {
                // 参数说明：level, pos, state
                // 注意：pickupBlock 会自动处理含水状态的移除
                if (!bucket.pickupBlock(null, level, testPos, blockstate).isEmpty()) {
                    return true;
                }
            }

            // --- 逻辑 B：处理流体源或流动流体 ---
            if (blockstate.getBlock() instanceof LiquidBlock) {
                // 替换为自定义的占位方块（例如 GHOST_SPONGE）
                // Flag 2 表示：发送更新给客户端
                level.setBlock(testPos, RgisterBlock.GHOST_SPONGE.get().defaultBlockState(), 2);
            }
            // --- 逻辑 C：处理水生植物（需移除并掉落物品） ---
            else if (blockstate.is(Blocks.KELP) || blockstate.is(Blocks.KELP_PLANT) ||
                    blockstate.is(Blocks.SEAGRASS) || blockstate.is(Blocks.TALL_SEAGRASS)) {

                // 1.21.1 掉落逻辑：推荐使用带有具体参数的 dropResources
                // 即使没有 BlockEntity，也建议传入 null 或获取实时的 BE
                BlockEntity be = blockstate.hasBlockEntity() ? level.getBlockEntity(testPos) : null;

                // 修正：1.21.1 的 dropResources 签名通常需要提供触发实体，如果是自动触发可传 null
                Block.dropResources(blockstate, level, testPos, be, null, ItemStack.EMPTY);

                level.setBlock(testPos, Blocks.AIR.defaultBlockState(), 2);
            } else {
                return false;
            }
            return true;
        });
    }
}