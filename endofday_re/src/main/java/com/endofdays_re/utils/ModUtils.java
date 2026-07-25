package com.endofdays_re.utils;

import com.endofdays_re.network.Network;
import com.endofdays_re.network.packer.s2c.UpdataBiomePaket;
import com.endofdays_re.utils.tools.TextureLoader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.LinearCongruentialGenerator;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class ModUtils {
    public static final String MODID = "endofdays_re";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<Entity> entities = new ArrayList<>();
    public static ThreadLocalRandom random = ThreadLocalRandom.current();
    public static RandomSource safeRandom = RandomSource.create();
    public static boolean IsShowDebug = !FMLEnvironment.production;
    public static boolean ShowDebug = true;

    public static void log(String message, Object... args) {
        LOGGER.info(message, args);
    }

    public static void error(String message, Object... args) {
        LOGGER.error(message, args);
    }

    public static void warn(String message, Object... args) {
        LOGGER.warn(message, args);
    }

    public static void debug(String message, Object... args) {
        LOGGER.debug(message, args);
    }

    public static Holder<Item> getItem(String key) {
        return BuiltInRegistries.ITEM.getHolder(ResourceLocation.parse(key))
                .orElse(BuiltInRegistries.ITEM.getHolder(net.minecraft.world.item.Items.AIR.builtInRegistryHolder().key()).orElseThrow());
    }

    public static Holder<Biome> getBiome(Level level, String key) {
        ResourceLocation location = ResourceLocation.parse(key);
        ResourceKey<Biome> biomeKey = ResourceKey.create(Registries.BIOME, location);
        return level.registryAccess()
                .lookupOrThrow(Registries.BIOME)
                .getOrThrow(biomeKey);
    }

    public static Holder<Block> getBlock(String key) {
        return BuiltInRegistries.BLOCK.getHolder(ResourceLocation.parse(key)).orElseThrow();
    }

    public static <T extends Entity> EntityType<?> getEntityType(String key) {
        return BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.tryParse(key));
    }

    public static String getItemID(Item item) {
        return item.builtInRegistryHolder().key().location().toString();
    }

    public static ResourceLocation getItemRID(Item item) {
        return item.builtInRegistryHolder().key().location();
    }

    public static Holder.Reference<Attribute> getAttribute(String key) {
        return BuiltInRegistries.ATTRIBUTE.getHolder(ResourceLocation.parse(key)).orElse(null);
    }

    public static @Nullable ResourceLocation getAttributeId(Attribute key) {
        return BuiltInRegistries.ATTRIBUTE.getKey(key);
    }

    public static String getBiomeID(Holder<Biome> biome) {
        return biome.unwrap().orThrow().location().toString();
    }

    public static String getBlockID(Block block) {
        return block.builtInRegistryHolder().key().location().toString();
    }

    public static Holder<Enchantment> getEnchantment(Level level, String key) {
        ResourceLocation location = ResourceLocation.parse(key);
        ResourceKey<Enchantment> enchantKey = ResourceKey.create(Registries.ENCHANTMENT, location);
        return level.registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(enchantKey);
    }

    public static String getEntityTypeID(EntityType<?> entityType) {
        return entityType.builtInRegistryHolder().key().location().toString();
    }

    public static ResourceLocation getEntityTypeRID(EntityType<?> entityType) {
        return entityType.builtInRegistryHolder().key().location();
    }

    public static List<String> getBlocksByIDOrRegex(String idOrRegex) {
        if (idOrRegex.startsWith("regex:")) {
            String regex = idOrRegex.substring(6);
            Pattern pattern = Pattern.compile(regex);
            List<String> matched = new ArrayList<>();
            for (Block block : BuiltInRegistries.BLOCK.stream().toList()) {
                String id = getBlockID(block).trim();
                if (pattern.matcher(id).matches()) {
                    matched.add(ModUtils.getBlockID(block));
                }
            }
            return matched;
        } else {
            String exact = getBlockID(getBlock(idOrRegex).value());
            return List.of(exact);
        }
    }

    public static List<EntityType<?>> getEntitiesSmart(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            return new ArrayList<>();
        }

        List<EntityType<?>> matched = new ArrayList<>();

        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE.stream().toList()) {
            String entityId = getEntityTypeID(entityType);
            if (entityId.equals(pattern)) {
                matched.add(entityType);
                return matched;
            }
        }

        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE.stream().toList()) {
            String entityId = getEntityTypeID(entityType);
            if (entityId.contains(pattern)) {
                matched.add(entityType);
            }
        }
        if (!matched.isEmpty()) return matched;

        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE.stream().toList()) {
            String entityId = getEntityTypeID(entityType);
            if (entityId.startsWith(pattern + ":")) {
                matched.add(entityType);
            }
        }
        if (!matched.isEmpty()) return matched;

        try {
            Pattern regexPattern = Pattern.compile(pattern);
            for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE.stream().toList()) {
                String entityId = getEntityTypeID(entityType);
                if (regexPattern.matcher(entityId).matches()) {
                    matched.add(entityType);
                }
            }
            if (!matched.isEmpty()) return matched;
        } catch (Exception e) {
            System.err.println("无效的正则表达式: " + pattern);
        }

        try {
            if (pattern.contains(":")) {
                String[] entity = pattern.split(":");
                MobCategory category = MobCategory.valueOf(entity[1].toUpperCase());
                return BuiltInRegistries.ENTITY_TYPE.stream()
                        .filter(entityType -> entityType.getCategory() == category && entity[0].equals(getEntityTypeRID(entityType).getNamespace()))
                        .collect(Collectors.toList());
            } else {
                MobCategory category = MobCategory.valueOf(pattern.toUpperCase());
                return BuiltInRegistries.ENTITY_TYPE.stream()
                        .filter(entityType -> entityType.getCategory() == category)
                        .collect(Collectors.toList());
            }
        } catch (IllegalArgumentException e) {
            return new ArrayList<>();
        }
    }

    public static ItemStack getItemStackWithNbt(String itemId, @Nullable String nbtStr) {
        Holder<Item> itemHolder = getItem(itemId);
        if (itemHolder.value() == net.minecraft.world.item.Items.AIR) {
            ModUtils.warn("Item not found or is AIR: " + itemId);
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(itemHolder);
        if (nbtStr != null && !nbtStr.trim().isEmpty() && !nbtStr.equals("{}")) {
            try {
                CompoundTag compoundtag = TagParser.parseTag(nbtStr);
                DataComponentPatch patch = DataComponentPatch.CODEC.parse(NbtOps.INSTANCE, compoundtag).result().orElse(DataComponentPatch.EMPTY);
                stack.applyComponents(patch);
            } catch (Exception e) {
                log("Failed to parse Data Components (NBT) for item " + itemId + ": " + nbtStr);
            }
        }
        return stack;
    }

    public static boolean isNaturalBlock(Block block) {
        return block instanceof GrassBlock
                || block instanceof LeavesBlock
                || block instanceof TallGrassBlock;
    }

    public static boolean doesTextureExist(ResourceLocation resourceLocation) {
        Minecraft mc = Minecraft.getInstance();
        Optional<Resource> resource = mc.getResourceManager().getResource(resourceLocation);
        return resource.isPresent();
    }

    public static String getItemTranslationName(Item item) {
        return item.getDefaultInstance().getHoverName().getString();
    }

    public static String KeyWraps(String key) {
        String keys = key.toLowerCase();
        return MODID + "_" + keys.replace(" ", "_");
    }

    public static boolean isloadMod(String modid) {
        return ModList.get().isLoaded(modid);
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public static List<String> getAllEntityIds() {
        List<String> entityIds = new ArrayList<>();
        for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
            ResourceLocation id = EntityType.getKey(entityType);
            entityIds.add(id.toString());
        }
        return entityIds;
    }

    public static String getEntityTypeName(EntityType<?> entityType) {
        return entityType.builtInRegistryHolder().key().location().getPath();
    }

    public static ResourceKey<Level> getDimensionKey(LevelAccessor world) {
        if (world instanceof Level) {
            return ((Level) world).dimension();
        } else if (world instanceof ServerLevelAccessor) {
            return ((ServerLevelAccessor) world).getLevel().dimension();
        } else {
            throw new IllegalStateException("Not possible to get a dimension key here!");
        }
    }

    public static String getDimensionKeyName(Level level) {
        ResourceLocation dim = getDimensionKey(level).location();
        return dim.toString();
    }

    // ==================== 修改后的 getGunItem 方法（反射，无硬依赖） ====================
    public static ItemStack getGunItem(HolderLookup.Provider provider, String key, String fireMode, int AmmoCount, boolean AmmoInBarrel) {
        if (!ModList.get().isLoaded("tacz")) {
            return ItemStack.EMPTY;
        }
        try {
            Class<?> gunModClass = Class.forName("com.tacz.guns.GunMod");
            String modId = (String) gunModClass.getField("MOD_ID").get(null);

            Class<?> builderClass = Class.forName("com.tacz.guns.api.item.builder.GunItemBuilder");
            Method createMethod = builderClass.getMethod("create");
            Object builder = createMethod.invoke(null);

            Class<?> fireModeClass = Class.forName("com.tacz.guns.api.item.gun.FireMode");
            Enum<?> modeEnum = Enum.valueOf((Class<Enum>) fireModeClass, fireMode.toUpperCase());

            ResourceLocation gunId = ResourceLocation.fromNamespaceAndPath(modId, key);
            Method setIdMethod = builderClass.getMethod("setId", ResourceLocation.class);
            builder = setIdMethod.invoke(builder, gunId);

            Method setFireModeMethod = builderClass.getMethod("setFireMode", fireModeClass);
            builder = setFireModeMethod.invoke(builder, modeEnum);

            Method setAmmoCountMethod = builderClass.getMethod("setAmmoCount", int.class);
            builder = setAmmoCountMethod.invoke(builder, AmmoCount);

            Method setAmmoInBarrelMethod = builderClass.getMethod("setAmmoInBarrel", boolean.class);
            builder = setAmmoInBarrelMethod.invoke(builder, AmmoInBarrel);

            Method buildMethod = builderClass.getMethod("build", HolderLookup.Provider.class);
            return (ItemStack) buildMethod.invoke(builder, provider);
        } catch (Exception e) {
            ModUtils.warn("Failed to build TACZ gun item via reflection: " + e.getMessage());
            return ItemStack.EMPTY;
        }
    }

    public static ItemStack getGunItem(HolderLookup.Provider provider, ResourceLocation key, String fireMode, int AmmoCount, boolean HeatData, boolean AmmoInBarrel) {
        if (!ModList.get().isLoaded("tacz")) {
            return ItemStack.EMPTY;
        }
        try {
            Class<?> builderClass = Class.forName("com.tacz.guns.api.item.builder.GunItemBuilder");
            Method createMethod = builderClass.getMethod("create");
            Object builder = createMethod.invoke(null);

            Class<?> fireModeClass = Class.forName("com.tacz.guns.api.item.gun.FireMode");
            Enum<?> modeEnum = Enum.valueOf((Class<Enum>) fireModeClass, fireMode.toUpperCase());

            Method setIdMethod = builderClass.getMethod("setId", ResourceLocation.class);
            builder = setIdMethod.invoke(builder, key);

            Method setFireModeMethod = builderClass.getMethod("setFireMode", fireModeClass);
            builder = setFireModeMethod.invoke(builder, modeEnum);

            Method setAmmoCountMethod = builderClass.getMethod("setAmmoCount", int.class);
            builder = setAmmoCountMethod.invoke(builder, AmmoCount);

            Method setHeatDataMethod = builderClass.getMethod("setHeatData", boolean.class);
            builder = setHeatDataMethod.invoke(builder, HeatData);

            Method setAmmoInBarrelMethod = builderClass.getMethod("setAmmoInBarrel", boolean.class);
            builder = setAmmoInBarrelMethod.invoke(builder, AmmoInBarrel);

            Method buildMethod = builderClass.getMethod("build", HolderLookup.Provider.class);
            return (ItemStack) buildMethod.invoke(builder, provider);
        } catch (Exception e) {
            ModUtils.warn("Failed to build TACZ gun item via reflection: " + e.getMessage());
            return ItemStack.EMPTY;
        }
    }

    // ==================== 其余未改动的方法 ====================
    public static Level getDimension(ServerLevel level, String world) {
        ResourceLocation key = ResourceLocation.tryParse(world);
        if (key != null) {
            ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, key);
            return level.getServer().getLevel(dimKey);
        }
        return null;
    }

    public static TextureLoader.SpriteInfo getConfigTexture(String fileName, String resourceName) {
        try {
            Path configPath = FMLPaths.CONFIGDIR.get();
            Path texturesDir = configPath.resolve(ModUtils.MODID).resolve("textures");
            if (!Files.exists(texturesDir)) {
                Files.createDirectories(texturesDir);
            }
            Path resourcePath = texturesDir.resolve(fileName);
            File textureFile = resourcePath.toFile();
            return TextureLoader.load(textureFile, resourceName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ServerLevel getServerWorld(LevelAccessor world) {
        ServerLevel sw;
        if (world instanceof ServerLevel) {
            sw = (ServerLevel) world;
        } else if (world instanceof ServerLevelAccessor) {
            sw = ((ServerLevelAccessor) world).getLevel();
        } else {
            throw new IllegalStateException("No world found!");
        }
        return sw;
    }

    public static EquipmentSlot getEquipmentSlot(String test) {
        return EquipmentSlot.byName(test);
    }

    public static boolean isEmptySlot(LivingEntity livingEntity, EquipmentSlot... slots) {
        for (EquipmentSlot slot : slots) {
            ItemStack stack = livingEntity.getItemBySlot(slot);
            if (stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static Optional<EquipmentSlot> findFirstEmptySlot(LivingEntity livingEntity, EquipmentSlot... slots) {
        for (EquipmentSlot slot : slots) {
            if (livingEntity.getItemBySlot(slot).isEmpty()) {
                return Optional.of(slot);
            }
        }
        return Optional.empty();
    }

    public static Item getSlotItem(LivingEntity livingEntity, Item ofItem, EquipmentSlot... slots) {
        for (EquipmentSlot slot : slots) {
            ItemStack stack = livingEntity.getItemBySlot(slot);
            if (!stack.isEmpty() && stack.is(ofItem)) {
                return stack.getItem();
            }
        }
        return Items.AIR;
    }

    public static boolean isSlotEmpty(LivingEntity livingEntity, List<Item> ofItem, EquipmentSlot... slots) {
        for (EquipmentSlot slot : slots) {
            ItemStack stack = livingEntity.getItemBySlot(slot);
            if (!stack.isEmpty() && ofItem.contains(stack.getItem())) {
                return true;
            }
        }
        return false;
    }

    public static String getRandomKey(String... list) {
        if (list.length == 0) return "minecraft:air";
        int indexed = safeRandom.nextInt(0, list.length);
        return list[indexed];
    }

    public static Item getRandomItem(Item... list) {
        if (list.length == 0) return Items.AIR;
        int indexed = safeRandom.nextInt(0, list.length);
        return list[indexed];
    }

    public static EquipmentSlot getRandomSlot(EquipmentSlot... slots) {
        if (slots.length == 0) return EquipmentSlot.HEAD;
        int indexed = safeRandom.nextInt(0, slots.length);
        return slots[indexed];
    }

    @SafeVarargs
    public static <T> T getValue(weight<T>... weights) {
        SimpleWeightedRandomList.Builder<T> weightedRandomList = SimpleWeightedRandomList.builder();
        for (weight<T> weight : weights) {
            weightedRandomList.add(weight.value, weight.weight);
        }
        return weightedRandomList.build().getRandomValue(ModUtils.safeRandom).orElseThrow();
    }

    public static LiteralArgumentBuilder<CommandSourceStack> register(LiteralArgumentBuilder<CommandSourceStack> stack) {
        return stack;
    }

    public static String getEntityTypeTranslationName(EntityType<Zombie> zombie) {
        String translationKey = zombie.getDescriptionId();
        Component translatedName = Component.translatable(translationKey);
        return translatedName.getString();
    }

    public static boolean setAttributeBaseValue(LivingEntity entity, Holder<Attribute> attribute, double value) {
        AttributeInstance attributeInstance = entity.getAttribute(attribute);
        if (attributeInstance != null) {
            attributeInstance.setBaseValue(value);
            if (attribute == Attributes.MAX_HEALTH && !entity.isDeadOrDying()) {
                entity.setHealth(entity.getMaxHealth());
            }
            return true;
        } else {
            return false;
        }
    }

    public static boolean setAttributeValue(LivingEntity entity, Holder<Attribute> attribute, double value) {
        if (attribute == null || entity == null) return false;

        AttributeInstance attributeInstance = entity.getAttribute(attribute);
        if (attributeInstance == null) return false;

        ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath(
                ModUtils.MODID,
                "custom_modifier_" + attribute.value().getDescriptionId().replace(".", "_")
        );

        double originalBaseValue = attributeInstance.getBaseValue();
        double oldEffectiveValue = attributeInstance.getValue();

        attributeInstance.removeModifier(modifierId);

        double multiplier = (originalBaseValue != 0) ? (value / originalBaseValue) - 1 : 0;
        AttributeModifier modifier = new AttributeModifier(
                modifierId,
                multiplier,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
        );

        attributeInstance.addPermanentModifier(modifier);

        if (attribute.equals(Attributes.MAX_HEALTH)) {
            adjustCurrentHealth(entity, oldEffectiveValue, value);
        }
        return true;
    }

    private static void adjustCurrentHealth(LivingEntity entity, double oldMaxHealth, double newMaxHealth) {
        if (entity.isDeadOrDying()) {
            return;
        }
        double currentHealth = entity.getHealth();
        double healthRatio = currentHealth / oldMaxHealth;
        double newHealth = newMaxHealth * healthRatio;
        if (newHealth < 1 && newMaxHealth >= 1) {
            newHealth = 1;
        }
        if (newHealth > newMaxHealth) {
            newHealth = newMaxHealth;
        }
        entity.setHealth((float) newHealth);
    }

    public static void broadcast(MinecraftServer server, Component msg) {
        if (server == null || msg == null) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.sendSystemMessage(msg);
        }
    }

    public static void setBiome(ServerLevel serverLevel, BlockPos blockPos, ResourceKey<Biome> biome) {
        setBlockPosBiome(serverLevel, blockPos, biome);
        updateChunkAfterBiomeChange(serverLevel, new ChunkPos(blockPos));
    }

    public static void setBlockPosBiome(ServerLevel level, BlockPos posIn, ResourceKey<Biome> biome) {
        int i = posIn.getX() - 2;
        int j = posIn.getY() - 2;
        int k = posIn.getZ() - 2;
        int l = i >> 2;
        int i1 = j >> 2;
        int j1 = k >> 2;
        double d0 = (double) (i & 3) / 4.0D;
        double d1 = (double) (j & 3) / 4.0D;
        double d2 = (double) (k & 3) / 4.0D;
        int k1 = 0;
        double d3 = Double.POSITIVE_INFINITY;

        for (int l1 = 0; l1 < 8; ++l1) {
            boolean flag = (l1 & 4) == 0;
            boolean flag1 = (l1 & 2) == 0;
            boolean flag2 = (l1 & 1) == 0;
            int i2 = flag ? l : l + 1;
            int j2 = flag1 ? i1 : i1 + 1;
            int k2 = flag2 ? j1 : j1 + 1;
            double d4 = flag ? d0 : d0 - 1.0D;
            double d5 = flag1 ? d1 : d1 - 1.0D;
            double d6 = flag2 ? d2 : d2 - 1.0D;
            double d7 = getFiddledDistance(level.getServer().getWorldData().worldGenOptions().seed(), i2, j2, k2, d4, d5, d6);
            if (d3 > d7) {
                k1 = l1;
                d3 = d7;
            }
        }

        int l2 = (k1 & 4) == 0 ? l : l + 1;
        int i3 = (k1 & 2) == 0 ? i1 : i1 + 1;
        int j3 = (k1 & 1) == 0 ? j1 : j1 + 1;

        ChunkAccess chunk = level.getChunk(QuartPos.toSection(l2), QuartPos.toSection(j3), ChunkStatus.BIOMES, false);
        if (chunk instanceof ImposterProtoChunk) {
            chunk = ((ImposterProtoChunk) chunk).getWrapped();
        }
        if (chunk != null) {
            Registry<Biome> biomeRegistry = level.registryAccess().registryOrThrow(Registries.BIOME);
            Optional<Holder.Reference<Biome>> biomeHack = biomeRegistry.getHolder(biome);
            if (biomeHack.isEmpty()) {
                return;
            }
            int minBuildHeight = QuartPos.fromBlock(chunk.getMinBuildHeight());
            int maxHeight = minBuildHeight + QuartPos.fromBlock(chunk.getHeight()) - 1;
            int dummyY = Mth.clamp(i3, minBuildHeight, maxHeight);
            int sectionIndex = chunk.getSectionIndex(QuartPos.toBlock(dummyY));
            ((PalettedContainer<Holder<Biome>>) chunk.getSections()[sectionIndex].getBiomes()).set(l2 & 3, dummyY & 3, j3 & 3, biomeHack.get());
            chunk.setUnsaved(true);
        } else {
            debug("Tried changing biome at non-existing chunk for position " + posIn, 2);
        }
    }

    private static double getFiddledDistance(long pSeed, int pX, int pY, int pZ, double pXNoise, double pYNoise, double pZNoise) {
        long $$7 = LinearCongruentialGenerator.next(pSeed, pX);
        $$7 = LinearCongruentialGenerator.next($$7, pY);
        $$7 = LinearCongruentialGenerator.next($$7, pZ);
        $$7 = LinearCongruentialGenerator.next($$7, pX);
        $$7 = LinearCongruentialGenerator.next($$7, pY);
        $$7 = LinearCongruentialGenerator.next($$7, pZ);
        double d0 = getFiddle($$7);
        $$7 = LinearCongruentialGenerator.next($$7, pSeed);
        double d1 = getFiddle($$7);
        $$7 = LinearCongruentialGenerator.next($$7, pSeed);
        double d2 = getFiddle($$7);
        return Mth.square(pZNoise + d2) + Mth.square(pYNoise + d1) + Mth.square(pXNoise + d0);
    }

    public static void updateChunkAfterBiomeChange(Level level, ChunkPos chunkPos) {
        LevelChunk chunkSafe = level.getChunkSource().getChunk(chunkPos.x, chunkPos.z, false);
        if (chunkSafe == null) {
            debug("数据块为空，在生物群系更改后无法更新数据块", 2);
            return;
        }
        ((ServerChunkCache) level.getChunkSource()).chunkMap.getPlayers(chunkPos, false).forEach((player) -> {
            player.connection.send(new ClientboundLevelChunkWithLightPacket(chunkSafe, ((ServerChunkCache) level.getChunkSource()).getLightEngine(), null, null));
            Network.sendToPlayer(player, new UpdataBiomePaket(chunkPos.x, chunkPos.z));
        });
    }

    private static double getFiddle(long pSeed) {
        double d0 = (double) Math.floorMod(pSeed >> 24, 1024) / 1024.0D;
        return (d0 - 0.5D) * 0.9D;
    }

    public boolean compareDimension(Level level, String dimKey) {
        ResourceLocation dim = getDimensionKey(level).location();
        return dim.equals(ResourceLocation.tryParse(dimKey));
    }

    public static class weight<T> {
        public T value;
        public int weight;

        public weight(T value, int weight) {
            this.value = value;
            this.weight = weight;
        }
    }
}