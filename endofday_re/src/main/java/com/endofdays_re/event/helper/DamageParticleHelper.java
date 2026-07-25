package com.endofdays_re.event.helper;

import com.endofdays_re.config.ConfigData;
import com.endofdays_re.level.register.RegisterEffect;
import com.endofdays_re.level.register.particle.DamageParticleOptions;
import com.endofdays_re.utils.ModUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;

import java.util.HashMap;
import java.util.Map;

public enum DamageParticleHelper {
    ;
    private static final Map<Integer, Integer> TICK_STACK_MAP = new HashMap<>();
    private static long lastTickTime = 0;

    // 新增：检查实体是否为 TACZ 子弹（无硬依赖）
    private static boolean isTaczBullet(Entity entity) {
        if (!ModList.get().isLoaded("tacz")) {
            return false;
        }
        try {
            Class<?> bulletClass = Class.forName("com.tacz.guns.entity.EntityKineticBullet");
            return bulletClass.isInstance(entity);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static void spawn(Entity entity, Entity Source, DamageSource damageSource, float Amount) {
        // 1. 全局开关与配置判空
        // 修改：使用反射检查 TACZ 子弹，避免硬依赖
        if (Source != null && isTaczBullet(Source)) {
            return; // 不处理 TACZ 枪械的伤害逻辑
        }
        if (entity instanceof LivingEntity target) {
            String entityId = ModUtils.getEntityTypeID(target.getType());
            if (ConfigData.ScreenConfigData.entityBlacklist.contains(entityId)) return;
            if (Amount <= 0) return;
            // 2. 同一 Tick 内多段伤害的堆叠处理
            long currentTick = entity.level().getGameTime();
            if (currentTick != lastTickTime) {
                TICK_STACK_MAP.clear();
                lastTickTime = currentTick;
            }
            int offsetCount = TICK_STACK_MAP.getOrDefault(target.getId(), 0);
            TICK_STACK_MAP.put(target.getId(), offsetCount + 1);

            DamageStyle style = determineStyle(entity, damageSource, Source);

            // 3. 计算粒子位置与发散
            double spread = 0.3;
            double x = target.getX() + (ModUtils.random.nextDouble() - 0.5) * spread;
            double z = target.getZ() + (ModUtils.random.nextDouble() - 0.5) * spread;
            double y = target.getY() + (target.getBbHeight() * 0.75) + (offsetCount * 0.15);

            double dx = (x - target.getX()) * 0.05;
            double dz = (z - target.getZ()) * 0.05;
            // 4. 生成自定义属性粒子
            for (Player player : entity.level().players()) {
                if (target.distanceToSqr(player) <= 1024) {
                    if (entity.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(
                                new DamageParticleOptions(
                                        Amount, style.mainColor, style.isCrit, style.mainBold,
                                        style.prefix, style.prefixColor, style.prefixBold,
                                        style.suffix, style.suffixColor, style.suffixBold
                                ),
                                x, y, z,
                                1,
                                dx, 0.1, dz,
                                0.0
                        );
                    }
                }
            }
        }
    }

    // 以下 determineStyle、parseColor、DamageStyle 保持不变
    // ...（省略，与原来完全一致）

    private static DamageStyle determineStyle(Entity entity, DamageSource source, Entity attacker) {
        var c = ConfigData.ScreenConfigData.damageStyle;
        DamageStyle s = new DamageStyle();
        boolean globalBold = c.enableBold;
        if (entity instanceof LivingEntity target) {
            // --- A. 默认样式初始化 (完全从配置读取) ---
            s.mainColor = parseColor(c.defaultColor, 0xEEEEEE);
            s.mainBold = globalBold;
            s.prefix = "";
            s.suffix = c.defaultSuffix;
            s.suffixColor = parseColor(c.defaultSuffixColor, 0x888888);
            s.suffixBold = globalBold;

            // --- B. 核心 Buff 状态判定 (高优先级，且受配置开关控制) ---

            // 1. 流血
            if (c.showBleeding && target.hasEffect(RegisterEffect.BLEEDING)) {
                s.mainColor = parseColor(c.bleedingColor, 0xFF2222);
                s.suffix = c.bleedingSuffix;
                s.suffixColor = s.mainColor;
                s.mainBold = true;
                s.suffixBold = true;
            }
            // 2. 击晕
            else if (c.showStun && target.hasEffect(RegisterEffect.STUN)) {
                s.mainColor = parseColor(c.stunColor, 0xFFD700);
                s.prefix = c.stunPrefix;
                s.prefixColor = s.mainColor;
                s.mainBold = true;
                s.prefixBold = true;
            }
            // 3. 撕裂
            else if (c.showLacerate && target.hasEffect(RegisterEffect.LACERATE)) {
                s.mainColor = parseColor(c.lacerateColor, 0xFF6B35);
                s.prefix = c.laceratePrefix;
                s.prefixColor = s.mainColor;
                s.mainBold = true;
                s.prefixBold = true;
            }
            // 4. 骨折
            else if (c.showFracture && target.hasEffect(RegisterEffect.FRACTURE)) {
                s.mainColor = parseColor(c.fractureColor, 0x8B4513);
                s.suffix = c.fractureSuffix;
                s.suffixColor = parseColor("#DDDDDD", 0xDDDDDD);
                s.mainBold = false;
            }

            // --- C. 暴击判定 (如果开启，覆盖后续环境属性) ---
            if (c.showCrit && attacker instanceof Player player) {
                boolean isFalling = (player.fallDistance > 0.0F || (!player.onGround() && player.getDeltaMovement().y < 0));
                boolean canCritStatus = isFalling && !player.onClimbable() && !player.isInWater() && !player.isPassenger();
                boolean isFullPower = player.getAttackStrengthScale(0.5F) > 0.9F;

                if (canCritStatus && isFullPower) {
                    s.mainColor = parseColor(c.critColor, 0xFFCC00);
                    s.mainBold = true;
                    s.isCrit = true;
                    s.prefix = c.critPrefix;
                    s.prefixColor = s.mainColor;
                    s.prefixBold = true;
                    s.suffix = c.critSuffix;
                    s.suffixColor = parseColor("#FFAA00", 0xFFAA00);
                    s.suffixBold = true;
                    return s; // 暴击直接返回，不再应用后续样式
                }
            }

            // --- D. 来源前缀 ---
            if (c.showSourcePrefix && s.prefix.isEmpty()) {
                if (attacker instanceof Player) {
                    s.prefix = c.playerPrefix;
                    s.prefixColor = 0x55FF55;
                    s.prefixBold = globalBold;
                } else if (attacker != null) {
                    s.prefix = c.mobPrefix;
                    s.prefixColor = 0xFFAA00;
                    s.prefixBold = globalBold;
                }
            }

            // --- E. 伤害属性判定 (根据配置开关显示) ---

            // 1. 闪电
            if (c.showLightning && source.is(DamageTypes.LIGHTNING_BOLT)) {
                s.mainColor = parseColor(c.lightningColor, 0xFFFFFF);
                s.suffix = c.lightningSuffix;
                s.suffixColor = s.mainColor;
                s.mainBold = true;
            }
            // 2. 冰冻
            else if (c.showFreeze && (source.is(DamageTypes.FREEZE) || source.is(DamageTypes.IN_WALL))) {
                s.mainColor = parseColor(c.freezeColor, 0x71A5FF);
                s.suffix = c.freezeSuffix;
                s.suffixColor = s.mainColor;
            }
            // 3. 凋零
            else if (c.showWither && source.is(DamageTypes.WITHER)) {
                s.mainColor = parseColor(c.witherColor, 0x333333);
                s.suffix = c.witherSuffix;
                s.suffixColor = s.mainColor;
                s.mainBold = true;
            }
            // 4. 坠落
            else if (c.showFall && (source.is(DamageTypes.FALL) || source.is(DamageTypes.STALAGMITE))) {
                s.mainColor = parseColor(c.fallColor, 0xAAAAAA);
                s.suffix = c.fallSuffix;
                s.suffixColor = s.mainColor;
            }
            // 5. 魔法
            else if (c.showMagic && source.is(DamageTypes.MAGIC)) {
                s.mainColor = parseColor(c.magicColor, 0x55FFFF);
                s.suffix = c.magicSuffix;
                s.suffixColor = 0xAA00AA;
                s.suffixBold = true;
            }
            // 6. 远程/箭矢
            else if (c.showArrow && source.is(DamageTypes.ARROW)) {
                s.mainColor = parseColor(c.arrowColor, 0xDDDDDD);
                s.suffix = c.arrowSuffix;
            }
            // 7. 火焰
            else if (c.showFire && (source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.LAVA))) {
                s.mainColor = parseColor(c.fireColor, 0xFFAC33);
                s.suffix = c.fireSuffix;
                s.suffixColor = s.mainColor;
            }
        }
        return s;
    }

    public static int parseColor(String hex, int defaultCol) {
        try {
            if (hex == null || hex.isEmpty()) return defaultCol;
            if (hex.startsWith("#")) hex = hex.substring(1);
            return (int) Long.parseLong(hex, 16);
        } catch (Exception e) {
            return defaultCol;
        }
    }

    private static class DamageStyle {
        int mainColor;
        boolean mainBold = false;
        String prefix = "";
        int prefixColor = 0xFFFFFF;
        boolean prefixBold = false;
        String suffix = "";
        int suffixColor = 0xFFFFFF;
        boolean suffixBold = false;
        boolean isCrit = false;
    }
}