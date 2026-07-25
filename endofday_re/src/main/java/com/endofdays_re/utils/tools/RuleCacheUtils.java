package com.endofdays_re.utils.tools;


import com.endofdays_re.utils.ModUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class RuleCacheUtils {
    public static RuleCacheUtils cache = new RuleCacheUtils();
    private final Map<ResourceKey<Level>, CachePerWorld> caches = new HashMap<>();

    public void performCount(LevelAccessor world) {
        ResourceKey<Level> key = ModUtils.getDimensionKey(world);
        // Get a cache or create it when it doesn't exist
        CachePerWorld cache = caches.computeIfAbsent(key, k -> new CachePerWorld());
        cache.count(world);
    }

    public int getValidSpawnChunks(LevelAccessor world) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getValidSpawnChunks();
    }

    public int getValidPlayers(LevelAccessor world) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getValidPlayers();
    }

    public int getCountAll(LevelAccessor world) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getCountAll();
    }

    public int getCountPassive(LevelAccessor world) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getCountPassive();
    }

    public int getCountHostile(LevelAccessor world) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getCountHostile();
    }

    public int getCountItemEntity(LevelAccessor world) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getCountItemEntity();
    }

    public int getCountNeutral(LevelAccessor world) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getCountNeutral();
    }


    public int getCount(LevelAccessor world, EntityType<?> entityType) {
        CachePerWorld cache = getOrCreateCache(world);
        return cache.getCount(entityType);
    }

    public int getCountPerMod(LevelAccessor world, String mod) {
        CachePerWorld cache = getOrCreateCache(world);
        CountPerMod countPerMod = cache.getCountPerMod(mod);
        return countPerMod == null ? 0 : countPerMod.total;
    }

    public int getCountPerModHostile(LevelAccessor world, String mod) {
        CachePerWorld cache = getOrCreateCache(world);
        CountPerMod countPerMod = cache.getCountPerMod(mod);
        return countPerMod == null ? 0 : countPerMod.hostile;
    }

    public int getCountPerModPassive(LevelAccessor world, String mod) {
        CachePerWorld cache = getOrCreateCache(world);
        CountPerMod countPerMod = cache.getCountPerMod(mod);
        return countPerMod == null ? 0 : countPerMod.passive;
    }

    public int getCountPerModAll(LevelAccessor world, String mod) {
        CachePerWorld cache = getOrCreateCache(world);
        CountPerMod countPerMod = cache.getCountPerMod(mod);
        return countPerMod == null ? 0 : countPerMod.total;
    }

    public void registerSpawn(LevelAccessor world, EntityType<?> entityType) {
        CachePerWorld cache = getOrCreateCache(world);
        cache.registerSpawn(entityType);
    }

    public void registerDespawn(LevelAccessor world, EntityType<?> entityType) {
        CachePerWorld cache = getOrCreateCache(world);
        cache.registerDespawn(entityType);
    }

    private CachePerWorld getOrCreateCache(LevelAccessor world) {
        ResourceKey<Level> key = ModUtils.getDimensionKey(world);
        CachePerWorld cache = caches.get(key);
        if (cache == null) {
            cache = new CachePerWorld();
            caches.put(key, cache);
        }
        return cache;
    }


    private static class CountPerMod {
        private int hostile;
        private int passive;
        private int neutral;
        private int total;
        private int item;
    }

    private static class CachePerWorld {

        private final Map<EntityType<?>, Integer> cachedCounters = new HashMap<>();
        private final Map<String, CountPerMod> countPerMod = new HashMap<>();
        private int countPassive = -1;
        private int countHostile = -1;
        private int countNeutral = -1;
        private int countItemEntity = -1;
        private int validSpawnChunks = -1;
        private int validPlayers = -1;

        public int getValidSpawnChunks() {
            return validSpawnChunks;
        }

        public int getValidPlayers() {
            return validPlayers;
        }

        public int getCountAll() {
            return countHostile + countPassive + countNeutral + countItemEntity;
        }

        public int getCountPassive() {
            return countPassive;
        }

        public int getCountHostile() {
            return countHostile;
        }

        public int getCountItemEntity() {
            return countItemEntity;
        }

        public int getCountNeutral() {
            return countNeutral;
        }

        private int countValidPlayers(LevelAccessor world) {
            int cnt = 0;
            for (Player entityplayer : world.players()) {
                if (!entityplayer.isSpectator()) {
                    cnt++;
                }
            }
            return cnt;
        }

        private void count(LevelAccessor world) {
            ServerLevel sw1 = ModUtils.getServerWorld(world);
            validSpawnChunks = sw1.getChunkSource().chunkMap.size();
            validPlayers = countValidPlayers(world);

            cachedCounters.clear();
            countPerMod.clear();
            countPassive = 0;
            countHostile = 0;
            countNeutral = 0;
            countItemEntity = 0;

            ServerLevel sw = ModUtils.getServerWorld(world);

            sw.getEntities().getAll().forEach(entity -> {
                if (entity instanceof Mob) {
                    int cnt = cachedCounters.getOrDefault(entity.getType(), 0) + 1;
                    cachedCounters.put(entity.getType(), cnt);
                    @Nullable ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
                    String mod = key.getNamespace();
                    CountPerMod count = countPerMod.computeIfAbsent(mod, s -> new CountPerMod());
                    count.total++;

                    if (entity instanceof Enemy) {
                        count.hostile++;
                        countHostile++;
                    } else if (entity instanceof Animal) {
                        count.passive++;
                        countPassive++;
                    } else {
                        count.neutral++;
                        countNeutral++;
                    }
                } else if (entity instanceof ItemEntity) {
                    int cnt = cachedCounters.getOrDefault(entity.getType(), 0) + 1;
                    cachedCounters.put(entity.getType(), cnt);
                    @Nullable ResourceLocation key = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
                    String mod = key.getNamespace();
                    CountPerMod count = countPerMod.computeIfAbsent(mod, s -> new CountPerMod());
                    count.total++;
                    if (entity instanceof ItemEntity item) {
                        count.item++;
                        countItemEntity++;
                    }


                }
            });
        }

        public int getCount(EntityType<?> entityType) {
            return cachedCounters.getOrDefault(entityType, 0);
        }

        public CountPerMod getCountPerMod(String mod) {
            return countPerMod.get(mod);
        }

        public void registerSpawn(EntityType<?> entityType) {
            cachedCounters.put(entityType, cachedCounters.getOrDefault(entityType, 0) + 1);
        }

        public void registerDespawn(EntityType<?> entityType) {
            Integer cnt = cachedCounters.getOrDefault(entityType, 0);
            if (cnt > 0) {
                cachedCounters.put(entityType, cnt - 1);
            }
        }
    }

}
