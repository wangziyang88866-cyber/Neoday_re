package com.endofdays_re.command;

import com.endofdays_re.client.config.data.CommonBuild;
import com.endofdays_re.config.ConfigData;
import com.endofdays_re.utils.ModUtils;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class CommandConfigCommon {

    public static void addConfigCommon(LiteralArgumentBuilder<CommandSourceStack> root) {

        ConfigData.commonConfigData.Target.values().forEach(enableData -> {
            CommandHelper.addCommon(root,
                    new String[]{"target", "add", "<string:name>", "<string:mob>", "<string:target>"},
                    (ctx, args) -> {
                        String name = (String) args.get("name");
                        String id = (String) args.get("mob");
                        String target = (String) args.get("target");
                        ConfigData.commonConfigData.Target.put(name, new CommonBuild.TargetSelect(id, target));
                        ConfigData.build();
                        ctx.getSource().sendSuccess(() -> Component.literal("已添加目标 [" + id + "]"), false);
                        return 1;
                    });
        });
        ConfigData.commonConfigData.Target.keySet().forEach(enableData -> {
            CommandHelper.addCommon(root,
                    new String[]{"target", "remove", enableData},
                    (ctx, args) -> {
                        ConfigData.commonConfigData.Target.keySet().removeIf(s -> s.equals(enableData));
                        ctx.getSource().sendSuccess(() -> Component.literal("已移除目标 [" + enableData + "]"), false);
                        ConfigData.build();
                        return 1;
                    });
        });


        ConfigData.commonConfigData.FollowBlockBreak.values().forEach(enableData -> {
            CommandHelper.addCommon(root,
                    new String[]{"followbreak", "add", "<string:name>"},
                    (ctx, args) -> {
                        String name = (String) args.get("name");
                        if (ctx.getSource().getEntity() instanceof Player player) {
                            String id = ModUtils.getItemID(player.getMainHandItem().getItem());
                            ConfigData.commonConfigData.FollowBlockBreak.put(name, ModUtils.getItemID(player.getMainHandItem().getItem()));
                            ConfigData.build();
                            ctx.getSource().sendSuccess(() -> Component.literal("已添加方块 [" + id + "]"), false);
                        }
                        return 1;
                    });
        });
        ConfigData.commonConfigData.FollowBlockBreak.values().forEach(enableData -> {
            CommandHelper.addCommon(root,
                    new String[]{"followbreak", "remove", enableData},
                    (ctx, args) -> {
                        ConfigData.commonConfigData.FollowBlockBreak.values().removeIf(s -> s.equals(enableData));
                        ctx.getSource().sendSuccess(() -> Component.literal("已移除方块 [" + enableData + "]"), false);
                        ConfigData.build();
                        return 1;
                    });
        });


        ConfigData.commonConfigData.EquipChestMob.values().forEach(enableData -> {
            CommandHelper.addCommon(root,
                    new String[]{"equip", "add", "<string:name>"},
                    (ctx, args) -> {
                        String name = (String) args.get("name");
                        if (ctx.getSource().getEntity() instanceof Player player) {
                            String id = ModUtils.getItemID(player.getMainHandItem().getItem());
                            ConfigData.commonConfigData.EquipChestMob.put(name, ModUtils.getItemID(player.getMainHandItem().getItem()));
                            ConfigData.build();
                            ctx.getSource().sendSuccess(() -> Component.literal("已添加物品 [" + id + "]"), false);
                        }
                        return 1;
                    });
        });
        ConfigData.commonConfigData.EquipChestMob.values().forEach(enableData -> {
            CommandHelper.addCommon(root,
                    new String[]{"equip", "remove", enableData},
                    (ctx, args) -> {
                        ConfigData.commonConfigData.EquipChestMob.values().removeIf(s -> s.equals(enableData));
                        ctx.getSource().sendSuccess(() -> Component.literal("已移除物品 [" + enableData + "]"), false);
                        ConfigData.build();
                        return 1;
                    });
        });


        ConfigData.commonConfigData.commonFloat.values().forEach(enableData -> {
            CommandHelper.addCommon(root,
                    new String[]{"common", "float", enableData.lang, "<float:value>", "<float:min_value>", "<float:max_value>"},
                    (ctx, args) -> {
                        float value = (float) args.get("value");
                        float min_value = (float) args.get("min_value");
                        float max_value = (float) args.get("max_value");
                        ctx.getSource().sendSuccess(() -> Component.literal("已将配置项 [" + enableData.value + "-" + enableData.min_value + "-" + enableData.max_value + "] 更新为了:" +
                                "[" + value + "-" + min_value + "-" + max_value + "]"
                        ), false);
                        enableData.value = value;
                        enableData.min_value = min_value;
                        enableData.max_value = max_value;
                        ConfigData.build();
                        return 1;
                    });
        });
        ConfigData.commonConfigData.commonData.values().forEach(enableData -> {
            CommandHelper.addCommon(root,
                    new String[]{"common", "int", enableData.lang, "<int:value>", "<int:min_value>", "<int:max_value>"},
                    (ctx, args) -> {
                        int value = (int) args.get("value");
                        int min_value = (int) args.get("min_value");
                        int max_value = (int) args.get("max_value");
                        ctx.getSource().sendSuccess(() -> Component.literal("已将配置项 [" + enableData.value + "-" + enableData.min_value + "-" + enableData.max_value + "] 更新为了:" +
                                "[" + value + "-" + min_value + "-" + max_value + "]"
                        ), false);
                        enableData.value = value;
                        enableData.min_value = min_value;
                        enableData.max_value = max_value;
                        ConfigData.build();
                        return 1;
                    });
        });


    }

}
