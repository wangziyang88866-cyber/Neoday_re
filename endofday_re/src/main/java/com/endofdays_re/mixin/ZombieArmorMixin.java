package com.endofdays_re.mixin;

import com.endofdays_re.client.config.data.ArrmorBuild;
import com.endofdays_re.config.ConfigData;
import com.endofdays_re.event.data.AllSyncValue;
import com.endofdays_re.utils.ModUtils;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 僵尸护甲优化Mixin - 高性能版本
 * 在实体生成时自动装备护甲，避免事件调用的开销
 */
@Mixin(Mob.class)
public abstract class ZombieArmorMixin {
    /**
     * 在Mob的finalizeSpawn方法后注入护甲装备逻辑
     * 这样可以在僵尸生成时就立即装备护甲
     */
    @Inject(method = "finalizeSpawn", at = @At("RETURN"))
    private void onFinalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                 MobSpawnType spawnType, @Nullable SpawnGroupData spawnGroupData,
                                 CallbackInfoReturnable<SpawnGroupData> cir) {
        Mob mob = (Mob) (Object) this;
        if (mob.level().isClientSide()) return;
        if (!(mob instanceof net.minecraft.world.entity.monster.Zombie)) return;
        if (!com.endofdays_re.config.ConfigData.isDayEnable("enable")) return;
        endofdays_re$equipArmor(mob);
    }

    @Unique
    private void endofdays_re$equipArmor(Mob mob) {
        var random = mob.getRandom();
        var allArmorConfig = ConfigData.arrmorData.Arrmor.values();

        for (ArrmorBuild.Arrmor value : allArmorConfig) {

            if (AllSyncValue.Instance.day < value.day || AllSyncValue.Instance.day > value.end_day) continue;
            if (random.nextDouble() > value.chance) continue;
            if (!mob.getItemBySlot(value.slot).isEmpty()) continue;

            if (value.slot == EquipmentSlot.HEAD && !mob.getPersistentData().contains(ModUtils.KeyWraps("dispenser"))) {
                continue;
            }

            ItemStack stack = new ItemStack(ModUtils.getItem(value.id).value());

            if (value.enchantes != null) {
                for (ArrmorBuild.Enchante ench : value.enchantes) {
                    if (random.nextDouble() <= ench.chance) {
                        Holder<Enchantment> enchantment = ModUtils.getEnchantment(mob.level(), ench.id);
                        int level = ench.level.level >= ench.level.maxLevel ? ench.level.level :
                                random.nextInt(ench.level.level, ench.level.maxLevel + 1);
                        stack.enchant(enchantment, level);
                    }
                }
            }

            // 5. 处理 NBT
            if (value.tag != null && !value.tag.isEmpty()) {
                try {
                    CompoundTag tag = TagParser.parseTag(value.tag);
                    CustomData.update(DataComponents.CUSTOM_DATA, stack, existingTag -> {
                        existingTag.merge(tag);
                    });
                } catch (CommandSyntaxException e) {
                    ModUtils.error("Invalid NBT in Armor Config: " + value.tag);
                }
            }

            // 6. 装备并设置掉落率
            mob.setItemSlot(value.slot, stack);
            mob.setDropChance(value.slot, 0.085F);
        }
    }
}
