//package com.endofdays_re.event.helper;
//
//import com.endofdays_re.client.config.data.ArrmorBuild;
//import com.endofdays_re.config.ConfigData;
//import com.endofdays_re.event.data.AllSyncValue;
//import com.endofdays_re.utils.ModUtils;
//import com.mojang.brigadier.exceptions.CommandSyntaxException;
//import net.minecraft.core.Holder;
//import net.minecraft.core.component.DataComponents;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.nbt.TagParser;
//import net.minecraft.world.entity.EquipmentSlot;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.Mob;
//import net.minecraft.world.item.ItemStack;
//import net.minecraft.world.item.component.CustomData;
//import net.minecraft.world.item.enchantment.Enchantment;
//
//public class ArrmorHelper {
//    public static void arrmor(LivingEntity livingEntity) {
//        if (!(livingEntity instanceof Mob mob)) return;
//        var random = mob.getRandom();
//        var allArmorConfig = ConfigData.arrmorData.Arrmor.values();
//
//        for (ArrmorBuild.Arrmor value : allArmorConfig) {
//            // 1. 基础条件检查
//            if (AllSyncValue.Instance.day < value.day || AllSyncValue.Instance.day > value.end_day) continue;
//            if (random.nextDouble() > value.chance) continue;
//            if (!mob.getItemBySlot(value.slot).isEmpty()) continue;
//
//            // 2. 特殊逻辑检查
//            if (value.slot == EquipmentSlot.HEAD && !mob.getPersistentData().contains(ModUtils.KeyWraps("dispenser"))) {
//                continue;
//            }
//
//            // 3. 创建物品实例 (1.21.1 物品获取逻辑)
//            ItemStack stack = new ItemStack(ModUtils.getItem(value.id).value());
//
//            // 4. 处理附魔 (1.21.1 附魔 API 变更)
//            if (value.enchantes != null) {
//                for (ArrmorBuild.Enchante ench : value.enchantes) {
//                    if (random.nextDouble() <= ench.chance) {
//                        Holder<Enchantment> enchantment = ModUtils.getEnchantment(livingEntity.level(), ench.id);
//                        // 随机等级计算
//                        int level = ench.level.level >= ench.level.maxLevel ? ench.level.level : random.nextInt(ench.level.level, ench.level.maxLevel + 1);
//                        // 1.21.1 推荐使用 enchant 方法，它会自动操作 DataComponents.ENCHANTMENTS
//                        stack.enchant(enchantment, level);
//                    }
//                }
//            }
//
//            // 5. 处理 NBT (1.21.1 修正：NBT 现在存放在 CustomData 组件中)
//            if (value.tag != null && !value.tag.isEmpty()) {
//                try {
//                    CompoundTag tag = TagParser.parseTag(value.tag);
//                    // 1.21.1 方式：通过 DataComponents.CUSTOM_DATA 写入传统 NBT 数据
//                    CustomData.update(DataComponents.CUSTOM_DATA, stack, existingTag -> {
//                        existingTag.merge(tag);
//                    });
//                } catch (CommandSyntaxException e) {
//                    ModUtils.error("Invalid NBT in Armor Config: " + value.tag);
//                }
//            }
//
//            // 6. 装备并设置掉落率
//            mob.setItemSlot(value.slot, stack);
//            mob.setDropChance(value.slot, 0.085F);
//        }
//    }
//}