package com.endofdays_re.event.helper;

import com.endofdays_re.client.config.data.InvasionBuild;
import com.endofdays_re.event.data.AllSyncValue;
import com.endofdays_re.utils.ModUtils;
import com.endofdays_re.utils.tools.ExpressionEvaluatorTool;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public enum InvasionHelper {
    ;
    static ExpressionEvaluatorTool Eval = new ExpressionEvaluatorTool();

    public static boolean tryExecuteInvasion(ServerPlayer player) {
        ServerLevel world = player.serverLevel();
        InvasionBuild.InvasionSettings settings = getRandomInvasionConfig();
        if (settings == null) return false;
        if (AllSyncValue.Instance.day >= settings.day_ && AllSyncValue.Instance.day <= settings.end_day) {
            String currentDim = world.dimension().location().toString();
            boolean dimValid = false;
            for (String d : settings.dim) {
                if (currentDim.equals(d)) {
                    dimValid = true;
                    break;
                }
            }
            if (!dimValid) return false;

            long time = world.getDayTime() % 24000;
            if (time < settings.time_range.min || time > settings.time_range.max) return false;
            if (world.random.nextFloat() > settings.probability) return false;

            int totalWaves = getRandomValue(settings.InvasionCount, world.random);
            InvasionManager.startInvasionTask(player, settings, totalWaves);
        }
        return true;
    }

    public static void executeSingleWave(ServerLevel world, ServerPlayer player, InvasionBuild.InvasionSettings settings, int currentWave, int totalWaves) {
        Component msg = Component.literal("警告：第 " + currentWave + " / " + totalWaves + " 波僵尸正在进行攻城...")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
        player.displayClientMessage(msg, true);
        performSpawning(world, player, settings);
    }

    private static void performSpawning(ServerLevel world, ServerPlayer player, InvasionBuild.InvasionSettings settings) {
        // 核心改动：获取动态生成的坐标点
        List<BlockPos> spawnPoints = getSmartSpawnPoints(world, player.blockPosition(), settings);
        if (spawnPoints.isEmpty()) return;

        int totalSpawned = 0;
        for (InvasionBuild.EntitySetting entitySet : settings.entitySetting) {
            if (totalSpawned >= settings.maxEntity) break;

            int count = getRandomValue(entitySet.max_count, world.random);
            for (int i = 0; i < count; i++) {
                if (totalSpawned >= settings.maxEntity) break;
                if (world.random.nextFloat() > entitySet.probability) continue;

                BlockPos basePos = spawnPoints.get(world.random.nextInt(spawnPoints.size()));

                // 给每个实体一点微小的位置偏移，防止重叠挤兑
                double spawnX = basePos.getX() + 0.5 + (world.random.nextDouble() - 0.5);
                double spawnZ = basePos.getZ() + 0.5 + (world.random.nextDouble() - 0.5);
                double spawnY = basePos.getY();

                ResourceLocation entityId = ResourceLocation.parse(entitySet.id);
                if (BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
                    LivingEntity entity = (LivingEntity) ModUtils.getEntityType(entitySet.id).create(world);
                    if (entity != null) {
                        entity.moveTo(spawnX, spawnY, spawnZ, world.random.nextFloat() * 360, 0);
                        applyDetailedSettings(entity, entitySet);
                        entity.getPersistentData().putBoolean("is_invasion", true);
                        world.addFreshEntity(entity);
                        totalSpawned++;
                    }
                }
            }
        }
    }

    /**
     * 智能寻点：在玩家周围 3D 空间采样，确保有地面且头顶空旷
     */
    private static List<BlockPos> getSmartSpawnPoints(ServerLevel world, BlockPos center, InvasionBuild.InvasionSettings settings) {
        List<BlockPos> points = new ArrayList<>();
        RandomSource random = world.random;

        // 尝试次数增加，保证采样成功率
        int attempts = settings.pos_max * 10;

        for (int i = 0; i < attempts && points.size() < settings.pos_max; i++) {
            // 1. 在配置的范围内随机角度和距离
            double angle = random.nextDouble() * Math.PI * 2;
            double dist = settings.pos_range.min + random.nextInt(Math.max(1, settings.pos_range.max - settings.pos_range.min));

            int x = center.getX() + (int) (Math.cos(angle) * dist);
            int z = center.getZ() + (int) (Math.sin(angle) * dist);

            // 2. Y 轴动态寻找：在玩家 Y 坐标上下 8 格范围内搜索
            int startY = center.getY() + 8;
            int endY = center.getY() - 8;

            for (int y = startY; y >= endY; y--) {
                BlockPos checkPos = new BlockPos(x, y, z);

                // 校验条件：脚下是固体，脚部和头部是空气
                if (world.getBlockState(checkPos.below()).isSolid() &&
                        world.isEmptyBlock(checkPos) &&
                        world.isEmptyBlock(checkPos.above())) {

                    points.add(checkPos);
                    break; // 找到该垂直线上的第一个可用点，跳到下一个平面坐标
                }
            }
        }
        return points;
    }

    private static void applyDetailedSettings(LivingEntity entity, InvasionBuild.EntitySetting config) {
        if (config.tag != null && !config.tag.isEmpty()) {
            try {
                CompoundTag nbt = TagParser.parseTag(config.tag);
                CompoundTag current = entity.saveWithoutId(new CompoundTag());
                current.merge(nbt);
                entity.load(current);
            } catch (Exception ignored) {
            }
        }
        for (InvasionBuild.AttributeSetting attr : config.attributeSetting) {
            var attribute = ModUtils.getAttribute(attr.id);
            if (attribute == null) {
                ModUtils.warn("Skipping unknown invasion attribute: {}", attr.id);
                continue;
            }
            AttributeInstance inst = entity.getAttribute(attribute);
            if (inst != null) {
                try {
                    double evl = Eval.evaluate(attr.evl);
                    inst.setBaseValue(evl);
                } catch (Exception ignored) {
                }
            }
        }
        for (InvasionBuild.ArrmorSetting armor : config.arrmorSetting) {
            if (entity.getRandom().nextFloat() < armor.probability) {
                ItemStack stack = new ItemStack(ModUtils.getItem(armor.arrmor_id));
                if (armor.durability > 0) stack.setDamageValue(Math.max(0, stack.getMaxDamage() - armor.durability));
                EquipmentSlot slot = EquipmentSlot.byName(armor.slot.toLowerCase());
                entity.setItemSlot(slot, stack);
                if (entity instanceof Mob mob) {
                    mob.setDropChance(slot, armor.on_drop ? 2.0f : 0.0f);
                }
            }
        }
        for (InvasionBuild.EffectSetting ef : config.effectSetting) {
            if (entity.getRandom().nextFloat() < ef.probability) {
                Optional<Holder.Reference<MobEffect>> effect = BuiltInRegistries.MOB_EFFECT.getHolder(ResourceLocation.parse(ef.ef_id));
                effect.ifPresent(mobEffectReference -> entity.addEffect(new MobEffectInstance(mobEffectReference, getRandomValue(ef.leftTime, entity.getRandom()), getRandomValue(ef.level, entity.getRandom()), false, ef.show_ef)));
            }
        }
    }

    public static InvasionBuild.InvasionSettings getRandomInvasionConfig() {
        return SimpleWeightListHelper.invasionSettingsBuilder.build()
                .getRandomValue(ModUtils.safeRandom)
                .orElse(null);
    }

    private static int getRandomValue(InvasionBuild.range r, RandomSource random) {
        if (r.min >= r.max) return r.min;
        return r.min + random.nextInt(r.max - r.min + 1);
    }
}
