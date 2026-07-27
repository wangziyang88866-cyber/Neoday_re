package com.endofdays_re.event;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;

/** Server-safe TACZ damage rules. Registered only when TACZ is present. */
public final class GunHurtServerEvent {
    private GunHurtServerEvent() {
    }

    @SubscribeEvent
    public static void onGunHurt(EntityHurtByGunEvent.Pre event) {
        Entity hurtEntity = event.getHurtEntity();
        LivingEntity attacker = event.getAttacker();
        if (attacker != null && hurtEntity != null && hurtEntity.getType() == attacker.getType()) {
            event.setCanceled(true);
        }
    }
}
