package com.endofdays_re.command;


import com.endofdays_re.config.ConfigData;
import com.endofdays_re.event.data.AllSyncValue;
import com.endofdays_re.event.data.LevelDataSava;
import com.endofdays_re.event.helper.InvasionHelper;
import com.endofdays_re.event.helper.InvasionManager;
import com.endofdays_re.event.helper.SimpleWeightListHelper;
import com.endofdays_re.utils.ModUtils;
import com.endofdays_re.utils.type.ModeEventType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class Command {
    public static final LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal(ModUtils.MODID);

    static {
        addScreen();
        addDayConfig();
        addDaySystemControl(); // 独立时间系统控制 (OP)
        addfunction();         // 温度与功能控制 (OP)
        CommandConfigCommon.addConfigCommon(root);
        addInvasionControl();  // 入侵系统控制 (OP)

        addSmelterControl();
        addInfectionControl();
        addPerformanceControl(); // 性能分析工具
    }

    /**
     * 9. 感染与附身控制 (Infection/Possession) - 仅限 OP
     */
    public static void addInfectionControl() {
        LiteralArgumentBuilder<CommandSourceStack> infection = Commands.literal("infect").requires(s -> s.hasPermission(2));

        // 指令格式: /eod infect <player>
        CommandHelper.addCommon(infection, new String[]{"<server_player:target>"}, (ctx, args) -> {
            ServerPlayer target = (ServerPlayer) args.get("target");

            // 调用我们之前写的辅助类方法
            // 注意：InfectionHerper 是你代码里的类名（即便有拼写错误也需保持一致）
            com.endofdays_re.event.helper.InfectionHerper.infectPlayer(target);

            ctx.getSource().sendSuccess(() -> Component.literal("§a[感染系统] §f已成功将玩家 §e" + target.getScoreboardName() + " §f转化为僵尸傀儡。"), true);
            return 1;
        });

        // 强制解除指令: /eod infect stop <player>
        CommandHelper.addCommon(infection, new String[]{"stop", "<server_player:target>"}, (ctx, args) -> {
            ServerPlayer target = (ServerPlayer) args.get("target");

            // 获取当前附身的实体并移除
            net.minecraft.world.entity.Entity puppet = com.endofdays_re.event.helper.InfectionHerper.getYourTargetMobForPlayer(target);
            if (puppet != null) {
                puppet.discard(); // 销毁僵尸
            }

            // 清理数据和视角
            target.getPersistentData().remove("TargetPossessedMob");
            target.setCamera(target);
            // 如果需要，恢复生存模式
            target.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);

            ctx.getSource().sendSuccess(() -> Component.literal("§6[感染系统] §f已解除玩家 §e" + target.getScoreboardName() + " §f的感染状态。"), true);
            return 1;
        });

        root.then(infection);
    }

    /**
     * 7. 析光熔炉控制 (Smelter Blacklist Management) - 仅限 OP
     */
    public static void addSmelterControl() {
        LiteralArgumentBuilder<CommandSourceStack> smelter = Commands.literal("smelter").requires(s -> s.hasPermission(2));

        // 添加黑名单 (支持 ID 或 #标签)
        CommandHelper.addCommon(smelter, new String[]{"blacklist", "add", "<string:entry>"}, (ctx, args) -> {
            String entry = (String) args.get("entry");
            if (!ConfigData.commonConfigData.SMELT_BLACKLIST.contains(entry)) {
                ConfigData.commonConfigData.SMELT_BLACKLIST.add(entry);
                ctx.getSource().sendSuccess(() -> Component.literal("§a[熔炉] §f已将 §e" + entry + " §f加入黑名单。"), true);
                ConfigData.build();
                return 1;
            }
            ctx.getSource().sendFailure(Component.literal("§c[错误] §f该条目已在黑名单中。"));
            return 0;
        });
        CommandHelper.addCommon(smelter, new String[]{"blacklist", "add_hand"}, (ctx, args) -> {
            ServerPlayer player;
            try {
                player = ctx.getSource().getPlayerOrException();
            } catch (CommandSyntaxException e) {
                return 0;
            }

            ItemStack stack = player.getMainHandItem();
            if (stack.isEmpty()) {
                ctx.getSource().sendFailure(Component.literal("§4[错误] §f请先手持需要禁用的物品！"));
                return 0;
            }

            // 获取物品注册名 (例如 minecraft:dirt)
            String registryName = ModUtils.getItemID(stack.getItem());

            // 逻辑：如果玩家潜行(Shift)，则尝试添加该物品的第一个标签，否则添加具体 ID
            // 这里为了简单直观，默认添加具体 ID
            return addEntryToBlacklist(ctx.getSource(), registryName);
        });
        // 移除黑名单
        CommandHelper.addCommon(smelter, new String[]{"blacklist", "remove", "<string:entry>"}, (ctx, args) -> {
            String entry = (String) args.get("entry");
            if (ConfigData.commonConfigData.SMELT_BLACKLIST.remove(entry)) {
                ctx.getSource().sendSuccess(() -> Component.literal("§6[熔炉] §f已将 §e" + entry + " §f从黑名单移除。"), true);
                ConfigData.build();
                return 1;
            }
            ctx.getSource().sendFailure(Component.literal("§c[错误] §f黑名单中未找到该条目。"));
            return 0;
        });

        // 查询当前黑名单
        CommandHelper.addCommon(smelter, new String[]{"blacklist", "query"}, (ctx, args) -> {
            List<String> list = ConfigData.commonConfigData.SMELT_BLACKLIST;
            if (list.isEmpty()) {
                ctx.getSource().sendSuccess(() -> Component.literal("§7[熔炉] 当前黑名单为空。"), false);
            } else {
                ctx.getSource().sendSuccess(() -> Component.literal("§d[熔炉] 当前黑名单: §f" + String.join(", ", list)), false);
            }
            return 1;
        });

        root.then(smelter);
    }


    /**
     * 3. 入侵系统控制
     */
    public static void addInvasionControl() {
        // 权限建议：0 是所有人，2 是管理员。入侵控制通常建议设为 2。
        LiteralArgumentBuilder<CommandSourceStack> invasion = Commands.literal("invasion").requires(s -> s.hasPermission(2));

        // 逻辑一：尝试触发自然入侵 (try)
        invasion.then(Commands.literal("try")
                .then(Commands.argument("target", EntityArgument.player())
                        .executes(ctx -> {
                            ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                            InvasionHelper.tryExecuteInvasion(target);
                            ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("§a已尝试对玩家 " + target.getScoreboardName() + " 发起自然入侵判定"), true);
                            return 1;
                        })
                ));

        // 逻辑二：强制发起指定 ID 的入侵 (force)
        invasion.then(Commands.literal("force")
                .then(Commands.argument("target", EntityArgument.player())
                        .then(Commands.argument("id", StringArgumentType.string())
                                .executes(ctx -> {
                                    ServerPlayer target = EntityArgument.getPlayer(ctx, "target");
                                    String id = StringArgumentType.getString(ctx, "id");

                                    // 1.21.1 修正：适配权重列表获取逻辑
                                    // 假设你的 invasionSettingsBuilder 返回的是经过 Holder 包装或资源定位的列表
                                    var settingsOpt = SimpleWeightListHelper.invasionSettingsBuilder.build().unwrap().stream()
                                            .filter(entry -> entry.data().key.equals(id))
                                            .findFirst();

                                    if (settingsOpt.isPresent()) {
                                        // 1.21.1 随机数建议使用 level().random
                                        int waveCount = 1 + target.level().getRandom().nextInt(3);
                                        InvasionManager.startInvasionTask(target, settingsOpt.get().data(), waveCount);

                                        ctx.getSource().sendSuccess(() -> net.minecraft.network.chat.Component.literal("§6§l[强制入侵] §f成功对 " + target.getScoreboardName() + " 开启了: §e" + id), true);
                                        return 1;
                                    } else {
                                        ctx.getSource().sendFailure(net.minecraft.network.chat.Component.literal("§c未找到 ID 为 " + id + " 的入侵配置"));
                                        return 0;
                                    }
                                })
                        )));

        root.then(invasion);
    }

    /**
     * 1. 独立时间系统控制 - 仅限 OP
     */
    public static void addDaySystemControl() {
        LiteralArgumentBuilder<CommandSourceStack> days = Commands.literal("daysystem").requires(s -> s.hasPermission(2));
        CommandHelper.addCommon(days, new String[]{"set", "<long:value>"}, (ctx, args) -> {
            AllSyncValue.Instance.day = (long) args.get("value");
            AllSyncValue.Instance.time = 0;
            LevelDataSava.get(ctx.getSource().getLevel()).sava();
            return 1;
        });
        CommandHelper.addCommon(days, new String[]{"query"}, (ctx, args) -> {
            ctx.getSource().sendSuccess(() -> Component.literal("§e当前天数: §f" + AllSyncValue.Instance.day), false);
            return 1;
        });
        root.then(days);
    }

    /**
     * 2. 温度控制 - 仅限 OP
     */
    public static void addfunction() {
        LiteralArgumentBuilder<CommandSourceStack> temp = Commands.literal("temp").requires(s -> s.hasPermission(2));
        CommandHelper.addCommon(temp, new String[]{"set", "<float:temp>"}, (ctx, args) -> {
            float t = (float) args.get("temp");
            AllSyncValue.Instance.temperature = t;
            ConfigData.commonConfigData.temperature = t;
            ConfigData.build();
            return 1;
        });
        root.then(temp);
    }

    /**
     * 3. 屏幕与月相控制 - 仅限 OP
     */
    public static void addScreen() {
        LiteralArgumentBuilder<CommandSourceStack> moon = Commands.literal("moon").requires(s -> s.hasPermission(2));
        for (ModeEventType type : ModeEventType.values()) {
            CommandHelper.addCommon(moon, new String[]{"event", type.name().toLowerCase()}, (ctx, args) -> {
                AllSyncValue.Instance.mode = type;
                return 1;
            });
        }
        root.then(moon);

        // 界面打开指令（可以保留给普通玩家，如果只是打开自己的配置）
        root.then(Commands.literal("screen").then(Commands.literal("set").then(Commands.literal("config"))));
    }

    /**
     * 4. 阶段配置控制 - 仅限 OP
     */
    public static void addDayConfig() {
        LiteralArgumentBuilder<CommandSourceStack> enable = Commands.literal("enable").requires(s -> s.hasPermission(2));
        ConfigData.enableConfigData.Data.values().forEach(data -> {
            CommandHelper.addCommon(enable, new String[]{data.lang, "set"}, (ctx, args) -> {
                data.enable = !data.enable;
                ConfigData.build();
                return 1;
            });
        });
        root.then(enable);

        LiteralArgumentBuilder<CommandSourceStack> dayRange = Commands.literal("day").requires(s -> s.hasPermission(2));
        ConfigData.dayConfigData.data.values().forEach(data -> {
            CommandHelper.addCommon(dayRange, new String[]{data.lang, "set", "<int:start>", "<int:end>"}, (ctx, args) -> {
                data.day = (int) args.get("start");
                data.endDay = (int) args.get("end");
                ConfigData.build();
                return 1;
            });
        });
        root.then(dayRange);
    }

    /**
     * 10. 性能分析工具 (Performance Analyzer) - 仅限 OP
     */
    public static void addPerformanceControl() {
        LiteralArgumentBuilder<CommandSourceStack> perf = Commands.literal("perf").requires(s -> s.hasPermission(2));

        // 开始监控: /eod perf start
        CommandHelper.addCommon(perf, new String[]{"start"}, (ctx, args) -> {
            com.endofdays_re.utils.PerformanceAnalyzer.startMonitoring();
            ctx.getSource().sendSuccess(() -> Component.literal("§a[性能分析] §f已开始监控，使用 §e/perf stop§f 查看报告"), true);
            return 1;
        });

        // 停止监控并生成报告: /eod perf stop
        CommandHelper.addCommon(perf, new String[]{"stop"}, (ctx, args) -> {
            if (!com.endofdays_re.utils.PerformanceAnalyzer.isMonitoring()) {
                ctx.getSource().sendFailure(Component.literal("§c[错误] §f监控未启动，请先使用 §e/perf start"));
                return 0;
            }
            com.endofdays_re.utils.PerformanceAnalyzer.stopMonitoring();
            com.endofdays_re.utils.PerformanceAnalyzer.generateReport(ctx.getSource().getServer());
            return 1;
        });

        // 重置数据: /eod perf reset
        CommandHelper.addCommon(perf, new String[]{"reset"}, (ctx, args) -> {
            com.endofdays_re.utils.PerformanceAnalyzer.resetData();
            ctx.getSource().sendSuccess(() -> Component.literal("§6[性能分析] §f已重置所有统计数据"), true);
            return 1;
        });

        // 查看当前状态: /eod perf status
        CommandHelper.addCommon(perf, new String[]{"status"}, (ctx, args) -> {
            boolean monitoring = com.endofdays_re.utils.PerformanceAnalyzer.isMonitoring();
            String status = monitoring ? "§a运行中" : "§c已停止";
            ctx.getSource().sendSuccess(() -> Component.literal("§b[性能分析] §f当前状态: " + status), false);
            return 1;
        });

        root.then(perf);
    }

    private static int addEntryToBlacklist(CommandSourceStack source, String entry) {
        if (!ConfigData.commonConfigData.SMELT_BLACKLIST.contains(entry)) {
            ConfigData.commonConfigData.SMELT_BLACKLIST.add(entry);
            source.sendSuccess(() -> Component.literal("§a[熔炉] §f成功禁用物品/标签: §e" + entry), true);
            ConfigData.build(); // 同步到配置文件
            return 1;
        }
        source.sendFailure(Component.literal("§c[错误] §e" + entry + " §f已在黑名单中。"));
        return 0;
    }
}