package com.endofdays_re.client.config.factory;

import com.endofdays_re.client.config.data.ScreenBuild;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class ScreenConfigFactory {

    public static void build(ConfigBuilder builder, ScreenBuild config) {
        ConfigEntryBuilder eb = builder.entryBuilder();
        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("config.endofdays_re.category.screen"));

        // --- 1. 基础全局开关 ---
        category.addEntry(eb.startBooleanToggle(Component.translatable("config.endofdays_re.screen.showHud"), config.showHud)
                .setSaveConsumer(v -> config.showHud = v).setDefaultValue(true).build());
        category.addEntry(eb.startBooleanToggle(Component.translatable("config.endofdays_re.screen.isShowJoin"), config.isShowJoin)
                .setSaveConsumer(v -> config.isShowJoin = v).setDefaultValue(true).build());
        category.addEntry(eb.startBooleanToggle(Component.translatable("config.endofdays_re.screen.isTitleShow"), config.isTitleShow)
                .setSaveConsumer(v -> config.isTitleShow = v).setDefaultValue(true).build());

        // --- 2. Title 时间设置 ---
        category.addEntry(eb.startIntField(Component.translatable("config.endofdays_re.screen.joinTime"), config.joinTime)
                .setSaveConsumer(v -> config.joinTime = v).setDefaultValue(100).build());
        category.addEntry(eb.startIntField(Component.translatable("config.endofdays_re.screen.ShowTime"), config.ShowTime)
                .setSaveConsumer(v -> config.ShowTime = v).setDefaultValue(300).build());
        category.addEntry(eb.startIntField(Component.translatable("config.endofdays_re.screen.OutTime"), config.OutTime)
                .setSaveConsumer(v -> config.OutTime = v).setDefaultValue(100).build());
        category.addEntry(eb.startBooleanToggle(Component.translatable("config.endofdays_re.screen.TitleNightShow"), config.TitleNightShow)
                .setSaveConsumer(v -> config.TitleNightShow = v).setDefaultValue(false).build());

        // --- 3. 粒子系统核心控制 ---
        category.addEntry(eb.startBooleanToggle(Component.translatable("config.endofdays_re.screen.showParticles"), config.showParticles)
                .setSaveConsumer(v -> config.showParticles = v).setDefaultValue(false).build());
        category.addEntry(eb.startBooleanToggle(Component.translatable("config.endofdays_re.screen.showDamage"), config.showDamage)
                .setSaveConsumer(v -> config.showDamage = v).setDefaultValue(true).build());
        category.addEntry(eb.startBooleanToggle(Component.translatable("config.endofdays_re.screen.showHeal"), config.showHeal)
                .setSaveConsumer(v -> config.showHeal = v).setDefaultValue(true).build());

        // --- 4. 黑名单 (固定 List) ---
        category.addEntry(eb.startStrList(Component.translatable("config.endofdays_re.screen.entityBlacklist"), config.entityBlacklist)
                .setSaveConsumer(v -> config.entityBlacklist = v)
                .setDefaultValue(List.of("minecraft:armor_stand", "minecraft:warden"))
                .build());

        // --- 5. 治疗样式 (子分类) ---
        List<AbstractConfigListEntry> healEntries = new ArrayList<>();
        healEntries.add(eb.startColorField(Component.translatable("config.endofdays_re.screen.heal.mainColor"), parseColor(config.healStyle.mainColor))
                .setSaveConsumer(v -> config.healStyle.mainColor = formatColor(v)).build());
        healEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.screen.heal.prefix"), config.healStyle.prefix)
                .setSaveConsumer(v -> config.healStyle.prefix = v).build());
        healEntries.add(eb.startColorField(Component.translatable("config.endofdays_re.screen.heal.prefixColor"), parseColor(config.healStyle.prefixColor))
                .setSaveConsumer(v -> config.healStyle.prefixColor = formatColor(v)).build());
        healEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.screen.heal.playerSuffix"), config.healStyle.playerSuffix)
                .setSaveConsumer(v -> config.healStyle.playerSuffix = v).build());
        healEntries.add(eb.startBooleanToggle(Component.translatable("config.endofdays_re.screen.heal.enableBold"), config.healStyle.enableBold)
                .setSaveConsumer(v -> config.healStyle.enableBold = v).build());
        category.addEntry(eb.startSubCategory(Component.translatable("config.endofdays_re.screen.sub.heal"), healEntries).build());

        // --- 6. 伤害样式 (全字段补全子分类) ---
        List<AbstractConfigListEntry> dmgEntries = new ArrayList<>();

        // A. 基础与暴击
        dmgEntries.add(eb.startBooleanToggle(Component.translatable("config.endofdays_re.screen.dmg.enableBold"), config.damageStyle.enableBold).setSaveConsumer(v -> config.damageStyle.enableBold = v).build());
        dmgEntries.add(eb.startBooleanToggle(Component.translatable("config.endofdays_re.screen.dmg.showCrit"), config.damageStyle.showCrit).setSaveConsumer(v -> config.damageStyle.showCrit = v).build());
        dmgEntries.add(eb.startColorField(Component.translatable("config.endofdays_re.screen.dmg.critColor"), parseColor(config.damageStyle.critColor)).setSaveConsumer(v -> config.damageStyle.critColor = formatColor(v)).build());
        dmgEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.screen.dmg.critPrefix"), config.damageStyle.critPrefix).setSaveConsumer(v -> config.damageStyle.critPrefix = v).build());
        dmgEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.screen.dmg.critSuffix"), config.damageStyle.critSuffix).setSaveConsumer(v -> config.damageStyle.critSuffix = v).build());

        // B. 来源显示
        dmgEntries.add(eb.startBooleanToggle(Component.translatable("config.endofdays_re.screen.dmg.showSource"), config.damageStyle.showSourcePrefix).setSaveConsumer(v -> config.damageStyle.showSourcePrefix = v).build());
        dmgEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.screen.dmg.pPrefix"), config.damageStyle.playerPrefix).setSaveConsumer(v -> config.damageStyle.playerPrefix = v).build());
        dmgEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.screen.dmg.mPrefix"), config.damageStyle.mobPrefix).setSaveConsumer(v -> config.damageStyle.mobPrefix = v).build());

        // C. 属性伤害 (火、魔、电、冰、凋、坠、箭)
        addDmgAttr(eb, dmgEntries, "fire", config.damageStyle.showFire, v -> config.damageStyle.showFire = v, config.damageStyle.fireColor, v -> config.damageStyle.fireColor = v, config.damageStyle.fireSuffix, v -> config.damageStyle.fireSuffix = v);
        addDmgAttr(eb, dmgEntries, "magic", config.damageStyle.showMagic, v -> config.damageStyle.showMagic = v, config.damageStyle.magicColor, v -> config.damageStyle.magicColor = v, config.damageStyle.magicSuffix, v -> config.damageStyle.magicSuffix = v);
        addDmgAttr(eb, dmgEntries, "light", config.damageStyle.showLightning, v -> config.damageStyle.showLightning = v, config.damageStyle.lightningColor, v -> config.damageStyle.lightningColor = v, config.damageStyle.lightningSuffix, v -> config.damageStyle.lightningSuffix = v);
        addDmgAttr(eb, dmgEntries, "freeze", config.damageStyle.showFreeze, v -> config.damageStyle.showFreeze = v, config.damageStyle.freezeColor, v -> config.damageStyle.freezeColor = v, config.damageStyle.freezeSuffix, v -> config.damageStyle.freezeSuffix = v);
        addDmgAttr(eb, dmgEntries, "wither", config.damageStyle.showWither, v -> config.damageStyle.showWither = v, config.damageStyle.witherColor, v -> config.damageStyle.witherColor = v, config.damageStyle.witherSuffix, v -> config.damageStyle.witherSuffix = v);
        addDmgAttr(eb, dmgEntries, "fall", config.damageStyle.showFall, v -> config.damageStyle.showFall = v, config.damageStyle.fallColor, v -> config.damageStyle.fallColor = v, config.damageStyle.fallSuffix, v -> config.damageStyle.fallSuffix = v);
        addDmgAttr(eb, dmgEntries, "arrow", config.damageStyle.showArrow, v -> config.damageStyle.showArrow = v, config.damageStyle.arrowColor, v -> config.damageStyle.arrowColor = v, config.damageStyle.arrowSuffix, v -> config.damageStyle.arrowSuffix = v);

        // D. 负面状态 (流血、击晕、撕裂、骨折)
        dmgEntries.add(eb.startBooleanToggle(Component.translatable("config.endofdays_re.screen.dmg.showBleeding"), config.damageStyle.showBleeding).setSaveConsumer(v -> config.damageStyle.showBleeding = v).build());
        dmgEntries.add(eb.startColorField(Component.translatable("config.endofdays_re.screen.dmg.bleedingColor"), parseColor(config.damageStyle.bleedingColor)).setSaveConsumer(v -> config.damageStyle.bleedingColor = formatColor(v)).build());

        dmgEntries.add(eb.startBooleanToggle(Component.translatable("config.endofdays_re.screen.dmg.showStun"), config.damageStyle.showStun).setSaveConsumer(v -> config.damageStyle.showStun = v).build());
        dmgEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.screen.dmg.stunPrefix"), config.damageStyle.stunPrefix).setSaveConsumer(v -> config.damageStyle.stunPrefix = v).build());

        // E. 兜底默认
        dmgEntries.add(eb.startColorField(Component.translatable("config.endofdays_re.screen.dmg.defColor"), parseColor(config.damageStyle.defaultColor)).setSaveConsumer(v -> config.damageStyle.defaultColor = formatColor(v)).build());
        dmgEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.screen.dmg.defSuffix"), config.damageStyle.defaultSuffix).setSaveConsumer(v -> config.damageStyle.defaultSuffix = v).build());

        category.addEntry(eb.startSubCategory(Component.translatable("config.endofdays_re.screen.sub.dmg"), dmgEntries).build());
    }

    // 辅助工具：快速添加属性伤害项
    private static void addDmgAttr(ConfigEntryBuilder eb, List<AbstractConfigListEntry> list, String name, boolean show, java.util.function.Consumer<Boolean> showSetter, String color, java.util.function.Consumer<String> colorSetter, String suffix, java.util.function.Consumer<String> suffixSetter) {
        list.add(eb.startBooleanToggle(Component.translatable("config.endofdays_re.screen.dmg.show." + name), show).setSaveConsumer(showSetter).build());
        list.add(eb.startColorField(Component.translatable("config.endofdays_re.screen.dmg.color." + name), parseColor(color)).setSaveConsumer(v -> colorSetter.accept(formatColor(v))).build());
        list.add(eb.startStrField(Component.translatable("config.endofdays_re.screen.dmg.suffix." + name), suffix).setSaveConsumer(suffixSetter).build());
    }

    private static int parseColor(String hex) {
        try {
            return Integer.parseInt(hex.replace("#", ""), 16);
        } catch (Exception e) {
            return 0xFFFFFF;
        }
    }

    private static String formatColor(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }
}