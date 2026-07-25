package com.endofdays_re.datagen.gen.lang;

import com.endofdays_re.utils.ModUtils;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;


public class LangDataCN extends LanguageProvider {
    public LangDataCN(PackOutput output, String locale) {
        super(output, ModUtils.MODID, locale);
    }

    @Override
    protected void addTranslations() {
        // --- [ 1. 核心 AI 行为与天数阶段配置 ] ---
        add("endofdays_re.day.tooltip", "启用时间");
        add("endofdays_re.end.day.tooltip", "结束时间");
        add("endofdays_re.day.game.enable", "游戏启用");
        add("endofdays_re.day.entity.attribute", "实体属性");
        add("endofdays_re.day.entity.goal.place_block", "放置方块目标");
        add("endofdays_re.day.entity.goal.break_block", "打破方块目标");
        add("endofdays_re.day.entity.goal.follow", "跟随目标");
        add("endofdays_re.day.entity.goal.place_tnt", "放置TNT目标");
        add("endofdays_re.day.entity.goal.use.fishing", "使用钓鱼目标");
        add("endofdays_re.day.entity.goal.use.trident", "使用三叉戟目标");
        add("endofdays_re.day.entity.goal.use.bow", "使用弓箭目标");
        add("endofdays_re.day.entity.goal.use.shield", "使用盾牌目标");
        add("endofdays_re.day.entity.goal.use.place_fluid", "放置流体目标");
        add("endofdays_re.day.entity.immune.lava", "免疫岩浆");
        add("endofdays_re.day.entity.goal.use.jump", "跳跃目标");
        add("endofdays_re.day.entity.rebirth", "复活");
        add("endofdays_re.day.entity.replace", "替换目标");
        add("endofdays_re.day.entity.immune.campfire", "免疫营火");
        add("endofdays_re.day.entity.immune.asphyxia", "免疫窒息");
        add("endofdays_re.day.entity.goal.use.pearls", "使用末影珍珠目标");
        add("endofdays_re.day.entity.spawn.spawn_tacz", "生成持枪僵尸");
        add("endofdays_re.day.entity.spawn.spawn_fly", "使用骑乘幻翼");
        add("endofdays_re.day.entity.use.ride", "使用僵尸堆叠");
        add("endofdays_re.day.entity.use.dispenser", "使用发射器");
        add("endofdays_re.day.entity.immune.sun", "免疫日照");
        add("endofdays_re.day.entity.spawn.spawn_equip", "穿戴装备");
        add("endofdays_re.day.entity.use.potions", "抛掷药水");
        add("endofdays_re.day.entity.bark.barker_vehicle", "破坏载具[船-矿车]");
        add("endofdays_re.day.entity.spawn.entity_climb", "爬墙");
        add("endofdays_re.day.entity.use.picked_target_container", "偷取容器物品");
        add("endofdays_re.day.entity.use.break_target_block", "破坏目标方块");
        add("endofdays_re.day.entity.spawn.spawn_gigantic", "巨人生成概率");
        add("endofdays_re.day.level.enable_temp", "世界荒芜化生效时间");

        // --- [ 2. 启用/禁用 配置开关 (Config Tooltips) ] ---
        add("endofdays_re.enable.entity.goal", "启用 AI");
        add("endofdays_re.enable.entity.attribute", "启用 属性");
        add("endofdays_re.enable.entity.use.place_block", "启用 放置方块");
        add("endofdays_re.enable.entity.use.place_tnt", "启用 放置TNT");
        add("endofdays_re.enable.entity.use.place_fluid", "启用 放置流体");
        add("endofdays_re.enable.entity.fly", "启用 飞行");
        add("endofdays_re.enable.entity.target", "启用 目标");
        add("endofdays_re.enable.entity.follow", "启用 跟随");
        add("endofdays_re.enable.entity.jump", "启用 跳跃");
        add("endofdays_re.enable.replace.entity.drop", "启用 掉落");
        add("endofdays_re.enable.replace.entity", "启用 替换");
        add("endofdays_re.enable.entity.use.fishing", "启用 使用钓鱼竿");
        add("endofdays_re.enable.entity.use.trident", "启用 使用三叉戟");
        add("endofdays_re.enable.entity.use.shield", "启用 使用盾牌");
        add("endofdays_re.enable.entity.use.bow", "启用 使用弓箭");
        add("endofdays_re.enable.entity.immune.lava", "启用 免疫岩浆");
        add("endofdays_re.enable.entity.immune.campfire", "启用 免疫营火");
        add("endofdays_re.enable.entity.immune.asphyxia", "启用 免疫窒息");
        add("endofdays_re.enable.entity.rebirth", "启用 重生");
        add("endofdays_re.enable.entity.goal.use.pearls", "启用 使用末影珍珠");
        add("endofdays_re.enable.entity.gigantic_follow", "启用 巨人僵尸跟随目标");
        add("endofdays_re.enable.entity.immune.sun", "启用 日照免疫");
        add("endofdays_re.enable.entity.use.break_block", "启用 方块破坏");
        add("endofdays_re.enable.entity.use.dispenser_enable", "启用 发射器");
        add("endofdays_re.enable.entity.use.potions_enable", "启用 抛掷药水");
        add("endofdays_re.enable.entity.use.ride_enable", "启用 实体堆叠");
        add("endofdays_re.enable.spawn.enable_spawn", "启用 实体生成");
        add("endofdays_re.enable.entity.spawn.spawn_tacz_enable", "启用 实体持枪僵尸生成");
        add("endofdays_re.enable.entity.bark.barker_vehicle_enable", "启用 载具破坏[矿车船]");
        add("endofdays_re.enable.entity.spawn.gigantic_enable", "启用 巨人僵尸生成");
        add("endofdays_re.enable.entity.equip", "启用 装备穿戴");
        add("endofdays_re.enable.spawn.entity_climb", "启用 爬墙");
        add("endofdays_re.enable.use.break_target_block", "启用 目标方块破坏");
        add("endofdays_re.enable.use.picked_target_container", "启用 目标容器偷取");
        add("endofdays_re.enable.level.enable_temp", "启用 世界荒芜化");

        // --- [ 3. 概率、范围与常规数值配置 ] ---
        add("endofdays_re.lang.probability", "概率");
        add("endofdays_re.lang.tag", "标签");
        add("endofdays_re.lang.item.id", "物品ID");
        add("endofdays_re.lang.attribute.id", "属性ID");
        add("endofdays_re.lang.entity.ids", "实体ID列表");
        add("endofdays_re.common.value.tooltip", "启用值");
        add("endofdays_re.common.min.tooltip", "取值-最小");
        add("endofdays_re.common.max.tooltip", "取值-最大");
        add("endofdays_re.common.follow_range", "追踪范围");
        add("endofdays_re.common.use.probability.tnt", "使用tnt概率");
        add("endofdays_re.common.spawner.probability.tnt_zombie", "生成手持TNT僵尸的概率");
        add("endofdays_re.common.use.probability.fishing", "使用钓鱼竿概率");
        add("endofdays_re.common.spawner.probability.fishing_zombie", "生成手持钓鱼竿僵尸的概率");
        add("endofdays_re.common.use.probability.trident", "使用三叉戟概率");
        add("endofdays_re.common.spawner.probability.trident_zombie", "生成手持三叉戟僵尸的概率");
        add("endofdays_re.common.use.probability.bow", "使用弓箭概率");
        add("endofdays_re.common.spawner.probability.bow_zombie", "生成手持弓箭僵尸的概率");
        add("endofdays_re.common.use.probability.shield", "使用盾牌概率");
        add("endofdays_re.common.spawner.probability.shield_zombie", "生成手持盾牌僵尸的概率");
        add("endofdays_re.common.use.probability.pearls", "使用末影珍珠概率");
        add("endofdays_re.common.spawner.probability.pearls", "生成手持末影珍珠僵尸的概率");
        add("endofdays_re.common.probability.jump", "僵尸跳跃概率");
        add("endofdays_re.common.float.title", "常规范围配置");
        add("endofdays_re.common.int.title", "常规其他配置");
        add("endofdays_re.common.spawner.place_block_zombie", "生成手持方块僵尸概率");
        add("endofdays_re.common.spawner.tacz", "生成持枪僵尸概率");
        add("endofdays_re.common.spawner.dispenser", "生成发射器僵尸概率");
        add("endofdays_re.common.spawner.ride", "生成叠层者概率");
        add("endofdays_re.common.spawner.break", "生成持镐僵尸概率");

        // --- [ 4. HUD、消息与界面翻译 ] ---
        add("endofday.screen.enable", "显示hud");
        add("endofday.screen.enable.join", "显示加入内容");
        add("endofday.screen.join.key", "内容");
        add("endofday.screen.x", "屏幕 X 坐标");
        add("endofday.screen.y", "屏幕 Y 坐标");
        add("endofdays_re.join.key", "<values:[【终焉之日】 :你已成功加载了模组如果遇到问题请前往群内寻找我群号：680332596\n:【默认配置界面打开按键是:J键:】],colors:[#A8E6CF:#FFB6C1:#FFD700:#9370DB:#FFD700]>");
        add("endofdays_re.join.key_buttom", "<values:[➤:进行模组配置],colors:[#ffffff:#FFD700],click:[/endofdays_re screen set config],hover:[点击打开配置界面],bold:[true]>");
        add("endofdays_re.join.key_buttom_1", "<values:[➤:进行修改天数],colors:[#ffffff:#FFD700],click:[/endofdays_re screen set day],hover:[点击打开UI界面],bold:[true]>");
        add("endofdays_re.lang.msg", "输出内容");
        add("endofdays_re.lang.pre", "前置要求");
        add("endofdays_re.lang.mode", "输出方式");
        add("endofdays_re.lang.weight", "权重");
        add("endofdays_re.day.time", "触发刻度");
        add("endofdays_re.screen.title", "终焉之日配置");
        add("endofdays_re.key.screen", "打开配置界面");
        add("endofdays_re.hud.day", "天数: %s 天");
        add("endofdays_re.hud.money", "余额: ");
        add("endofdays_re.hud.currency_unit", " CR");
        add("endofdays_re.hud.low_balance", "余额不足!");

        // --- [ 5. 事件提醒消息 ] ---
        add("endofdays_re.event.moon.msg", "<values:[血月降临了],colors:[#8B0000]>");
        add("endofdays_re.event.next.moon.msg", "<values:[今天晚上会发生血月请小心应对.],colors:[#8B0000]>");
        add("endofdays_re.event.moon.msg.1", "<values:[血月落下了,亡灵焚烧殆尽.],colors:[#DCDCDC]>");
        add("endofdays_re.event.next.moon.title", "<values:[血月预警],colors:[#8B0000]>");
        add("endofdays_re.event.day.msg", "<values:[今天是你存活的第:${day}:天.],colors:[#DCDCDC:#FFFFFF:#DCDCDC]>");
        add("endofdays_re.event.neight.msg", "<values:[入夜了你应该找一个安全的庇护所.],colors:[#FFFFFF]>");
        add("message." + ModUtils.MODID + ".heavy_injury_active", "§c你受了重伤，治疗效果大打折扣！");
        add("endofdays_re.message.healing_reduced", "我感到很虚弱~");

        // --- [ 6. 医疗物品与药水效果 (完整) ] ---
        add("item.endofdays_re.quicksand_bucket", "液态流沙桶");
        add("item.endofdays_re.bandage", "绷带");
        add("item.endofdays_re.standard_medkit", "医疗包");
        add("item.endofdays_re.medical_bandage", "医疗绷带");
        add("message.bandage.healed", "绷带治疗了你的伤口");
        add("tooltip.bandage.description", "基础的医疗用品，用于紧急治疗");
        add("tooltip.bandage.heal_amount", "治疗量: %s点");
        add("tooltip.bandage.use_time", "使用时间: %s秒");
        add("message.medical_bandage.healed", "医疗绷带治疗了 %s 点生命值");
        add("message.medical_bandage.stopped_bleeding", "医疗绷带止住了严重的出血");
        add("message.medical_bandage.cleared_effects", "医疗绷带清除了所有负面效果");
        add("message.medical_bandage.no_need", "你目前不需要使用医疗绷带");
        add("tooltip.medical_bandage.description", "高级医疗用品，提供全面的治疗效果");
        add("tooltip.medical_bandage.heal_amount", "立即治疗: %s 点生命值");
        add("tooltip.medical_bandage.clears_effects", "清除所有负面状态效果");
        add("tooltip.medical_bandage.regeneration", "提供多级生命恢复效果");
        add("tooltip.medical_bandage.absorption", "获得伤害吸收和抗性提升");
        add("tooltip.medical_bandage.use_time", "使用时间: %s 秒");

        add("effect." + ModUtils.MODID + ".bleeding", "流血");
        add("effect." + ModUtils.MODID + ".stun", "击晕");
        add("effect." + ModUtils.MODID + ".fracture", "骨折");
        add("effect." + ModUtils.MODID + ".lacerate", "撕裂");
        add("effect." + ModUtils.MODID + ".heavy_injury", "重伤");
        add("tooltip." + ModUtils.MODID + ".heavy_injury.description", "§4[ 核心伤残 ]");
        add("tooltip." + ModUtils.MODID + ".heavy_injury.ability_1", "§c禁疗：大幅削弱或完全抑制自然回血与药效");
        add("tooltip." + ModUtils.MODID + ".heavy_injury.ability_2", "§c易伤：受到的所有伤害大幅提升");
        add("tooltip." + ModUtils.MODID + ".heavy_injury.detail", "伤口已经深及骨髓，普通的包扎已无济于事。");


        // --- [ 8. 特殊方块与建筑翻译 ] ---
        add("block." + ModUtils.MODID + ".corpse_zombie", "僵尸尸体");
        add("container.corpse_zombie", "僵尸尸体");
        add("tooltip." + ModUtils.MODID + ".corpse_zombie.description", "§7一具高度腐烂的躯壳，似乎还残留着生前的物资。");
        add("tooltip." + ModUtils.MODID + ".corpse_zombie.ability_1", "§6搜刮：右键点击可打开背包搜刮物资");
        add("tooltip." + ModUtils.MODID + ".corpse_zombie.ability_2", "§c隐患：搜刮时有概率惊醒尸体");
        add("tooltip." + ModUtils.MODID + ".corpse_zombie.ability_3", "§e清理：使用打火石、火弹或TNT可将其彻底焚烧");
        add("tooltip." + ModUtils.MODID + ".corpse_zombie.detail", "§4警告：长时间未清理的尸体可能会发生突变并重新站起。");
        add("block." + ModUtils.MODID + ".barbed_wire_fence", "倒刺栅栏");
        add("tooltip." + ModUtils.MODID + ".barbed_wire_fence.description", "§7带有锋利倒钩的金属防御工事。");
        add("tooltip." + ModUtils.MODID + ".barbed_wire_fence.ability_1", "§c倒钩：穿过时造成基于最大生命值的百分比伤害");
        add("tooltip." + ModUtils.MODID + ".barbed_wire_fence.ability_2", "§4感染：极高概率造成流血，低概率导致撕裂或重伤");
        add("tooltip." + ModUtils.MODID + ".barbed_wire_fence.ability_3", "§e阻滞：大幅度降低通过者的移动速度");
        add("tooltip." + ModUtils.MODID + ".barbed_wire_fence.detail", "§8“它不能挡住所有人，但能让所有人慢下来流干血。”");
        add("block." + ModUtils.MODID + ".spike_block", "钢制地刺");
        add("tooltip." + ModUtils.MODID + ".spike_block.description", "§7简陋但致命的陷阱。");
        add("tooltip." + ModUtils.MODID + ".spike_block.ability_1", "§c贯穿：对踩踏其上的生物造成最大生命值的百分比伤害");
        add("tooltip." + ModUtils.MODID + ".spike_block.ability_2", "§e无视护甲：地刺的伤害直接作用于躯干");
        add("tooltip." + ModUtils.MODID + ".spike_block.detail", "§8“别低头，脚下的危险更致命。”");

        // --- [ 9. 入侵配置系统全覆盖 (无偷懒版本) ] ---
        add("config.endofdays_re.category.invasion", "环境演变：入侵配置系统");

        // --- [ 10. 自定义刷怪系统 ] ---
        add("config.endofdays_re.category.spawner", "自定义刷怪系统");
        add("config.endofdays_re.spawner.enable", "启用自定义刷怪");
        add("config.endofdays_re.spawner.check_interval", "检查间隔 (Tick)");
        add("config.endofdays_re.spawner.max_groups", "最大组数");
        add("config.endofdays_re.spawner.max_per_group", "每组最大数量");
        add("config.endofdays_re.spawner.max_total_entities", "全局最大实体数 (-1不限制)");
        add("config.endofdays_re.spawner.spawn_range", "水平刷怪范围配置");
        add("config.endofdays_re.spawner.spawn_range.min", "最小水平距离 (格)");
        add("config.endofdays_re.spawner.spawn_range.max", "最大水平距离 (格)");
        add("config.endofdays_re.spawner.vertical_range", "垂直刷怪范围配置");
        add("config.endofdays_re.spawner.vertical_range.min", "最小垂直距离 (格)");
        add("config.endofdays_re.spawner.vertical_range.max", "最大垂直距离 (格)");
        add("config.endofdays_re.spawner.spawn_time", "生效天数配置");
        add("config.endofdays_re.spawner.spawn_time.start", "开始天数（第几天）");
        add("config.endofdays_re.spawner.spawn_time.end", "结束天数（-1不限制）");
        add("config.endofdays_re.spawner.allowed_dimensions", "维度白名单");
        add("config.endofdays_re.spawner.entity_configs", "实体配置列表");
        add("config.endofdays_re.spawner.entity.key", "配置键名");
        add("config.endofdays_re.spawner.entity.entity_id", "实体 ID");
        add("config.endofdays_re.spawner.entity.weight", "生成权重");
        add("config.endofdays_re.spawner.entity.nbt_tag", "NBT 标签");
        add("config.endofdays_re.spawner.entity.attributes", "属性配置");
        add("config.endofdays_re.spawner.attribute.header", "属性配置项");
        add("config.endofdays_re.spawner.attribute.id", "属性 ID");
        add("config.endofdays_re.spawner.attribute.formula", "计算公式");
        add("config.endofdays_re.spawner.entity.equipments", "装备配置");
        add("config.endofdays_re.spawner.equipment.header", "装备配置项");
        add("config.endofdays_re.spawner.equipment.item_id", "物品 ID");
        add("config.endofdays_re.spawner.equipment.slot", "装备槽位");
        add("config.endofdays_re.spawner.equipment.probability", "穿戴概率");

        // 阶段配置字段
        add("config.endofdays_re.spawner.stage_configs", "阶段配置列表（按天数自动切换）");
        add("config.endofdays_re.spawner.stage.key", "阶段键名");
        add("config.endofdays_re.spawner.stage.description", "阶段描述");
        add("config.endofdays_re.spawner.stage.start_day", "阶段开始天数");
        add("config.endofdays_re.spawner.stage.end_day", "阶段结束天数（-1表示无限）");
        add("config.endofdays_re.spawner.stage.max_groups", "最大刷怪组数");
        add("config.endofdays_re.spawner.stage.max_per_group", "每组最大实体数");
        add("config.endofdays_re.spawner.stage.check_interval", "刷怪检查间隔（tick）");
        add("config.endofdays_re.spawner.stage.check_interval.tooltip", "20 tick = 1秒。覆盖全局配置，实现分阶段难度递增");
        add("config.endofdays_re.spawner.stage.spawn_range_min", "水平刷怪范围最小值（格）");
        add("config.endofdays_re.spawner.stage.spawn_range_max", "水平刷怪范围最大值（格）");
        add("config.endofdays_re.spawner.stage.vertical_range_min", "垂直刷怪范围最小值（Y轴）");
        add("config.endofdays_re.spawner.stage.vertical_range_max", "垂直刷怪范围最大值（Y轴）");
        add("config.endofdays_re.spawner.stage.only_spawn_at_night", "是否只在晚上刷怪");
        add("config.endofdays_re.spawner.stage.only_spawn_at_night.tooltip", "开启后该阶段只在夜晚刷怪");
        add("config.endofdays_re.spawner.stage.check_light_level", "是否检查光照等级");
        add("config.endofdays_re.spawner.stage.check_light_level.tooltip", "开启后有火把等光源的地方不会刷怪（原版逻辑）");
        add("config.endofdays_re.invasion.max_time", "全局入侵冷却间隔 (24000=1天)");
        add("config.endofdays_re.invasion.list", "已注册的入侵事件列表");
        add("config.endofdays_re.common.key", "标识符 (Unique Key)");
        add("config.endofdays_re.invasion.weight", "生成权重 (Weight)");
        add("config.endofdays_re.invasion.dim", "生效维度限制");
        add("config.endofdays_re.invasion.max_entity", "单次入侵最大实体数");
        add("config.endofdays_re.invasion.pos_max", "生成点最大尝试次数");
        add("config.endofdays_re.common.probability", "触发成功概率 (0.0-1.0)");
        add("config.endofdays_re.common.min_waves", "单次入侵最小波次");
        add("config.endofdays_re.common.max_waves", "单次入侵最大波次");
        add("config.endofdays_re.invasion.pos_range", "生成位置配置层");
        add("config.endofdays_re.invasion.pos_range.min", "生成位置：最小半径");
        add("config.endofdays_re.invasion.pos_range.max", "生成位置：最大半径");
        add("config.endofdays_re.invasion.time_range", "触发时间窗层");
        add("config.endofdays_re.invasion.time_range.min", "触发时间窗：起始刻度");
        add("config.endofdays_re.invasion.time_range.max", "触发时间窗：结束刻度");
        add("config.endofdays_re.invasion.entities", "刷新的实体配置列表");
        add("config.endofdays_re.entity.id", "实体注册名 (ID)");
        add("config.endofdays_re.entity.tag", "额外 NBT 标签 (JSON格式)");
        add("config.endofdays_re.entity.count", "波次数量控制层");
        add("config.endofdays_re.entity.count.min", "单波次最小生成数");
        add("config.endofdays_re.entity.count.max", "单波次最大生成数");
        add("config.endofdays_re.entity.effects", "实体的药水效果列表");
        add("config.endofdays_re.effect.header", "药水效果具体配置");
        add("config.endofdays_re.effect.id", "效果注册名 (Effect ID)");
        add("config.endofdays_re.effect.time", "药水持续时间层");
        add("config.endofdays_re.effect.time.min", "效果持续时间：最小");
        add("config.endofdays_re.effect.time.max", "效果持续时间：最大");
        add("config.endofdays_re.common.min_lv", "效果最小等级 (0为I级)");
        add("config.endofdays_re.common.max_lv", "效果最大等级");
        add("config.endofdays_re.effect.show_particle", "显示药水粒子");
        add("config.endofdays_re.entity.attributes", "实体的属性修改列表");
        add("config.endofdays_re.attribute.header", "属性加成具体配置");
        add("config.endofdays_re.attribute.id", "属性名 (如 generic.max_health)");
        add("config.endofdays_re.attribute.evl", "属性计算公式 (EVL String)");
        add("config.endofdays_re.entity.armor", "实体的装备/手持列表");
        add("config.endofdays_re.armor.header", "装备项具体配置");
        add("config.endofdays_re.armor.id", "装备物品 ID");
        add("config.endofdays_re.armor.slot", "装备槽位");
        add("config.endofdays_re.armor.on_drop", "死亡是否允许掉落装备");
        add("config.endofdays_re.armor.durability", "装备耐久度 (0-100)");

        // --- [ 10. 各种模块配置 (Armor, Attr, Market, Drop) ] ---
        add("config.endofdays_re.title", "终焉之日 - 模块化配置");
        add("config.endofdays_re.category.armor", "僵尸盔甲配置");
        add("config.endofdays_re.armor_list", "盔甲配置列表");
        add("config.endofdays_re.armor_spawn_max", "盔甲尝试生成次数");
        add("config.endofdays_re.armor.chance", "生成概率 (0.0-1.0)");
        add("config.endofdays_re.armor.enchanted", "是否允许附魔");
        add("config.endofdays_re.armor.day", "起始生效天数");
        add("config.endofdays_re.armor.end_day", "结束生效天数");
        add("config.endofdays_re.armor.tag", "NBT标签 (JSON)");
        add("config.endofdays_re.armor.enchants", "附魔配置表");
        add("config.endofdays_re.enchant.id", "附魔ID");
        add("config.endofdays_re.enchant.chance", "该附魔出现概率");
        add("config.endofdays_re.enchant.min_level", "最小等级");
        add("config.endofdays_re.enchant.max_level", "最大等级");

        add("config.endofdays_re.category.attribute", "实体属性增强配置");
        add("config.endofdays_re.attribute_list", "属性调整列表");
        add("config.endofdays_re.attr.key", "配置项名称");
        add("config.endofdays_re.attr.id", "属性 ID");
        add("config.endofdays_re.attr.entity_id", "作用实体 ID");
        add("config.endofdays_re.attr.value", "增益表达式/数值");
        add("config.endofdays_re.attr.value.tooltip", "支持变量: BASE_HEALTH, day 等");
        add("config.endofdays_re.attr.start", "起始生效天数");
        add("config.endofdays_re.attr.end", "结束生效天数");
        add("config.endofdays_re.attr.max_limit", "属性增益上限");

        add("config.endofdays_re.category.market", "黑市交易配置");
        add("config.endofdays_re.market_list", "商品条目列表");
        add("config.endofdays_re.market.key", "注册名 (唯一 Key)");
        add("config.endofdays_re.market.id", "物品 ID");
        add("config.endofdays_re.market.count", "单次交易数量");
        add("config.endofdays_re.market.price", "基础价格 (金币)");
        add("config.endofdays_re.market.limit", "刷新限量 (-1 为无限)");
        add("config.endofdays_re.market.info", "商品显示名 (支持 § 颜色代码)");
        add("config.endofdays_re.market.mode", "交易模式 (买入/卖出)");
        add("config.endofdays_re.market.weight", "随机权重");

        add("config.endofdays_re.category.drop_living", "生物死亡掉落 (直接死亡)");
        add("config.endofdays_re.category.drop_corpse", "尸体搜刮战利品 (右键尸体)");
        add("config.endofdays_re.drop_list", "掉落配置列表");
        add("config.endofdays_re.drop.lang", "分类显示名");
        add("config.endofdays_re.drop.entities", "关联实体列表");
        add("config.endofdays_re.drop.day", "生效起始天数");
        add("config.endofdays_re.drop.end", "失效起始天数");
        add("config.endofdays_re.drop.items", "掉落物详细配置");
        add("config.endofdays_re.item.lang", "物品名称");
        add("config.endofdays_re.item.id", "物品 ID");
        add("config.endofdays_re.item.weight", "随机权重");
        add("config.endofdays_re.item.min", "最小数量");
        add("config.endofdays_re.item.max", "最大数量");
        add("config.endofdays_re.item.chance", "额外掉落概率 (0-1)");
        add("config.endofdays_re.item.tag", "NBT 标签");

        add("config.endofdays_re.common.scan_interval", "方块扫描间隔 (秒)");
        add("config.endofdays_re.common.temperature", "当前世界基础温度");
        add("config.endofdays_re.common.max_money", "玩家金钱上限");
        add("config.endofdays_re.common.use_currency", "启用货币系统");
        add("config.endofdays_re.common.smelt_blacklist", "析光熔融器黑名单");

        add("config.endofdays_re.category.probability", "生成/触发/其余设置");
        add("config.endofdays_re.common.probability_list", "触发概率详细列表");
        add("config.endofdays_re.common.value", "当前数值/表达式");
        add("config.endofdays_re.common.min", "允许最小值");
        add("config.endofdays_re.common.max", "允许最大值");

        add("config.endofdays_re.common.target_list", "仇恨目标重定向");
        add("config.endofdays_re.target.mob", "攻击者实体 ID");
        add("config.endofdays_re.target.victim", "目标实体 ID");

        add("config.endofdays_re.category.limit_setting", "极限与死亡惩罚配置");
        add("config.endofdays_re.common.limit_percent", "极限模式保命扣费比例 (0-1)");
        add("config.endofdays_re.common.limit_min_cost", "保命触发最低余额要求");
        add("config.endofdays_re.common.normal_death_cost", "普通模式死亡扣费比例 (0-1)");

        add("config.endofdays_re.common.replace_map", "实体生成替换映射表/方块破坏AI的黑名单方块");
        add("config.endofdays_re.common.replace_list", "全局实体替换列表");
        add("config.endofdays_re.replace.original", "原始实体 ID");
        add("config.endofdays_re.replace.target", "替换为实体 ID");
        add("config.endofdays_re.common.ban_list", "方块破坏/交互黑名单");
        add("config.endofdays_re.common.sync_interval", "数据包同步间隔 (Ticks)");
        add("config.endofdays_re.common.default_money", "玩家初始金钱");
        add("config.endofdays_re.common.market_max_count", "黑市每日商品上限 (-1全开)");

        add("config.endofdays_re.category.ai", "僵尸行为 AI 设置");
        add("config.endofdays_re.common.block_break_list", "主动破坏方块列表");
        add("config.endofdays_re.common.block_break.tooltip", "支持 regex: 前缀开启正则表达式匹配。");
        add("config.endofdays_re.common.equip_chest_list", "偷盗/开启容器配置");

        add("config.endofdays_re.category.tacz", "TACZ 枪械扩展配置");
        add("config.endofdays_re.tacz.id", "枪械物品 ID");
        add("config.endofdays_re.tacz.fire_mode", "射击模式 (AUTO/SEMI/BURST)");
        add("config.endofdays_re.tacz.ammo", "检查弹药（是否消耗弹药）");
        add("config.endofdays_re.tacz.weight", "僵尸持有权重");
        add("config.endofdays_re.tacz.radius", "最大感知/射程半径");
        add("config.endofdays_re.tacz.speed", "持有该枪时的移速倍率");
        add("config.endofdays_re.tacz.attack_speed", "僵尸射击间隔 (Tick)");

        add("config.endofdays_re.category.day_settings", "天数阶段配置");
        add("config.endofdays_re.day_list", "功能随天数演进列表");
        add("config.endofdays_re.day.key", "内部标识符");
        add("config.endofdays_re.day.start", "开始生效天数");
        add("config.endofdays_re.day.end", "失效天数");
        add("config.endofdays_re.day.lang_key", "功能翻译键 (Lang Key)");
        add("config.endofdays_re.day.default_value", "默认值");

        add("config.endofdays_re.category.bloodmoon", "血月与维度设置");
        add("config.endofdays_re.bloodmoon.enable", "启用血月系统");
        add("config.endofdays_re.bloodmoon.weight", "血月初始权重修正");
        add("config.endofdays_re.bloodmoon.probability", "每日概率递增值");
        add("config.endofdays_re.bloodmoon.chat_show", "血月开始提示 (聊天框)");
        add("config.endofdays_re.bloodmoon.sleep", "血月期间允许睡眠");
        add("config.endofdays_re.bloodmoon.spawn_weight", "血月刷怪倍率");

        add("config.endofdays_re.category.module_enable", "游戏模块总控");
        add("config.endofdays_re.common.enable", "功能状态 (开启/关闭)");
        add("config.endofdays_re.common.lang_key", "内部翻译键");
        add("config.endofdays_re.common.default_value", "模组默认状态");

        // --- [ 其他杂项 ] ---
        add("endofdays_re.drop.add.success", "完成物品添加");
        add("endofdays_re.bp.blood.msg", "<values:[血月升起概率:${bp}],colors:[#DCDCDC:map(${bp}>0.15,#90EE90,#FFFFFF):#DCDCDC]>");

// --- 画面主分类与全局 ---
        add("config.endofdays_re.category.screen", "画面与粒子特效");
        add("config.endofdays_re.screen.showHud", "§b显示 HUD 界面信息");
        add("config.endofdays_re.screen.isShowJoin", "显示加入服务器信息");
        add("config.endofdays_re.screen.isTitleShow", "显示屏幕正中央大标题");
        add("config.endofdays_re.screen.joinTime", "标题淡入时长 (Tick)");
        add("config.endofdays_re.screen.ShowTime", "标题停留时长 (Tick)");
        add("config.endofdays_re.screen.OutTime", "标题淡出时长 (Tick)");
        add("config.endofdays_re.screen.TitleNightShow", "夜晚是否显示标题");
        add("config.endofdays_re.screen.showParticles", "开启药水/环境粒子效果");
        add("config.endofdays_re.screen.showDamage", "开启伤害数值显示");
        add("config.endofdays_re.screen.showHeal", "开启治疗数值显示");
        add("config.endofdays_re.screen.entityBlacklist", "屏蔽数值显示的实体黑名单");

// --- 子分类标题 ---
        add("config.endofdays_re.screen.sub.heal", "§a✚ 治疗样式详细配置");
        add("config.endofdays_re.screen.sub.dmg", "§c⚔ 伤害样式详细配置");

// --- 治疗样式细节 (HealStyleConfig) ---
        add("config.endofdays_re.screen.heal.mainColor", "治疗主颜色");
        add("config.endofdays_re.screen.heal.prefix", "治疗前缀 (如 +)");
        add("config.endofdays_re.screen.heal.prefixColor", "前缀颜色");
        add("config.endofdays_re.screen.heal.playerSuffix", "玩家回复后缀 (如 HP)");
        add("config.endofdays_re.screen.heal.enableBold", "治疗数值加粗");

        add("config.endofdays_re.screen.dmg.enableBold", "伤害数值全局加粗");
        add("config.endofdays_re.screen.dmg.showCrit", "显示暴击提示");
        add("config.endofdays_re.screen.dmg.critColor", "暴击文字颜色");
        add("config.endofdays_re.screen.dmg.critPrefix", "暴击前缀文本");
        add("config.endofdays_re.screen.dmg.critSuffix", "暴击后缀文本");

        add("config.endofdays_re.screen.dmg.showSource", "显示伤害来源前缀 (P/M)");
        add("config.endofdays_re.screen.dmg.pPrefix", "玩家伤害标识 (默认 P)");
        add("config.endofdays_re.screen.dmg.mPrefix", "生物伤害标识 (默认 M)");

        String[] attrs = {"fire", "magic", "light", "freeze", "wither", "fall", "arrow"};
        String[] attrNames = {"火焰", "魔法", "闪电", "冰冻", "凋零", "坠落", "远程"};
        for (int i = 0; i < attrs.length; i++) {
            add("config.endofdays_re.screen.dmg.show." + attrs[i], "显示" + attrNames[i] + "伤害样式");
            add("config.endofdays_re.screen.dmg.color." + attrs[i], attrNames[i] + "伤害主颜色");
            add("config.endofdays_re.screen.dmg.suffix." + attrs[i], attrNames[i] + "图标后缀");
        }

        add("config.endofdays_re.screen.dmg.showBleeding", "显示流血特效 (🩸)");
        add("config.endofdays_re.screen.dmg.bleedingColor", "流血文字颜色");
        add("config.endofdays_re.screen.dmg.showStun", "显示击晕特效 (💫)");
        add("config.endofdays_re.screen.dmg.stunPrefix", "击晕文字前缀");
        add("config.endofdays_re.screen.dmg.showLacerate", "显示撕裂特效 (✖)");
        add("config.endofdays_re.screen.dmg.lacerateColor", "撕裂文字颜色");
        add("config.endofdays_re.screen.dmg.laceratePrefix", "撕裂文字前缀");
        add("config.endofdays_re.screen.dmg.showFracture", "显示骨折特效 (🦴)");
        add("config.endofdays_re.screen.dmg.fractureColor", "骨折文字颜色");
        add("config.endofdays_re.screen.dmg.fractureSuffix", "骨折文字后缀");

// 默认项
        add("config.endofdays_re.screen.dmg.defColor", "普通伤害兜底颜色");
        add("config.endofdays_re.screen.dmg.defSuffix", "普通伤害默认后缀 (⚔)");
        add("config.endofdays_re.category.common_main", "基础设置");
        add("config.endofdays_re.category.economy", "经济设置");
        add("endofdays_re.common.spawner.spawn_tnt_zombie_ca", "生成TNT僵尸概率");
        add("endofdays_re.day.level.spawn_tnt_zombie", "生成TNT僵尸");
        add("endofdays_re.enable.level.spawn_tnt_zombie", "启用TNT僵尸");

// 类别与主列表
        add("config.endofdays_re.spawner.rules_list", "生成规则定义列表");

// --- 1. 基础属性 / Base ---
        add("config.endofdays_re.spawner.rule_name", "规则识别名称");
        add("config.endofdays_re.spawner.mobs", "生成的实体 ID 列表");
        add("config.endofdays_re.spawner.weights", "对应实体的权重 (需与ID一一对应)");
        add("config.endofdays_re.spawner.tags", "附加计分板标签 (Scoreboard Tags)");
        add("config.endofdays_re.spawner.mobs_from_biome", "此项为 MobCategory 值");
        add("config.endofdays_re.spawner.mobs_from_bime_a", "会先获取群系的生物组配置,然后抽取此组的生物额外进行生成 默认可以留空 ");

// --- 2. 频率与数量 / Rate & Count ---
        add("config.endofdays_re.spawner.chance", "每秒生成概率 (0.0 - 1.0)");
        add("config.endofdays_re.spawner.attempts", "每秒尝试次数 (Attempts)");
        add("config.endofdays_re.spawner.min", "单次生成的最小数量");
        add("config.endofdays_re.spawner.max", "单次生成的最大数量");
        add("config.endofdays_re.spawner.group_distance", "群体分散距离 (-1为默认)");

// --- 3. 环境与距离条件 / Conditions ---
        add("config.endofdays_re.spawner.dims", "生效维度 (Namespace:Path)");
        add("config.endofdays_re.spawner.min_dist", "水平最小距离 (距玩家)");
        add("config.endofdays_re.spawner.max_dist", "水平最大距离 (距玩家)");
        add("config.endofdays_re.spawner.v_min_dist", "垂直最小距离 (-1关闭)");
        add("config.endofdays_re.spawner.v_max_dist", "垂直最大距离 (-1关闭)");
        add("config.endofdays_re.spawner.min_day", "起始存活天数限制");
        add("config.endofdays_re.spawner.max_day", "截止存活天数限制");
        add("config.endofdays_re.spawner.min_height", "最低生成高度 (Y轴)");
        add("config.endofdays_re.spawner.max_height", "最高生成高度 (Y轴)");

// --- 4. 亮度设置 / Lighting ---
        add("config.endofdays_re.spawner.check_block_light", "启用方块亮度检查");
        add("config.endofdays_re.spawner.min_block_light", "最低方块亮度要求");
        add("config.endofdays_re.spawner.max_block_light", "最高方块亮度要求");
        add("config.endofdays_re.spawner.check_sky_light", "启用天空亮度检查");
        add("config.endofdays_re.spawner.min_sky_light", "最低天空亮度要求");
        add("config.endofdays_re.spawner.max_sky_light", "最高天空亮度要求");
        add("config.endofdays_re.spawner.is_night", "夜晚生效");
        add("tooltip.config.is_light", "§e仅限夜晚：§r开启后，实体只在晚上生成。§7(注意：开启此项后，晚上的天空光检查将自动失效)§r");
        add("tooltip.config.min_sky_light", "最小天空光照：限定位置接收天空光的能力 (0-15)。15 为完全露天。");
        add("tooltip.config.max_sky_light", "最大天空光照：限定位置接收天空光的能力 (0-15)。0 为完全遮蔽。");
        // 如果你的 Config UI 需要更详细的解释
        add("tooltip.config.sky_light_hint", "提示：天空光与方块光不同，即使是深夜，露天位置的天空光依然是 15。");

// --- 5. 状态标记 / Flags ---
        add("config.endofdays_re.spawner.sturdy", "必须在坚固顶面生成");
        add("config.endofdays_re.spawner.sturdy.tooltip", "开启后，生物只能在完整的方块顶面（如全砖、倒放楼梯）生成，禁止在台阶或透明方块上生成。");
        add("config.endofdays_re.spawner.valid_spawn", "严格碰撞检查 (ValidSpawn)");
        add("config.endofdays_re.spawner.valid_spawn.tooltip", "模仿原版刷怪逻辑：禁止在玻璃、栅栏、树叶等非完整固体方块上生成。");
        add("config.endofdays_re.spawner.force_surface", "强制在地表生成");
        add("config.endofdays_re.spawner.in_liquid", "必须在液体中 (水或岩浆)");
        add("config.endofdays_re.spawner.in_water", "必须在水中生成");
        add("config.endofdays_re.spawner.no_restrictions", "无视所有限制 (暴力刷怪)");
        add("config.endofdays_re.spawner.in_lava", "必须在岩浆中生成");
        add("config.endofdays_re.spawner.in_air", "必须在空中/空气中生成(飞行生物)");

// --- 6. 上限限制 / Caps ---
        add("config.endofdays_re.spawner.max_this", "该规则实体上限 (-1不限)");
        add("config.endofdays_re.spawner.max_local", "128格区域内实体上限");
        add("config.endofdays_re.spawner.max_total", "全局实体总数上限");
        add("config.endofdays_re.spawner.max_hostile", "敌对生物总数限制");
        add("config.endofdays_re.spawner.max_peaceful", "被动生物总数限制");
        add("config.endofdays_re.spawner.max_neutral", "中立生物总数限制");
        add("config.endofdays_re.common.enable_stuck_day", "启用天数滞留");
        add("config.endofdays_re.common.retention_interval", "天数滞留间隔/天");
        add("config.endofdays_re.common.enable_corpse", "启用尸体生成");
        add("config.endofdays_re.common.corpse_max", "每个区域尸体量");

        add("gtm.endofdays_re.title", "末日交易终端");
        add("gtm.endofdays_re.button", "刷新");
        add("gtm.endofdays_re.button_1", "退出");
    }
}