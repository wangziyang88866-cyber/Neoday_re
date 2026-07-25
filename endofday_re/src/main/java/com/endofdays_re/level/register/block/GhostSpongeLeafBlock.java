package com.endofdays_re.level.register.block;

import com.endofdays_re.level.register.RgisterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class GhostSpongeLeafBlock extends AirBlock {
    // 状态机计数器：用于确认周围完全清理干净
    public static final IntegerProperty CHECK_COUNT = IntegerProperty.create("check_count", 0, 5);

    private static final Direction[] ALL_DIRECTIONS = Direction.values();
    // 扩散延迟：设置为 1 或 2，让它有节奏地蔓延，而不是瞬间爆发
    private static final int TICK_DELAY = 1;

    public GhostSpongeLeafBlock(Properties pProperties) {
        super(pProperties.air().noCollission().noOcclusion().noLootTable());
        this.registerDefaultState(this.stateDefinition.any().setValue(CHECK_COUNT, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHECK_COUNT);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        if (!level.isClientSide) {
            // 【重要修复】不要在这里直接执行 tryRemoveLeaves！
            // 仅仅请求一个 Tick 任务，把逻辑交给 tick() 处理，打断递归链
            level.scheduleTick(pos, this, TICK_DELAY);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {
            // 邻居变化也只请求 Tick
            if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
                level.scheduleTick(pos, this, TICK_DELAY);
            }
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // 在这里执行核心逻辑
        boolean hasLeaves = isLeavesNearby(level, pos);

        if (hasLeaves) {
            // 如果发现树叶，扩散并重置计数器
            this.tryRemoveLeaves(level, pos);
            level.setBlock(pos, state.setValue(CHECK_COUNT, 0), 2);
            // 继续计划下一次检查
            level.scheduleTick(pos, this, TICK_DELAY + 1);
        } else {
            int count = state.getValue(CHECK_COUNT);
            if (count < 3) {
                // 没发现树叶，增加巡逻计数
                level.setBlock(pos, state.setValue(CHECK_COUNT, count + 1), 2);
                level.scheduleTick(pos, this, 5); // 没活干时放慢巡逻频率
            } else {
                // 功成身退
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private boolean isLeavesNearby(Level level, BlockPos pos) {
        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-2, -2, -2), pos.offset(2, 2, 2))) {
            if (level.getBlockState(checkPos).is(BlockTags.LEAVES)) {
                return true;
            }
        }
        return false;
    }

    private void tryRemoveLeaves(Level level, BlockPos pos) {
        // 广度优先遍历：用于跨越木头搜寻叶子
        BlockPos.breadthFirstTraversal(pos, 4, 64, (currentPos, consumer) -> {
            for (Direction direction : ALL_DIRECTIONS) {
                consumer.accept(currentPos.relative(direction));
            }
        }, (testPos) -> {
            if (testPos.equals(pos)) return true;

            BlockState blockstate = level.getBlockState(testPos);

            // 1. 如果是叶子：替换为幽灵块
            if (blockstate.is(BlockTags.LEAVES)) {
                // 【重要修复】使用 flag 2 (不触发邻居更新)，防止在 BFS 内部又触发 neighborChanged
                level.setBlock(testPos, RgisterBlock.GHOST_SPONGE_Leaf.get().defaultBlockState(), 2);
                return true;
            }

            // 2. 如果是木头：充当导线，在旁边寻找空气位点火
            if (blockstate.is(BlockTags.LOGS)) {
                for (Direction dir : ALL_DIRECTIONS) {
                    BlockPos neighbor = testPos.relative(dir);
                    if (level.getBlockState(neighbor).isAir()) {
                        level.setBlock(neighbor, RgisterBlock.GHOST_SPONGE_Leaf.get().defaultBlockState(), 2);
                        break;
                    }
                }
                return true;
            }

            // 3. 处理藤蔓
            if (blockstate.is(BlockTags.REPLACEABLE_BY_TREES)) {
                level.setBlock(testPos, Blocks.AIR.defaultBlockState(), 2);
                return true;
            }

            return false;
        });
    }
}