package com.endofdays_re.level.register.block;

import com.endofdays_re.level.register.RegisterBlockEntityTypes;
import com.endofdays_re.level.register.entity.block.CorpseZombieBlockEntity;
import com.endofdays_re.utils.ModUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CorpseZombieBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final IntegerProperty POSE = IntegerProperty.create("pose", 0, 2);
    // 1.21.1 必须实现的 Codec
    public static final MapCodec<CorpseZombieBlock> CODEC = simpleCodec(CorpseZombieBlock::new);
    private static final VoxelShape SITTING_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 13.0D, 13.0D);
    private static final VoxelShape LYING_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 3.0D, 16.0D);

    public CorpseZombieBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, false)
                .setValue(POSE, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip." + ModUtils.MODID + ".corpse_zombie.description"));
            tooltip.add(Component.translatable("tooltip." + ModUtils.MODID + ".corpse_zombie.ability_1"));
            tooltip.add(Component.translatable("tooltip." + ModUtils.MODID + ".corpse_zombie.ability_2"));
            tooltip.add(Component.translatable("tooltip." + ModUtils.MODID + ".corpse_zombie.ability_3"));
            tooltip.add(Component.empty());
            tooltip.add(Component.translatable("tooltip." + ModUtils.MODID + ".corpse_zombie.detail"));
        } else {
            tooltip.add(Component.translatable("tooltip." + ModUtils.MODID + ".hold_shift"));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, WATERLOGGED, POSE);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return pState.getValue(POSE) == 0 ? SITTING_SHAPE : LYING_SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        FluidState fluid = pContext.getLevel().getFluidState(pContext.getClickedPos());
        return this.defaultBlockState()
                .setValue(FACING, pContext.getHorizontalDirection().getOpposite())
                .setValue(WATERLOGGED, fluid.getType() == Fluids.WATER);
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        if (!pLevel.isClientSide) {
            int randomPose = pLevel.random.nextInt(3);

            // 坐姿校验
            if (randomPose == 0) {
                BlockPos backPos = pPos.relative(pState.getValue(FACING).getOpposite());
                if (!pLevel.getBlockState(backPos).isSolidRender(pLevel, backPos)) {
                    randomPose = pLevel.random.nextInt(2) + 1;
                }
            }

            pLevel.setBlock(pPos, pState.setValue(POSE, randomPose), 3);

            BlockEntity be = pLevel.getBlockEntity(pPos);
            if (be instanceof CorpseZombieBlockEntity corpse && pLevel instanceof ServerLevel serverLevel) {
                corpse.fillLoot(serverLevel);
            }
        }
    }

    /**
     * 1.21.1 交互逻辑重写
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHit) {
        if (pLevel.getBlockEntity(pPos) instanceof CorpseZombieBlockEntity corpse) {
            // 这里建议在 BlockEntity 中更新 handleInteract 的签名以匹配新系统
            // 如果 handleInteract 内部只用到了 Player，可以直接调用
            return corpse.handleInteract(pPlayer, pPlayer.getUsedItemHand());
        }
        return InteractionResult.PASS;
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (!pState.is(pNewState.getBlock())) {
            BlockEntity be = pLevel.getBlockEntity(pPos);
            if (be instanceof CorpseZombieBlockEntity corpse) {
                // 1.21.1 Containers.dropContents 依然可用
                Containers.dropContents(pLevel, pPos, corpse);
            }
            super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new CorpseZombieBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, RegisterBlockEntityTypes.CORPSE_ZOMBIE_BE.get(), CorpseZombieBlockEntity::tick);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        // 如果是自定义动画模型，使用 ENTITYBLOCK_ANIMATED 没问题
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }
}