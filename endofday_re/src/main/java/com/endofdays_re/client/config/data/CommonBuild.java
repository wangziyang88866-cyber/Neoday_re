package com.endofdays_re.client.config.data;

import com.endofdays_re.utils.ModUtils;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Config(
        name = ModUtils.MODID + "/common"
)
public class CommonBuild implements ConfigData {
    @Comment("追踪方块扫描间隔单位为秒,作用于僵尸寻找破坏方块,过小的值可能导致性能问题,如果发现在僵尸量很多的情况下TPS狂掉请调整此项")
    public int follow_block_scan_interval = 3;
    @Comment("基础温度<=-10[地表转为雪地]树木枯萎,[>=30]地表转为沙地,树木枯萎.")
    public float temperature = 30;

    @Comment("每日尝试抽取出售多少商品上限 当值 -1 表示根据已添加商品总量进行")
    public int max_count = -1;


    @Comment("析光熔融器 黑名单列表")
    public List<String> SMELT_BLACKLIST = List.of(
            "minecraft:rotten_flesh",   // 具体的 ID
            "#minecraft:logs",          // 所有原木标签
            "#minecraft:planks",        // 所有木板标签
            "#forge:cobblestone",       // 所有圆石标签
            "minecraft:sand"            // 具体的 ID
    );
    @Comment("自动同步全局数据包间隔,每隔5秒,越小的值可能对服务端的压力越大,但是越小的值客户端更新数据越快,针对非本地服务端.-1禁用")
    public int max_time = 50;
    @Comment("开启天数滞留")
    public boolean enable_stuck_day = false;
    @Comment("滞留间隔每多少天为1天")
    public long Retention_interval = 7;
    @Comment("允许生成尸体")
    public boolean enable_corpse = false;
    @Comment("一个范围内生成多少尸体")
    public int corpse_max = 6;


    @Comment("游戏模块设置")
    public Map<String, CommonData_Int> commonData = new HashMap<>(Map.ofEntries(
            Map.entry("follow_range", new CommonData_Int(16, 16, 32, ModUtils.MODID + ".common.follow_range", 16))
    ));
    public Map<String, CommonData_Float> commonFloat = new HashMap<>(Map.ofEntries(
            Map.entry("place_tnt", new CommonData_Float(500.0f, 1.0f, 1000.0f, ModUtils.MODID + ".common.use.probability.tnt", 0.5f)),
            Map.entry("spawn_tnt_zombie", new CommonData_Float(250.0f, 1.0f, 1000.0f, ModUtils.MODID + ".common.spawner.probability.tnt_zombie", 0.25f)),
            Map.entry("use_fishing", new CommonData_Float(250.0f, 1.0f, 1000.0f, ModUtils.MODID + ".common.use.probability.fishing", 0.25f)),
            Map.entry("spawn_fishing_zombie", new CommonData_Float(250.0f, 1.0f, 1000.0f, ModUtils.MODID + ".common.spawner.probability.fishing_zombie", 0.25f)),
            Map.entry("use_trident", new CommonData_Float(450.0f, 1.0f, 1000.0f, ModUtils.MODID + ".common.use.probability.trident", 0.45f)),
            Map.entry("spawn_trident_zombie", new CommonData_Float(250.0f, 1.0f, 1000.0f, ModUtils.MODID + ".common.spawner.probability.trident_zombie", 0.25f)),
            Map.entry("use_bow", new CommonData_Float(290.0f, 1.0f, 1000.0f, ModUtils.MODID + ".common.use.probability.bow", 0.29f)),
            Map.entry("spawn_bow_zombie", new CommonData_Float(250.0f, 1.0f, 1000.0f, ModUtils.MODID + ".common.spawner.probability.bow_zombie", 0.25f)),
            Map.entry("use_shield", new CommonData_Float(290.0f, 1.0f, 1000.0f, ModUtils.MODID + ".common.use.probability.shield", 0.29f)),
            Map.entry("spawn_zombie_shield", new CommonData_Float(500f, 1.0f, 1000.0f, ModUtils.MODID + ".common.spawner.probability.shield_zombie", 0.5f)),
            Map.entry("use_pearls", new CommonData_Float(150.0f, 1.0f, 1000.0f, ModUtils.MODID + ".common.use.probability.pearls", 0.15f)),
            Map.entry("spawn_zombie_pearls", new CommonData_Float(260.0f, 1.0f, 1000.0f, ModUtils.MODID + ".common.spawner.probability.pearls", 0.26f)),
            Map.entry("probability_jump", new CommonData_Float(450.0f, 1.0f, 1000.0f, ModUtils.MODID + ".common.probability.jump", 0.45f)),
            Map.entry("spawn_break_zombie", new CommonData_Float(550.0f, 1.0f, 1000.0f, ModUtils.MODID + ".common.spawner.break", 0.45f)),
            Map.entry("spawn_tacz_zombie", new CommonData_Float(290.0f, 1.0f, 1000.0f, ModUtils.MODID + ".common.spawner.tacz", 0.45f)),
            Map.entry("spawn_dispenser_zombie", new CommonData_Float(270.0f, 1.0f, 1000.0f, ModUtils.MODID + ".common.spawner.dispenser", 0.45f)),
            Map.entry("spawn_ride_zombie", new CommonData_Float(220.0f, 1.0f, 1000.0f, ModUtils.MODID + ".common.spawner.ride", 0.20f)),
            Map.entry("place_block_zombie_spawn", new CommonData_Float(440.0f, 1.0f, 1000.0f, ModUtils.MODID + ".common.spawner.place_block_zombie", 0.50f)),
            Map.entry("spawn_tnt_zombie_ca", new CommonData_Float(440.0f, 1.0f, 1000.0f, ModUtils.MODID + ".common.spawner.spawn_tnt_zombie_ca", 0.50f))


    ));
    @Comment("敌对目标实体")
    public Map<String, TargetSelect> Target = new HashMap<>(
            Map.ofEntries(
                    Map.entry("村民", new TargetSelect("minecraft:zombie", "minecraft:villager")),
                    Map.entry("僵尸", new TargetSelect("minecraft:zombie", "minecraft:player")),
                    Map.entry("坚守者", new TargetSelect("minecraft:zombie", "minecraft:warden"))
            )
    );

    @Comment("替换的目标实体")
    public Map<String, String> replace_ = new HashMap<>(
            Map.ofEntries(
                    Map.entry("minecraft:MONSTER", "minecraft:zombie")
            )
    );
    @Comment("方块破坏黑名单")
    public Map<String, String> banlist = new HashMap<>(
            Map.ofEntries(
                    Map.entry("黑曜石", "minecraft:obsidian")
            )
    );
    @Comment("僵尸应该主动破坏什么方块,regex前缀为正则表达式")
    public Map<String, String> FollowBlockBreak = new HashMap<>(
            Map.ofEntries(
                    Map.entry("所有箱子", "regex:^minecraft:.*chest$"),
                    Map.entry("火把", "regex:^minecraft:*torch$"),
                    Map.entry("灵魂火把", "minecraft:soul_torch"),
                    Map.entry("工作台", "minecraft:crafting_table"),
                    Map.entry("所有床", "regex:^minecraft:.*_bed$")
            )
    );
    @Comment("僵尸偷盗配置 container前缀为可偷盗的容器,items前缀为可偷盗的物品,regex前缀为正则表达式")
    public Map<String, String> EquipChestMob = new HashMap<>(
            Map.ofEntries(
                    Map.entry("所有箱子", "container:regex:^minecraft:.*chest$"),
                    Map.entry("所有护甲", "items:regex:^minecraft:.*_(helmet|chestplate|leggings|boots)$"),
                    Map.entry("金苹果", "items:minecraft:golden_apple"),
                    Map.entry("附魔金苹果", "items:minecraft:enchanted_golden_apple"),
                    Map.entry("不死图腾", "items:minecraft:totem_of_undying")
            )
    );
    @Comment("Tacz枪械配置")
    public Map<String, TaczData> taczData = new HashMap<>(
            Map.ofEntries(
                    Map.entry("ak47", new TaczData("ak47", "AUTO", false, 40, 22, 1.0f, 12)),  // 0.6秒一发，常见
                    Map.entry("m700", new TaczData("m700", "SEMI", false, 25, 40, 0.9f, 45)),  // 2.25秒一发，拉栓
                    Map.entry("m95", new TaczData("m95", "SEMI", false, 10, 55, 0.7f, 85)),  // 4.25秒一发，重型狙击
                    Map.entry("deagle", new TaczData("deagle", "SEMI", false, 60, 15, 1.1f, 20)),  // 1.0秒一发，手枪
                    Map.entry("rpg7", new TaczData("rpg7", "UNKNOWN", false, 5, 32, 0.6f, 180)), // 9秒一发，拆迁精英
                    Map.entry("aa12", new TaczData("aa12", "SEMI", false, 15, 10, 1.05f, 60)) //4秒
            )
    );

    public static class TargetSelect {
        @Comment("攻击者")
        public String mobId;
        @Comment("允许被攻击者")
        public String Target;

        public TargetSelect() {
            this("minecraft:zombie", "minecraft:pig");
        }
        public TargetSelect(String mobId, String target) {
            this.mobId = mobId;
            Target = target;
        }
    }

    public static class TaczData {
        @Comment("枪械id")
        public String id;
        @Comment("射击模式")
        public String FireMode;
        @Comment("需要弹药?")
        public boolean AmmoInBarrel;
        @Comment("生成权重")
        public int weight;
        @Comment("攻击半径")
        public int radius;
        @Comment("移动速度")
        public float move_sped;
        @Comment("攻击间隔")
        public int attadk_speed;
        public TaczData() {
            this("ak47", "SEMI", true, 40, 24, 1.0f, 5);
        }
        public TaczData(String id, String fireMode, boolean ammoInBarrel, int weight, int radius, float move_speed, int attadk_speed) {
            this.id = id;
            FireMode = fireMode;
            AmmoInBarrel = ammoInBarrel;
            this.weight = weight;
            this.radius = radius;
            this.move_sped = move_speed;
            this.attadk_speed = attadk_speed;
        }


    }


    public static class CommonData_Float {
        @Comment("值")
        public float value;
        @Comment("最小值")
        public float min_value;
        @Comment("最大值")
        public float max_value;
        @Comment("Language 键映射")
        public String lang;
        @Comment("默认值")
        public float DefaultValue;
        public CommonData_Float() {
            this(0, 0, 100, "", 0);
        }
        public CommonData_Float(float value, float min_value, float max_value, String lang, float defaultValue) {
            this.value = value;
            this.min_value = min_value;
            this.max_value = max_value;
            this.lang = lang;
            DefaultValue = defaultValue;
        }


    }


    public static class CommonData_Int {
        @Comment("值")
        public int value;
        @Comment("最小值")
        public int min_value;
        @Comment("最大值")
        public int max_value;
        @Comment("Language 键映射")
        public String lang;
        @Comment("默认值")
        public int DefaultValue;
        public CommonData_Int() {
            this(0, 0, 100, "", 0);
        }
        public CommonData_Int(int value, int min_value, int max_value, String lang, int defaultValue) {
            this.value = value;
            this.min_value = min_value;
            this.max_value = max_value;
            this.lang = lang;
            DefaultValue = defaultValue;
        }


    }


}
