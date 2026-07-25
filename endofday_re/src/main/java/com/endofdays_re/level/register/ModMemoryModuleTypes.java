package com.endofdays_re.level.register;

import com.endofdays_re.utils.ModUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;

public class ModMemoryModuleTypes {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULES =
            DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, ModUtils.MODID);

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<BlockPos>> TARGET_BLOCK_POS =
            MEMORY_MODULES.register("target_block_pos",
                    () -> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<BlockPos>> TARGET_BLOCK_POS_ARRMOR =
            MEMORY_MODULES.register("target_block_pos_arrmor",
                    () -> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<BlockPos>> ATTACK_TARGET_BLOCK_POS =
            MEMORY_MODULES.register("attack_target_block_pos_arrmor",
                    () -> new MemoryModuleType<>(Optional.empty()));

}
