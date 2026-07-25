package com.endofdays_re.client.config.data;

import com.endofdays_re.utils.ModUtils;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.HashMap;
import java.util.Map;

@Config(name = ModUtils.MODID + "/spawner")
public class SpawnerBuild implements ConfigData {
    @Comment("启用自定义刷怪系统")
    public boolean enable = true;
    @Comment("维度白名单（留空表示所有维度）")
    public String[] allowed_dimensions = new String[]{"minecraft:overworld"};
    @Comment("全局最大僵尸数量（-1表示不限制）")
    public int max_total_entities = 150;

    @Comment("阶段配置列表（按天数自动切换）")
    public Map<String, StageConfig> stage_configs = new HashMap<>(
            Map.ofEntries(
                    Map.entry("stage_1", new StageConfig(0, 20, "第一阶段：初期威胁",
                            2, 3, 400, 32, 120, 32, 64, true, true,
                            Map.ofEntries(Map.entry("zombie", new EntityConfig("minecraft:zombie", 100, "{IsBaby:0b}", new AttributeConfig[]{}, new EquipmentConfig[]{})))
                    )),      // 第0-20天
                    Map.entry("stage_2", new StageConfig(21, 40, "第二阶段：逐渐增强",
                            3, 4, 340, 32, 120, 32, 64, true, true,
                            Map.ofEntries(Map.entry("zombie", new EntityConfig("minecraft:zombie", 100, "{IsBaby:0b}", new AttributeConfig[]{}, new EquipmentConfig[]{})))
                    )),     // 第21-40天
                    Map.entry("stage_3", new StageConfig(41, 60, "第三阶段：中等难度",
                            4, 5, 280, 32, 120, 32, 64, true, true,
                            Map.ofEntries(Map.entry("zombie", new EntityConfig("minecraft:zombie", 100, "{IsBaby:0b}", new AttributeConfig[]{}, new EquipmentConfig[]{})))
                    )),     // 第41-60天
                    Map.entry("stage_4", new StageConfig(61, 80, "第四阶段：高度危险",
                            5, 6, 200, 32, 120, 32, 64, false, false,
                            Map.ofEntries(Map.entry("zombie", new EntityConfig("minecraft:zombie", 100, "{IsBaby:0b}", new AttributeConfig[]{}, new EquipmentConfig[]{})))
                    )),     // 第61-80天，70%尸壳，30%僵尸
                    Map.entry("stage_5", new StageConfig(81, 100, "第五阶段：末日降临",
                            6, 7, 100, 32, 120, 32, 64, false, false,
                            Map.ofEntries(Map.entry("zombie", new EntityConfig("minecraft:zombie", 80, "{IsBaby:0b}", new AttributeConfig[]{}, new EquipmentConfig[]{})), Map.entry("husk", new EntityConfig("minecraft:husk", 20, "{IsBaby:0b}", new AttributeConfig[]{}, new EquipmentConfig[]{})))
                    ))
            )
    );

    public static class EntityConfig {
        @Comment("实体ID")
        public String entity_id;
        @Comment("生成权重")
        public int weight;
        @Comment("NBT标签")
        public String nbt_tag;
        @Comment("属性配置")
        public AttributeConfig[] attributes;
        @Comment("装备配置")
        public EquipmentConfig[] equipments;

        public EntityConfig(String entity_id, int weight, String nbt_tag, AttributeConfig[] attributes, EquipmentConfig[] equipments) {
            this.entity_id = entity_id;
            this.weight = weight;
            this.nbt_tag = nbt_tag;
            this.attributes = attributes;
            this.equipments = equipments;
        }

        public EntityConfig() {
            this("minecraft:zombie", 100, "", new AttributeConfig[]{}, new EquipmentConfig[]{});
        }
    }

    public static class AttributeConfig {
        @Comment("属性ID")
        public String attribute_id;
        @Comment("计算公式（支持day变量）")
        public String formula;

        public AttributeConfig(String attribute_id, String formula) {
            this.attribute_id = attribute_id;
            this.formula = formula;
        }

        public AttributeConfig() {
            this("", "");
        }
    }

    public static class EquipmentConfig {
        @Comment("物品ID")
        public String item_id;
        @Comment("装备槽位（mainhand, offhand, head, chest, legs, feet）")
        public String slot;
        @Comment("穿戴概率")
        public float probability;

        public EquipmentConfig(String item_id, String slot, float probability) {
            this.item_id = item_id;
            this.slot = slot;
            this.probability = probability;
        }

        public EquipmentConfig() {
            this("", "", 0.0f);
        }
    }

    public static class StageConfig {
        @Comment("阶段开始天数")
        public int start_day;
        @Comment("阶段结束天数（-1表示无限）")
        public int end_day;
        @Comment("阶段描述")
        public String description;
        @Comment("最大刷怪组数")
        public int max_groups;
        @Comment("每组最大实体数")
        public int max_per_group;
        @Comment("刷怪检查间隔（tick），20 tick = 1秒")
        public int check_interval;
        @Comment("水平刷怪范围最小值（格）")
        public int spawn_range_min;
        @Comment("水平刷怪范围最大值（格）")
        public int spawn_range_max;
        @Comment("垂直刷怪范围最小值（Y轴）")
        public int vertical_range_min;
        @Comment("垂直刷怪范围最大值（Y轴）")
        public int vertical_range_max;
        @Comment("是否只在晚上刷怪")
        public boolean only_spawn_at_night;
        @Comment("是否检查光照等级（开启后有火把的地方不刷怪）")
        public boolean check_light_level;
        @Comment("阶段专属实体配置")
        public Map<String, EntityConfig> entity_configs;

        public StageConfig(int start_day, int end_day, String description,
                           int max_groups, int max_per_group, int check_interval,
                           int spawn_range_min, int spawn_range_max,
                           int vertical_range_min, int vertical_range_max,
                           boolean only_spawn_at_night, boolean check_light_level,
                           Map<String, EntityConfig> entity_configs) {
            this.start_day = start_day;
            this.end_day = end_day;
            this.description = description;
            this.max_groups = max_groups;
            this.max_per_group = max_per_group;
            this.check_interval = check_interval;
            this.spawn_range_min = spawn_range_min;
            this.spawn_range_max = spawn_range_max;
            this.vertical_range_min = vertical_range_min;
            this.vertical_range_max = vertical_range_max;
            this.only_spawn_at_night = only_spawn_at_night;
            this.check_light_level = check_light_level;
            this.entity_configs = entity_configs;
        }

        public StageConfig() {
            this(1, 20, "未命名阶段", 2, 3, 160, 32, 120, 32, 64, false, true,
                    Map.ofEntries(
                            Map.entry("zombie", new EntityConfig("minecraft:zombie", 100, "{IsBaby:0b}", new AttributeConfig[]{}, new EquipmentConfig[]{}))
                    )
            );
        }
    }
}
