package com.endofdays_re.client.config.data;

import com.endofdays_re.utils.ModUtils;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.HashMap;
import java.util.Map;

@Config(
        name = ModUtils.MODID + "/day"
)

public class Daybuild implements ConfigData {

    @ConfigEntry.Category("acquisition1")
    @Comment("游戏天数设置")
    public Map<String, DayData> data = new HashMap<>(Map.ofEntries(
            Map.entry("enable", new DayData(0, 100, ModUtils.MODID + ".day.game.enable", 0)),
            Map.entry("attribute", new DayData(4, 100, ModUtils.MODID + ".day.entity.attribute", 4)),
            Map.entry("place_block", new DayData(32, 100, ModUtils.MODID + ".day.entity.goal.place_block", 0)),
            Map.entry("break_block", new DayData(24, 100, ModUtils.MODID + ".day.entity.goal.break_block", 11)),
            Map.entry("follow", new DayData(5, 100, ModUtils.MODID + ".day.entity.goal.follow", 5)),
            Map.entry("place_tnt", new DayData(45, 100, ModUtils.MODID + ".day.entity.goal.place_tnt", 45)),
            Map.entry("fishing", new DayData(57, 100, ModUtils.MODID + ".day.entity.goal.use.fishing", 57)),
            Map.entry("trident", new DayData(32, 100, ModUtils.MODID + ".day.entity.goal.use.trident", 32)),
            Map.entry("bow", new DayData(20, 100, ModUtils.MODID + ".day.entity.goal.use.bow", 20)),
            Map.entry("shield", new DayData(50, 100, ModUtils.MODID + ".day.entity.goal.use.shield", 50)),
            Map.entry("place_fluid", new DayData(60, 100, ModUtils.MODID + ".day.entity.goal.use.place_fluid", 60)),
            Map.entry("fire", new DayData(45, 100, ModUtils.MODID + ".day.entity.immune.lava", 45)),
            Map.entry("jump", new DayData(28, 100, ModUtils.MODID + ".day.entity.goal.use.jump", 28)),
            Map.entry("rebirth", new DayData(65, 100, ModUtils.MODID + ".day.entity.rebirth", 65)),
            Map.entry("replace", new DayData(19, 100, ModUtils.MODID + ".day.entity.replace", 19)),
            Map.entry("pearls", new DayData(35, 100, ModUtils.MODID + ".day.entity.goal.use.pearls", 69)),
            Map.entry("immune_sun", new DayData(35, 100, ModUtils.MODID + ".day.entity.immune.sun", 7)),
            Map.entry("potions", new DayData(35, 100, ModUtils.MODID + ".day.entity.use.potions", 12)),
            Map.entry("ride", new DayData(35, 100, ModUtils.MODID + ".day.entity.use.ride", 48)),
            Map.entry("dispenser", new DayData(35, 100, ModUtils.MODID + ".day.entity.use.dispenser", 64)),
            Map.entry("barker_vehicle", new DayData(35, 100, ModUtils.MODID + ".day.entity.bark.barker_vehicle", 48)),
            Map.entry("spawn_tacz", new DayData(35, 100, ModUtils.MODID + ".day.entity.spawn.spawn_tacz", 24)),
            Map.entry("equip", new DayData(35, 100, ModUtils.MODID + ".day.entity.spawn.spawn_equip", 16)),
            Map.entry("fly", new DayData(35, 100, ModUtils.MODID + ".day.entity.spawn.spawn_fly", 49)),
            Map.entry("gigantic", new DayData(7, 100, ModUtils.MODID + ".day.entity.spawn.spawn_gigantic", 49)),
            Map.entry("entity_climb", new DayData(7, 100, ModUtils.MODID + ".day.entity.spawn.entity_climb", 32)),
            Map.entry("break_target_block", new DayData(7, 100, ModUtils.MODID + ".day.entity.use.break_target_block", 19)),
            Map.entry("picked_target_container", new DayData(7, 100, ModUtils.MODID + ".day.entity.use.picked_target_container", 30)),
            Map.entry("enable_temp", new DayData(7, 100, ModUtils.MODID + ".day.level.enable_temp", 70)),
            Map.entry("spawn_tnt_zombie", new DayData(75, 100, ModUtils.MODID + ".day.level.spawn_tnt_zombie", 70))


    ));


    public static class DayData {
        @Comment("生效时间")
        public int day;
        @Comment("失效时间")
        public int endDay;
        @Comment("Language 键映射")
        public String lang;
        @Comment("默认值")
        public int DefaultValue;
        public DayData(int day, int endDay, String lang, int defaultValue) {
            this.day = day;
            this.endDay = endDay;
            this.lang = lang;
            this.DefaultValue = defaultValue;
        }
        public DayData() {
            this(0, 100, "", 0);
        }
    }
}
