package com.endofdays_re.client.config.data;

import com.endofdays_re.utils.ModUtils;
import com.endofdays_re.utils.type.LevelTimeType;
import com.endofdays_re.utils.type.ModeEventType;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.HashMap;
import java.util.Map;

@Config(
        name = ModUtils.MODID + "/msg"
)
public class LanguageBuild implements ConfigData {
    @ConfigEntry.Category("acquisition1")
    @Comment("消息")
    public Map<String, LanguageMsgData> data = new HashMap<>(Map.ofEntries(
            Map.entry("moon_msg", new LanguageMsgData(0, 100, ModUtils.MODID + ".event.moon.msg", ModeEventType.BLOOD, LevelTimeType.NIGHT, new String[]{}, 100, "")),
            Map.entry("day_msg", new LanguageMsgData(0, 100, ModUtils.MODID + ".event.day.msg", ModeEventType.TIME, LevelTimeType.DAY, new String[]{}, 100, "")),
            Map.entry("neight_msg", new LanguageMsgData(0, 100, ModUtils.MODID + ".event.neight.msg", ModeEventType.TIME, LevelTimeType.NIGHT, new String[]{}, 100, "")),
            Map.entry("blood_sleep", new LanguageMsgData(-1, -1, ModUtils.MODID + ".blood.sleep", ModeEventType.NONE, LevelTimeType.NONE, new String[]{}, 100, "")),
            Map.entry("moon_msg_end", new LanguageMsgData(0, 100, ModUtils.MODID + ".event.moon.msg.1", ModeEventType.BLOOD, LevelTimeType.DAY, new String[]{}, 100, ""))

    ));


    public static class LanguageMsgData {
        @Comment("启用时间")
        public int day;
        @Comment("结束时间")
        public int endDay;
        @Comment("Language 键映射 MSG")
        public String msg;
        @Comment("Language 键映射")
        public String lang;
        @Comment("方法类型")
        public ModeEventType eventMode;
        @Comment("触发刻度")
        public LevelTimeType time;
        @Comment("前置要求")
        public String[] pre;
        @Comment("权重")
        public int weight;
        public LanguageMsgData() {
            this(0, 100, "test", ModeEventType.BLOOD, LevelTimeType.DAY, new String[]{}, 1, "");
        }
        public LanguageMsgData(int day, int endDay, String msg, ModeEventType eventMode, LevelTimeType time, String[] pre, int weight, String name) {
            this.day = day;
            this.endDay = endDay;
            this.msg = msg;
            this.eventMode = eventMode;
            this.time = time;
            this.pre = pre;
            this.weight = weight;
            this.lang = name;
        }


    }


}
