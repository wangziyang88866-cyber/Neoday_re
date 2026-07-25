package com.endofdays_re.event.helper;

import com.endofdays_re.config.ConfigData;
import com.endofdays_re.level.register.RegisterBiomes;
import com.endofdays_re.level.register.RgisterBlock;
import com.endofdays_re.utils.ModUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum levelHelper {
    ;

    public static final Map<Block, Block> HOT_MAP = Map.ofEntries(
            Map.entry(Blocks.GRASS_BLOCK, Blocks.COARSE_DIRT),
            Map.entry(Blocks.MYCELIUM, Blocks.DIRT),
            Map.entry(Blocks.PODZOL, Blocks.COARSE_DIRT),
            Map.entry(Blocks.DIRT, Blocks.GRAVEL),
            Map.entry(Blocks.COARSE_DIRT, Blocks.MUD),
            Map.entry(Blocks.STONE, Blocks.COBBLESTONE),
            Map.entry(Blocks.COBBLESTONE, Blocks.GRAVEL),
            Map.entry(Blocks.ICE, Blocks.WATER),
            Map.entry(Blocks.WATER, RgisterBlock.GHOST_SPONGE.get()),
            Map.entry(Blocks.SNOW, Blocks.AIR),
            Map.entry(Blocks.SNOW_BLOCK, Blocks.AIR)
    );
    public static final Map<Block, Block> COLD_MAP = Map.ofEntries(
            Map.entry(Blocks.WATER, Blocks.ICE),
            Map.entry(Blocks.ICE, Blocks.PACKED_ICE),
            Map.entry(Blocks.GRASS_BLOCK, Blocks.COARSE_DIRT),
            Map.entry(Blocks.DIRT, Blocks.COARSE_DIRT),
            Map.entry(Blocks.SAND, Blocks.GRAVEL)
    );
    // 区块冷却映射表
    private static final Map<Long, Long> CHUNK_COOLDOWN = new ConcurrentHashMap<>();
    // 基础冷却（温和气候下）
    private static final int BASE_COOLDOWN = 100;
    // 最快冷却（极端气候下，0.5秒一次）
    private static final int MIN_COOLDOWN = 10;

    public static void processEnvironmentalDrop(ServerLevel world, BlockPos topPos, float temp) {
        if (!ConfigData.isDayEnable("enable_temp") || !ConfigData.isModeEnable("enable_temp")) return;

        float effectiveTemp = getEffectiveTemp(world, temp);

        // --- 核心逻辑：温度影响冷却时间 ---
        int dynamicCooldown = calculateDynamicCooldown(effectiveTemp);

        long chunkKey = ChunkPos.asLong(topPos);
        long currentTime = world.getGameTime();
        if (CHUNK_COOLDOWN.containsKey(chunkKey)) {
            if (currentTime - CHUNK_COOLDOWN.get(chunkKey) < dynamicCooldown) return;
        }

        // 概率判定
        double chance = calculateConversionChance(effectiveTemp);
        if (world.random.nextDouble() > chance) return;

        // 空间判定
        boolean isExposed = world.canSeeSky(topPos) || world.getBrightness(LightLayer.BLOCK, topPos) > 11;
        if (!isExposed) return;

        // 更新冷却
        CHUNK_COOLDOWN.put(chunkKey, currentTime);
        if (currentTime % 5000 == 0) CHUNK_COOLDOWN.entrySet().removeIf(e -> currentTime - e.getValue() > 1000);

        // 执行动作
        if (effectiveTemp > 30) clearVegetation(world, topPos);
        if (effectiveTemp <= -10) handleSnowStacking(world, topPos);
        applyTransformation(world, topPos, effectiveTemp);
    }

    /**
     * 计算动态冷却：温度越极端（极热或极冷），CD越短
     */
    private static int calculateDynamicCooldown(float temp) {
        float delta = 0;

        // 极热判定 (超过 30°C)
        if (temp > 30) {
            delta = temp - 30;
        }
        // 极冷判定 (低于 -10°C)
        else if (temp < -10) {
            delta = -10 - temp; // 例如 -40°C 时，delta 为 30
        }

        // 如果在正常范围内，使用基础冷却
        if (delta <= 0) return BASE_COOLDOWN;

        // 加速公式：delta 越大，CD 减得越多
        // 每偏离 1 度，CD 减少 3 Ticks (可根据测试反馈调整此倍数)
        int reduction = (int) (delta * 3);

        // 确保冷却不会低于 5 Ticks (0.25秒)，防止计算过于密集导致服务器卡顿
        return Math.max(BASE_COOLDOWN - reduction, 5);
    }

    private static double calculateConversionChance(float temp) {
        float delta = 0;
        if (temp > 30) delta = temp - 30;
        else if (temp < -10) delta = -10 - temp;
        if (delta <= 0) return 0;
        return Math.min(delta * 0.025, 0.95);
    }

    private static float getEffectiveTemp(ServerLevel world, float baseTemp) {
        long time = world.getDayTime() % 24000;
        boolean isNight = time > 13000 && time < 23000;
        return isNight ? baseTemp - 8.0f : baseTemp;
    }

    private static void applyTransformation(ServerLevel world, BlockPos topPos, float temp) {
        Map<Block, Block> activeMap = (temp >= 30) ? HOT_MAP : (temp <= -10 ? COLD_MAP : null);
        if (activeMap == null) return;

        for (int d = 0; d < 2; d++) {
            BlockPos targetPos = topPos.below(d);
            BlockState state = world.getBlockState(targetPos);
            Block currentBlock = state.getBlock();

            if (activeMap.containsKey(currentBlock)) {
                if (d > 0 && world.random.nextBoolean()) continue;

                if (currentBlock == Blocks.WATER && activeMap.get(currentBlock) == RgisterBlock.GHOST_SPONGE.get()
                        && temp > 30
                ) {
                    if (world.random.nextInt(5) != 0) continue;
                }

                // 替换方块
                world.setBlock(targetPos, activeMap.get(currentBlock).defaultBlockState(), 2);

                // --- 转换群系为玄武岩三角洲 (仅高温且概率触发，防止卡顿) ---
                if (temp > 30 && world.random.nextInt(10) == 0) {
                    ModUtils.setBiome(world, targetPos, RegisterBiomes.END_OF_DAYS_WASTELAND.getKey());
                }

                // 高温起火
                if (temp > 55 && d == 0 && world.random.nextInt(10) == 0) {
                    BlockPos firePos = targetPos.above();
                    if (world.getBlockState(firePos).isAir())
                        world.setBlock(firePos, Blocks.FIRE.defaultBlockState(), 3);
                }
                break;
            }
        }
    }

    private static void clearVegetation(ServerLevel world, BlockPos topPos) {
        // 扫描范围：周围 3x3，向上 10 格 (适配暮色大树根部)
        for (BlockPos checkPos : BlockPos.betweenClosed(topPos.offset(-1, 0, -1), topPos.offset(1, 10, 1))) {
            BlockState checkState = world.getBlockState(checkPos);

            // 1. 碰到叶子：点火连锁
            if (checkState.is(BlockTags.LEAVES)) {
                world.setBlock(checkPos, RgisterBlock.GHOST_SPONGE_Leaf.get().defaultBlockState(), 3);
                return;
            }

            // 2. 碰到原木：在原木【邻近空气】点火，不删原木
            if (checkState.is(BlockTags.LOGS)) {
                // 寻找原木上方的空气放置幽灵块，让它去吸上面的叶子
                BlockPos aboveLog = checkPos.above();
                if (world.getBlockState(aboveLog).isAir() || isVegetation(world.getBlockState(aboveLog))) {
                    world.setBlock(aboveLog, RgisterBlock.GHOST_SPONGE_Leaf.get().defaultBlockState(), 3);
                    return;
                }
            }

            // 3. 普通地表植被
            if (isVegetation(checkState)) {
                world.setBlock(checkPos, Blocks.AIR.defaultBlockState(), 2);
            }
        }
    }

    private static boolean isVegetation(BlockState state) {
        // 1.21.1 推荐使用更加通用的标签
        return state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.REPLACEABLE) // 包含草、花等可被替换的方块
                || state.is(BlockTags.LEAVES)
                || state.is(Blocks.SHORT_GRASS)   // 1.21 中 Blocks.GRASS 已重命名或映射到 SHORT_GRASS
                || state.is(Blocks.FERN)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.LARGE_FERN);
    }

    private static void handleSnowStacking(ServerLevel world, BlockPos pos) {
        if (!world.canSeeSky(pos)) return;
        BlockPos snowPos = world.getBlockState(pos).isAir() ? pos : pos.above();
        BlockState current = world.getBlockState(snowPos);
        BlockState below = world.getBlockState(snowPos.below());
        if (world.random.nextInt(10) == 0) ModUtils.setBiome(world, snowPos, RegisterBiomes.FROZEN_DEADLAND.getKey());
        if (below.isSolidRender(world, snowPos.below()) || below.is(Blocks.SNOW_BLOCK)) {
            if (current.isAir()) world.setBlock(snowPos, Blocks.SNOW.defaultBlockState(), 2);
            else if (current.is(Blocks.SNOW)) {
                int layers = current.getValue(SnowLayerBlock.LAYERS);
                if (layers < 8) world.setBlock(snowPos, current.setValue(SnowLayerBlock.LAYERS, layers + 1), 2);
                else world.setBlock(snowPos, Blocks.SNOW_BLOCK.defaultBlockState(), 2);
            }
        }

    }
}