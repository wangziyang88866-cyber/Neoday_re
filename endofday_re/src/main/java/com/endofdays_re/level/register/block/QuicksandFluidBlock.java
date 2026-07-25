package com.endofdays_re.level.register.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class QuicksandFluidBlock extends LiquidBlock {

    public QuicksandFluidBlock(Supplier<? extends FlowingFluid> fluid, Properties properties) {
        super(fluid.get(), properties);
        // 注意：在 1.21.1 的某些构建中，LiquidBlock 构造函数直接接收 Fluid 而非 Supplier
        // 如果报错，请改为 super(fluid.get(), properties);
    }

    @Override
    protected void entityInside(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Entity entity) {
        super.entityInside(state, level, pos, entity);

        if (!(entity instanceof LivingEntity living)) return;

        // 1. 移动限制逻辑
        // makeStuckInBlock 会处理基本的减速，Vec3(0.1, 0.5, 0.1) 表示 XZ 轴极大减速，Y 轴中等减速
        living.makeStuckInBlock(state, new Vec3(0.1D, 0.5D, 0.1D));

        // 2. 深度判定
        // 获取流体实际高度
        double fluidHeight = state.getFluidState().getHeight(level, pos);
        // 计算实体脚部相对于方块底部的距离
        double entityBottom = entity.getY() - pos.getY();
        double submergedDepth = fluidHeight - entityBottom;

        // 当淹没深度超过身高 90% 时触发窒息/伤害逻辑
        if (submergedDepth > living.getBbHeight() * 0.90) {

            if (!level.isClientSide && living.isAlive() && !living.isSpectator()) {
                if (living instanceof Player player && player.isCreative()) return;

                // 3. 性能优化：使用 tickCount 代替 HashMap 计时器，防止内存泄漏
                // 每 20 tick 执行一次伤害
                if (living.tickCount % 20 == 0) {
                    float damage = Math.max(living.getMaxHealth() * 0.05f, 1.0f);
                    // 1.21.1 推荐使用 inWall() 表示窒息感，或者自定义伤害源
                    living.hurt(level.damageSources().inWall(), damage);
                }

                // 4. 行为限制
                if (living instanceof Mob mob) {
                    // 禁用该实体的攻击和移动能力（流沙束缚感）
                    // 标志位包括：MOVE, LOOK, JUMP, TARGET
                    mob.goalSelector.setControlFlag(Goal.Flag.TARGET, false);
                    // 如果你想让它彻底不动
                    mob.goalSelector.setControlFlag(Goal.Flag.MOVE, false);
                }
            }
        }
    }

    @Override
    protected boolean isOcclusionShapeFullBlock(@NotNull BlockState state, @NotNull BlockGetter world, @NotNull BlockPos pos) {
        // 确保流沙像水一样不透明（阻挡光线）
        return true;
    }
}