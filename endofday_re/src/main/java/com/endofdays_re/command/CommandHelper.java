package com.endofdays_re.command;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class CommandHelper {

    /**
     * 注册通用命令
     *
     * @param root     命令根
     * @param path     语法链，例如 {"day", "<long:value>"}
     * @param callback 回调
     */
    public static LiteralArgumentBuilder<CommandSourceStack> addCommon(
            LiteralArgumentBuilder<CommandSourceStack> root,
            String[] path,
            BiFunction<CommandContext<CommandSourceStack>, Map<String, Object>, Integer> callback
    ) {
        ArgumentBuilder<CommandSourceStack, ?> current = null;

        for (int i = path.length - 1; i >= 0; i--) {
            String p = path[i];
            boolean isLast = (i == path.length - 1);

            if (p.startsWith("<") && p.endsWith(">")) {
                String inner = p.substring(1, p.length() - 1);
                String typeName;
                String argName;

                if (inner.contains(":")) {
                    String[] sp = inner.split(":", 2);
                    typeName = sp[0].toLowerCase();
                    argName = sp[1];
                } else {
                    typeName = "string";
                    argName = inner;
                }

                ArgumentType<?> argType = parseArgumentType(typeName);

                @SuppressWarnings({"rawtypes", "unchecked"})
                RequiredArgumentBuilder<CommandSourceStack, ?> argBuilder =
                        Commands.argument(argName, argType);

                if (isLast) {
                    argBuilder.executes(ctx -> {
                        Map<String, Object> values = new HashMap<>();
                        for (String arg : extractArgs(path)) {
                            String type = getTypeName(arg, path);
                            // 针对特殊类型进行特殊获取逻辑
                            if ("server_player".equals(type) || "player".equals(type)) {
                                values.put(arg, EntityArgument.getPlayer(ctx, arg));
                            } else {
                                values.put(arg, ctx.getArgument(arg, getArgClass(arg, path)));
                            }
                        }
                        return callback.apply(ctx, values);
                    });
                }

                if (current != null) {
                    argBuilder.then(current);
                }
                current = argBuilder;

            } else {
                LiteralArgumentBuilder<CommandSourceStack> literal = Commands.literal(p);
                if (isLast) {
                    literal.executes(ctx -> callback.apply(ctx, new HashMap<>()));
                }
                if (current != null) {
                    literal.then(current);
                }
                current = literal;
            }
        }

        if (current != null) {
            root.then(current);
        }

        return root;
    }

    /**
     * 解析参数类型，新增 server_player 支持
     */
    private static ArgumentType<?> parseArgumentType(String typeName) {
        return switch (typeName) {
            case "long" -> LongArgumentType.longArg();
            case "int", "integer" -> IntegerArgumentType.integer();
            case "float" -> FloatArgumentType.floatArg();
            case "double" -> DoubleArgumentType.doubleArg();
            case "server_player", "player" -> EntityArgument.player(); // 获取单体玩家
            case "word" -> StringArgumentType.word();
            case "string" -> StringArgumentType.string();
            case "greedy" -> StringArgumentType.greedyString();
            default -> StringArgumentType.word();
        };
    }

    /**
     * 获取参数对应的 Class 映射
     */
    private static Class<?> getArgClass(String argName, String[] path) {
        for (String p : path) {
            if (p.startsWith("<") && p.endsWith(">")) {
                String inner = p.substring(1, p.length() - 1);
                if (inner.contains(":")) {
                    String[] sp = inner.split(":", 2);
                    if (sp[1].equals(argName)) {
                        return switch (sp[0].toLowerCase()) {
                            case "long" -> Long.class;
                            case "int", "integer" -> Integer.class;
                            case "float" -> Float.class;
                            case "double" -> Double.class;
                            case "server_player", "player" -> ServerPlayer.class;
                            default -> String.class;
                        };
                    }
                } else if (inner.equals(argName)) {
                    return String.class;
                }
            }
        }
        return String.class;
    }

    /**
     * 获取参数定义的原始类型名字符串
     */
    private static String getTypeName(String argName, String[] path) {
        for (String p : path) {
            if (p.startsWith("<") && p.endsWith(">")) {
                String inner = p.substring(1, p.length() - 1);
                if (inner.contains(":")) {
                    String[] sp = inner.split(":", 2);
                    if (sp[1].equals(argName)) return sp[0].toLowerCase();
                }
            }
        }
        return "string";
    }

    private static String[] extractArgs(String[] path) {
        return java.util.Arrays.stream(path)
                .filter(p -> p.startsWith("<") && p.endsWith(">"))
                .map(p -> {
                    String inner = p.substring(1, p.length() - 1);
                    if (inner.contains(":")) {
                        return inner.split(":", 2)[1];
                    }
                    return inner;
                })
                .toArray(String[]::new);
    }
}