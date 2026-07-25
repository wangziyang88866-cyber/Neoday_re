package com.endofdays_re.client.config.data;

import com.endofdays_re.utils.ModUtils;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.HashMap;
import java.util.Map;

@Config(
        name = ModUtils.MODID + "/entity"
)
public class ArrmorBuild implements ConfigData {

    @Comment("盔甲尝试生成次数")
    public int ArrmorSpawnMax = 4;
    @Comment("僵尸盔甲生成配置")
    public Map<String, Arrmor> Arrmor = new HashMap<>(
            Map.ofEntries(
                    // 钻石套
                    Map.entry("钻石头", new Arrmor(
                            "minecraft:diamond_helmet",
                            0.05,
                            true,
                            new Enchante[]{},
                            "{}",
                            EquipmentSlot.HEAD,
                            45,
                            100
                    )),
                    Map.entry("钻石胸甲", new Arrmor(
                            "minecraft:diamond_chestplate",
                            0.03,
                            true,
                            new Enchante[]{},
                            "{}",
                            EquipmentSlot.CHEST,
                            80,
                            150
                    )),
                    Map.entry("钻石裤子", new Arrmor(
                            "minecraft:diamond_leggings",
                            0.04,
                            true,
                            new Enchante[]{},
                            "{}",
                            EquipmentSlot.LEGS,
                            70,
                            120
                    )),
                    Map.entry("钻石靴子", new Arrmor(
                            "minecraft:diamond_boots",
                            0.06,
                            true,
                            new Enchante[]{},
                            "{}",
                            EquipmentSlot.FEET,
                            50,
                            90
                    )),

                    // 铁套（可附魔）
                    Map.entry("铁头", new Arrmor(
                            "minecraft:iron_helmet",
                            0.5,
                            true,
                            new Enchante[]{
                                    new Enchante("minecraft:protection", 0.5, new EnchanteLevel(1, 3)),
                                    new Enchante("minecraft:unbreaking", 0.3, new EnchanteLevel(1, 2))
                            },
                            "{}",
                            EquipmentSlot.HEAD,
                            20,
                            80
                    )),
                    Map.entry("铁胸甲", new Arrmor(
                            "minecraft:iron_chestplate",
                            0.5,
                            true,
                            new Enchante[]{
                                    new Enchante("minecraft:protection", 0.6, new EnchanteLevel(1, 4))
                            },
                            "{}",
                            EquipmentSlot.CHEST,
                            20,
                            80
                    )),
                    Map.entry("铁裤子", new Arrmor(
                            "minecraft:iron_leggings",
                            0.5,
                            true,
                            new Enchante[]{
                                    new Enchante("minecraft:protection", 0.5, new EnchanteLevel(1, 3))
                            },
                            "{}",
                            EquipmentSlot.LEGS,
                            20,
                            80
                    )),
                    Map.entry("铁靴子", new Arrmor(
                            "minecraft:iron_boots",
                            0.5,
                            true,
                            new Enchante[]{
                                    new Enchante("minecraft:feather_falling", 0.4, new EnchanteLevel(1, 2))
                            },
                            "{}",
                            EquipmentSlot.FEET,
                            20,
                            80
                    ))
            )
    );

    public static class Arrmor {
        @Comment("盔甲ID")
        public String id;
        @Comment("盔甲生成可能性")
        public double chance;
        @Comment("应该携带附魔")
        public boolean enchanted;
        @Comment("附魔配置表")
        public Enchante[] enchantes;
        @Comment("标签")
        public String tag;
        @Comment("插槽名称")
        public EquipmentSlot slot;
        @Comment("配置什么时候启用 基于天数时间")
        public int day;
        @Comment("配置什么时候结束使用 基于天数时间")
        public int end_day;


        public Arrmor(String id, double chance, boolean enchanted, Enchante[] enchantes, String tag, EquipmentSlot slot, int day, int end_day) {
            this.id = id;
            this.chance = chance;
            this.enchanted = enchanted;
            this.enchantes = enchantes;
            this.tag = tag;
            this.slot = slot;
            this.day = day;
            this.end_day = end_day;
        }

        public Arrmor() {
            this("", 0.15, false, new Enchante[]{}, "", EquipmentSlot.HEAD, 0, 0);
        }
    }

    public static class Enchante {
        @Comment("附魔ID")
        public String id;
        @Comment("附魔生成可能性")
        public double chance;
        @Comment("附魔等级配置")
        public EnchanteLevel level;

        public Enchante(String id, double chance, EnchanteLevel level) {
            this.id = id;
            this.chance = chance;
            this.level = level;
        }

        public Enchante() {
            this("", 0.15, new EnchanteLevel());
        }

    }

    public static class EnchanteLevel {
        @Comment("附魔允许最小级别")
        public int level;
        @Comment("附魔允许最大级别")
        public int maxLevel;

        public EnchanteLevel(int level, int maxLevel) {
            this.level = level;
            this.maxLevel = maxLevel;
        }
        public EnchanteLevel() {
            this(1, 5);
        }
    }


}
