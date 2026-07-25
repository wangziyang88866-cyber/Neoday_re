package com.endofdays_re.mixin;


import com.endofdays_re.utils.tools.ExpressionEvaluatorTool;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 僵尸属性优化Mixin - 高性能版本
 * 在tick时自动同步属性，避免事件调用的开销
 */
@Mixin(LivingEntity.class)
public abstract class ZombieAttributeMixin {

    // 注意：不能使用静态字段，因为会在类加载时初始化，此时ConfigData还未就绪
    // 改为在方法内部创建实例

    @Unique
    private long endofdays_re$lastAttributeSync = 0;

    /**
     * 在LivingEntity的tick中注入属性同步逻辑
     * 使用缓存机制减少计算频率
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;

        if (livingEntity.level().isClientSide()) return;

        if (!(livingEntity instanceof net.minecraft.world.entity.monster.Monster)) return;

        if (!com.endofdays_re.config.ConfigData.isModeEnable("attribute_enable") ||
                !com.endofdays_re.config.ConfigData.isDayEnable("attribute")) return;

        if (livingEntity.getPersistentData().contains("is_invasion")) return;

        int syncInterval = endofdays_re$getSyncInterval(livingEntity);

        long gameTime = livingEntity.level().getGameTime();
        if (gameTime - endofdays_re$lastAttributeSync < syncInterval) return;

        endofdays_re$syncAttributes(livingEntity);
        endofdays_re$lastAttributeSync = gameTime;
    }

    @Unique
    private void endofdays_re$syncAttributes(LivingEntity livingEntity) {
        ExpressionEvaluatorTool eval = new ExpressionEvaluatorTool();

        String entityId = com.endofdays_re.utils.ModUtils.getEntityTypeID(livingEntity.getType());
        eval.setVariable("day", (double) com.endofdays_re.event.data.AllSyncValue.Instance.day);
        double healthRatio = 1.0;
        AttributeInstance maxHealthAttr = livingEntity.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null && maxHealthAttr.getValue() > 0) {
            healthRatio = livingEntity.getHealth() / maxHealthAttr.getValue();
        }

        endofdays_re$injectVariables(livingEntity, eval);

        com.endofdays_re.config.ConfigData.AttributeConfigData.attributes.values().forEach(data -> {
            if (com.endofdays_re.event.data.AllSyncValue.Instance.day >= data.start &&
                    com.endofdays_re.event.data.AllSyncValue.Instance.day <= data.end &&
                    entityId.equals(data.EntityID)) {

                var attr = com.endofdays_re.utils.ModUtils.getAttribute(data.id);
                if (attr == null) return;

                net.minecraft.world.entity.ai.attributes.AttributeInstance old = livingEntity.getAttribute(attr);
                if (old == null || old.getValue() > data.max) return;

                double finalValue = eval.evaluate(data.value);
                com.endofdays_re.utils.ModUtils.setAttributeValue(livingEntity, attr, finalValue);
            }
        });
        if (maxHealthAttr != null && !livingEntity.isDeadOrDying()) {
            double newMaxHealth = maxHealthAttr.getValue();
            float newHealth = (float) (newMaxHealth * healthRatio);
            newHealth = Math.max(1.0f, Math.min(newHealth, (float) newMaxHealth));
            livingEntity.setHealth(newHealth);
        }
    }

    @Unique
    private void endofdays_re$injectVariables(LivingEntity livingEntity, ExpressionEvaluatorTool eval) {
        for (com.endofdays_re.utils.type.AttributeType type : com.endofdays_re.utils.type.AttributeType.values()) {
            if (type == com.endofdays_re.utils.type.AttributeType.HEALTH) {
                eval.setVariable(type.name(), (double) livingEntity.getHealth());
            } else if (type.attribute != null) {
                net.minecraft.world.entity.ai.attributes.AttributeInstance instance = livingEntity.getAttribute(type.attribute);
                if (instance != null) {
                    double val = type.base ? instance.getBaseValue() : instance.getValue();
                    eval.setVariable(type.name(), val);
                } else {
                    eval.setVariable(type.name(), 0.0);
                }
            }
        }
    }

    /**
     * 根据距离玩家的远近动态计算同步间隔
     * - 32格内：50 tick (2.5秒) - 高精度
     * - 64格内：100 tick (5秒) - 标准
     * - 超过64格：200 tick (10秒) - 低频率
     */
    @Unique
    private int endofdays_re$getSyncInterval(LivingEntity entity) {
        net.minecraft.world.entity.player.Player nearestPlayer = entity.level().getNearestPlayer(entity, 128);
        if (nearestPlayer == null) {
            return 200;
        }

        double dist = entity.distanceTo(nearestPlayer);
        if (dist < 32.0) {
            return 50;
        } else if (dist < 64.0) {
            return 100;
        } else {
            return 200;
        }
    }
}
