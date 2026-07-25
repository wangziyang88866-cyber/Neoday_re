package com.endofdays_re.client.config.data;

import com.endofdays_re.utils.ModUtils;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;


@Config(
        name = ModUtils.MODID + "/dimension"
)
public class Dimensionbuild implements ConfigData {
    @ConfigEntry.Category("acquisition1")
    @Comment("血月启用")
    public boolean enable = true;
    @Comment("血月计算调整值[0.01-1.0],此值会和每天的血月概率相加,当随机值<=了此值就会触发血月.")
    public double weight = 0.01;
    @Comment("血月概率递增值")
    public double bloodMoonProbability = 0.05;
    @Comment("聊天框显示")
    public boolean chat_show = true;
    @Comment("血月允许睡觉")
    public boolean sleep = false;
    @Comment("血月期间刷怪倍率,原始刷怪量*此值,比如 限制刷100*1.35 实际血月刷怪量就是135")
    public float spawn_weight = 1.35f;


}
