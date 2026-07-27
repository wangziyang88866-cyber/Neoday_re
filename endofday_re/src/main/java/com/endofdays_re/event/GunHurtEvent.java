package com.endofdays_re.event;

import com.endofdays_re.config.ConfigData;
import com.endofdays_re.level.register.particle.DamageParticleOptions;
import com.endofdays_re.utils.ModUtils;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.client.animation.statemachine.GunAnimationConstant;
import com.tacz.guns.client.resource.GunDisplayInstance;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

// 只在 ClientEvent 中、且确认安装 TACZ 后反射注册。
public enum GunHurtEvent {
    ;

    @SubscribeEvent
    public static void onGunFire(GunFireEvent event) {
        GunDisplayInstance display = TimelessAPI.getGunDisplay(event.getGunItemStack()).orElse(null);
        if (display != null && display.getAnimationStateMachine() != null
                && event.getLogicalSide().isClient()
        ) {
            display.getAnimationStateMachine().trigger(GunAnimationConstant.INPUT_SHOOT);
        }
    }
}
