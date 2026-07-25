package com.endofdays_re.level.register.fluid;

import com.endofdays_re.level.register.RegisterFluidType;
import com.endofdays_re.level.register.RegisterrFluid;
import com.endofdays_re.level.register.RgisterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;

public abstract class QuicksandFluid extends FlowingFluid {

    @Override
    public @NotNull Fluid getFlowing() {
        return RegisterrFluid.FLOWING_QUICK_SAND.get();
    }

    @Override
    public @NotNull Fluid getSource() {
        return RegisterrFluid.SOURCE_QUICK_SAND.get();
    }

    @Override
    public @NotNull Item getBucket() {
        return Items.AIR.getDefaultInstance().getItem();
    }

    @Override
    public @NotNull FluidType getFluidType() {
        // 修正：从 DeferredHolder 中获取 FluidType
        return RegisterFluidType.QUICK_SAND_TYPE.get();
    }

    @Override
    protected boolean canBeReplacedWith(@NotNull FluidState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Fluid fluid, @NotNull Direction direction) {
        // 保持原样：流沙不容易被替换
        return false;
    }

    @Override
    protected boolean canConvertToSource(@NotNull Level level) {
        // 类似熔岩，流沙不会自动无限生成源头
        return false;
    }

    @Override
    protected void beforeDestroyingBlock(@NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockState state) {
        // 1.21 依然支持此逻辑：当流体破坏方块时触发
        // 但注意：原版流体通常在此掉落方块实体，你的逻辑是替换为方块，保持不变
    }

    @Override
    protected int getSlopeFindDistance(@NotNull LevelReader level) {
        return 2; // 流动距离限制
    }

    @Override
    protected int getDropOff(@NotNull LevelReader level) {
        return 1; // 坡度下降量
    }

    @Override
    protected float getExplosionResistance() {
        return 100f;
    }

    @Override
    public boolean isSame(@NotNull Fluid fluid) {
        return fluid == RegisterrFluid.SOURCE_QUICK_SAND.get() || fluid == RegisterrFluid.FLOWING_QUICK_SAND.get();
    }

    @Override
    protected @NotNull BlockState createLegacyBlock(@NotNull FluidState state) {
        // 修正：1.21.1 建议使用 LiquidBlock.LEVEL
        return RgisterBlock.QUICK_SAND_BLOCK.get().defaultBlockState()
                .setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    public @NotNull Vec3 getFlow(@NotNull BlockGetter world, @NotNull BlockPos pos, @NotNull FluidState state) {
        // 减慢流动的视觉和物理效果
        return super.getFlow(world, pos, state).scale(0.2);
    }

    @Override
    public int getTickDelay(@NotNull LevelReader level) {
        // 流动速度：越大概流动越慢（水是5，熔岩是30，这里设为15）
        return 15;
    }

    // --- Source 流体子类
    public static class Source extends QuicksandFluid {
        @Override
        public int getAmount(@NotNull FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(@NotNull FluidState state) {
            return true;
        }
    }

    // --- Flowing 流体子类
    public static class Flowing extends QuicksandFluid {
        @Override
        protected void createFluidStateDefinition(StateDefinition.@NotNull Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(@NotNull FluidState state) {
            return false;
        }
    }
}