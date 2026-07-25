package com.endofdays_re.event.helper;

import com.endofdays_re.utils.ModUtils;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import javax.annotation.Nullable;
import java.util.*;

public class DropsHelper {
    public static final DropsHelper INSTANCE = new DropsHelper();

    /**
     * 执行掉落逻辑
     */
    public void dropStings(Optional<LootSting> build, Collection<ItemEntity> drops, ServerLevel level, LivingEntity entity) {
        if (drops == null || build.isEmpty()) return;

        LootSting loot = build.get();
        String entityId = ModUtils.getEntityTypeID(entity.getType());

        if (!loot.isEntityMatch(entityId)) return;

        // 1.21.1 修正：确保从 Holder 中安全获取 Item
        Item dropItem = ModUtils.getItem(loot.getItemId()).value();
        if (dropItem == Items.AIR) return;

        // 创建 ItemStack
        ItemStack dropStack = new ItemStack(dropItem);
        dropStack.setCount(loot.getRandomCount(level.random));

        // 1.21.1 核心修正：使用 CustomData 组件代替 setTag
        if (loot.getCachedTag() != null) {
            CustomData.set(DataComponents.CUSTOM_DATA, dropStack, loot.getCachedTag().copy());
        }

        if (dropStack.isEmpty()) return;

        // 计算位置 (使用 level.random 保持统一)
        RandomSource random = level.getRandom();
        double x = entity.getX() + (random.nextDouble() - 0.5) * entity.getBbWidth();
        double y = entity.getY() + 0.5;
        double z = entity.getZ() + (random.nextDouble() - 0.5) * entity.getBbWidth();

        ItemEntity dropEntity = new ItemEntity(level, x, y, z, dropStack);

        // 设置随机运动量
        dropEntity.setDeltaMovement(
                (random.nextDouble() - 0.5) * 0.2,
                random.nextDouble() * 0.2 + 0.1,
                (random.nextDouble() - 0.5) * 0.2
        );
        dropEntity.setDefaultPickUpDelay();

        drops.add(dropEntity);
    }

    public static class LootSting {
        private final int min_count;
        private final int max_count;
        private final String itemId;
        private final Set<String> entityIdSet;
        private final String[] rawIds;
        private final String rawTag;
        private CompoundTag cachedTag;

        public LootSting(String itemId, String[] ids, int min_count, int max_count, @Nullable String tagStr) {
            this.itemId = itemId;
            this.rawIds = ids;
            this.entityIdSet = new HashSet<>(Arrays.asList(ids));
            this.min_count = Math.max(1, min_count);
            this.max_count = Math.max(this.min_count, max_count);
            this.rawTag = tagStr;

            if (tagStr != null && !tagStr.trim().isEmpty() && !tagStr.equals("{}")) {
                try {
                    this.cachedTag = TagParser.parseTag(tagStr);
                } catch (CommandSyntaxException e) {
                    this.cachedTag = null;
                }
            }
        }

        public String getItemId() {
            return itemId;
        }

        public String[] getId() {
            return rawIds;
        }

        public int getMinCount() {
            return min_count;
        }

        public int getMaxCount() {
            return max_count;
        }

        @Nullable
        public String getTag() {
            return rawTag;
        }

        @Nullable
        public CompoundTag getCachedTag() {
            return cachedTag;
        }

        public boolean isEntityMatch(String id) {
            return entityIdSet.contains(id);
        }

        public int getRandomCount(RandomSource random) {
            if (max_count <= min_count) return min_count;
            return random.nextInt(max_count - min_count + 1) + min_count;
        }
    }
}