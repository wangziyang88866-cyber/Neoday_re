package com.endofdays_re.level.goal;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.EnumSet;
import java.util.List;

public class PickupEquipmentGoal extends Goal {
    private final Mob zombie;
    private final Level level;
    private ItemEntity targetItem;
    private int cooldown;

    public PickupEquipmentGoal(Mob zombie) {
        this.zombie = zombie;
        this.level = zombie.level();
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.cooldown > 0) {
            this.cooldown--;
            return false;
        }
        // 只在空闲时寻找装备，避免与战斗冲突
        if (this.zombie.getTarget() != null) {
            return false;
        }
        // 每5秒有20%几率尝试寻找装备
        if (this.zombie.getRandom().nextInt(100) > 20) {
            return false;
        }

        this.targetItem = findNearbyEquipment();
        return this.targetItem != null;
    }

    @Override
    public boolean canContinueToUse() {
        return this.targetItem != null &&
                this.targetItem.isAlive() &&
                this.zombie.distanceToSqr(this.targetItem) < 16.0D;
    }

    @Override
    public void start() {
        if (this.targetItem != null) {
            this.zombie.getNavigation().moveTo(this.targetItem, 1.0D);
        }
    }

    @Override
    public void stop() {
        this.targetItem = null;
        this.zombie.getNavigation().stop();
        this.cooldown = 100; // 5秒冷却
    }

    @Override
    public void tick() {
        if (this.targetItem != null && this.zombie.distanceToSqr(this.targetItem) < 2.0D) {
            pickupEquipment();
            this.targetItem = null;
        }
    }

    private ItemEntity findNearbyEquipment() {
        AABB searchArea = this.zombie.getBoundingBox().inflate(8.0D);
        List<ItemEntity> items = this.level.getEntitiesOfClass(ItemEntity.class, searchArea);

        for (ItemEntity item : items) {
            if (isEquipment(item.getItem())) {
                return item;
            }
        }
        return null;
    }

    private boolean isEquipment(ItemStack stack) {
        Item item = stack.getItem();
        // 只检测武器和护甲
        return item instanceof SwordItem ||
                item instanceof AxeItem ||
                item instanceof ArmorItem;
    }

    private void pickupEquipment() {
        if (this.targetItem == null) return;

        ItemStack itemStack = this.targetItem.getItem();
        Item item = itemStack.getItem();

        if (item instanceof SwordItem || item instanceof AxeItem) {
            // 武器装备到主手
            if (shouldReplaceMainHand(itemStack)) {
                equipToMainHand(itemStack);
                this.targetItem.discard();
            }
        } else if (item instanceof ArmorItem armorItem) {
            // 护甲装备到对应槽位
            EquipmentSlot slot = getSlotForArmor(armorItem);
            if (shouldReplaceArmor(slot, itemStack)) {
                equipToArmorSlot(slot, itemStack);
                this.targetItem.discard();
            }
        }
    }

    private boolean shouldReplaceMainHand(ItemStack newItem) {
        ItemStack currentMainHand = this.zombie.getMainHandItem();
        // 如果手是空的或者新武器更好，就替换
        return currentMainHand.isEmpty() ||
                getWeaponDamage(newItem) > getWeaponDamage(currentMainHand);
    }

    private boolean shouldReplaceArmor(EquipmentSlot slot, ItemStack newArmor) {
        ItemStack currentArmor = this.zombie.getItemBySlot(slot);
        // 如果槽位是空的或者新护甲更好，就替换
        return currentArmor.isEmpty() ||
                getArmorValue(newArmor) > getArmorValue(currentArmor);
    }

    private void equipToMainHand(ItemStack stack) {
        // 如果手上已经有物品，先丢弃
        if (!this.zombie.getMainHandItem().isEmpty()) {
            this.zombie.spawnAtLocation(this.zombie.getMainHandItem().copy());
        }
        this.zombie.setItemSlot(EquipmentSlot.MAINHAND, stack);
    }

    private void equipToArmorSlot(EquipmentSlot slot, ItemStack stack) {
        // 如果该槽位已经有护甲，先丢弃
        if (!this.zombie.getItemBySlot(slot).isEmpty()) {
            this.zombie.spawnAtLocation(this.zombie.getItemBySlot(slot).copy());
        }
        this.zombie.setItemSlot(slot, stack);
    }

    private EquipmentSlot getSlotForArmor(ArmorItem armor) {
        return armor.getEquipmentSlot();
    }

    private float getWeaponDamage(ItemStack stack) {
        // 简单的武器伤害评估
        if (stack.getItem() instanceof SwordItem sword) {
            return sword.getDamage(stack);
        } else if (stack.getItem() instanceof AxeItem axe) {
            return axe.getDamage(stack);
        }
        return 0;
    }

    private int getArmorValue(ItemStack stack) {
        // 护甲值评估
        if (stack.getItem() instanceof ArmorItem armor) {
            return armor.getDefense();
        }
        return 0;
    }
}