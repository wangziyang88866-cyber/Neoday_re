package com.endofdays_re.level.register;


import com.endofdays_re.level.register.block.*;
import com.endofdays_re.utils.ModUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public enum RgisterBlock {
    ;
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, ModUtils.MODID);
    // 方块
    public static final DeferredHolder<Block, QuicksandFluidBlock> QUICK_SAND_BLOCK =
            BLOCKS.register("quicksand_block", () ->
                    new QuicksandFluidBlock(RegisterrFluid.SOURCE_QUICK_SAND, BlockBehaviour.Properties.of()
                            .strength(100.0F).noLootTable().noOcclusion()));

    public static final DeferredHolder<Block, GhostSpongeWaterAirBlock> GHOST_SPONGE = BLOCKS.register("ghost_sponge",
            () -> new GhostSpongeWaterAirBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(0.6F)
                    .noCollission() // 属性层面的无碰撞
                    .noOcclusion()  // 允许光线穿透，防止渲染错误
            ));

    public static final DeferredHolder<Block, GhostSpongeLeafBlock> GHOST_SPONGE_Leaf = BLOCKS.register("ghost_sponge_leaf",
            () -> new GhostSpongeLeafBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_YELLOW)
                    .strength(0.6F)
                    .noCollission() // 属性层面的无碰撞
                    .noOcclusion()  // 允许光线穿透，防止渲染错误
            ));
    public static final DeferredHolder<Block, CorpseZombieBlock> CORPSE_ZOMBIE_BLOCK = BLOCKS.register("corpse_zombie",
            () -> new CorpseZombieBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.SKELETON_SKULL)
                    .strength(1.5F, 6.0F)
                    .noOcclusion())); // 允许光线透过，不透明

    //地刺
    public static final DeferredHolder<Block, SpikeBlock> SPIKE_BLOCK = BLOCKS.register("spike_block",
            () -> new SpikeBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(2.0f)
                    .noOcclusion() // 允许光线穿过，如果模型不完整
            ));
    public static final DeferredHolder<Block, BarbedWireFenceBlock> BARBED_WIRE_FENCE = BLOCKS.register("barbed_wire_fence",
            () -> new BarbedWireFenceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.0F, 3.0F) // 硬度和抗爆炸性
                    .sound(SoundType.METAL) // 金属音效
                    .noOcclusion() // 允许光线透射（非全框方块必备）
                    .requiresCorrectToolForDrops() // 需要工具掉落
            ));
}

