package com.endofdays_re.event.helper;

import com.endofdays_re.config.ConfigData;
import com.endofdays_re.level.register.particle.DamageParticleOptions;
import com.endofdays_re.utils.ModUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;

import java.util.HashMap;
import java.util.Map;

public enum HealParticleHelper {
    ;

    private static final Map<Integer, Integer> HEAL_STACK_MAP = new HashMap<>();
    private static long lastTickTime = 0;

    public static void spawn(LivingHealEvent event) {
        // --- 1. 全局配置开关检查 ---
        if (ConfigData.ScreenConfigData == null || !ConfigData.ScreenConfigData.showHeal) return;


        LivingEntity target = event.getEntity();
        if (Minecraft.getInstance().player != null && target.distanceToSqr(Minecraft.getInstance().player) > 1024)
            return;
        // 核心修复：检查黑名单
        if (ConfigData.ScreenConfigData.entityBlacklist.contains(ModUtils.getEntityTypeID(target.getType()))) return;
        float amount = event.getAmount();
        if (amount <= 0) return;

        // --- 2. 堆叠逻辑处理 ---
        long currentTick = event.getEntity().level().getGameTime();
        if (currentTick != lastTickTime) {
            HEAL_STACK_MAP.clear();
            lastTickTime = currentTick;
        }
        int offsetCount = HEAL_STACK_MAP.getOrDefault(target.getId(), 0);
        HEAL_STACK_MAP.put(target.getId(), offsetCount + 1);

        // --- 3. 样式初始化 (联动配置) ---
        HealStyle style = determineStyle(target);

        // --- 4. 坐标逻辑 ---
        double spread = 0.15;
        double x = target.getX() + (ModUtils.safeRandom.nextDouble() - 0.5) * spread;
        double z = target.getZ() + (ModUtils.safeRandom.nextDouble() - 0.5) * spread;
        double y = target.getY() + (target.getBbHeight() * 0.75) + (offsetCount * 0.2);

        // --- 5. 生成粒子 ---
        event.getEntity().level().addParticle(
                new DamageParticleOptions(
                        amount, style.mainColor, false, style.mainBold,
                        style.prefix, style.prefixColor, style.prefixBold,
                        style.suffix, style.suffixColor, style.suffixBold
                ),
                x, y, z,
                0, 0.03, 0
        );
    }

    private static HealStyle determineStyle(LivingEntity target) {
        // 直接引用你的静态配置实例
        var c = ConfigData.ScreenConfigData.healStyle;
        HealStyle s = new HealStyle();

        // 从配置中读取主颜色与加粗
        s.mainColor = parseColor(c.mainColor, 0x55FF55);
        s.mainBold = c.enableBold;

        // 从配置中读取前缀样式
        s.prefix = c.prefix;
        s.prefixColor = parseColor(c.prefixColor, 0x00AA00);
        s.prefixBold = c.enableBold;

        // 如果是玩家恢复，应用后缀配置
        if (target instanceof Player) {
            s.suffix = c.playerSuffix;
            s.suffixColor = s.mainColor;
            s.suffixBold = c.enableBold;
        }

        return s;
    }

    /**
     * 复用颜色解析逻辑
     */
    private static int parseColor(String hex, int defaultCol) {
        try {
            if (hex == null || hex.isEmpty()) return defaultCol;
            if (hex.startsWith("#")) hex = hex.substring(1);
            return (int) Long.parseLong(hex, 16);
        } catch (Exception e) {
            return defaultCol;
        }
    }

    private static class HealStyle {
        int mainColor;
        boolean mainBold = false;
        String prefix = "";
        int prefixColor = 0xFFFFFF;
        boolean prefixBold = false;
        String suffix = "";
        int suffixColor = 0xFFFFFF;
        boolean suffixBold = false;
    }
}