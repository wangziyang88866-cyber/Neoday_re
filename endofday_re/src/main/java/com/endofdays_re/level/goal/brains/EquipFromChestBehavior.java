package com.endofdays_re.level.goal.brains;

import com.endofdays_re.config.ConfigData;
import com.endofdays_re.level.register.ModMemoryModuleTypes;
import com.endofdays_re.utils.ModUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;

import static com.endofdays_re.config.ConfigData.isDayEnable;
import static com.endofdays_re.config.ConfigData.isModeEnable;

public class EquipFromChestBehavior extends Behavior<Mob> {

    private final int searchRadius;
    private final double speed;
    private final int scanCooldown;
    private final Set<BlockPos> reservedChests = new HashSet<>();
    private final List<String> allowedContainerIDs = new ArrayList<>();
    private final List<String> allowedItemIDs = new ArrayList<>();
    private final List<Pattern> allowedContainerPatterns = new ArrayList<>();
    private final List<Pattern> allowedItemPatterns = new ArrayList<>();
    private long lastScanTime = 0;
    private int equipCooldown = 0;
    private BlockPos targetPos;
    private int normalItemCooldown = 0;
    private ItemStack bestEquipmentItem = ItemStack.EMPTY;

    public EquipFromChestBehavior(int searchRadius, double speed, List<String> configList) {
        // 1.21.1 必须通过 Map 定义 Memory 依赖
        super(Map.of(ModMemoryModuleTypes.TARGET_BLOCK_POS_ARRMOR.get(), MemoryStatus.REGISTERED));
        this.searchRadius = searchRadius;
        this.speed = speed;
        this.scanCooldown = ConfigData.commonConfigData.follow_block_scan_interval * 20;

        for (String entry : configList) {
            if (entry.startsWith("container:regex:")) {
                allowedContainerPatterns.add(Pattern.compile(entry.substring("container:regex:".length())));
            } else if (entry.startsWith("container:")) {
                allowedContainerIDs.add(entry.substring("container:".length()));
            } else if (entry.startsWith("items:regex:")) {
                allowedItemPatterns.add(Pattern.compile(entry.substring("items:regex:".length())));
            } else if (entry.startsWith("items:")) {
                allowedItemIDs.add(entry.substring("items:".length()));
            }
        }
    }

    public static boolean isLootChest(ServerLevel world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        // 1.21.1 原生支持：RandomizableContainerBlockEntity 涵盖了所有带战利品表的容器
        if (be instanceof RandomizableContainerBlockEntity lootable) {
            return lootable.getLootTable() != null;
        }
        return false;
    }

    @Override
    protected boolean checkExtraStartConditions(@NotNull ServerLevel level, Mob mob) {
        if (!isDayEnable("equip") && !isModeEnable("equip_enable")) return false;

        if (mob.getBrain().hasMemoryValue(ModMemoryModuleTypes.TARGET_BLOCK_POS_ARRMOR.get())) {
            return true;
        }

        if (!mob.getPassengers().isEmpty() || mob.isVehicle()) return false;
        if (level.getGameTime() - lastScanTime < scanCooldown) return false;

        lastScanTime = level.getGameTime();

        Optional<BlockPos> target = BlockPos.findClosestMatch(
                mob.blockPosition(),
                searchRadius,
                4,
                pos -> {
                    if (reservedChests.contains(pos)) return false;

                    BlockState state = level.getBlockState(pos);
                    if (!state.is(Blocks.CHEST) && !state.is(Blocks.TRAPPED_CHEST)) return false;

                    // 1.21.1 标准 LootChest 判断
                    if (isLootChest(level, pos)) return false;

                    String blockID = ModUtils.getBlockID(state.getBlock());
                    boolean allowed = allowedContainerIDs.contains(blockID) ||
                            allowedContainerPatterns.stream().anyMatch(p -> p.matcher(blockID).matches());
                    if (!allowed) return false;

                    Container chest = getChestContainer(level, pos);
                    return chest != null && hasUsefulItem(mob, chest);
                }
        );

        target.ifPresent(pos -> {
            mob.getBrain().setMemory(ModMemoryModuleTypes.TARGET_BLOCK_POS_ARRMOR.get(), pos.immutable());
            reservedChests.add(pos);
        });

        return target.isPresent() && isDayEnable("picked_target_container") && isModeEnable("picked_target_container");
    }

    @Override
    protected void tick(@NotNull ServerLevel level, @NotNull Mob mob, long gameTime) {
        if (targetPos == null) return;

        // 持续校验目标合法性
        if (isLootChest(level, targetPos) || level.isEmptyBlock(targetPos)) {
            mob.getBrain().eraseMemory(ModMemoryModuleTypes.TARGET_BLOCK_POS_ARRMOR.get());
            return;
        }

        // 移动逻辑：使用 1.21.1 推荐的 Vec3 中心点
        if (mob.distanceToSqr(Vec3.atCenterOf(targetPos)) > 4.0) {
            mob.getNavigation().moveTo(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, speed);
            mob.getLookControl().setLookAt(Vec3.atCenterOf(targetPos));
            return;
        }

        if (normalItemCooldown > 0) normalItemCooldown--;
        if (equipCooldown > 0) {
            equipCooldown--;
            return;
        }

        Container chest = getChestContainer(level, targetPos);
        if (chest == null) {
            mob.getBrain().eraseMemory(ModMemoryModuleTypes.TARGET_BLOCK_POS_ARRMOR.get());
            return;
        }

        // 1. 尝试装备逻辑
        ItemStack bestItem = findBestEquipmentInChest(mob, chest);
        if (!bestItem.isEmpty()) {
            EquipmentSlot slot = mob.getEquipmentSlotForItem(bestItem);
            if (mob.getItemBySlot(slot).isEmpty()) {
                // 1.21.1 播放开箱动画 (事件ID 1, 参数 1 为开启)
                level.blockEvent(targetPos, level.getBlockState(targetPos).getBlock(), 1, 1);
                mob.swing(InteractionHand.MAIN_HAND);

                // 从箱子取出
                if (takeFromChest(chest, bestItem)) {
                    mob.setItemSlot(slot, bestItem.copy());
                    equipCooldown = 40;
                    chest.setChanged();
                    return;
                }
            }
        }

        // 2. 尝试拾取普通物品逻辑
        if (normalItemCooldown <= 0) {
            List<ItemStack> itemsToPick = findNormalItemsInChest(chest);
            for (ItemStack stack : itemsToPick) {
                ItemStack single = stack.copy();
                single.setCount(1);

                boolean added = false;
                if (mob instanceof InventoryCarrier carrier) {
                    added = addToContainer(carrier.getInventory(), single);
                } else {
                    added = giveToHand(mob, single);
                }

                if (added) {
                    takeFromChest(chest, stack);
                    normalItemCooldown = 20;
                    break;
                }
            }
        }
    }

    // --- 辅助工具方法 ---

    @Override
    protected void stop(@NotNull ServerLevel level, @NotNull Mob mob, long gameTime) {
        if (targetPos != null) {
            // 1.21.1 关闭箱子动画
            level.blockEvent(targetPos, level.getBlockState(targetPos).getBlock(), 1, 0);
            reservedChests.remove(targetPos);
        }
        targetPos = null;
        bestEquipmentItem = ItemStack.EMPTY;
        super.stop(level, mob, gameTime);
    }

    private boolean takeFromChest(Container chest, ItemStack target) {
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack stack = chest.getItem(i);
            if (ItemStack.isSameItemSameComponents(stack, target)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private boolean giveToHand(Mob mob, ItemStack stack) {
        if (mob.getMainHandItem().isEmpty()) {
            mob.setItemSlot(EquipmentSlot.MAINHAND, stack);
            return true;
        } else if (mob.getOffhandItem().isEmpty()) {
            mob.setItemSlot(EquipmentSlot.OFFHAND, stack);
            return true;
        }
        return false;
    }

    private boolean addToContainer(Container inv, ItemStack stack) {
        // 使用 1.21.1 原生工具类尝试添加
        int firstEmpty = -1;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack existing = inv.getItem(i);
            if (existing.isEmpty() && firstEmpty == -1) firstEmpty = i;
            if (ItemStack.isSameItemSameComponents(existing, stack) && existing.getCount() < existing.getMaxStackSize()) {
                existing.grow(1);
                return true;
            }
        }
        if (firstEmpty != -1) {
            inv.setItem(firstEmpty, stack);
            return true;
        }
        return false;
    }

    private Container getChestContainer(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof ChestBlock chest) {
            return ChestBlock.getContainer(chest, state, level, pos, true);
        }
        return null;
    }

    private boolean hasUsefulItem(Mob mob, Container chest) {
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack stack = chest.getItem(i);
            if (stack.isEmpty() || !isAllowedItem(stack)) continue;
            if (isEquipable(stack)) {
                if (mob.getItemBySlot(mob.getEquipmentSlotForItem(stack)).isEmpty()) return true;
            } else {
                return true; // 普通物品也要
            }
        }
        return false;
    }

    private ItemStack findBestEquipmentInChest(Mob mob, Container chest) {
        ItemStack best = ItemStack.EMPTY;
        double bestValue = -1;
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack stack = chest.getItem(i);
            if (!stack.isEmpty() && isAllowedItem(stack) && isEquipable(stack)) {
                if (mob.getItemBySlot(mob.getEquipmentSlotForItem(stack)).isEmpty()) {
                    double val = getItemValue(stack);
                    if (val > bestValue) {
                        bestValue = val;
                        best = stack;
                    }
                }
            }
        }
        return best;
    }

    private List<ItemStack> findNormalItemsInChest(Container chest) {
        List<ItemStack> list = new ArrayList<>();
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack stack = chest.getItem(i);
            if (!stack.isEmpty() && isAllowedItem(stack) && !isEquipable(stack)) {
                list.add(stack);
            }
        }
        return list;
    }

    private boolean isEquipable(ItemStack stack) {
        Item item = stack.getItem();
        return item instanceof ArmorItem || item instanceof ShieldItem ||
                item instanceof SwordItem || item instanceof AxeItem || item instanceof TridentItem;
    }

    private boolean isAllowedItem(ItemStack stack) {
        String id = ModUtils.getItemID(stack.getItem());
        return allowedItemIDs.contains(id) || allowedItemPatterns.stream().anyMatch(p -> p.matcher(id).matches());
    }

    private double getItemValue(ItemStack stack) {
        // 1.21.1 建议使用 Data Components 获取属性，这里为了兼容简化处理
        if (stack.getItem() instanceof ArmorItem armor) return armor.getDefense();
        if (stack.getItem() instanceof TieredItem tiered) return tiered.getTier().getAttackDamageBonus();
        return 1.0;
    }
}