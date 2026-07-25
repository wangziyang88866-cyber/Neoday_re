package com.endofdays_re.client.config.data;

import com.endofdays_re.utils.ModUtils;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.HashMap;
import java.util.Map;

@Config(
        name = ModUtils.MODID + "/enable"
)
public class Enablebuild implements ConfigData {

    @ConfigEntry.Category("acquisition1")
    @Comment("游戏模块设置")
    public Map<String, EnableData> Data = new HashMap<>(
            Map.ofEntries(
                    Map.entry("goal_enable", new EnableData(true, ModUtils.MODID + ".enable.entity.goal", true)),//启用AI
                    Map.entry("attribute_enable", new EnableData(true, ModUtils.MODID + ".enable.entity.attribute", true)),//启用属性
                    Map.entry("place_block_enable", new EnableData(true, ModUtils.MODID + ".enable.entity.use.place_block", true)),//放置方块
                    Map.entry("break_block_enable", new EnableData(true, ModUtils.MODID + ".enable.entity.use.break_block", true)),//破坏方块
                    Map.entry("place_tnt_enable", new EnableData(true, ModUtils.MODID + ".enable.entity.use.place_tnt", true)),//放置TNT
                    Map.entry("fly_enable", new EnableData(false, ModUtils.MODID + ".enable.entity.fly", false)),//幻翼飞行
                    Map.entry("target_enable", new EnableData(true, ModUtils.MODID + ".enable.entity.target", true)),//目标选中
                    Map.entry("follow_enable", new EnableData(true, ModUtils.MODID + ".enable.entity.follow", true)),//追踪
                    Map.entry("jump_enable", new EnableData(true, ModUtils.MODID + ".enable.entity.jump", true)),//飞扑跳跃
                    Map.entry("entity_drop_enable", new EnableData(true, ModUtils.MODID + ".enable.replace.entity.drop", true)),//掉落物串改
                    Map.entry("entity_replace_enable", new EnableData(true, ModUtils.MODID + ".enable.replace.entity", true)),//实体替换
                    Map.entry("fishing_enable", new EnableData(true, ModUtils.MODID + ".enable.entity.use.fishing", true)),//钓鱼竿
                    Map.entry("trident_enable", new EnableData(true, ModUtils.MODID + ".enable.entity.use.trident", true)),//三叉戟
                    Map.entry("shield_enable", new EnableData(true, ModUtils.MODID + ".enable.entity.use.shield", true)),//盾
                    Map.entry("bow_enable", new EnableData(true, ModUtils.MODID + ".enable.entity.use.bow", true)),//弓
                    Map.entry("immune_fire", new EnableData(true, ModUtils.MODID + ".enable.entity.immune.lava", true)),//免疫营火
                    Map.entry("rebirth_enable", new EnableData(true, ModUtils.MODID + ".enable.entity.rebirth", true)),//复活
                    Map.entry("pearls_enable", new EnableData(true, ModUtils.MODID + ".enable.entity.goal.use.pearls", true)),//末影珍珠
                    Map.entry("immune_sun_enable", new EnableData(true, ModUtils.MODID + ".enable.entity.immune.sun", true)),//日照,免疫
                    Map.entry("use_potions_enable", new EnableData(true, ModUtils.MODID + ".enable.entity.use.potions_enable", true)),//使用药水
                    Map.entry("ride_enable", new EnableData(true, ModUtils.MODID + ".enable.entity.use.ride_enable", true)),//堆叠
                    Map.entry("dispenser_enable", new EnableData(true, ModUtils.MODID + ".enable.entity.use.dispenser_enable", true)),//发射器
                    Map.entry("barker_vehicle_enable", new EnableData(true, ModUtils.MODID + ".enable.entity.bark.barker_vehicle_enable", true)),//打破载具
                    Map.entry("spawn_tacz_enable", new EnableData(true, ModUtils.MODID + ".enable.entity.spawn.spawn_tacz_enable", true)),//持枪
                    Map.entry("gigantic_enable", new EnableData(true, ModUtils.MODID + ".enable.entity.spawn.gigantic_enable", true)),//巨人生成
                    Map.entry("equip_enable", new EnableData(true, ModUtils.MODID + ".enable.entity.equip", false)),//装备启用
                    Map.entry("enable_spawn", new EnableData(true, ModUtils.MODID + ".enable.spawn.enable_spawn", true)),//生成控制
                    Map.entry("replace_entity", new EnableData(true, ModUtils.MODID + ".enable.spawn.replace_entity", false)),//实体替换
                    Map.entry("entity_climb", new EnableData(true, ModUtils.MODID + ".enable.spawn.entity_climb", false)),//实体爬墙
                    Map.entry("break_target_block", new EnableData(true, ModUtils.MODID + ".enable.use.break_target_block", false)),//实体破坏目标方块
                    Map.entry("picked_target_container", new EnableData(true, ModUtils.MODID + ".enable.use.picked_target_container", false)),//实体偷取物品
                    Map.entry("enable_temp", new EnableData(true, ModUtils.MODID + ".enable.level.enable_temp", false)),//世界温度控制
                    Map.entry("spawn_tnt_zombie", new EnableData(true, ModUtils.MODID + ".enable.level.spawn_tnt_zombie", false))//僵尸自爆


            )
    );


    public static class EnableData {
        @Comment("是否启用")
        public boolean enable;
        @Comment("Language 键映射")
        public String lang;
        @Comment("默认值")
        public boolean DefaultValue;

        public EnableData() {
            this(true, "", true);
        }

        public EnableData(boolean enable, String lang, boolean DefaultValue) {
            this.enable = enable;
            this.lang = lang;
            this.DefaultValue = DefaultValue;
        }

    }


}
