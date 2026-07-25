package com.endofdays_re.level.register;

import com.endofdays_re.utils.ModUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber
public class RegisterEntityAttributes {
    // 1. 使用 DeferredRegister 注册属性
    public static final DeferredRegister<Attribute> ATTRIBUTES =
            DeferredRegister.create(Registries.ATTRIBUTE, ModUtils.MODID);

    // 2. 将属性包装在 DeferredHolder 中
    // 注意：1.21.1 构造函数现在需要翻译键、默认值、最小值、最大值
    public static final Holder<Attribute> XRAY_FOLLOW_RANGE = ATTRIBUTES.register("xray",
            () -> new RangedAttribute("attribute.name." + ModUtils.MODID + ".xray", 1.0, 0.0, 512.0).setSyncable(true));

    public static final Holder<Attribute> BUILD_SPEED = ATTRIBUTES.register("build_speed",
            () -> new RangedAttribute("attribute.name." + ModUtils.MODID + ".build_speed", 12.0, 0.0, 512.0).setSyncable(true));

    public static final Holder<Attribute> BREAKER_SPEED = ATTRIBUTES.register("breaker_speed",
            () -> new RangedAttribute("attribute.name." + ModUtils.MODID + ".breaker_speed", 0.5, 0.0, 512.0).setSyncable(true));

    /**
     * 将自定义属性添加到所有生物实体
     * 该方法应订阅 EntityAttributeModificationEvent
     */
    @SubscribeEvent
    public static void modifyAttributes(EntityAttributeModificationEvent event) {
        // 遍历所有已注册的 LivingEntity 类型
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            // 使用 event.add 向实体注入属性（如果实体还没有该属性）
            // 注意：这里需要调用 .value() 获取真正的 Attribute 对象
            event.add(type, XRAY_FOLLOW_RANGE);
            event.add(type, BUILD_SPEED);
            event.add(type, BREAKER_SPEED);
        }
    }
}