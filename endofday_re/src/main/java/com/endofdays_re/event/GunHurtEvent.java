package com.endofdays_re.event;

import com.endofdays_re.config.ConfigData;
import com.endofdays_re.level.register.particle.DamageParticleOptions;
import com.endofdays_re.utils.ModUtils;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.client.animation.statemachine.GunAnimationConstant;
import com.tacz.guns.client.resource.GunDisplayInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

import static com.endofdays_re.event.helper.DamageParticleHelper.parseColor;

// 删除了 @EventBusSubscriber
public enum GunHurtEvent {
    ;
    private static final Map<Integer, Integer> TICK_STACK_MAP = new HashMap<>();
    private static long lastTickTime = 0;

    @OnlyIn(value = Dist.CLIENT)
    @SubscribeEvent
    public static void onEntityHurtByGun(EntityHurtByGunEvent event) {
        // 1. 基础环境检查
        if (ConfigData.ScreenConfigData == null || !ConfigData.ScreenConfigData.showDamage) return;
        if (event.getHurtEntity() instanceof LivingEntity target) {
            if (event.getAttacker() == null) return;
            long currentTick = event.getAttacker().level().getGameTime();
            if (currentTick != lastTickTime) {
                TICK_STACK_MAP.clear();
                lastTickTime = currentTick;
            }
            int offsetCount = TICK_STACK_MAP.getOrDefault(target.getId(), 0);
            TICK_STACK_MAP.put(target.getId(), offsetCount + 1);

            float damage = event.getAmount();
            boolean isHeadshot = event.isHeadShot();
            var c = ConfigData.ScreenConfigData.damageStyle;
            int mainColor;
            boolean isCrit = false;
            boolean isBold = c.enableBold;
            String prefix = "";
            int prefixColor = 0xFFFFFF;
            String suffix = c.defaultSuffix;
            int suffixColor = parseColor(c.defaultSuffixColor, 0x888888);

            if (isHeadshot) {
                mainColor = parseColor(c.critColor, 0xFFCC00);
                isCrit = true;
                isBold = true;
                prefix = c.critPrefix;
                prefixColor = mainColor;
                suffix = c.critSuffix;
                suffixColor = parseColor("#FFAA00", 0xFFAA00);
            } else {
                mainColor = parseColor(c.defaultColor, 0xEEEEEE);
            }

            double spread = 0.3;
            double x = target.getX() + (ModUtils.random.nextDouble() - 0.5) * spread;
            double z = target.getZ() + (ModUtils.random.nextDouble() - 0.5) * spread;
            double y = target.getY() + (target.getBbHeight() * 0.85) + (offsetCount * 0.15);

            double dx = (x - target.getX()) * 0.05;
            double dz = (z - target.getZ()) * 0.05;
            event.getAttacker().level().addParticle(
                    new DamageParticleOptions(
                            damage, mainColor, isCrit, isBold,
                            prefix, prefixColor, isBold,
                            suffix, suffixColor, isBold
                    ),
                    x, y, z,
                    dx, 0.1, dz
            );
        }
    }

    @SubscribeEvent
    public static void onGunHurt(EntityHurtByGunEvent.Pre event) {
        Entity hurtEntity = event.getHurtEntity();
        LivingEntity attacker = event.getAttacker();
        if (attacker != null && hurtEntity != null && hurtEntity.getType() == attacker.getType()) {
            event.setCanceled(true);
        }
    }

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