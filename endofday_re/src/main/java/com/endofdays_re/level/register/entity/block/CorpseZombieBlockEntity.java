package com.endofdays_re.level.register.entity.block;

import com.endofdays_re.client.config.data.ArrmorBuild;
import com.endofdays_re.config.ConfigData;
import com.endofdays_re.event.helper.SimpleWeightListHelper;
import com.endofdays_re.level.register.RegisterBlockEntityTypes;
import com.endofdays_re.utils.ModUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class CorpseZombieBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int GUI_INVENTORY_SIZE = 27;
    public static final int SLOT_MAINHAND = 27;
    public static final int SLOT_OFFHAND = 28;
    public static final int SLOT_HEAD = 29;
    public static final int SLOT_CHEST = 30;
    public static final int SLOT_LEGS = 31;
    public static final int SLOT_FEET = 32;
    public static final int INTERNAL_TOTAL_SIZE = 33;
    private static final int MAX_BURN_TIME = 80;
    private final NonNullList<ItemStack> items = NonNullList.withSize(INTERNAL_TOTAL_SIZE, ItemStack.EMPTY);
    private int liftTime = 3600;
    private boolean hasFilledLoot = false;
    private int poseType = 0;
    private boolean isBurning = false;
    private int burnProgress = 0;

    public CorpseZombieBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(RegisterBlockEntityTypes.CORPSE_ZOMBIE_BE.get(), pPos, pBlockState);
    }

    public static void tick(Level pLevel, BlockPos pPos, BlockState pState, CorpseZombieBlockEntity pBlockEntity) {
        if (pLevel.isClientSide) {
            if (pBlockEntity.isBurning && pLevel.random.nextFloat() < 0.4F) {
                pLevel.addParticle(ParticleTypes.FLAME,
                        pPos.getX() + 0.5 + (pLevel.random.nextDouble() - 0.5) * 0.6,
                        pPos.getY() + 0.2,
                        pPos.getZ() + 0.5 + (pLevel.random.nextDouble() - 0.5) * 0.6, 0, 0.04, 0);
            }
            return;
        }

        if (!pBlockEntity.hasFilledLoot && pLevel instanceof ServerLevel serverLevel) {
            pBlockEntity.fillLoot(serverLevel);
        }

        if (pBlockEntity.isBurning) {
            pBlockEntity.burnProgress++;
            if (pBlockEntity.burnProgress % 20 == 0) {
                pLevel.playSound(null, pPos, SoundEvents.FIRE_AMBIENT, SoundSource.BLOCKS, 1.0F, 1.0F);
            }
            if (pBlockEntity.burnProgress >= MAX_BURN_TIME) {
                pBlockEntity.dropOnlyLoot(pLevel, pPos);
                pLevel.destroyBlock(pPos, false);
            }
        } else {
            // 优化：1.21.1 建议使用更高效的玩家查找
            Player player = pLevel.getNearestPlayer(pPos.getX() + 0.5, pPos.getY() + 0.5, pPos.getZ() + 0.5, 8.0D, false);
            if (player != null) {
                if (pBlockEntity.liftTime > 0) {
                    pBlockEntity.liftTime--;
                } else {
                    pBlockEntity.spawnZombieAndRemove(pLevel, pPos);
                }
            }
        }
    }

    private void spawnZombieAndRemove(Level level, BlockPos pos) {
        if (level.isClientSide) return;
        Zombie zombie = EntityType.ZOMBIE.create(level);
        if (zombie != null) {
            zombie.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.random.nextFloat() * 360.0F, 0.0F);

            // 设置装备
            zombie.setItemSlot(EquipmentSlot.MAINHAND, this.removeItemNoUpdate(SLOT_MAINHAND));
            zombie.setItemSlot(EquipmentSlot.OFFHAND, this.removeItemNoUpdate(SLOT_OFFHAND));
            zombie.setItemSlot(EquipmentSlot.HEAD, this.removeItemNoUpdate(SLOT_HEAD));
            zombie.setItemSlot(EquipmentSlot.CHEST, this.removeItemNoUpdate(SLOT_CHEST));
            zombie.setItemSlot(EquipmentSlot.LEGS, this.removeItemNoUpdate(SLOT_LEGS));
            zombie.setItemSlot(EquipmentSlot.FEET, this.removeItemNoUpdate(SLOT_FEET));

            // 掉落背包物品
            for (int i = 0; i < GUI_INVENTORY_SIZE; i++) {
                ItemStack invStack = this.removeItemNoUpdate(i);
                if (!invStack.isEmpty()) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), invStack);
                }
            }

            zombie.finalizeSpawn((ServerLevelAccessor) level, level.getCurrentDifficultyAt(pos), MobSpawnType.EVENT, null);
            level.addFreshEntity(zombie);
            level.removeBlock(pos, false);
        }
    }

    // --- 战利品逻辑 ---
    public boolean fillLoot(ServerLevel level) {
        RandomSource safeRandom = level.getRandom();
        return fillLoot(level, safeRandom);
    }

    public boolean fillLoot(ServerLevel level, RandomSource random) {
        if (this.hasFilledLoot) return false;
        this.hasFilledLoot = true;

        fillEquipmentFromConfig(level, random);

        if (SimpleWeightListHelper.zombieLoot != null) {
            var builder = SimpleWeightListHelper.zombieLoot.build();
            int rolls = random.nextInt(6) + 3;
            for (int i = 0; i < rolls; i++) {
                builder.getRandomValue(random).ifPresent(wrapper -> {
                    ItemStack stack = ModUtils.getItemStackWithNbt(wrapper.getItemId(), wrapper.getTag());
                    if (!stack.isEmpty()) {
                        int count = Math.max(1, wrapper.getMinCount() + random.nextInt(Math.max(1, wrapper.getMaxCount() - wrapper.getMinCount() + 1)));
                        stack.setCount(count);
                        insertStackIntoInventory(stack, random);
                    }
                });
            }
        }
        this.syncToClient();
        return true;
    }

    private void fillEquipmentFromConfig(ServerLevel level, RandomSource random) {
        if (ConfigData.arrmorData == null || ConfigData.arrmorData.Arrmor == null) return;
        long currentDay = com.endofdays_re.event.data.AllSyncValue.Instance.day;

        for (var entry : ConfigData.arrmorData.Arrmor.entrySet()) {
            ArrmorBuild.Arrmor data = entry.getValue();
            if (data == null) continue;
            if (currentDay >= data.day && (data.end_day == -1 || currentDay <= data.end_day)) {
                if (random.nextFloat() < data.chance) {
                    Item item = ModUtils.getItem(data.id).value();
                    if (item != Items.AIR) {
                        ItemStack stack = new ItemStack(item);
                        int targetSlot = getSlotIndexFromName(data.slot.getName());
                        if (targetSlot != -1 && this.items.get(targetSlot).isEmpty()) {
                            this.items.set(targetSlot, stack);
                        }
                    }
                }
            }
        }
    }

    private void insertStackIntoInventory(ItemStack stack, RandomSource random) {
        for (int attempt = 0; attempt < 10; attempt++) {
            int i = random.nextInt(GUI_INVENTORY_SIZE);
            if (this.items.get(i).isEmpty()) {
                this.items.set(i, stack.copy());
                stack.setCount(0);
                return;
            }
        }
        for (int i = 0; i < GUI_INVENTORY_SIZE; i++) {
            if (this.items.get(i).isEmpty()) {
                this.items.set(i, stack.copy());
                stack.setCount(0);
                return;
            }
        }
    }

    private int getSlotIndexFromName(String name) {
        return switch (name.toLowerCase()) {
            case "mainhand", "weapon" -> SLOT_MAINHAND;
            case "offhand" -> SLOT_OFFHAND;
            case "head", "helmet" -> SLOT_HEAD;
            case "chest", "chestplate" -> SLOT_CHEST;
            case "legs", "leggings" -> SLOT_LEGS;
            case "feet", "boots" -> SLOT_FEET;
            default -> -1;
        };
    }

    // --- Container 实现 ---
    @Override
    public int getContainerSize() {
        return INTERNAL_TOTAL_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int pSlot) {
        return items.get(pSlot);
    }

    @Override
    public ItemStack removeItem(int pSlot, int pAmount) {
        ItemStack result = ContainerHelper.removeItem(items, pSlot, pAmount);
        if (!result.isEmpty()) this.setChanged();
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int pSlot) {
        return ContainerHelper.takeItem(items, pSlot);
    }

    @Override
    public void setItem(int pSlot, ItemStack pStack) {
        items.set(pSlot, pStack);
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return Container.stillValidBlockEntity(this, pPlayer);
    }

    @Override
    public void clearContent() {
        items.clear();
        this.setChanged();
    }

    // --- 1.21.1 数据持久化 (核心修正) ---
    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);
        pTag.putBoolean("IsBurning", this.isBurning);
        pTag.putInt("PoseType", this.poseType);
        pTag.putBoolean("HasFilledLoot", this.hasFilledLoot);
        pTag.putInt("LiftTime", this.liftTime);
        pTag.putInt("BurnProgress", this.burnProgress);
        // 1.21.1 必须传入注册表查询器
        ContainerHelper.saveAllItems(pTag, this.items, pRegistries);
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        this.isBurning = pTag.getBoolean("IsBurning");
        this.poseType = pTag.getInt("PoseType");
        this.hasFilledLoot = pTag.getBoolean("HasFilledLoot");
        this.liftTime = pTag.getInt("LiftTime");
        this.burnProgress = pTag.getInt("BurnProgress");
        // 1.21.1 必须传入注册表查询器
        ContainerHelper.loadAllItems(pTag, this.items, pRegistries);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = super.getUpdateTag(pRegistries);
        saveAdditional(tag, pRegistries);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void syncToClient() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void dropOnlyLoot(Level pLevel, BlockPos pPos) {
        for (int i = 0; i < GUI_INVENTORY_SIZE; ++i) {
            ItemStack stack = this.removeItemNoUpdate(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(pLevel, pPos.getX(), pPos.getY(), pPos.getZ(), stack);
            }
        }
    }

    public int getPoseType() {
        return poseType;
    }

    public void setPoseType(int poseType) {
        this.poseType = poseType;
        this.syncToClient();
    }


    public boolean isBurning() {
        return isBurning;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.corpse_zombie");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pId, Inventory pInv, Player pPlayer) {
        return new ChestMenu(MenuType.GENERIC_9x3, pId, pInv, this, 3);
    }

    /**
     * 修正后的 handleInteract
     */
    public InteractionResult handleInteract(Player pPlayer, InteractionHand hand) {
        ItemStack itemStack = pPlayer.getItemInHand(hand);

        // 如果玩家拿着打火石，点燃僵尸
        if (itemStack.is(Items.FLINT_AND_STEEL)) {
            if (!this.isBurning) {
                if (!pPlayer.isCreative()) {
                    itemStack.hurtAndBreak(1, pPlayer, Player.getSlotForHand(hand));
                }
                this.isBurning = true;
                this.level.playSound(null, this.worldPosition, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
                this.syncToClient();
                return InteractionResult.SUCCESS;
            }
        }

        // 默认打开 UI
        if (!this.level.isClientSide) {
            pPlayer.openMenu(this);
        }
        return InteractionResult.SUCCESS;
    }
}