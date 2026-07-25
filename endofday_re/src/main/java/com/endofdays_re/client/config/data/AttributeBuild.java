package com.endofdays_re.client.config.data;


import com.endofdays_re.level.register.RegisterEntityAttributes;
import com.endofdays_re.utils.ModUtils;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Config(
        name = ModUtils.MODID + "/attribute"
)
public class AttributeBuild implements ConfigData {
    @Comment("属性")
    public Map<String, AttributeData> attributes = new HashMap<>(
            Map.ofEntries(
                    Map.entry("最大生命值", new AttributeData(ModUtils.getAttributeId(Attributes.MAX_HEALTH.value()) == null ? "" : Objects.requireNonNull(ModUtils.getAttributeId(Attributes.MAX_HEALTH.value())).toString(), "minecraft:zombie", "endofdays_re.attribute.max_health", "BASE * (1 + day * 0.05)", 1, 100, 1024.0f)),
                    Map.entry("伤害值", new AttributeData(ModUtils.getAttributeId(Attributes.ATTACK_DAMAGE.value()) == null ? "" : Objects.requireNonNull(ModUtils.getAttributeId(Attributes.ATTACK_DAMAGE.value())).toString(), "minecraft:zombie", "endofdays_re.attribute.attack_damage", "BASE * (1 + day * 0.03)", 1, 100, 1024.0f)),
                    Map.entry("护甲值", new AttributeData(ModUtils.getAttributeId(Attributes.ARMOR.value()) == null ? "" : Objects.requireNonNull(ModUtils.getAttributeId(Attributes.ARMOR.value())).toString(), "minecraft:zombie", "endofdays_re.attribute.armor", "BASE * (1 + day * 0.02)", 1, 100, 512.0f)),
                    Map.entry("追踪距离", new AttributeData(ModUtils.getAttributeId(Attributes.FOLLOW_RANGE.value()) == null ? "" : Objects.requireNonNull(ModUtils.getAttributeId(Attributes.FOLLOW_RANGE.value())).toString(), "minecraft:zombie", "endofdays_re.attribute.follow", "128", 1, 100, 1024.0f)),
                    Map.entry("穿墙追踪距离", new AttributeData(ModUtils.getAttributeId(RegisterEntityAttributes.XRAY_FOLLOW_RANGE.value()) == null ? "" : Objects.requireNonNull(ModUtils.getAttributeId(RegisterEntityAttributes.XRAY_FOLLOW_RANGE.value())).toString(), "minecraft:zombie", "endofdays_re.attribute.xray_follow", "256", 1, 100, 1024.0f)),
                    Map.entry("移动速度", new AttributeData(ModUtils.getAttributeId(Attributes.MOVEMENT_SPEED.value()) == null ? "" : Objects.requireNonNull(ModUtils.getAttributeId(Attributes.MOVEMENT_SPEED.value())).toString(), "minecraft:zombie", "endofdays_re.attribute.move", "BASE * (1 + day * 0.01)", 1, 100, 1024.0f)),
                    Map.entry("搭建速度", new AttributeData(ModUtils.getAttributeId(RegisterEntityAttributes.BUILD_SPEED.value()) == null ? "" : Objects.requireNonNull(ModUtils.getAttributeId(RegisterEntityAttributes.BUILD_SPEED.value())).toString(), "minecraft:zombie", "endofdays_re.attribute.build_speed", "max(2.0, BASE - (day * 0.10))", 1, 100, 1024.0f)),
                    Map.entry("挖掘速度", new AttributeData(ModUtils.getAttributeId(RegisterEntityAttributes.BREAKER_SPEED.value()) == null ? "" : Objects.requireNonNull(ModUtils.getAttributeId(RegisterEntityAttributes.BREAKER_SPEED.value())).toString(), "minecraft:zombie", "endofdays_re.attribute.breaker_speed", "min(2.0, BASE * (1 + day * 0.01))", 1, 100, 1024.0f))
            )
    );


    public static class AttributeData {
        @Comment("键名称")
        public String key;
        @Comment("属性ID")
        public String id;
        @Comment("实体id（支持逗号分隔多个，或使用 * 表示所有实体生效）")
        public String EntityID;
        @Comment("语言")
        public String lang;
        @Comment("属性值或表达式。可用变量: BASE=该属性在原版中的基础值, day=僵尸生成时的天数（生成后固定，不会随时间再变化）。" +
                "示例: BASE * (1 + day * 0.05)，比如第3天生成，20 * (1 + 3*0.05) = 23")
        public String value;
        @Comment("开始生效天数")
        public int start;
        @Comment("结束生效天数（小于0表示不限制）")
        public int end;
        @Comment("属性增益上限值")
        public float max;
        public AttributeData(String id, String entityID, String lang, String value, int start, int end, float max) {
            this.id = id;
            this.EntityID = entityID;
            this.lang = lang;
            this.value = value;
            this.start = start;
            this.end = end;
            this.max = max;
        }
        public AttributeData() {
            this("", "", "", String.valueOf(0.45), 1, 100, 1.0f);
        }
    }


}