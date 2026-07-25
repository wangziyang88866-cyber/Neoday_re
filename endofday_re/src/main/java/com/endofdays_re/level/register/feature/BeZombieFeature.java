package com.endofdays_re.level.register.feature;

import com.endofdays_re.config.ConfigData;
import com.endofdays_re.level.register.RgisterBlock;
import com.endofdays_re.level.register.entity.block.CorpseZombieBlockEntity;
import com.endofdays_re.utils.ModUtils;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

import java.util.ArrayList;
import java.util.List;

public class BeZombieFeature extends Feature<NoneFeatureConfiguration> {
    public BeZombieFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();
        if (!ConfigData.commonConfigData.enable_corpse) return false;


        int countToSpawn = random.nextInt(ConfigData.commonConfigData.corpse_max) + 1;
        int spawnedCount = 0;
        List<BlockPos> spawnedPositions = new ArrayList<>();
        Block corpseBlock = RgisterBlock.CORPSE_ZOMBIE_BLOCK.get();

        for (int i = 0; i < 100 && spawnedCount < countToSpawn; i++) {
            int offsetX = random.nextInt(16) - 8;
            int offsetZ = random.nextInt(16) - 8;

            // 1. 获取地表/水底位置
            BlockPos spawnPos = level.getHeightmapPos(Heightmap.Types.OCEAN_FLOOR_WG, origin.offset(offsetX, 0, offsetZ));

            // 基础校验
            if (spawnPos.getY() < 3) continue;
            if (!level.getBlockState(spawnPos.below()).isSolid()) continue;

            // 2. 环境识别
            BlockState currentState = level.getBlockState(spawnPos);
            boolean isWater = currentState.getFluidState().isSourceOfType(Fluids.WATER);
            boolean isAir = currentState.isAir();
            if (!isWater && !isAir) continue;

            // 3. 容纳区域检查 (确保尸体头部位置不是固体方块)
            // 即使是躺着，上方也最好是透明/空气，防止渲染穿模
            if (!level.getBlockState(spawnPos.above()).isAir() && !level.getFluidState(spawnPos.above()).isSource()) {
                continue;
            }

            // 4. 水下深度校验
            if (isWater) {
                boolean isDeepEnough = true;
                for (int yOffset = 1; yOffset <= 3; yOffset++) {
                    if (!level.getFluidState(spawnPos.above(yOffset)).isSourceOfType(Fluids.WATER)) {
                        isDeepEnough = false;
                        break;
                    }
                }
                if (!isDeepEnough) continue;
            }

            // 5. 间距校验
            boolean tooClose = false;
            for (BlockPos otherPos : spawnedPositions) {
                if (spawnPos.distSqr(otherPos) < 9) {
                    tooClose = true;
                    break;
                }
            }
            if (tooClose) continue;

            // 6. 确定方向与姿态逻辑
            Direction randomDirection = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            int poseType;

            if (isWater) {
                // 水下逻辑：只有趴(1)或躺(2)
                poseType = random.nextInt(2) + 1;
            } else {
                // 陆地逻辑：优先尝试坐(0)
                poseType = random.nextInt(3);

                if (poseType == 0) {
                    // --- 严格靠背逻辑 ---
                    BlockPos backPos = spawnPos.relative(randomDirection.getOpposite());
                    BlockState backState = level.getBlockState(backPos);

                    // 如果背后不是固体方块，坐不住
                    if (!backState.isSolid()) {
                        // 尝试随机换个方向看看能不能坐
                        boolean foundWall = false;
                        for (Direction dir : Direction.Plane.HORIZONTAL) {
                            if (level.getBlockState(spawnPos.relative(dir.getOpposite())).isSolid()) {
                                randomDirection = dir;
                                foundWall = true;
                                break;
                            }
                        }
                        // 如果转了一圈还是没墙，那就只能躺下或趴下
                        if (!foundWall) {
                            poseType = random.nextInt(2) + 1;
                        }
                    }
                }
            }

            // 7. 放置方块
            BlockState stateToPlace = corpseBlock.defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, randomDirection);

            if (isWater && stateToPlace.hasProperty(BlockStateProperties.WATERLOGGED)) {
                stateToPlace = stateToPlace.setValue(BlockStateProperties.WATERLOGGED, true);
            }

            if (level.setBlock(spawnPos, stateToPlace, 2)) {
                BlockEntity be = level.getBlockEntity(spawnPos);
                if (be instanceof CorpseZombieBlockEntity corpse) {
                    corpse.setPoseType(poseType);

                    // 填充战利品
                    if (!corpse.fillLoot(level.getLevel(), context.random())) {
                        ModUtils.error("Loot build failed at: " + spawnPos);
                    }

                    spawnedPositions.add(spawnPos);
                    spawnedCount++;
                }
            }
        }
        return spawnedCount > 0;
    }
}