package com.endofdays_re.event.helper;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.GameType;

import java.util.List;
import java.util.UUID;

public enum InfectionHerper {
    ;
    // NBT 键名常量，防止拼写错误
    private static final String TAG_TARGET_MOB = "TargetPossessedMob";
    private static final String TAG_PREVIOUS_MODE = "PreInfectionGameMode";

    public static Entity getYourTargetMobForPlayer(ServerPlayer player) {
        if (player.getPersistentData().contains(TAG_TARGET_MOB)) {
            UUID targetUUID = player.getPersistentData().getUUID(TAG_TARGET_MOB);
            Entity target = player.serverLevel().getEntity(targetUUID);

            // 如果实体不在了，清理所有相关 NBT
            if (target == null || !target.isAlive()) {
                clearInfectionData(player);
                return null;
            }
            return target;
        }
        return null;
    }

    /**
     * 清理感染数据并恢复玩家模式
     */
    public static void clearInfectionData(ServerPlayer player) {
        // 1. 还原游戏模式
        if (player.getPersistentData().contains(TAG_PREVIOUS_MODE)) {
            int modeId = player.getPersistentData().getInt(TAG_PREVIOUS_MODE);
            GameType prevMode = GameType.byId(modeId);
            player.setGameMode(prevMode);
            player.getPersistentData().remove(TAG_PREVIOUS_MODE);
        }

        // 2. 移除目标绑定和视角
        player.getPersistentData().remove(TAG_TARGET_MOB);
        player.setCamera(player);
    }

    /**
     * 启动感染：记录模式 -> 绑定实体 -> 切换观察者
     */
    public static void infectPlayer(ServerPlayer player) {
        if (player == null) return;

        // 1. 记录当前模式 (使用 id 存储)
        player.getPersistentData().putInt(TAG_PREVIOUS_MODE, player.gameMode.getGameModeForPlayer().getId());

        // 2. 生成僵尸
        Zombie zombie = new Zombie(player.level());
        zombie.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
        zombie.setPersistenceRequired();

        // 3. 拷贝属性和装备
        deepClonePlayerToMob(player, zombie);
        player.serverLevel().addFreshEntity(zombie);

        // 4. 绑定 UUID
        player.getPersistentData().putUUID(TAG_TARGET_MOB, zombie.getUUID());

        // 5. 切换到观察者附身
        player.setGameMode(GameType.SPECTATOR);
        player.setCamera(zombie);

        player.displayClientMessage(Component.literal("§c感染爆发！你失去了对身体的控制..."), true);
    }

    public static void deepClonePlayerToMob(ServerPlayer player, LivingEntity target) {
        if (player == null || target == null) return;

        // 1. 装备 (包含附魔)
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            target.setItemSlot(slot, player.getItemBySlot(slot).copy());
            if (target instanceof Mob mob) mob.setDropChance(slot, 0.0F);
        }

        // 2. 核心战斗属性
        List<Holder<Attribute>> attributesToSync = List.of(
                Attributes.MAX_HEALTH,
                Attributes.MOVEMENT_SPEED,
                Attributes.ATTACK_DAMAGE,
                Attributes.ATTACK_KNOCKBACK,
                Attributes.ARMOR,
                Attributes.ARMOR_TOUGHNESS
        );
        for (Holder<Attribute> attr : attributesToSync) {
            var targetInstance = target.getAttribute(attr);
            if (targetInstance != null) {
                targetInstance.setBaseValue(player.getAttributeValue(attr));
            }
        }

        // 3. 状态同步
        target.setHealth(player.getHealth());
        target.setAirSupply(player.getAirSupply()); // 甚至可以同步氧气值防止溺水

        // 4. 清除并重新同步药水效果
        target.removeAllEffects();
        for (MobEffectInstance effect : player.getActiveEffects()) {
            target.addEffect(new MobEffectInstance(effect));
        }
        // 5. 燃烧状态
        if (player.isOnFire()) {
            int fireTicks = player.getRemainingFireTicks();
            target.setRemainingFireTicks(fireTicks);
        }
    }


}
