package com.endofdays_re.utils.tools;


import com.endofdays_re.event.data.AllSyncValue;
import com.endofdays_re.utils.ModUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
    解析示例:
    <value:[点我1],value:[点我2],value:[点我3],colors:[#ff0000:#00ff00:#0000ff],clicks:[/say hi1:/say hi2:/say hi3],hovers:[提示1:提示2:提示3]>
    <value:[点我],click:[/say hello],hover:[悬停提示],color:[#ff00ff],bold:[true]>
    <value:[我爱世界],value:[爱你妈妈的个麻花],value:[${player}],colors:[#137852:#264635:#778899]>

    新增渐变索引支持:
    colors:[gradient(#FF0000:#00FF00, 0, 10)] - 从索引0到10的红色到绿色渐变
    colors:[gradient(#0000FF:#FFFFFF:#00FF00, 5, 15)] - 从索引5到15的蓝-白-绿三色渐变
 */
public class MessageTool implements Component {
    private final Map<String, List<Result>> result = new HashMap<>();
    private final Map<String, Object> variables = new HashMap<>();
    // 新增：渐变缓存
    private final Map<String, List<TextColor>> gradientCache = new HashMap<>();
    private List<TextColor> multiColors = new ArrayList<>();
    private List<Boolean> multiBolds = new ArrayList<>();
    private List<Boolean> multiItalics = new ArrayList<>();
    private List<Boolean> multiUnderlineds = new ArrayList<>();
    private List<Boolean> multiStrikethroughs = new ArrayList<>();
    private List<Boolean> multiObfuscateds = new ArrayList<>();
    private List<String> multiClicks = new ArrayList<>();
    private List<String> multiHovers = new ArrayList<>();

    public MessageTool() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            Minecraft minecraft = Minecraft.getInstance();
            variables.put("Mc", minecraft);
            variables.put("day", AllSyncValue.Instance.day);
            variables.put("time", AllSyncValue.Instance.time);
            if (minecraft.player != null) {
                variables.put("LocatePlayer", minecraft.player);
                variables.put("LocatePlayer.Name", minecraft.player.getDisplayName().getString());
                variables.put("LocatePlayer.X", minecraft.player.getX());
                variables.put("LocatePlayer.Y", minecraft.player.getY());
                variables.put("LocatePlayer.Z", minecraft.player.getZ());
                variables.put("LocatePlayer.Dimension", minecraft.player.level());
                variables.put("LocatePlayer.Dimension.Name", ModUtils.getDimensionKey(minecraft.player.level()).location().getPath());
            }
        }
    }

    // ===== 主解析方法 =====
    public Map<String, List<Result>> parse(String message) {
        message = parseVariables(message);
        result.clear();
        multiColors.clear();
        multiBolds.clear();
        multiItalics.clear();
        multiUnderlineds.clear();
        multiStrikethroughs.clear();
        multiObfuscateds.clear();
        multiClicks.clear();
        multiHovers.clear();
        gradientCache.clear(); // 清除渐变缓存

        Parser parser = new Parser(message);
        boolean matched = parser.parseKeyValuePairs();

        // 如果完全没匹配上，直接返回原始字符串作为默认 value
        if (!matched) {
            result.clear();
            result.computeIfAbsent("value", k -> new ArrayList<>())
                    .add(new Result("value", message, 0));
        }

        return result;
    }

    // ===== 变量解析 =====
    private String parseVariables(String message) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        boolean escaped = false;

        while (i < message.length()) {
            char c = message.charAt(i);

            // 处理转义
            if (escaped) {
                sb.append(c);
                escaped = false;
                i++;
                continue;
            }
            if (c == '\\') {
                escaped = true;
                i++;
                continue;
            }

            if ((c == '$' && i + 1 < message.length() && message.charAt(i + 1) == '{') ||
                    c == '%') {

                char endChar = (c == '$') ? '}' : '%';
                int start = i;
                i += (c == '$') ? 2 : 1;

                int varStart = i;
                while (i < message.length() && message.charAt(i) != endChar) {
                    i++;
                }

                if (i < message.length()) {
                    String variableName = message.substring(varStart, i).trim();
                    Object value = variables.get(variableName.toLowerCase());
                    String replacement = value != null ? value.toString() : "";
                    sb.append(replacement);
                    i++; // 跳过结束字符
                } else {
                    // 没有找到结束字符，保持原样
                    sb.append(message.substring(start));
                    break;
                }
            } else {
                sb.append(c);
                i++;
            }
        }

        return sb.toString();
    }

    public <T> MessageTool setVariable(String name, T value) {
        variables.put(name.toLowerCase(), value);
        return this;
    }

    private Object parseValue(String value) {
        value = value.trim();
        if (value.startsWith("#")) {
            try {
                return TextColor.parseColor(value);
            } catch (Exception ignored) {
            }
        }
        if (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false")) {
            return Boolean.parseBoolean(value);
        }
        if (isFloatOrDouble(value)) {
            if (value.contains("f") || value.contains("F")) return Float.parseFloat(value);
            else return Double.parseDouble(value);
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
        }
        return value;
    }

    private boolean isFloatOrDouble(String value) {
        return value.matches("^[+-]?\\d*\\.\\d+([eE][+-]?\\d+)?$");
    }

    // ===== 样式列表解析 - 新增渐变支持 =====
    private List<TextColor> parseColorList(String value) {
        List<TextColor> list = new ArrayList<>();
        Parser parser = new Parser(value);
        List<String> colors = parser.splitValues(value);
        for (String c : colors) {
            c = c.trim();
            try {
                TextColor color = resolveColorMap(c);
                if (color != null) {
                    list.add(color);
                }
            } catch (Exception ignored) {
            }
        }
        return list;
    }

    private TextColor resolveColorMap(String c) {
        c = c.trim();

        // 新增：渐变支持
        if (c.startsWith("gradient(") && c.endsWith(")")) {
            return resolveGradient(c);
        }

        if (c.startsWith("map(") && c.endsWith(")")) {
            String inside = c.substring(4, c.length() - 1);
            String[] parts = splitMapArgs(inside);
            if (parts.length == 3) {
                String condition = parts[0];
                String trueColor = parts[1];
                String falseColor = parts[2];
                ExpressionEvaluatorTool evaluator = new ExpressionEvaluatorTool();
                variables.forEach(evaluator::setVariable);
                boolean cond = evaluator.evaluate(condition) != 0;
                return resolveColorMap(cond ? trueColor : falseColor);
            }
        }
        return TextColor.parseColor(c).getOrThrow();
    }

    // 新增：渐变解析方法
    private TextColor resolveGradient(String gradientExpr) {
        // gradient(#FF0000:#00FF00, 0, 10)
        String inside = gradientExpr.substring(9, gradientExpr.length() - 1);
        String[] parts = splitMapArgs(inside);

        if (parts.length == 3) {
            String colorsPart = parts[0].trim(); // #FF0000:#00FF00
            String startIndexStr = parts[1].trim(); // 0
            String endIndexStr = parts[2].trim(); // 10

            try {
                int startIndex = Integer.parseInt(startIndexStr);
                int endIndex = Integer.parseInt(endIndexStr);

                // 解析颜色列表
                List<TextColor> gradientColors = new ArrayList<>();
                Parser parser = new Parser(colorsPart);
                List<String> colorStrs = parser.splitValues(colorsPart);
                for (String colorStr : colorStrs) {
                    gradientColors.add(TextColor.parseColor(colorStr.trim()).getOrThrow());
                }

                if (gradientColors.size() < 2) {
                    return gradientColors.get(0); // 只有一个颜色，直接返回
                }

                // 生成渐变并缓存
                List<TextColor> generatedGradient = generateGradient(gradientColors, startIndex, endIndex);
                gradientCache.put(gradientExpr, generatedGradient);

                // 返回第一个颜色作为占位符，实际颜色在Component方法中处理
                return gradientColors.get(0);

            } catch (NumberFormatException e) {
                // 索引解析失败，返回第一个颜色
                Parser parser = new Parser(colorsPart);
                List<String> colorStrs = parser.splitValues(colorsPart);
                return TextColor.parseColor(colorStrs.stream().findFirst().orElse("#FFFFFF")).getOrThrow();
            }
        }

        return TextColor.parseColor("#FFFFFF").getOrThrow(); // 默认白色
    }

    // 新增：生成渐变颜色
    private List<TextColor> generateGradient(List<TextColor> colors, int startIndex, int endIndex) {
        List<TextColor> gradient = new ArrayList<>();
        int segmentCount = colors.size() - 1;
        int totalSteps = endIndex - startIndex + 1;
        int stepsPerSegment = totalSteps / segmentCount;

        for (int segment = 0; segment < segmentCount; segment++) {
            TextColor startColor = colors.get(segment);
            TextColor endColor = colors.get(segment + 1);

            int steps = (segment == segmentCount - 1) ?
                    totalSteps - (stepsPerSegment * segment) : stepsPerSegment;

            for (int step = 0; step < steps; step++) {
                float ratio = (float) step / steps;
                TextColor interpolated = interpolateColor(startColor, endColor, ratio);
                gradient.add(interpolated);
            }
        }

        return gradient;
    }

    // 新增：颜色插值
    private TextColor interpolateColor(TextColor start, TextColor end, float ratio) {
        int startR = (start.getValue() >> 16) & 0xFF;
        int startG = (start.getValue() >> 8) & 0xFF;
        int startB = start.getValue() & 0xFF;

        int endR = (end.getValue() >> 16) & 0xFF;
        int endG = (end.getValue() >> 8) & 0xFF;
        int endB = end.getValue() & 0xFF;

        int r = (int) (startR + (endR - startR) * ratio);
        int g = (int) (startG + (endG - startG) * ratio);
        int b = (int) (startB + (endB - startB) * ratio);

        return TextColor.fromRgb((r << 16) | (g << 8) | b);
    }

    private List<Boolean> parseBooleanList(String value) {
        List<Boolean> list = new ArrayList<>();
        Parser parser = new Parser(value);
        List<String> values = parser.splitValues(value);
        for (String s : values) list.add(resolveBooleanMap(s));
        return list;
    }

    private boolean resolveBooleanMap(String s) {
        s = s.trim();
        if (s.startsWith("map(") && s.endsWith(")")) {
            String inside = s.substring(4, s.length() - 1);
            String[] parts = splitMapArgs(inside);
            if (parts.length == 3) {
                String condition = parts[0];
                String trueValue = parts[1];
                String falseValue = parts[2];
                ExpressionEvaluatorTool evaluator = new ExpressionEvaluatorTool();
                variables.forEach(evaluator::setVariable);
                boolean cond = evaluator.evaluate(condition) != 0;
                return Boolean.parseBoolean(cond ? trueValue : falseValue);
            }
        }
        return Boolean.parseBoolean(s);
    }

    private List<String> parseStringList(String value) {
        List<String> list = new ArrayList<>();
        Parser parser = new Parser(value);
        List<String> values = parser.splitValues(value);
        for (String s : values) list.add(resolveStringMap(s));
        return list;
    }

    private String resolveStringMap(String s) {
        s = s.trim();
        if (s.startsWith("map(") && s.endsWith(")")) {
            String inside = s.substring(4, s.length() - 1);
            String[] parts = splitMapArgs(inside);
            if (parts.length == 3) {
                String condition = parts[0];
                String trueValue = parts[1];
                String falseValue = parts[2];
                ExpressionEvaluatorTool evaluator = new ExpressionEvaluatorTool();
                variables.forEach(evaluator::setVariable);
                return evaluator.evaluate(condition) != 0 ? trueValue : falseValue;
            }
        }
        return s;
    }

    private String[] splitMapArgs(String inside) {
        List<String> args = new ArrayList<>();
        int bracketLevel = 0;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean escaped = false;
        StringBuilder sb = new StringBuilder();

        for (char ch : inside.toCharArray()) {
            // 处理转义
            if (escaped) {
                sb.append(ch);
                escaped = false;
                continue;
            }
            if (ch == '\\') {
                escaped = true;
                sb.append(ch);
                continue;
            }

            // 处理引号
            if (ch == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
            } else if (ch == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            }

            // 分割条件
            if (ch == ',' && bracketLevel == 0 && !inSingleQuote && !inDoubleQuote) {
                args.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                if (ch == '(' && !inSingleQuote && !inDoubleQuote) bracketLevel++;
                if (ch == ')' && !inSingleQuote && !inDoubleQuote) bracketLevel--;
                sb.append(ch);
            }
        }
        args.add(sb.toString().trim());
        return args.toArray(new String[0]);
    }

    // ===== 构建组件 - 新增渐变处理 =====
    public MutableComponent Component(Map<String, List<Result>> map) {
        MutableComponent newComponent = net.minecraft.network.chat.Component.literal("");
        int maxIndex = map.values().stream().flatMap(List::stream).mapToInt(r -> r.indexed).max().orElse(0);

        for (int i = 0; i <= maxIndex; i++) {
            String text = null;
            TextColor color = null;
            boolean bold = false, italic = false, underlined = false, strikethrough = false, obfuscated = false;
            String clickEvent = (i < multiClicks.size()) ? multiClicks.get(i) : null;
            String hoverEvent = (i < multiHovers.size()) ? multiHovers.get(i) : null;

            for (Map.Entry<String, List<Result>> entry : map.entrySet()) {
                for (Result res : entry.getValue()) {
                    if (res.indexed != i) continue;
                    switch (res.key.toLowerCase()) {
                        case "value" -> text = String.valueOf(res.value);
                        case "language" ->
                                text = net.minecraft.network.chat.Component.translatable((String) res.value).getString();
                        case "color" ->
                                color = res.value instanceof TextColor v ? v : TextColor.parseColor((String) res.value).getOrThrow();
                        case "bold" -> bold = (Boolean) res.value;
                        case "italic" -> italic = (Boolean) res.value;
                        case "underlined" -> underlined = (Boolean) res.value;
                        case "strikethrough" -> strikethrough = (Boolean) res.value;
                        case "obfuscated" -> obfuscated = (Boolean) res.value;
                        case "click" -> clickEvent = (String) res.value;
                        case "hover" -> hoverEvent = (String) res.value;
                    }
                }
            }

            // 新增：渐变颜色处理
            color = resolveGradientColor(i, color);

            if (color == null && i < multiColors.size()) color = resolveGradientColor(i, multiColors.get(i));
            if (!bold && i < multiBolds.size()) bold = multiBolds.get(i);
            if (!italic && i < multiItalics.size()) italic = multiItalics.get(i);
            if (!underlined && i < multiUnderlineds.size()) underlined = multiUnderlineds.get(i);
            if (!strikethrough && i < multiStrikethroughs.size()) strikethrough = multiStrikethroughs.get(i);
            if (!obfuscated && i < multiObfuscateds.size()) obfuscated = multiObfuscateds.get(i);

            if (text != null) {
                Style style = Style.EMPTY;
                if (color != null) style = style.withColor(color);
                if (bold) style = style.withBold(true);
                if (italic) style = style.withItalic(true);
                if (underlined) style = style.withUnderlined(true);
                if (strikethrough) style = style.withStrikethrough(true);
                if (obfuscated) style = style.withObfuscated(true);
                if (clickEvent != null && !clickEvent.isEmpty())
                    style = style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, clickEvent));
                if (hoverEvent != null && !hoverEvent.isEmpty())
                    style = style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, net.minecraft.network.chat.Component.literal(hoverEvent)));

                newComponent.append(net.minecraft.network.chat.Component.literal(text).setStyle(style));
            }
        }
        return newComponent;
    }

    // 新增：解析渐变颜色
    private TextColor resolveGradientColor(int index, TextColor defaultColor) {
        for (Map.Entry<String, List<TextColor>> entry : gradientCache.entrySet()) {
            String gradientExpr = entry.getKey();
            List<TextColor> gradient = entry.getValue();

            // 解析渐变范围
            String inside = gradientExpr.substring(9, gradientExpr.length() - 1);
            String[] parts = splitMapArgs(inside);
            if (parts.length == 3) {
                try {
                    int startIndex = Integer.parseInt(parts[1].trim());
                    int endIndex = Integer.parseInt(parts[2].trim());

                    if (index >= startIndex && index <= endIndex) {
                        int gradientIndex = index - startIndex;
                        if (gradientIndex < gradient.size()) {
                            return gradient.get(gradientIndex);
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return defaultColor;
    }

    public static class Result {
        public String key;
        public Object value;
        public int indexed;

        public Result(String key, Object value, int indexed) {
            this.key = key;
            this.value = value;
            this.indexed = indexed;
        }
    }

    public static class Variable<T> {
        public String key;
        public T value;

        public Variable(String key, T value) {
            this.key = key;
            this.value = value;
        }
    }

    // ===== 新的解析器类 =====
    private class Parser {
        private final String input;
        private int pos;
        private int currentIndex = -1;

        public Parser(String input) {
            this.input = input;
            this.pos = 0;
        }

        public boolean parseKeyValuePairs() {
            boolean matched = false;

            while (pos < input.length()) {
                // 跳过空白字符
                skipWhitespace();
                if (pos >= input.length()) break;

                // 查找 key:[value] 模式
                int keyStart = pos;
                while (pos < input.length() && Character.isLetterOrDigit(input.charAt(pos))) {
                    pos++;
                }

                if (pos == keyStart) {
                    pos++; // 跳过非字母数字字符
                    continue;
                }

                String key = input.substring(keyStart, pos);

                // 检查后面是否是 :[
                skipWhitespace();
                if (pos + 1 >= input.length() || input.charAt(pos) != ':' || input.charAt(pos + 1) != '[') {
                    continue; // 不符合 key:[ 格式
                }

                pos += 2; // 跳过 :[
                matched = true;

                // 解析括号内的值
                String value = parseBracketContent();
                if (value == null) {
                    continue; // 括号不匹配
                }

                // 处理不同类型的 key
                processKeyValue(key.toLowerCase(), value);
            }

            return matched;
        }

        private String parseBracketContent() {
            int start = pos;
            int depth = 1;
            boolean inSingleQuote = false;
            boolean inDoubleQuote = false;
            boolean escaped = false;

            while (pos < input.length() && depth > 0) {
                char c = input.charAt(pos);

                // 处理转义
                if (escaped) {
                    escaped = false;
                    pos++;
                    continue;
                }
                if (c == '\\') {
                    escaped = true;
                    pos++;
                    continue;
                }

                // 处理引号
                if (c == '\'' && !inDoubleQuote) {
                    inSingleQuote = !inSingleQuote;
                } else if (c == '"' && !inSingleQuote) {
                    inDoubleQuote = !inDoubleQuote;
                }
                // 只在非引号内计算括号
                else if (c == '[' && !inSingleQuote && !inDoubleQuote) {
                    depth++;
                } else if (c == ']' && !inSingleQuote && !inDoubleQuote) {
                    depth--;
                }

                pos++;
            }

            if (depth != 0) {
                return null; // 括号不匹配
            }

            // 返回括号内的内容，不包括最后的 ]
            return input.substring(start, pos - 1);
        }

        private void processKeyValue(String key, String value) {
            // 处理样式列表
            switch (key) {
                case "colors" -> multiColors = parseColorList(value);
                case "bolds" -> multiBolds = parseBooleanList(value);
                case "italics" -> multiItalics = parseBooleanList(value);
                case "underlineds" -> multiUnderlineds = parseBooleanList(value);
                case "strikethroughs" -> multiStrikethroughs = parseBooleanList(value);
                case "obfuscateds" -> multiObfuscateds = parseBooleanList(value);
                case "clicks" -> multiClicks = parseStringList(value);
                case "hovers" -> multiHovers = parseStringList(value);
            }

            Object parsedValue = parseValue(value);

            if (key.equals("value") || key.equals("values") || key.equals("language")) {
                if (key.equals("values")) {
                    // 使用新的分割方法处理嵌套
                    List<String> values = splitValues(value);
                    for (String v : values) {
                        currentIndex++;
                        result.computeIfAbsent("value", k -> new ArrayList<>())
                                .add(new Result("value", parseValue(v), currentIndex));
                    }
                    return;
                }
                currentIndex++;
            }
            if (currentIndex < 0) currentIndex = 0;

            result.computeIfAbsent(key, k -> new ArrayList<>())
                    .add(new Result(key, parsedValue, currentIndex));
        }

        private List<String> splitValues(String value) {
            List<String> result = new ArrayList<>();
            StringBuilder current = new StringBuilder();
            int bracketDepth = 0;
            boolean inSingleQuote = false;
            boolean inDoubleQuote = false;
            boolean escaped = false;

            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);

                // 处理转义字符
                if (escaped) {
                    current.append(c);
                    escaped = false;
                    continue;
                }

                if (c == '\\') {
                    escaped = true;
                    continue; // 不添加转义符本身
                }

                // 处理引号开始/结束
                if (c == '\'' && !inDoubleQuote) {
                    inSingleQuote = !inSingleQuote;
                    continue; // 不添加引号字符
                } else if (c == '"' && !inSingleQuote) {
                    inDoubleQuote = !inDoubleQuote;
                    continue; // 不添加引号字符
                }

                // 处理括号（只在不在引号内时计算）
                if (c == '[' && !inSingleQuote && !inDoubleQuote) {
                    bracketDepth++;
                } else if (c == ']' && !inSingleQuote && !inDoubleQuote) {
                    bracketDepth--;
                }

                // 分割条件：冒号 + 不在括号内 + 不在引号内
                if (c == ':' && bracketDepth == 0 && !inSingleQuote && !inDoubleQuote) {
                    if (!current.isEmpty()) {
                        result.add(current.toString().trim());
                        current.setLength(0);
                    }
                } else {
                    current.append(c);
                }
            }

            // 添加最后一段
            if (!current.isEmpty()) {
                result.add(current.toString().trim());
            }

            return result;
        }

        private void skipWhitespace() {
            while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
                pos++;
            }
        }
    }
}