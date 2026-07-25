package com.endofdays_re.utils.type;

import com.endofdays_re.level.register.RegisterEntityAttributes;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;

public enum AttributeType {
    ATTACK_DAMAGE(Attributes.ATTACK_DAMAGE, false),
    ATTACK_SPEED(Attributes.ATTACK_SPEED, false),
    ARMOR(Attributes.ARMOR, false),
    ARMOR_TOUGHNESS(Attributes.ARMOR_TOUGHNESS, false),
    MAX_HEALTH(Attributes.MAX_HEALTH, false),
    KNOCKBACK_RESISTANCE(Attributes.KNOCKBACK_RESISTANCE, false),
    MOVEMENT_SPEED(Attributes.MOVEMENT_SPEED, false),
    FOLLOW(Attributes.FOLLOW_RANGE, false),
    XAREMAP(RegisterEntityAttributes.XRAY_FOLLOW_RANGE, false),
    BUILD_SPEED(RegisterEntityAttributes.BUILD_SPEED, false),
    BREAKER_SPEED(RegisterEntityAttributes.BREAKER_SPEED, false), // 新增
    // 特殊：直接获取实体状态
    HEALTH(null, false),
    BASE_HEALTH(Attributes.MAX_HEALTH, true),
    BASE_MOVE_SPEED(Attributes.MOVEMENT_SPEED, true),
    BASE_ARMOR(Attributes.ARMOR, true),
    BASE_ATTACK_SPEED(Attributes.ATTACK_SPEED, true),
    BASE_ARMOR_TOUGHNESS(Attributes.ARMOR_TOUGHNESS, true),
    BASE_KNOCKBACK_RESISTANCE(Attributes.KNOCKBACK_RESISTANCE, true),
    BASE_ATTACK_DAMAGE(Attributes.ATTACK_DAMAGE, true),
    BASE_XAREMAP(RegisterEntityAttributes.XRAY_FOLLOW_RANGE, true),
    BASE_FOLLOW(Attributes.FOLLOW_RANGE, true),
    BASE_BUILD_SPEED(RegisterEntityAttributes.BUILD_SPEED, true),
    BASE_BREAKER_SPEED(RegisterEntityAttributes.BREAKER_SPEED, true); // 新增


    public final Holder<Attribute> attribute;
    public final boolean base;

    AttributeType(Holder<Attribute> attribute, boolean base) {
        this.attribute = attribute;
        this.base = base;
    }
}

