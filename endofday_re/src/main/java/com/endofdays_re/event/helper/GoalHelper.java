package com.endofdays_re.event.helper;

import com.endofdays_re.level.goal.*;
import com.endofdays_re.utils.ModUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.List;

import static com.endofdays_re.config.ConfigData.*;
import static com.endofdays_re.event.helper.SimpleWeightListHelper.list;
import static com.endofdays_re.event.helper.SimpleWeightListHelper.taczItems;

public enum GoalHelper {
    ;

    // 建议：如果项目里已经有统一的 Logger（比如 Mod 主类里的 LogUtils.getLogger()），
    // 优先复用那一个，避免每个类各建各的 Logger。这里用 SLF4J 独立声明作为示例。
    private static final Logger LOGGER = LoggerFactory.getLogger(GoalHelper.class);

    public static void initGoal(GoalSelector goal, Mob mob) {
        if (goal != null && mob != null) {
            if (isDayEnable("enable") && isModeEnable("goal_enable") && mob instanceof Zombie zombie) {

                LOGGER.info("[GoalHelper] 开始为僵尸初始化AI目标, uuid={}, pos={}", mob.getUUID(), mob.blockPosition());

                // 按生成天数固定基础属性（AttributeBuild 配置），幂等，不会重复应用
                AttributeHelper.apply(mob);

                goal.addGoal(1, new SwimGoal(zombie, 1.0));
                goal.addGoal(1, new PickupEquipmentGoal(zombie));
                goal.addGoal(2, new EatGoldenAppleGoal(zombie));

                AttributeInstance follow_attribute = mob.getAttribute(Attributes.FOLLOW_RANGE);
                if (follow_attribute != null) {
                    double fixedFollowRange = 128.0;
                    goal.addGoal(8, new NearestAttackTargetGoal<>(mob, LivingEntity.class, false, fixedFollowRange, fixedFollowRange, commonConfigData.Target.values().stream().toList()));
                    LOGGER.info("[GoalHelper] uuid={} 添加索敌目标, followRange={}", mob.getUUID(), fixedFollowRange);
                }
                if (isDayEnable("entity_climb") && isModeEnable("entity_climb")) {
                    goal.addGoal(4, new ClimbGoal(zombie));
                    LOGGER.info("[GoalHelper] uuid={} 添加攀爬能力", mob.getUUID());
                }
                // 使用盾牌
                if (isDayEnable("shield") && isModeEnable("shield_enable")) {
                    if (!mob.getPersistentData().contains(ModUtils.KeyWraps("shield"))) {
                        if (CheckProbabilityFloat("spawn_zombie_shield")) {
                            if (ModUtils.isEmptySlot(mob, EquipmentSlot.OFFHAND)) {
                                mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(Items.SHIELD));
                                mob.setDropChance(EquipmentSlot.OFFHAND, -1);
                            }
                            mob.setDropChance(EquipmentSlot.MAINHAND, -1);
                            mob.getPersistentData().putBoolean(ModUtils.KeyWraps("shield"), true);
                        } else {
                            mob.getPersistentData().putBoolean(ModUtils.KeyWraps("shield"), false);
                        }
                    }
                    if (mob.getPersistentData().getBoolean(ModUtils.KeyWraps("shield"))) {
                        goal.addGoal(1, new UseShieldGoal(mob, 32.0, commonConfigData, List.of(ModUtils.getItemID(Items.BOW), ModUtils.getItemID(Items.TRIDENT))));
                        LOGGER.info("[GoalHelper] uuid={} 装备盾牌并添加护盾AI", mob.getUUID());
                    }
                }
                // 跟随目标
                if (isDayEnable("follow") && isModeEnable("follow_enable")) {
                    // 修复：原逻辑在这里 `return`，会导致后面所有 goal 都不会被添加。
                    // 这里改为只跳过 FollowGoal 本身，不影响后续分支。
                    if (isModeEnable("gigantic_follow_enable") || !mob.getPersistentData().contains(ModUtils.KeyWraps("gigantic"))) {
                        goal.addGoal(7, new FollowGoal(mob, 0.8, Zombie.class));
                        LOGGER.info("[GoalHelper] uuid={} 添加跟随AI", mob.getUUID());
                    } else {
                        LOGGER.info("[GoalHelper] uuid={} 为巨型僵尸且未开启gigantic_follow_enable，跳过跟随AI", mob.getUUID());
                    }
                }
                // 破坏载具
                if (isDayEnable("barker_vehicle") && isModeEnable("barker_vehicle_enable")) {
                    goal.addGoal(1, new BarkerVehicle(mob));
                    LOGGER.info("[GoalHelper] uuid={} 添加破坏载具AI", mob.getUUID());
                }

                // ========== 使用枪械（依赖 TACZ）—— 可选 ==========
                if (isDayEnable("spawn_tacz") && ModUtils.isloadMod("tacz") && isModeEnable("spawn_tacz_enable")) {
                    if (!mob.getPersistentData().contains(ModUtils.KeyWraps("tacz_zombie"))) {
                        if (CheckProbabilityFloat("spawn_tacz_zombie")) {
                            if (mob.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
                                var builderOptional = taczItems.build().getRandomValue(mob.getRandom());
                                if (builderOptional.isPresent()) {
                                    var builder1 = builderOptional.get();
                                    ItemStack item = ModUtils.getGunItem(
                                            mob.level().registryAccess(),
                                            builder1.id,
                                            builder1.FireMode,
                                            9999,
                                            builder1.AmmoInBarrel
                                    );
                                    net.minecraft.world.item.component.CustomData.update(
                                            net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                                            item,
                                            tag -> tag.putBoolean("tacz_zombie_item", true)
                                    );
                                    mob.setItemSlot(EquipmentSlot.MAINHAND, item);
                                    mob.setDropChance(EquipmentSlot.MAINHAND, -1.0F);
                                    mob.getPersistentData().putBoolean(ModUtils.KeyWraps("tacz_zombie"), true);
                                }
                            }
                        } else {
                            mob.getPersistentData().putBoolean(ModUtils.KeyWraps("tacz_zombie"), false);
                        }
                    }

                    if (mob.getPersistentData().getBoolean(ModUtils.KeyWraps("tacz_zombie"))) {
                        try {
                            Class<?> goalClass = Class.forName("com.endofdays_re.level.goal.TaczGunAttackGoal");
                            Constructor<?> ctor = goalClass.getConstructor(Mob.class, commonConfigData.getClass());
                            Goal taczGoal = (Goal) ctor.newInstance(mob, commonConfigData);
                            goal.addGoal(1, taczGoal);
                            LOGGER.info("[GoalHelper] uuid={} 装备TACZ枪械并添加射击AI", mob.getUUID());
                        } catch (Exception e) {
                            // 修复：原来这里静默吞掉异常，排查反射失败原因非常困难，改为打印警告日志。
                            LOGGER.warn("[GoalHelper] uuid={} 加载 TaczGunAttackGoal 失败: {}", mob.getUUID(), e.toString());
                        }
                    }
                }

                // 飞扑
                if (isDayEnable("jump") && isModeEnable("jump_enable")) {
                    goal.addGoal(5, new JumpGoal(mob));
                    LOGGER.info("[GoalHelper] uuid={} 添加飞扑AI", mob.getUUID());
                }
                // 抛投使用药水
                if (isDayEnable("potions") && isModeEnable("use_potions_enable")) {
                    goal.addGoal(3, new PotionThrowGoal(mob, ScreenConfigData.showParticles));
                    LOGGER.info("[GoalHelper] uuid={} 添加投掷药水AI", mob.getUUID());
                }
                // 使用珍珠
                if (isDayEnable("pearls") && isModeEnable("pearls_enable")) {
                    if (!mob.getPersistentData().contains(ModUtils.KeyWraps("pearls"))) {
                        if (CheckProbabilityFloat("spawn_zombie_pearls")) {
                            if (ModUtils.isEmptySlot(mob, EquipmentSlot.OFFHAND, EquipmentSlot.OFFHAND)) {
                                ModUtils.findFirstEmptySlot(mob, EquipmentSlot.OFFHAND, EquipmentSlot.OFFHAND).ifPresent(
                                        equipmentSlot -> {
                                            mob.setItemSlot(equipmentSlot, new ItemStack(Items.ENDER_PEARL));
                                            mob.setDropChance(EquipmentSlot.OFFHAND, -1);
                                            mob.setDropChance(EquipmentSlot.MAINHAND, -1);
                                        }
                                );
                            }
                            mob.getPersistentData().putBoolean(ModUtils.KeyWraps("pearls"), true);
                        } else {
                            mob.getPersistentData().putBoolean(ModUtils.KeyWraps("pearls"), false);
                        }
                    }
                    if (mob.getPersistentData().getBoolean(ModUtils.KeyWraps("pearls"))) {
                        goal.addGoal(2, new UsePearls(mob));
                        LOGGER.info("[GoalHelper] uuid={} 装备末影珍珠并添加珍珠AI", mob.getUUID());
                    }
                }
                // 丢TNT
                if (isDayEnable("place_tnt") && isModeEnable("place_tnt_enable")) {
                    if (!mob.getPersistentData().contains(ModUtils.KeyWraps("tnt"))) {
                        if (CheckProbabilityFloat("spawn_tnt_zombie")) {
                            if (ModUtils.isEmptySlot(mob, EquipmentSlot.OFFHAND, EquipmentSlot.OFFHAND)) {
                                ModUtils.findFirstEmptySlot(mob, EquipmentSlot.OFFHAND, EquipmentSlot.OFFHAND).ifPresent(
                                        equipmentSlot -> {
                                            mob.setItemSlot(equipmentSlot, new ItemStack(Items.TNT));
                                            mob.setDropChance(EquipmentSlot.OFFHAND, -1);
                                            mob.setDropChance(EquipmentSlot.MAINHAND, -1);
                                        });
                            }
                            mob.getPersistentData().putBoolean(ModUtils.KeyWraps("tnt"), true);
                        } else {
                            mob.getPersistentData().putBoolean(ModUtils.KeyWraps("tnt"), false);
                        }
                    }
                    if (mob.getPersistentData().getBoolean(ModUtils.KeyWraps("tnt"))) {
                        goal.addGoal(2, new PlaceUseTntGoal(mob, 16, 5));
                        LOGGER.info("[GoalHelper] uuid={} 装备TNT并添加投掷/放置TNT AI", mob.getUUID());
                    }
                }
                // 远程攻击
                if (isDayEnable("bow") && isModeEnable("bow_enable")) {
                    if (!mob.getPersistentData().contains(ModUtils.KeyWraps("ranged_attack"))) {
                        if (CheckProbabilityFloat("spawn_bow_zombie")) {
                            if (ModUtils.isEmptySlot(mob, EquipmentSlot.OFFHAND, EquipmentSlot.OFFHAND)) {
                                list.build().getRandomValue(mob.getRandom()).ifPresent(itemStack -> {
                                    // 修复：原代码是 `A || B && C`，因为 && 优先级高于 ||，
                                    // 实际等价于 `A || (B && C)`，导致只要抽到弓就无视主手是否为空直接覆盖装备；
                                    // 并且 B（getPersistentData().getInt(...)）读取的 tag 是用 putBoolean 写入的，
                                    // 类型不匹配导致 getInt 恒为 0，B 分支永远不会触发，属于死代码，这里直接移除。
                                    if (itemStack.is(Items.BOW) && mob.getMainHandItem().isEmpty()) {
                                        mob.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
                                        mob.setDropChance(EquipmentSlot.MAINHAND, -1);
                                    } else if (itemStack.is(Items.CROSSBOW) && mob.getMainHandItem().isEmpty()) {
                                        mob.setItemSlot(EquipmentSlot.MAINHAND, itemStack);
                                        mob.setDropChance(EquipmentSlot.MAINHAND, -1);
                                    } else if (itemStack.is(Items.TRIDENT) && mob.getOffhandItem().isEmpty()) {
                                        mob.setItemSlot(EquipmentSlot.OFFHAND, itemStack);
                                        mob.setDropChance(EquipmentSlot.OFFHAND, -1);
                                    }
                                });
                            }
                            mob.getPersistentData().putBoolean(ModUtils.KeyWraps("ranged_attack"), true);
                        } else {
                            mob.getPersistentData().putBoolean(ModUtils.KeyWraps("ranged_attack"), false);
                        }
                    }
                    if (mob.getPersistentData().getBoolean(ModUtils.KeyWraps("ranged_attack"))) {
                        goal.addGoal(1, new RangedAttackGoal(mob, 1.0, 30, 16));
                        LOGGER.info("[GoalHelper] uuid={} 装备远程武器并添加远程攻击AI", mob.getUUID());
                    }
                }
                // 钓鱼竿
                if (isDayEnable("fishing") && isModeEnable("fishing_enable")) {
                    if (!mob.getPersistentData().contains(ModUtils.KeyWraps("has_fishing"))) {
                        if (CheckProbabilityFloat("spawn_fishing_zombie")) {
                            if (ModUtils.isEmptySlot(mob, EquipmentSlot.OFFHAND, EquipmentSlot.OFFHAND)) {
                                ModUtils.findFirstEmptySlot(mob, EquipmentSlot.OFFHAND, EquipmentSlot.OFFHAND).ifPresent(
                                        equipmentSlot -> {
                                            mob.setItemSlot(equipmentSlot, new ItemStack(Items.FISHING_ROD));
                                            mob.setDropChance(EquipmentSlot.OFFHAND, -1);
                                            mob.setDropChance(EquipmentSlot.MAINHAND, -1);
                                            mob.getPersistentData().putBoolean(ModUtils.KeyWraps("has_fishing"), true);
                                        });
                            }
                            // 注意：如果 isEmptySlot 为 false，has_fishing 这个 tag 既不会置 true 也不会置 false，
                            // 会导致每 tick 重新走一次判定（与其它分支写法不一致），暂保留原行为，未在此处修改，
                            // 如需修复建议把 putBoolean(true) 移到 isEmptySlot 判断外层，与其它分支保持一致。
                        } else {
                            mob.getPersistentData().putBoolean(ModUtils.KeyWraps("has_fishing"), false);
                        }
                    }
                    if (mob.getPersistentData().getBoolean(ModUtils.KeyWraps("has_fishing"))) {
                        goal.addGoal(1, new UseFishingGoal(mob, 30));
                        LOGGER.info("[GoalHelper] uuid={} 装备钓鱼竿并添加钓鱼AI", mob.getUUID());
                    }
                }
                // 破坏方块
                if (isDayEnable("break_block") && isModeEnable("break_block_enable")) {
                    if (!mob.getPersistentData().contains(ModUtils.KeyWraps("spawn_break_zombie"))) {
                        if (CheckProbabilityFloat("spawn_break_zombie")) {
                            if (ModUtils.isEmptySlot(mob, EquipmentSlot.OFFHAND)) {
                                mob.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ModUtils.getRandomItem(Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE)));
                                mob.setDropChance(EquipmentSlot.OFFHAND, -1);
                            }
                            mob.getPersistentData().putBoolean(ModUtils.KeyWraps("spawn_break_zombie"), true);
                        } else {
                            mob.getPersistentData().putBoolean(ModUtils.KeyWraps("spawn_break_zombie"), false);
                        }
                    }
                    if (mob.getPersistentData().getBoolean(ModUtils.KeyWraps("spawn_break_zombie"))) {
                        goal.addGoal(1, new BreakerBlockGoal(
                                mob,
                                0.0,
                                false,
                                false,
                                false,
                                null,
                                true,
                                commonConfigData.banlist.values()
                        ));
                        LOGGER.info("[GoalHelper] uuid={} 装备镐并添加破坏方块AI", mob.getUUID());
                    }
                }
                // 堆叠
                if (isDayEnable("ride") && isModeEnable("ride_enable")) {
                    if (!mob.getPersistentData().contains(ModUtils.KeyWraps("ride"))) {
                        mob.getPersistentData().putBoolean(ModUtils.KeyWraps("ride"), CheckProbabilityFloat("spawn_ride_zombie"));
                    }
                    if (mob.getPersistentData().getBoolean(ModUtils.KeyWraps("ride"))) {
                        goal.addGoal(5, new RideTargetGoal(mob, 6, 32, 1.0));
                        LOGGER.info("[GoalHelper] uuid={} 添加骑乘/堆叠AI", mob.getUUID());
                    }
                }
                // 发射器
                if (isDayEnable("dispenser") && isModeEnable("dispenser_enable")) {
                    if (!mob.getPersistentData().contains(ModUtils.KeyWraps("dispenser"))) {
                        if (CheckProbabilityFloat("spawn_dispenser_zombie")) {
                            if (ModUtils.isEmptySlot(mob, EquipmentSlot.HEAD)) {
                                mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DISPENSER));
                                mob.setDropChance(EquipmentSlot.HEAD, -1);
                            }
                            mob.getPersistentData().putBoolean(ModUtils.KeyWraps("dispenser"), true);
                        } else {
                            mob.getPersistentData().putBoolean(ModUtils.KeyWraps("dispenser"), false);
                        }
                    }
                    if (mob.getPersistentData().getBoolean(ModUtils.KeyWraps("dispenser"))) {
                        goal.addGoal(4, new UltimateDispenserAttackGoal(mob));
                        LOGGER.info("[GoalHelper] uuid={} 头戴发射器并添加发射器攻击AI", mob.getUUID());
                    }
                }
                // 飞行
                if (isDayEnable("fly") && isModeEnable("fly_enable") && !mob.getPersistentData().contains(ModUtils.KeyWraps("gigantic"))) {
                    goal.addGoal(9, new FlyRidingGoal(mob));
                    LOGGER.info("[GoalHelper] uuid={} 添加飞行AI", mob.getUUID());
                }
                // 放置方块
                if (isDayEnable("place_block") && isModeEnable("place_block_enable")) {
                    if (!mob.getPersistentData().contains(ModUtils.KeyWraps("spawn_place_zombie"))) {
                        if (CheckProbabilityFloat("place_block_zombie_spawn")) {
                            if (ModUtils.isEmptySlot(mob, EquipmentSlot.OFFHAND)) {
                                zombie.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Blocks.COBBLESTONE));
                                zombie.setDropChance(EquipmentSlot.OFFHAND, 0.0f);
                            }
                            mob.getPersistentData().putBoolean(ModUtils.KeyWraps("spawn_place_zombie"), true);
                        } else {
                            mob.getPersistentData().putBoolean(ModUtils.KeyWraps("spawn_place_zombie"), false);
                        }
                    }
                    if (mob.getPersistentData().getBoolean(ModUtils.KeyWraps("spawn_place_zombie"))) {
                        PathBuildingGoal zombiepath = new PathBuildingGoal(zombie, Blocks.COBBLESTONE);
                        goal.addGoal(1, zombiepath);
                        GoalTracker.register(zombie, zombiepath);
                        LOGGER.info("[GoalHelper] uuid={} 装备圆石并添加放置方块AI", mob.getUUID());
                    }
                }
                // TNT 僵尸（头戴 TNT）
                // 注意：isModeEnable("spawn_tnt_zombie") 这里没有按其它分支惯用的 "_enable" 后缀命名，
                // 如果配置项实际叫 "spawn_tnt_zombie_enable"，这条永远读不到对应配置，建议核对配置文件。
                if (isDayEnable("spawn_tnt_zombie") && isModeEnable("spawn_tnt_zombie")) {
                    if (!mob.getPersistentData().contains(ModUtils.KeyWraps("spawn_tnt_zombie"))) {
                        if (CheckProbabilityFloat("spawn_tnt_zombie_ca")) {
                            if (ModUtils.isEmptySlot(mob, EquipmentSlot.HEAD)) {
                                mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.TNT));
                                mob.setDropChance(EquipmentSlot.HEAD, -1);
                            }
                            mob.getPersistentData().putBoolean(ModUtils.KeyWraps("spawn_tnt_zombie"), true);
                        } else {
                            mob.getPersistentData().putBoolean(ModUtils.KeyWraps("spawn_tnt_zombie"), false);
                        }
                    }
                    if (mob.getPersistentData().getBoolean(ModUtils.KeyWraps("spawn_tnt_zombie"))) {
                        goal.addGoal(1, new TNTGoal(mob));
                        LOGGER.info("[GoalHelper] uuid={} 头戴TNT并添加TNT自爆AI", mob.getUUID());
                    }
                }

                LOGGER.info("[GoalHelper] uuid={} AI目标初始化完成", mob.getUUID());
            }
        }
    }
}