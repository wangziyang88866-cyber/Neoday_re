package com.endofdays_re.event;


import com.endofdays_re.command.Command;
import com.endofdays_re.config.ConfigData;
import com.endofdays_re.event.data.AllSyncValue;
import com.endofdays_re.event.data.LevelDataSava;
import com.endofdays_re.event.helper.*;
import com.endofdays_re.event.register.BrainInitEvent;
import com.endofdays_re.level.goal.PathBuildingGoal;
import com.endofdays_re.level.goal.brains.EquipFromChestBehavior;
import com.endofdays_re.level.goal.brains.FollowBreakBlockBehavior;
import com.endofdays_re.level.register.ModMemoryModuleTypes;
import com.endofdays_re.level.register.RegisterEffect;
import com.endofdays_re.network.Network;
import com.endofdays_re.network.packer.s2c.SyncValueDataPacker;
import com.endofdays_re.utils.ModUtils;
import com.endofdays_re.utils.tools.RuleCacheUtils;
import com.endofdays_re.utils.type.EventPase;
import com.endofdays_re.utils.type.LevelTimeType;
import com.endofdays_re.utils.type.ModeEventType;
import com.google.common.collect.ImmutableList;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Path;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.List;
import java.util.Optional;

import static com.endofdays_re.config.ConfigData.isDayEnable;
import static com.endofdays_re.config.ConfigData.isModeEnable;
import static com.endofdays_re.event.helper.SimpleWeightListHelper.loot;
import static com.endofdays_re.level.goal.GoalTracker.getPathGoalFromZombie;

@EventBusSubscriber(modid = ModUtils.MODID)
public enum ServerForgeEvent {
    ;

    // --- 1. 实体生成与初始化 ---
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onMobSpawnFinalize(FinalizeSpawnEvent event) {
        LivingEntity entity = event.getEntity();
        // 1.21.1 逻辑优化：通常 FinalizeSpawn 只在服务端触发，但保持检查是好习惯
        if (entity.level().isClientSide) return;


        // 状态强化逻辑 (如果实体没有被取消生成)
        if (!event.isSpawnCancelled()) {
            if (entity instanceof Zombie zombie && isDayEnable("enable")) {
                // 僵尸血月标记
                if (AllSyncValue.Instance.mode == ModeEventType.BLOOD) {
                    // 1.21.1 中 getPersistentData() 依然可用，但建议之后迁移到 Data Attachments
                    zombie.getPersistentData().putBoolean(ModUtils.KeyWraps("blood"), true);
                }
                // 护甲强化已由ZombieArmorMixin处理
            }
        }

    }

    // --- 2. 行为/AI 注册 (Brain 系统适配) ---

    @SubscribeEvent
    public static void onBrainInit(BrainInitEvent<Mob> event) {
        if (event.getEntity() instanceof Zombie && !event.getEntity().level().isClientSide()) {
            if (isDayEnable("enable") && isModeEnable("goal_enable")) {
                if (event.getPase() == EventPase.Pre) {
                    event.setBrainProvider(Brain.provider(
                            List.of(ModMemoryModuleTypes.TARGET_BLOCK_POS.get(), ModMemoryModuleTypes.TARGET_BLOCK_POS_ARRMOR.get(),
                                    ModMemoryModuleTypes.ATTACK_TARGET_BLOCK_POS.get(), MemoryModuleType.ATTACK_TARGET,
                                    MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET),
                            List.of()));
                } else if (event.getPase() == EventPase.Post) {
                    event.getBrain().addActivity(Activity.CORE, 0, ImmutableList.of(
                            new EquipFromChestBehavior(8, 1.2D, ConfigData.commonConfigData.EquipChestMob.values().stream().toList()),
                            new FollowBreakBlockBehavior(8, 1.2D, ConfigData.commonConfigData.FollowBlockBreak.values().toArray(String[]::new))
                    ));
                    event.getBrain().setActiveActivityToFirstValid(List.of(Activity.CORE));
                }
            }
        }
    }

    // --- 3. 玩家逻辑 (附身/同步/惩罚) ---

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) { // 1.21.1 修正
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            // 附身逻辑
            Entity targetMob = InfectionHerper.getYourTargetMobForPlayer(serverPlayer);
            if (targetMob != null && targetMob.isAlive()) {
                if (!serverPlayer.isSpectator()) serverPlayer.setGameMode(GameType.SPECTATOR);
                if (serverPlayer.getCamera() != targetMob) serverPlayer.setCamera(targetMob);
                if (serverPlayer.tickCount % 20 == 0)
                    InfectionHerper.deepClonePlayerToMob(serverPlayer, (LivingEntity) targetMob);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingIncomingDamage(LivingIncomingDamageEvent event) { // 拦截致命伤
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(RegisterEffect.LACERATE)) {
            MobEffectInstance effect = entity.getEffect(RegisterEffect.LACERATE);
            if (effect != null) {
                int amp = effect.getAmplifier();

                // 1.21.1 获取原始伤害
                float originalAmount = event.getAmount();

                // 计算新伤害：基础增加 10%，每级再加 10%
                // 例如：0级倍率 1.1x, 1级倍率 1.2x
                float multiplier = 1.1F + (amp * 0.1F);
                float newAmount = originalAmount * multiplier;

                // 设置新伤害
                event.setAmount(newAmount);
            }
        }


    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;

        // 1. 附身联动：实体死，玩家死
        if (entity.getServer() != null) {
            for (ServerPlayer player : entity.getServer().getPlayerList().getPlayers()) {
                Entity target = InfectionHerper.getYourTargetMobForPlayer(player);
                if (target != null && target.getUUID().equals(entity.getUUID())) {
                    player.kill();
                    player.getPersistentData().remove("TargetPossessedMob");
                    break;
                }
            }
        }

        // 2. 僵尸复活逻辑
        if (entity instanceof Zombie zombie && zombie.getRandom().nextFloat() < 0.5f) {
            InteractionHand hand = zombie.getItemInHand(InteractionHand.MAIN_HAND).is(Items.TOTEM_OF_UNDYING) ? InteractionHand.MAIN_HAND :
                    zombie.getItemInHand(InteractionHand.OFF_HAND).is(Items.TOTEM_OF_UNDYING) ? InteractionHand.OFF_HAND : null;
            if (hand != null) {
                event.setCanceled(true);
                zombie.getItemInHand(hand).shrink(1);
                zombie.setHealth(zombie.getMaxHealth() / 2.0F);
                zombie.removeAllEffects();
                zombie.level().broadcastEntityEvent(zombie, (byte) 35);
            }
        }


    }

    // --- 4. 核心系统 Tick ---

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        var server = event.getServer();

        // 时间逻辑处理
        long worldTime = server.overworld().getDayTime();
        AllSyncValue.Instance.time = (int) (worldTime % 24000L);
        int currentDay = (int) (worldTime / 24000L);
        AllSyncValue.Instance.day = ConfigData.commonConfigData.enable_stuck_day ? currentDay / 7 : currentDay;

        boolean isDay = server.overworld().isDay();
        if (isDay != AllSyncValue.Instance.isDay && isDayEnable("enable")) {
            AllSyncValue.Instance.isDay = isDay;
            if (isDay) {
                // 白天切换判定（血月概率等）
                handleDaySwitch(server, currentDay);
            } else {
                // 夜晚切换
                AllSyncValue.Instance.mode = AllSyncValue.Instance.nextNightMode != null ? AllSyncValue.Instance.nextNightMode : ModeEventType.TIME;
                GameTimeHelper.update(server, server.overworld(), currentDay, LevelTimeType.NIGHT, false, AllSyncValue.Instance.mode, ConfigData.dimensionConfigData.weight + AllSyncValue.Instance.BProbability);
            }
        }

        // 定期全局同步
        if (ConfigData.commonConfigData.max_time != -1 && server.getTickCount() % ConfigData.commonConfigData.max_time == 0) {
            Network.sendToALLClient(new SyncValueDataPacker(AllSyncValue.Instance.temperature, Optional.of(""), AllSyncValue.Instance.day, AllSyncValue.Instance.time, AllSyncValue.Instance.mode, AllSyncValue.Instance.isDay, AllSyncValue.Instance.BProbability, true, AllSyncValue.Instance.nextNightMode));
            server.getPlayerList().getPlayers().forEach(p -> {
            });
        }
    }

    private static void handleDaySwitch(net.minecraft.server.MinecraftServer server, int currentDay) {
        double currentChance = ConfigData.dimensionConfigData.weight + AllSyncValue.Instance.BProbability;
        int daysSinceLast = currentDay - AllSyncValue.Instance.lastBloodMoonDay;

        if (daysSinceLast >= 5 && ConfigData.dimensionConfigData.enable && server.overworld().random.nextDouble() <= currentChance) {
            AllSyncValue.Instance.nextNightMode = ModeEventType.BLOOD;
            AllSyncValue.Instance.BProbability = 0.0f;
            AllSyncValue.Instance.lastBloodMoonDay = currentDay;
        } else {
            AllSyncValue.Instance.nextNightMode = ModeEventType.TIME;
            AllSyncValue.Instance.BProbability += ConfigData.dimensionConfigData.bloodMoonProbability;
        }
        GameTimeHelper.update(server, server.overworld(), currentDay, LevelTimeType.DAY, true, ModeEventType.TIME, currentChance);
        AllSyncValue.Instance.mode = ModeEventType.TIME;
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel level) {
            RuleCacheUtils.cache.performCount(level);
        }
    }

    // --- 5. 环境与交互 ---


    // --- 6. 其他常用事件补全 ---


    @SubscribeEvent
    public static void onLivingHeal(LivingHealEvent event) { // 重伤禁疗
        LivingEntity entity = event.getEntity();
        MobEffectInstance effect = entity.getEffect(RegisterEffect.HEAVY_INJURY);
        if (effect != null) {
            float multiplier = Math.max(0, 1.0f - (0.5f + (effect.getAmplifier() * 0.25f)));
            if (multiplier <= 0) event.setCanceled(true);
            else event.setAmount(event.getAmount() * multiplier);
        }
        if (ConfigData.ScreenConfigData.showHeal) {
            HealParticleHelper.spawn(event);
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        // 注册主命令 /endofdays_re
        event.getDispatcher().register(Command.root);

        // 注册短别名 /eod
        event.getDispatcher().register(
                net.minecraft.commands.Commands.literal("eod")
                        .then(net.minecraft.commands.Commands.literal("perf")
                                .requires(s -> s.hasPermission(2))
                                .then(net.minecraft.commands.Commands.literal("start")
                                        .executes(ctx -> {
                                            com.endofdays_re.utils.PerformanceAnalyzer.startMonitoring();
                                            ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("§a[性能分析] §f已开始监控，使用 §e/eod stop§f 查看报告"), true);
                                            return 1;
                                        })
                                )
                                .then(net.minecraft.commands.Commands.literal("stop")
                                        .executes(ctx -> {
                                            if (!com.endofdays_re.utils.PerformanceAnalyzer.isMonitoring()) {
                                                ctx.getSource().sendFailure(net.minecraft.network.chat.Component.literal("§c[错误] §f监控未启动，请先使用 §e/eod start"));
                                                return 0;
                                            }
                                            com.endofdays_re.utils.PerformanceAnalyzer.stopMonitoring();
                                            com.endofdays_re.utils.PerformanceAnalyzer.generateReport(ctx.getSource().getServer());
                                            return 1;
                                        })
                                )
                                .then(net.minecraft.commands.Commands.literal("reset")
                                        .executes(ctx -> {
                                            com.endofdays_re.utils.PerformanceAnalyzer.resetData();
                                            ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("§6[性能分析] §f已重置所有统计数据"), true);
                                            return 1;
                                        })
                                )
                                .then(net.minecraft.commands.Commands.literal("status")
                                        .executes(ctx -> {
                                            boolean monitoring = com.endofdays_re.utils.PerformanceAnalyzer.isMonitoring();
                                            String status = monitoring ? "§a运行中" : "§c已停止";
                                            ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("§b[性能分析] §f当前状态: " + status), false);
                                            return 1;
                                        })
                                )
                        )
        );
    }

    // --- 7. 实体进出场逻辑 ---
    // 注意：实体属性同步和AI初始化已移至Mixin处理，无需Event

    // --- 8. 维度与数据存取逻辑 ---

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;

        // 1. 权重列表初始化
        if (isDayEnable("enable")) {
            SimpleWeightListHelper.register();
        }


        // 注意：1.21.1 的 LevelDataSava.get(level) 内部通常已经处理了 NBT 读取
    }

    @SubscribeEvent
    public static void onLevelSave(LevelEvent.Save event) {
        if (event.getLevel() instanceof ServerLevel level) {
            // 显式标记数据为脏，确保下一次存盘动作执行
            LevelDataSava.get(level).setDirty();
        }
    }


    // --- 9. 环境逻辑 (ChunkTick) ---

    @SubscribeEvent
    public static void onChunkTick(LevelTickEvent.Post event) { // 1.21.1 建议在 LevelTick 中处理环境
        if (!(event.getLevel() instanceof ServerLevel world)) return;

        // 1.21.1 的随机环境掉落逻辑
        // 我们不遍历每个 Chunk，而是利用 ServerLevel 的随机刻或者自定义逻辑
        if (world.getGameTime() % 20 == 0) { // 每秒检查一次
            float temp = AllSyncValue.Instance.temperature;

            // 温度安全区间判断
            if (temp > -10 && temp < 30) return;

            // 针对在线玩家周围的随机采样
            for (ServerPlayer player : world.players()) {
                if (ModUtils.safeRandom.nextFloat() < 0.1f) {
                    // 在玩家附近 32 格内寻找随机点
                    int rx = player.getBlockX() + ModUtils.safeRandom.nextInt(64) - 32;
                    int rz = player.getBlockZ() + ModUtils.safeRandom.nextInt(64) - 32;
                    BlockPos pos = new BlockPos(rx, 0, rz);

                    if (world.hasChunkAt(pos)) {
                        BlockPos topPos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
                        levelHelper.processEnvironmentalDrop(world, topPos, temp);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        // 1. 实体掉落物篡改
        if (isDayEnable("enable") && isModeEnable("entity_drop_enable")) {
            DropsHelper.INSTANCE.dropStings(loot.build().getRandomValue(event.getEntity().getRandom()), event.getDrops(), (ServerLevel) event.getEntity().level(), event.getEntity());
        }
    }
    // --- 10. 实体燃烧与 AI 特殊逻辑 ---

    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Pre event) {
        if (event.getEntity() instanceof LivingEntity entity) {
            if (entity.level().isClientSide) return;

            // 确保是僵尸且功能开启
            if (entity instanceof Zombie zombie && isDayEnable("enable")) {

                // 1. 阳光免疫与血月燃烧逻辑
                if (isDayEnable("immune_sun") && ConfigData.enableConfigData.Data.get("immune_sun_enable").enable) {
                    // 如果在着火，强制熄灭
                    if (zombie.isOnFire()) {
                        zombie.clearFire();
                        zombie.setRemainingFireTicks(0);
                    }

                    // 特殊逻辑：如果是血月僵尸且是白天，反而让它自燃（根据你的业务逻辑）
                    if (AllSyncValue.Instance.isDay && zombie.getPersistentData().contains(ModUtils.KeyWraps("blood"))) {
                        if (!zombie.isOnFire()) {
                            zombie.igniteForSeconds(60); // 1.21.1 推荐使用 igniteForSeconds
                        }
                    }
                }

                // 2. 僵尸搭建 AI 触发逻辑
                // 建议每 10 tick 检查一次以节省 CPU
                if (zombie.tickCount % 10 == 0) {
                    PathBuildingGoal pathGoal = getPathGoalFromZombie(zombie);

                    // 只有当僵尸有目标、没在干活、且冷却完毕时才触发
                    if (pathGoal != null && zombie.getTarget() != null && !pathGoal.isWorking() && pathGoal.canUsePathBuilder()) {

                        Path path = zombie.getNavigation().getPath();

                        // 使用平方距离避免 Math.sqrt 开销 (3.5 * 3.5 = 12.25)
                        double hDistSqr = zombie.distanceToSqr(zombie.getTarget().getX(), zombie.getY(), zombie.getTarget().getZ());
                        int vDist = Math.abs(zombie.blockPosition().getY() - zombie.getTarget().blockPosition().getY());

                        // 触发条件：没有路径/路径无法到达 OR (水平距离近但高度差大)
                        boolean noPath = (path == null || !path.canReach());
                        boolean verticalGap = (hDistSqr <= 12.25D && vDist > 2);

                        if (noPath || verticalGap) {
                            pathGoal.triggerBuildSequence(zombie.getTarget().blockPosition());
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent.Post event) {
        if (ConfigData.ScreenConfigData.showDamage) {
            DamageParticleHelper.spawn(
                    event.getEntity(),
                    event.getSource().getEntity(), // 攻击者实体
                    event.getSource(),
                    event.getNewDamage()
            );
        }
    }

}