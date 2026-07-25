package com.endofdays_re.level.register.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.MagmaBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SpikeBlock extends MagmaBlock {
    // 稍微矮一点，确保脚部能进入方块判定区
    protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);

    public SpikeBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }


    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        // 1. 基础判定：非潜行、是生物、没有冰霜行者附魔
        if (entity instanceof LivingEntity livingEntity) {
            // 2. 创造模式玩家免疫
            if (livingEntity instanceof ServerPlayer player && player.isCreative()) return;
            if (livingEntity.hurtTime <= 0) {
                float maxHealth = livingEntity.getMaxHealth();
                // 计算伤害：最大生命值的 5%，最小不低于 0.5 (1/4 颗心)
                float damageAmount = Math.max(maxHealth * 0.05F, 0.5F);

                // 4. 造成伤害
                livingEntity.hurt(level.damageSources().genericKill(), damageAmount);
            }
        }
    }
}