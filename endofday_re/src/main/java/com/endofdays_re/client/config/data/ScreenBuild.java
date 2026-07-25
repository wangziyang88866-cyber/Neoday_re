package com.endofdays_re.client.config.data;

import com.endofdays_re.utils.ModUtils;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.List;

@Config(
        name = ModUtils.MODID + "/screen"
)
public class ScreenBuild implements ConfigData {
    @Comment("显示HUD信息")
    public boolean showHud = true;
    @Comment("显示加入信息")
    public boolean isShowJoin = true;
    @Comment("显示屏幕标题")
    public boolean isTitleShow = true;
    @Comment("Title淡入Time-越大淡入越慢")
    public int joinTime = 100;
    @Comment("Title显示Time-显示停留时间")
    public int ShowTime = 300;
    @Comment("Title淡出Time-越大淡出越慢")
    public int OutTime = 100;
    @Comment("Title显示夜晚标题")
    public boolean TitleNightShow = false;
    @Comment("显示药水粒子效果")
    public boolean showParticles = false;
    @Comment("显示伤害粒子")
    public boolean showDamage = true;
    @Comment("显示治疗粒子")
    public boolean showHeal = true;
    @ConfigEntry.Gui.CollapsibleObject
    @Comment("详细的治疗粒子样式配置")
    public HealStyleConfig healStyle = new HealStyleConfig();
    @ConfigEntry.Gui.CollapsibleObject
    @Comment("详细的伤害粒子样式配置")
    public DamageStyleConfig damageStyle = new DamageStyleConfig();
    @Comment("伤害/治疗数字黑名单 (实体注册名, 如 minecraft:armor_stand)")
    public List<String> entityBlacklist = List.of(
            "minecraft:armor_stand",
            "minecraft:warden" // 在这里加入监守者
    );

    public static class DamageStyleConfig {
        // --- 基础控制 ---
        @Comment("是否开启加粗显示 (影响魔法、凋零等)")
        public boolean enableBold = true;

        // --- 暴击样式 ---
        @Comment("显示暴击提示")
        public boolean showCrit = true; // 新增开关
        @Comment("暴击时的数字颜色")
        public String critColor = "#FFCC00";
        @Comment("暴击前缀")
        public String critPrefix = "\uD83D\uDCA5 CRIT";
        @Comment("暴击后缀")
        public String critSuffix = "!!";

        // --- 来源前缀 ---
        @Comment("显示伤害来源前缀 (P/M)")
        public boolean showSourcePrefix = true; // 新增开关
        @Comment("玩家伤害前缀")
        public String playerPrefix = "P";
        @Comment("生物伤害前缀")
        public String mobPrefix = "M";

        // --- 属性伤害细节 (带开关) ---
        @Comment("显示火焰伤害")
        public boolean showFire = true;
        public String fireColor = "#FFAC33";
        public String fireSuffix = "\uD83D\uDD25";

        @Comment("显示魔法伤害")
        public boolean showMagic = true;
        public String magicColor = "#55FFFF";
        public String magicSuffix = "✨";

        @Comment("显示闪电伤害")
        public boolean showLightning = true;
        public String lightningColor = "#FFFFFF";
        public String lightningSuffix = "⚡";

        @Comment("显示冰冻伤害")
        public boolean showFreeze = true;
        public String freezeColor = "#71A5FF";
        public String freezeSuffix = "❄";

        @Comment("显示凋零伤害")
        public boolean showWither = true;
        public String witherColor = "#333333";
        public String witherSuffix = "☠";

        @Comment("显示坠落伤害")
        public boolean showFall = true;
        public String fallColor = "#AAAAAA";
        public String fallSuffix = "⇣";

        @Comment("显示远程伤害")
        public boolean showArrow = true;
        public String arrowColor = "#DDDDDD";
        public String arrowSuffix = "➹";

        // --- 核心负面效果样式 (含开关) ---
        @Comment("是否显示流血样式")
        public boolean showBleeding = true;
        public String bleedingColor = "#FF2222";
        public String bleedingSuffix = "🩸";

        @Comment("是否显示击晕样式")
        public boolean showStun = true;
        public String stunColor = "#FFD700";
        public String stunPrefix = "💫 ";

        @Comment("是否显示撕裂样式")
        public boolean showLacerate = true;
        public String lacerateColor = "#FF6B35";
        public String laceratePrefix = "✖ ";

        @Comment("是否显示骨折样式")
        public boolean showFracture = true;
        public String fractureColor = "#8B4513";
        public String fractureSuffix = "🦴";

        // --- 默认兜底 ---
        @Comment("默认主颜色")
        public String defaultColor = "#EEEEEE";
        @Comment("默认后缀图标")
        public String defaultSuffix = "⚔";
        @Comment("默认后缀颜色")
        public String defaultSuffixColor = "#888888";
    }

    public static class HealStyleConfig {

        @Comment("治疗主颜色 (十六进制)")
        public String mainColor = "#55FF55";
        @Comment("治疗前缀 (如 +)")
        public String prefix = "+";
        @Comment("前缀颜色")
        public String prefixColor = "#00AA00";
        @Comment("玩家专属后缀 (如 HP)")
        public String playerSuffix = " HP";
        @Comment("是否开启加粗显示")
        public boolean enableBold = true;
    }


}
