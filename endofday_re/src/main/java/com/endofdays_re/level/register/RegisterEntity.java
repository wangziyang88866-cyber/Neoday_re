package com.endofdays_re.level.register;


import com.endofdays_re.level.register.entity.FlyingEntity;
import com.endofdays_re.level.register.entity.item.entity.FishingHook;
import com.endofdays_re.level.register.entity.item.entity.ThrownTNTEntity;
import com.endofdays_re.utils.ModUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@SuppressWarnings("removal")
public enum RegisterEntity {
    ;
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, ModUtils.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<FishingHook>> FISHING_HOOK = ENTITIES.register("fishing_hook", () -> EntityType.Builder.<FishingHook>of(FishingHook::new, MobCategory.MISC)
            .noSave()
            .noSummon()
            .sized(0.25F, 0.25F)
            .clientTrackingRange(4)
            .updateInterval(5)
            .build("fishing_hook"));


    public static final DeferredHolder<EntityType<?>, EntityType<ThrownTNTEntity>> THROWN_TNT = ENTITIES.register("thrown_tnt",
            () -> EntityType.Builder.<ThrownTNTEntity>of(ThrownTNTEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(10)
                    .updateInterval(10)
                    .build("thrown_tnt"));


    // 在你的注册类中
    public static final DeferredHolder<EntityType<?>, EntityType<FlyingEntity>> FLYING_ENTITY =
            ENTITIES.register("flying_entity",
                    () -> EntityType.Builder.<FlyingEntity>of(FlyingEntity::new, MobCategory.MONSTER)
                            .sized(0.9F, 0.5F) // Phantom 的默认尺寸
                            .build("flying_entity"));


}
