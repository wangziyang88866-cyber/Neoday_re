package com.endofdays_re.event;

import com.endofdays_re.event.data.AllSyncValue;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.monster.Zombie;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventBusSubscriber(modid = "endofdays_re")
public final class ZombieDeathDebugEvent {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ZombieDeathDebugEvent.class);

    private ZombieDeathDebugEvent() {
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Zombie zombie)) {
            return;
        }

        if (zombie.level().isClientSide()) {
            return;
        }

        String source = event.getSource()
                .typeHolder()
                .unwrapKey()
                .map(key -> key.location().toString())
                .orElse("unknown");

        LOGGER.error(
                "[ZombieDeathDebug] day={}, entity={}, health={}, maxHealth={}, onFire={}, source={}",
                AllSyncValue.Instance.day,
                BuiltInRegistries.ENTITY_TYPE.getKey(zombie.getType()),
                zombie.getHealth(),
                zombie.getMaxHealth(),
                zombie.isOnFire(),
                source
        );
    }
}