package com.endofdays_re.client.config.data;

import com.endofdays_re.utils.ModUtils;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.HashMap;
import java.util.Map;

@Config(
        name = ModUtils.MODID + "/invasion"
)

public class InvasionBuild implements ConfigData {

    @Comment("入侵可能性冷却间隔/24000为1天")
    public long max_time = 24000L;


    @Comment("入侵配置列表")
    public Map<String, InvasionBuild.InvasionSettings> invasionSettings = new HashMap<>(
            Map.ofEntries(
                    Map.entry("zombie_horde", new InvasionBuild.InvasionSettings(
                            "zombie_horde",      // key: 标识ID
                            100,                 // weight: 权重
                            new InvasionBuild.range(15, 45), // pos_range: 距离玩家 15-45 格
                            12,                  // pos_max: 附近最大选点数
                            30,                  // maxEntity: 该类型入侵最大实体上限
                            new InvasionBuild.EntitySetting[]{
                                    new InvasionBuild.EntitySetting(
                                            "minecraft:zombie",   // id: 实体ID
                                            new InvasionBuild.range(5, 12), // max_count: 尝试生成次数
                                            "{IsInvasion:1b}",    // tag: NBT 标签
                                            new InvasionBuild.EffectSetting[]{
                                                    new InvasionBuild.EffectSetting("minecraft:speed", new InvasionBuild.range(0, 1), new InvasionBuild.range(600, 2400), false, 0.3f)
                                            },
                                            new InvasionBuild.ArrmorSetting[]{
                                                    new InvasionBuild.ArrmorSetting("minecraft:leather_chestplate", "chest", "{display:{color:16711680}}", 80, false, 0.5f),
                                                    new InvasionBuild.ArrmorSetting("minecraft:iron_sword", "mainhand", "", 250, true, 0.2f)
                                            },
                                            new InvasionBuild.AttributeSetting[]{
                                                    new InvasionBuild.AttributeSetting("generic.max_health", "30")         // evl: 增加血量
                                            },
                                            1.0f // probability: 成功生成概率
                                    )
                            },
                            new String[]{"minecraft:overworld"}, // dim: 触发维度
                            new InvasionBuild.range(13000, 21000), // time_range: 触发时间段 (深夜)
                            0.6f,                // probability: 成功发动入侵的概率
                            new InvasionBuild.range(3, 9) // InvasionCount: 发动 3 到 5 波
                            , 0, 100))//生效时间
            )
    );

    public static class InvasionSettings {
        @Comment("标识ID")
        public String key;
        @Comment("权重")
        public int weight;
        @Comment("发动入侵尝试波次")
        public range InvasionCount;
        @Comment("成功概率")
        public float probability;
        @Comment("触发时间段")
        public range time_range;
        @Comment("触发允许世界")
        public String[] dim;
        @Comment("实体配置")
        public EntitySetting[] entitySetting;
        @Comment("当前标识ID的入侵配置允许的最大实体上限")
        public int maxEntity;
        @Comment("玩家附近最大选点数量")
        public int pos_max;
        @Comment("距离玩家的范围")
        public range pos_range;
        @Comment("生效日")
        public int day_;
        @Comment("失效日")
        public int end_day;
        public InvasionSettings(String key, int weight, range pos_range, int pos_max, int maxEntity, EntitySetting[] entitySetting, String[] dim, range time_range, float probability, range invasionCount, int day, int end_day) {
            this.pos_range = pos_range;
            this.pos_max = pos_max;
            this.maxEntity = maxEntity;
            this.entitySetting = entitySetting;
            this.dim = dim;
            this.time_range = time_range;
            this.probability = probability;
            InvasionCount = invasionCount;
            this.weight = weight;
            this.key = key;
            this.day_ = day;
            this.end_day = end_day;
        }
        public InvasionSettings() {
            this("", 0, new range(), 0, 0, new EntitySetting[]{}, new String[]{}, new range(), 0.0f, new range(), 0, 100);
        }


    }


    public static class EntitySetting {
        @Comment("实体id")
        public String id;
        @Comment("尝试生成次数")
        public range max_count;
        @Comment("实体标签")
        public String tag;
        @Comment("实体效果配置")
        public EffectSetting[] effectSetting;
        @Comment("实护甲配置")
        public ArrmorSetting[] arrmorSetting;
        @Comment("实体属性设置")
        public AttributeSetting[] attributeSetting;
        @Comment("成功生成概率")
        public float probability;
        public EntitySetting(String id, range max_count, String tag, EffectSetting[] effectSetting, ArrmorSetting[] arrmorSetting, AttributeSetting[] attributeSetting, float probability) {
            this.id = id;
            this.max_count = max_count;
            this.tag = tag;
            this.effectSetting = effectSetting;
            this.arrmorSetting = arrmorSetting;
            this.attributeSetting = attributeSetting;
            this.probability = probability;
        }
        public EntitySetting() {
            this("", new range(), "", new EffectSetting[]{}, new ArrmorSetting[]{}, new AttributeSetting[]{}, 0.0f);
        }
    }

    public static class AttributeSetting {
        @Comment("属性ID")
        public String id;
        @Comment("EVL计算公式")
        public String evl;

        public AttributeSetting(String id, String evl) {
            this.id = id;
            this.evl = evl;
        }
        public AttributeSetting() {
            this("", "");
        }
    }


    public static class ArrmorSetting {
        @Comment("物品ID")
        public String arrmor_id;
        @Comment("插槽id")
        public String slot;
        @Comment("标签")
        public String tag;
        @Comment("耐久程度")
        public int durability;
        @Comment("掉落")
        public boolean on_drop;
        @Comment("穿戴概率")
        public float probability;
        public ArrmorSetting(String arrmor_id, String slot, String tag, int durability, boolean on_drop, float probability) {
            this.arrmor_id = arrmor_id;
            this.slot = slot;
            this.tag = tag;
            this.durability = durability;
            this.on_drop = on_drop;
            this.probability = probability;
        }
        public ArrmorSetting() {
            this("", "", "", 1600, false, 0.0f);
        }

    }


    public static class EffectSetting {
        @Comment("效果id")
        public String ef_id;
        @Comment("允许等级")
        public range level;
        @Comment("持续时间")
        public range leftTime;
        @Comment("效果粒子显示")
        public boolean show_ef;
        @Comment("添加概率")
        public float probability;
        public EffectSetting(String ef_id, range level, range leftTime, boolean show_ef, float probability) {
            this.ef_id = ef_id;
            this.level = level;
            this.leftTime = leftTime;
            this.show_ef = show_ef;
            this.probability = probability;
        }
        public EffectSetting() {
            this("", new range(), new range(), false, 0.0f);
        }
    }


    public static class range {
        @Comment("最小允许")
        public int min;
        @Comment("最大允许")
        public int max;

        public range(int min, int max) {
            this.min = min;
            this.max = max;
        }
        public range() {
            this(0, 1);
        }

    }


}
