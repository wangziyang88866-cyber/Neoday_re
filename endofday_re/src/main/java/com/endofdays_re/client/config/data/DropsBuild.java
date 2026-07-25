package com.endofdays_re.client.config.data;

import com.endofdays_re.utils.ModUtils;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

@Config(
        name = ModUtils.MODID + "/drop"
)
public class DropsBuild implements ConfigData {
    @ConfigEntry.Category("acquisition1")
    @Comment("生物掉落物配置")
    public Map<String, DropInfo> data = new HashMap<>(Map.ofEntries(
            Map.entry(
                    "key_drop_1", new DropInfo(
                            ModUtils.getEntityTypeTranslationName(EntityType.ZOMBIE),
                            new String[]{"minecraft:zombie"},
                            new ItemInfo[]{
                                    // 1. 基础垃圾/占位 (保持高权重，模拟末世物资匮乏)
                                    new ItemInfo("占位", "minecraft:air", 550, 1, 2, 0.25, ""),
                                    new ItemInfo(ModUtils.getItemTranslationName(Items.ROTTEN_FLESH), "minecraft:rotten_flesh", 200, 1, 2, 0.25, "{}"),

                                    // 2. 完整矿石锭
                                    new ItemInfo(ModUtils.getItemTranslationName(Items.IRON_INGOT), "minecraft:iron_ingot", 40, 1, 1, 0.05, "{}"),
                                    new ItemInfo(ModUtils.getItemTranslationName(Items.COPPER_INGOT), "minecraft:copper_ingot", 50, 1, 2, 0.08, "{}")
                            },
                            0, 100
                    )
            )
    ));
    @Comment("尸体战利品表配置")
    public Map<String, DropInfo> zombie_data = new HashMap<>(Map.ofEntries(
            Map.entry(
                    "key_drop_1", new DropInfo(
                            ModUtils.getEntityTypeTranslationName(EntityType.ZOMBIE),
                            new String[]{"minecraft:zombie"},
                            new ItemInfo[]{
                                    new ItemInfo("占位", "minecraft:air", 500, 1, 2, 0.45, ""), // 略微调低占位权重，给新道具留空间
                                    new ItemInfo(ModUtils.getItemTranslationName(Items.ROTTEN_FLESH), "minecraft:rotten_flesh", 200, 1, 2, 0.45, "{}"),
                                    new ItemInfo(ModUtils.getItemTranslationName(Items.BONE), "minecraft:bone", 150, 1, 2, 0.45, "{}"),


                                    // --- 矿石/锭类资源 ---
                                    new ItemInfo(ModUtils.getItemTranslationName(Items.COPPER_INGOT), "minecraft:copper_ingot", 120, 1, 4, 0.20, "{}"),
                                    new ItemInfo(ModUtils.getItemTranslationName(Items.IRON_INGOT), "minecraft:iron_ingot", 100, 1, 3, 0.25, "{}"),
                                    new ItemInfo(ModUtils.getItemTranslationName(Items.GOLD_INGOT), "minecraft:gold_ingot", 80, 1, 2, 0.15, "{}"),
                                    new ItemInfo(ModUtils.getItemTranslationName(Items.DIAMOND), "minecraft:diamond", 30, 1, 1, 0.05, "{}"),
                                    new ItemInfo(ModUtils.getItemTranslationName(Items.NETHERITE_SCRAP), "minecraft:netherite_scrap", 10, 1, 1, 0.02, "{}"),

                                    // --- 极稀有钻石套装 ---
                                    new ItemInfo(ModUtils.getItemTranslationName(Items.DIAMOND_CHESTPLATE), "minecraft:diamond_chestplate", 2, 1, 1, 0.005, "{Enchantments:[{id:\"minecraft:protection\",lvl:4},{id:\"minecraft:unbreaking\",lvl:3}]}"),
                                    new ItemInfo(ModUtils.getItemTranslationName(Items.DIAMOND_LEGGINGS), "minecraft:diamond_leggings", 2, 1, 1, 0.005, "{Enchantments:[{id:\"minecraft:protection\",lvl:4}]}"),
                                    new ItemInfo(ModUtils.getItemTranslationName(Items.DIAMOND_BOOTS), "minecraft:diamond_boots", 2, 1, 1, 0.005, "{Enchantments:[{id:\"minecraft:protection\",lvl:4},{id:\"minecraft:feather_falling\",lvl:4}]}"),
                                    new ItemInfo(ModUtils.getItemTranslationName(Items.DIAMOND_HELMET), "minecraft:diamond_helmet", 2, 1, 1, 0.005, "{Enchantments:[{id:\"minecraft:protection\",lvl:4},{id:\"minecraft:respiration\",lvl:3}]}")
                            },
                            0, 100
                    )
            )
    ));


    public static class DropInfo {
        @Comment("实体列表")
        public String[] entitys;
        @Comment("掉落物列表")
        public ItemInfo[] items;
        @Comment("生效时间")
        public int day;
        @Comment("失效时间")
        public int end;
        @Comment("Language 键映射")
        public String Language;
        public DropInfo(String Language, String[] entitys, ItemInfo[] items, int day, int end) {
            this.entitys = entitys;
            this.items = items;
            this.day = day;
            this.end = end;
            this.Language = Language;
        }
        public DropInfo() {
            this("", new String[]{"minecraft:zombie"},
                    new ItemInfo[]{}
                    , 0, 100);
        }

    }

    public static class ItemInfo {
        @Comment("id")
        public String ItemId;
        @Comment("weight")
        public int weight;
        @Comment("min")
        public int min;
        @Comment("max")
        public int max;
        @Comment("chance")
        public double chance;
        @Comment("标签")
        public String tag;
        @Comment("Language 键映射")
        public String Language;
        public ItemInfo(String Language, String ItemId, int weight, int min, int max, double chance, String ItemTag) {
            this.ItemId = ItemId;
            this.weight = weight;
            this.min = min;
            this.max = max;
            this.chance = chance;
            this.tag = ItemTag;
            this.Language = Language;
        }
        public ItemInfo() {
            this("木棍", "minecraft:stack", 100, 1, 4, 0.25, "{}");
        }
    }


}
