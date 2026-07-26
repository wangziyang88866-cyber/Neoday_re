package com.endofdays_re.event.helper;

import com.endofdays_re.client.config.data.SpawnerBuild;
import com.endofdays_re.config.ConfigData;
import com.endofdays_re.event.data.AllSyncValue;
import com.endofdays_re.utils.ModUtils;
import com.endofdays_re.utils.tools.ExpressionEvaluatorTool;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public enum SpawnerHelper {
    ;

    private static final Logger LOGGER = LoggerFactory.getLogger(SpawnerHelper.class);
    private static final ExpressionEvaluatorTool FUNCTION_CHECKER =
            new ExpressionEvaluatorTool();

    private static final Set<String> KNOWN_ATTRIBUTE_VARIABLES =
            Set.of("day", "time", "BASE");

    private static final Pattern ATTRIBUTE_IDENTIFIER_PATTERN =
            Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

    private static final Pattern LEGACY_BASE_VARIABLE_PATTERN = Pattern.compile(
            "\\bBASE_(?:HEALTH|BUILD_SPEED|BREAKER_SPEED|ARMOR|MOVE_SPEED|ATTACK_DAMAGE)\\b"
    );

    private static final long ENTITY_COUNT_CACHE_TICKS = 20L;
    private static final int MAX_SPAWN_POINT_ATTEMPTS = 256;
    private static final int MIN_SPAWN_POINT_ATTEMPTS = 24;

    /*
     * ServerLevel 使用弱引用，世界卸载后缓存不会阻止其被回收。
     * 每个维度单独缓存，避免主世界计数错误套用到下界/末地。
     */
    private static final Map<ServerLevel, EntityCountCache> ENTITY_COUNT_CACHE = new WeakHashMap<>();

    public static SpawnerBuild.StageConfig getCurrentStage(SpawnerBuild config) {
        if (config == null || config.stage_configs == null || config.stage_configs.isEmpty()) {
            return null;
        }

        long currentDay = AllSyncValue.Instance.day;
        SpawnerBuild.StageConfig selected = null;

        /*
         * 阶段重叠时，优先选择 start_day 最大的阶段，
         * 使后面的难度阶段能够正确覆盖前面的阶段。
         */
        for (SpawnerBuild.StageConfig stage : config.stage_configs.values()) {
            if (stage == null || currentDay < stage.start_day) {
                continue;
            }

            if (stage.end_day >= 0 && currentDay > stage.end_day) {
                continue;
            }

            if (selected == null || stage.start_day > selected.start_day) {
                selected = stage;
            }
        }

        return selected;
    }

    public static boolean trySpawn(ServerPlayer player) {
        return trySpawn(player, 1.0F);
    }

    public static boolean trySpawn(ServerPlayer player, float spawnWeight) {
        if (player == null
                || !ConfigData.SpawnerConfigData.enable
                || spawnWeight <= 0.0F) {
            return false;
        }

        ServerLevel world = player.serverLevel();
        if (!isDimensionAllowed(world)) {
            return false;
        }

        SpawnerBuild.StageConfig stageConfig = getCurrentStage(ConfigData.SpawnerConfigData);
        if (stageConfig == null || !isTimeValid(stageConfig)) {
            return false;
        }

        if (isMaxEntitiesReached(world, stageConfig)) {
            return false;
        }

        return performGroupSpawning(world, player, spawnWeight, stageConfig);
    }

    private static boolean isDimensionAllowed(ServerLevel world) {
        String[] allowedDimensions = ConfigData.SpawnerConfigData.allowed_dimensions;
        if (allowedDimensions == null || allowedDimensions.length == 0) {
            return true;
        }

        String dimensionId = world.dimension().location().toString();
        for (String allowedDimension : allowedDimensions) {
            if (allowedDimension != null && dimensionId.equals(allowedDimension.trim())) {
                return true;
            }
        }

        return false;
    }

    private static boolean isTimeValid(SpawnerBuild.StageConfig stageConfig) {
        long currentDay = AllSyncValue.Instance.day;

        return currentDay >= stageConfig.start_day
                && (stageConfig.end_day < 0 || currentDay <= stageConfig.end_day);
    }

    private static boolean isNightTime(ServerLevel world, SpawnerBuild.StageConfig stageConfig) {
        if (!stageConfig.only_spawn_at_night) {
            return true;
        }

        long time = world.getDayTime() % 24000L;
        return time >= 13000L || time < 1000L;
    }

    private static boolean isMaxEntitiesReached(
            ServerLevel world,
            SpawnerBuild.StageConfig stageConfig
    ) {
        int maxTotal = ConfigData.SpawnerConfigData.max_total_entities;
        if (maxTotal < 0) {
            return false;
        }

        double range = Math.max(0.0D, stageConfig.spawn_range_max);
        long currentTick = world.getGameTime();

        EntityCountCache cache = ENTITY_COUNT_CACHE.get(world);
        if (cache != null
                && cache.range == range
                && currentTick - cache.tick < ENTITY_COUNT_CACHE_TICKS) {
            return cache.count >= maxTotal;
        }

        Set<Monster> monsters = new HashSet<>();

        for (ServerPlayer onlinePlayer : world.players()) {
            AABB area = new AABB(
                    onlinePlayer.getX() - range,
                    world.getMinBuildHeight(),
                    onlinePlayer.getZ() - range,
                    onlinePlayer.getX() + range,
                    world.getMaxBuildHeight(),
                    onlinePlayer.getZ() + range
            );

            monsters.addAll(world.getEntitiesOfClass(Monster.class, area));
        }

        int count = monsters.size();
        ENTITY_COUNT_CACHE.put(world, new EntityCountCache(currentTick, range, count));

        return count >= maxTotal;
    }

    private static boolean performGroupSpawning(
            ServerLevel world,
            ServerPlayer player,
            float spawnWeight,
            SpawnerBuild.StageConfig stageConfig
    ) {
        if (!isNightTime(world, stageConfig)) {
            return false;
        }

        int baseGroups = stageConfig.max_groups;
        int maxPerGroup = stageConfig.max_per_group;

        if (baseGroups <= 0 || maxPerGroup <= 0) {
            return false;
        }

        RandomSource random = world.random;
        int adjustedGroups = Math.max(1, (int) Math.ceil(baseGroups * spawnWeight));
        int minGroups = Math.max(1, adjustedGroups / 2);
        int groupsToSpawn = random.nextInt(minGroups, adjustedGroups + 1);

        List<BlockPos> spawnPoints = findSpawnPoints(
                world,
                player.blockPosition(),
                stageConfig,
                groupsToSpawn
        );

        if (spawnPoints.isEmpty()) {
            return false;
        }

        int totalSpawned = 0;

        for (int groupIndex = 0; groupIndex < groupsToSpawn; groupIndex++) {
            BlockPos groupCenter = spawnPoints.get(random.nextInt(spawnPoints.size()));
            int groupSize = random.nextInt(1, maxPerGroup + 1);

            for (int memberIndex = 0; memberIndex < groupSize; memberIndex++) {
                BlockPos spawnPos = findGroupMemberPosition(
                        world,
                        groupCenter,
                        stageConfig,
                        random
                );

                if (spawnPos != null && spawnEntity(world, spawnPos, random, stageConfig)) {
                    totalSpawned++;
                }
            }
        }

        if (totalSpawned > 0) {
            LOGGER.debug("[Spawner] 本次生成 {} 个实体", totalSpawned);
        }

        return totalSpawned > 0;
    }

    private static List<BlockPos> findSpawnPoints(
            ServerLevel world,
            BlockPos center,
            SpawnerBuild.StageConfig stageConfig,
            int groupCount
    ) {
        int minRange = Math.max(1, Math.min(
                stageConfig.spawn_range_min,
                stageConfig.spawn_range_max
        ));
        int maxRange = Math.max(minRange, Math.max(
                stageConfig.spawn_range_min,
                stageConfig.spawn_range_max
        ));

        int desiredPoints = Math.max(1, groupCount * 2);
        int attempts = Math.min(
                MAX_SPAWN_POINT_ATTEMPTS,
                Math.max(MIN_SPAWN_POINT_ATTEMPTS, desiredPoints * 12)
        );

        boolean underground = !world.canSeeSky(center.above());
        RandomSource random = world.random;
        Set<BlockPos> points = new LinkedHashSet<>();

        for (int attempt = 0; attempt < attempts && points.size() < desiredPoints; attempt++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            int distance = random.nextInt(minRange, maxRange + 1);

            int x = center.getX() + (int) Math.round(Math.cos(angle) * distance);
            int z = center.getZ() + (int) Math.round(Math.sin(angle) * distance);

            BlockPos candidate = underground
                    ? findUndergroundSpawnPosition(world, center, x, z, stageConfig, random)
                    : findSurfaceSpawnPosition(world, x, z);

            if (candidate != null && isValidSpawnPosition(world, candidate, stageConfig)) {
                points.add(candidate.immutable());
            }
        }

        return new ArrayList<>(points);
    }

    private static BlockPos findUndergroundSpawnPosition(
            ServerLevel world,
            BlockPos center,
            int x,
            int z,
            SpawnerBuild.StageConfig stageConfig,
            RandomSource random
    ) {
        int minVertical = Math.max(1, Math.min(
                stageConfig.vertical_range_min,
                stageConfig.vertical_range_max
        ));
        int maxVertical = Math.max(minVertical, Math.max(
                stageConfig.vertical_range_min,
                stageConfig.vertical_range_max
        ));

        int verticalDistance = random.nextInt(minVertical, maxVertical + 1);
        if (random.nextBoolean()) {
            verticalDistance = -verticalDistance;
        }

        int y = center.getY() + verticalDistance;
        if (y <= world.getMinBuildHeight() || y >= world.getMaxBuildHeight() - 1) {
            return null;
        }

        return new BlockPos(x, y, z);
    }

    private static BlockPos findSurfaceSpawnPosition(ServerLevel world, int x, int z) {
        /*
         * 高度图查询是 O(1) 的；不要对每次候选点从世界最高处扫描到最低处。
         * MOTION_BLOCKING_NO_LEAVES 可避免把树叶当作地面。
         */
        int y = world.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);

        if (y <= world.getMinBuildHeight() || y >= world.getMaxBuildHeight() - 1) {
            return null;
        }

        BlockPos pos = new BlockPos(x, y, z);

        return world.canSeeSky(pos) ? pos : null;
    }

    private static BlockPos findGroupMemberPosition(
            ServerLevel world,
            BlockPos groupCenter,
            SpawnerBuild.StageConfig stageConfig,
            RandomSource random
    ) {
        if (isValidSpawnPosition(world, groupCenter, stageConfig)) {
            return groupCenter;
        }

        for (int attempt = 0; attempt < 6; attempt++) {
            int offsetX = random.nextInt(-2, 3);
            int offsetZ = random.nextInt(-2, 3);

            BlockPos candidate = groupCenter.offset(offsetX, 0, offsetZ);
            if (isValidSpawnPosition(world, candidate, stageConfig)) {
                return candidate;
            }
        }

        return null;
    }

    private static boolean isValidSpawnPosition(
            ServerLevel world,
            BlockPos pos,
            SpawnerBuild.StageConfig stageConfig
    ) {
        if (!world.hasChunkAt(pos)) {
            return false;
        }

        BlockPos below = pos.below();
        BlockState belowState = world.getBlockState(below);
        BlockState bodyState = world.getBlockState(pos);
        BlockState headState = world.getBlockState(pos.above());

        if (!isSolidAndSafeStanding(world, below, belowState)) {
            return false;
        }

        if (!isPassable(bodyState) || !isPassable(headState)) {
            return false;
        }

        return !stageConfig.check_light_level
                || world.getBrightness(LightLayer.BLOCK, pos) <= 0;
    }

    private static boolean isSolidAndSafeStanding(
            ServerLevel world,
            BlockPos pos,
            BlockState state
    ) {
        return state.isSolid()
                && state.isCollisionShapeFullBlock(world, pos);
    }

    private static boolean isPassable(BlockState state) {
        return state.isAir() || state.canBeReplaced();
    }

    private static boolean spawnEntity(
            ServerLevel world,
            BlockPos pos,
            RandomSource random,
            SpawnerBuild.StageConfig stageConfig
    ) {
        SpawnerBuild.EntityConfig entityConfig = selectRandomEntity(random, stageConfig);
        if (entityConfig == null || entityConfig.entity_id == null) {
            return false;
        }

        ResourceLocation entityId;
        try {
            entityId = ResourceLocation.parse(entityConfig.entity_id);
        } catch (Exception exception) {
            LOGGER.warn("[Spawner] 无效实体 ID: {}", entityConfig.entity_id);
            return false;
        }

        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
            LOGGER.warn("[Spawner] 未注册实体 ID: {}", entityId);
            return false;
        }

        Entity created = BuiltInRegistries.ENTITY_TYPE.get(entityId).create(world);
        if (!(created instanceof LivingEntity entity)) {
            LOGGER.warn("[Spawner] {} 不是 LivingEntity，无法作为刷怪实体", entityId);
            return false;
        }

        /*
         * NBT 先应用，属性配置后应用。
         * 这样 NBT 即使修改了基础属性，最终仍由属性配置接管。
         */
        applyNBTTag(entity, entityConfig.nbt_tag);

        /*
         * 默认按生成天数增强。
         */
        AttributeHelper.apply(entity);

        /*
         * 单条刷怪规则的属性覆盖，优先级最高。
         */
        applyAttributes(entity, entityConfig.attributes);

        applyEquipments(entity, entityConfig.equipments, random);

        if (entity instanceof Mob mob) {
            endofdays_re$postSpawnInit(mob, world);
        }

        /*
         * 最终兜底：NBT 或配置不能让实体带着 0 生命出生。
         */
        if (!ensureValidSpawnHealth(entity, entityId)) {
            return false;
        }

        entity.moveTo(
                pos.getX() + 0.5D,
                pos.getY(),
                pos.getZ() + 0.5D,
                random.nextFloat() * 360.0F,
                0.0F
        );

        return world.addFreshEntity(entity);
    }

    private static SpawnerBuild.EntityConfig selectRandomEntity(
            RandomSource random,
            SpawnerBuild.StageConfig stageConfig
    ) {
        Map<String, SpawnerBuild.EntityConfig> configs = stageConfig.entity_configs;
        if (configs == null || configs.isEmpty()) {
            return null;
        }

        int totalWeight = 0;
        for (SpawnerBuild.EntityConfig config : configs.values()) {
            if (config != null && config.weight > 0) {
                totalWeight += config.weight;
            }
        }

        if (totalWeight <= 0) {
            LOGGER.warn("[Spawner] 实体权重总和必须大于 0");
            return null;
        }

        int roll = random.nextInt(totalWeight);
        int currentWeight = 0;

        for (SpawnerBuild.EntityConfig config : configs.values()) {
            if (config == null || config.weight <= 0) {
                continue;
            }

            currentWeight += config.weight;
            if (roll < currentWeight) {
                return config;
            }
        }

        return null;
    }

    private static void applyNBTTag(LivingEntity entity, String nbtTag) {
        if (nbtTag == null || nbtTag.isBlank()) {
            return;
        }

        try {
            CompoundTag tag = TagParser.parseTag(nbtTag);
            CompoundTag current = entity.saveWithoutId(new CompoundTag());
            current.merge(tag);
            entity.load(current);
        } catch (Exception exception) {
            LOGGER.warn("[Spawner] 实体 NBT 应用失败: {}", exception.getMessage());
        }
    }
    private static boolean ensureValidSpawnHealth(
            LivingEntity entity,
            ResourceLocation entityId
    ) {
        float maxHealth = entity.getMaxHealth();
        float health = entity.getHealth();

        if (!Float.isFinite(maxHealth) || maxHealth <= 0.0F) {
            LOGGER.error(
                    "[Spawner] 实体最大生命非法，取消生成：entity={}, health={}, maxHealth={}",
                    entityId, health, maxHealth
            );
            return false;
        }

        if (!Float.isFinite(health) || health <= 0.0F) {
            LOGGER.warn(
                    "[Spawner] 实体当前生命非法，重置为最大生命：entity={}, health={}, maxHealth={}",
                    entityId, health, maxHealth
            );
            entity.setHealth(maxHealth);
            return true;
        }

        if (health > maxHealth) {
            entity.setHealth(maxHealth);
        }

        return true;
    }

    private static String normalizeLegacyBaseVariables(String formula) {
        if (formula == null || formula.isBlank()) {
            return formula;
        }

        return LEGACY_BASE_VARIABLE_PATTERN
                .matcher(formula)
                .replaceAll("BASE");
    }

    private static String findUnknownAttributeVariable(String formula) {
        Matcher matcher = ATTRIBUTE_IDENTIFIER_PATTERN.matcher(formula);

        while (matcher.find()) {
            String token = matcher.group();

            if (KNOWN_ATTRIBUTE_VARIABLES.contains(token)) {
                continue;
            }

            if (FUNCTION_CHECKER.containsKey(token)) {
                continue;
            }

            return token;
        }

        return null;
    }

    private static void applyAttributes(
            LivingEntity entity,
            SpawnerBuild.AttributeConfig[] attributes
    ) {
        if (attributes == null || attributes.length == 0) {
            return;
        }

        long day = AllSyncValue.Instance.day;
        long time = entity.level().getGameTime();

        for (SpawnerBuild.AttributeConfig attributeConfig : attributes) {
            if (attributeConfig == null
                    || attributeConfig.attribute_id == null
                    || attributeConfig.attribute_id.isBlank()
                    || attributeConfig.formula == null
                    || attributeConfig.formula.isBlank()) {
                continue;
            }

            try {
                /*
                 * 1.21.1 NeoForge：attribute 是 Holder.Reference<Attribute>。
                 */
                var attribute = ModUtils.getAttribute(attributeConfig.attribute_id);

                if (attribute == null) {
                    LOGGER.warn(
                            "[Spawner] 未知属性 ID，跳过：{}",
                            attributeConfig.attribute_id
                    );
                    continue;
                }

                AttributeInstance instance = entity.getAttribute(attribute);
                if (instance == null) {
                    LOGGER.debug(
                            "[Spawner] 实体不支持属性，跳过：entity={}, attribute={}",
                            BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()),
                            attributeConfig.attribute_id
                    );
                    continue;
                }

                String originalFormula = attributeConfig.formula;
                String formula = normalizeLegacyBaseVariables(originalFormula);

                String badVariable = findUnknownAttributeVariable(formula);
                if (badVariable != null) {
                    LOGGER.warn(
                            "[Spawner] 属性公式有未知变量，跳过：attribute={}, variable={}, formula={}",
                            attributeConfig.attribute_id,
                            badVariable,
                            originalFormula
                    );
                    continue;
                }

                double baseValue = instance.getBaseValue();

                ExpressionEvaluatorTool evaluator = new ExpressionEvaluatorTool();
                evaluator.setVariable("day", (double) day);
                evaluator.setVariable("time", (double) time);
                evaluator.setVariable("BASE", baseValue);

                double result = evaluator.evaluate(formula);

                if (!Double.isFinite(result)) {
                    LOGGER.error(
                            "[Spawner] 属性公式结果非法，拒绝写入：entity={}, attribute={}, BASE={}, day={}, formula={}, result={}",
                            BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()),
                            attributeConfig.attribute_id,
                            baseValue,
                            day,
                            formula,
                            result
                    );
                    continue;
                }

                boolean isMaxHealth =
                        attribute.value() == Attributes.MAX_HEALTH.value();

                if (isMaxHealth && result <= 0.0D) {
                    LOGGER.error(
                            "[Spawner] 最大生命结果 <= 0，拒绝写入：entity={}, BASE={}, day={}, formula={}, result={}",
                            BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()),
                            baseValue,
                            day,
                            formula,
                            result
                    );
                    continue;
                }

                instance.setBaseValue(result);

                LOGGER.debug(
                        "[Spawner] 属性覆盖成功：entity={}, attribute={}, BASE={}, day={}, formula={}, result={}",
                        BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()),
                        attributeConfig.attribute_id,
                        baseValue,
                        day,
                        formula,
                        result
                );
            } catch (Exception exception) {
                LOGGER.warn(
                        "[Spawner] 属性公式计算失败：attribute={}, formula={}, error={}",
                        attributeConfig.attribute_id,
                        attributeConfig.formula,
                        exception.getMessage()
                );
            }
        }
    }

    private static void applyEquipments(
            LivingEntity entity,
            SpawnerBuild.EquipmentConfig[] equipments,
            RandomSource random
    ) {
        if (!(entity instanceof Mob mob) || equipments == null) {
            return;
        }

        for (SpawnerBuild.EquipmentConfig equipment : equipments) {
            if (equipment == null || random.nextFloat() >= equipment.probability) {
                continue;
            }

            var itemHolder = ModUtils.getItem(equipment.item_id);
            if (itemHolder == null || itemHolder.value() == Items.AIR) {
                continue;
            }

            EquipmentSlot slot = getEquipmentSlot(equipment.slot);
            mob.setItemSlot(slot, new ItemStack(itemHolder.value()));
            mob.setDropChance(slot, 0.0F);
        }
    }

    private static EquipmentSlot getEquipmentSlot(String slotName) {
        if (slotName == null) {
            return EquipmentSlot.MAINHAND;
        }

        return switch (slotName.toLowerCase()) {
            case "mainhand" -> EquipmentSlot.MAINHAND;
            case "offhand" -> EquipmentSlot.OFFHAND;
            case "head" -> EquipmentSlot.HEAD;
            case "chest" -> EquipmentSlot.CHEST;
            case "legs" -> EquipmentSlot.LEGS;
            case "feet" -> EquipmentSlot.FEET;
            default -> EquipmentSlot.MAINHAND;
        };
    }

    private static void endofdays_re$postSpawnInit(Mob mob, ServerLevel world) {
        if (mob instanceof net.minecraft.world.entity.monster.Zombie zombie) {
            if (AllSyncValue.Instance.mode
                    == com.endofdays_re.utils.type.ModeEventType.BLOOD) {
                zombie.getPersistentData().putBoolean(
                        ModUtils.KeyWraps("blood"),
                        true
                );
            }

            endofdays_re$applyModAI(zombie);
        }

        endofdays_re$applyModArmor(mob);
    }

    private static void endofdays_re$applyModAI(
            net.minecraft.world.entity.monster.Zombie zombie
    ) {
        if (!ConfigData.isDayEnable("enable")
                || !ConfigData.isModeEnable("goal_enable")) {
            return;
        }

        GoalHelper.initGoal(zombie.goalSelector, zombie);
    }

    private static void endofdays_re$applyModArmor(Mob mob) {
        if (!ConfigData.isDayEnable("enable")
                || !(mob instanceof net.minecraft.world.entity.monster.Zombie)) {
            return;
        }

        RandomSource random = mob.getRandom();
        Collection<com.endofdays_re.client.config.data.ArrmorBuild.Arrmor> armorConfigs =
                ConfigData.arrmorData.Arrmor.values();

        for (com.endofdays_re.client.config.data.ArrmorBuild.Arrmor armor : armorConfigs) {
            if (armor == null) {
                continue;
            }

            long currentDay = AllSyncValue.Instance.day;
            if (currentDay < armor.day
                    || (armor.end_day >= 0 && currentDay > armor.end_day)
                    || random.nextDouble() > armor.chance
                    || !mob.getItemBySlot(armor.slot).isEmpty()) {
                continue;
            }

            if (armor.slot == EquipmentSlot.HEAD
                    && !mob.getPersistentData().contains(
                    ModUtils.KeyWraps("dispenser"))) {
                continue;
            }

            var itemHolder = ModUtils.getItem(armor.id);
            if (itemHolder == null || itemHolder.value() == Items.AIR) {
                continue;
            }

            ItemStack stack = new ItemStack(itemHolder.value());

            if (armor.enchantes != null) {
                for (com.endofdays_re.client.config.data.ArrmorBuild.Enchante enchantment
                        : armor.enchantes) {
                    if (enchantment == null
                            || random.nextDouble() > enchantment.chance) {
                        continue;
                    }

                    var enchantmentHolder = ModUtils.getEnchantment(
                            mob.level(),
                            enchantment.id
                    );

                    int minLevel = enchantment.level.level;
                    int maxLevel = enchantment.level.maxLevel;
                    int level = maxLevel <= minLevel
                            ? minLevel
                            : random.nextInt(minLevel, maxLevel + 1);

                    stack.enchant(enchantmentHolder, level);
                }
            }

            /*
             * 为保持原配置行为，armor.tag 仍合并到实体 NBT。
             * 若该字段实际想保存"物品组件/NBT"，应单独设计为 ItemStack 配置；
             * 1.21.1 不应再把它直接当作旧版 ItemStack NBT 使用。
             */
            if (armor.tag != null && !armor.tag.isBlank()) {
                LOGGER.warn(
                        "[Spawner] armor.tag 已忽略：1.21.1 中它不能合并到实体 NBT；" +
                                "否则可能覆盖实体的 Health 或 attributes。item={}",
                        armor.id
                );
            }

            mob.setItemSlot(armor.slot, stack);
            mob.setDropChance(armor.slot, 0.085F);
        }
    }

    private record EntityCountCache(long tick, double range, int count) {
    }
}