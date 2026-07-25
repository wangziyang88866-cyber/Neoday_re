package com.endofdays_re.level.register;


import com.endofdays_re.level.register.entity.block.CorpseZombieBlockEntity;
import com.endofdays_re.utils.ModUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.endofdays_re.level.register.RgisterBlock.CORPSE_ZOMBIE_BLOCK;

public enum RegisterBlockEntityTypes {
    ;
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ModUtils.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CorpseZombieBlockEntity>> CORPSE_ZOMBIE_BE =
            BLOCK_ENTITIES.register("corpse_zombie_be",
                    () -> BlockEntityType.Builder.of(CorpseZombieBlockEntity::new, CORPSE_ZOMBIE_BLOCK.get()).build(null));
}