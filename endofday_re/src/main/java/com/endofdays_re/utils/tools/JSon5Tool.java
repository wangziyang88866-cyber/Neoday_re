package com.endofdays_re.utils.tools;


import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.*;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.api.SyntaxError;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;


public class JSon5Tool {
    private static final Jankson jankson = Jankson.builder().build();


    public static JsonObject getRootElement(String path, String filename, Logger logger) {
        // 修复：不再强制拼接 "rules" 文件夹，直接根据传入的 path 和 filename 寻找
        File file = (path == null) ? new File(filename) : new File(path, filename);

        if (!file.exists()) {
            // 如果文件不存在，仅创建父目录，具体填充逻辑交给 CityManager
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            logger.warn("配置文件不存在: " + file.getAbsolutePath());
            return null;
        }

        logger.debug("正在读取规则文件: " + filename);
        try {
            return jankson.load(file);
        } catch (Exception e) {
            logger.error("解析文件失败 " + filename + ": " + e.getMessage());
            return null;
        }
    }

    public static void mergeMissing(JsonObject target, JsonObject defaults) {
        for (Map.Entry<String, JsonElement> entry : defaults.entrySet()) {
            String key = entry.getKey();
            JsonElement defaultValue = entry.getValue();

            if (!target.containsKey(key)) {
                // 如果目标不存在该键，直接补全
                target.put(key, defaultValue);
            } else if (defaultValue instanceof JsonObject defaultObj && target.get(key) instanceof JsonObject targetObj) {
                // 如果双方都是对象，递归合并子项
                mergeMissing(targetObj, defaultObj);
            }
        }
    }

    private static void makeEmptyRuleFile(File file, Logger logger) {
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {

            JsonObject emptyObject = new JsonObject();
            String json = emptyObject.toJson(true, true);
            writer.print(json);
            logger.info("Created empty rule file: " + file.getName());
        } catch (Exception e) {
            logger.error("Error writing " + file.getName() + ": " + e.getMessage());
        }
    }

    public static Optional<JsonElement> getElement(JsonObject element, String name) {
        JsonElement el = element.get(name);
        return el != null ? Optional.of(el) : Optional.empty();
    }

    public static @NotNull Float parseFloat(JsonObject jsonObject, String name) {
        JsonElement element = jsonObject.get(name);
        if (element instanceof JsonPrimitive) {
            Object value = ((JsonPrimitive) element).getValue();
            if (value instanceof Number) {
                return ((Number) value).floatValue();
            }
        }
        return 0.0F;
    }

    @Nullable
    public static Integer parseInt(JsonObject jsonObject, String name) {
        JsonElement element = jsonObject.get(name);
        if (element instanceof JsonPrimitive) {
            Object value = ((JsonPrimitive) element).getValue();
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        }
        return null;
    }

    @Nullable
    public static Boolean parseBool(JsonObject jsonObject, String name) {
        JsonElement element = jsonObject.get(name);
        if (element instanceof JsonPrimitive) {
            Object value = ((JsonPrimitive) element).getValue();
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
        }
        return null;
    }

    public static Stream<Pair<String, String>> asPairs(JsonObject jsonObject) {
        Stream.Builder<Pair<String, String>> builder = Stream.builder();

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            JsonElement value = entry.getValue();
            if (value instanceof JsonPrimitive) {
                Object primitiveValue = ((JsonPrimitive) value).getValue();
                builder.add(Pair.of(entry.getKey(), String.valueOf(primitiveValue)));
            }
        }
        return builder.build();
    }

    public static Stream<JsonElement> asArrayOrSingle(JsonElement element) {
        if (element instanceof JsonArray array) {
            Stream.Builder<JsonElement> builder = Stream.builder();
            for (int i = 0; i < array.size(); i++) {
                builder.add(array.get(i));
            }
            return builder.build();
        } else {
            return Stream.of(element);
        }
    }

    public static void addPairs(JsonObject parent, String name, Map<String, String> pairs) {
        if (pairs == null || pairs.isEmpty()) return;

        JsonObject object = new JsonObject();
        for (Map.Entry<String, String> entry : pairs.entrySet()) {
            object.put(entry.getKey(), new JsonPrimitive(entry.getValue()));
        }
        parent.put(name, object);
    }

    public static void addArrayOrSingle(JsonObject parent, String name, Collection<String> strings) {
        if (strings == null || strings.isEmpty()) return;

        if (strings.size() == 1) {
            parent.put(name, new JsonPrimitive(strings.iterator().next()));
        } else {
            JsonArray array = new JsonArray();
            for (String value : strings) {
                array.add(new JsonPrimitive(value));
            }
            parent.put(name, array);
        }
    }

    public static void addIntArrayOrSingle(JsonObject parent, String name, Collection<Integer> integers) {
        if (integers == null || integers.isEmpty()) return;

        if (integers.size() == 1) {
            parent.put(name, new JsonPrimitive(integers.iterator().next()));
        } else {
            JsonArray array = new JsonArray();
            for (Integer value : integers) {
                array.add(new JsonPrimitive(value));
            }
            parent.put(name, array);
        }
    }

    public static String toJsonString(JsonElement element) {
        return element.toJson(true, true);
    }

    public static JsonObject fromJsonString(String jsonString) throws SyntaxError {
        return jankson.load(jsonString);
    }

    public static void saveToFile(JsonElement element, File file, Logger logger) {
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(file), StandardCharsets.UTF_8))) {

            String jsonString = toJsonString(element);
            writer.print(jsonString);
        } catch (Exception e) {
            logger.error("Error writing to " + file.getName() + ": " + e.getMessage());
        }
    }

    // 新增一些实用的辅助方法
    public static String getString(JsonObject jsonObject, String name, String defaultValue) {
        JsonElement element = jsonObject.get(name);
        if (element instanceof JsonPrimitive) {
            Object value = ((JsonPrimitive) element).getValue();
            if (value instanceof String) {
                return (String) value;
            }
        }
        return defaultValue;
    }

    public static int getInt(JsonObject jsonObject, String name, int defaultValue) {
        JsonElement element = jsonObject.get(name);
        if (element instanceof JsonPrimitive) {
            Object value = ((JsonPrimitive) element).getValue();
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
        }
        return defaultValue;
    }

    public static boolean getBoolean(JsonObject jsonObject, String name, boolean defaultValue) {
        JsonElement element = jsonObject.get(name);
        if (element instanceof JsonPrimitive) {
            Object value = ((JsonPrimitive) element).getValue();
            if (value instanceof Boolean) {
                return (Boolean) value;
            }
        }
        return defaultValue;
    }

    public static JsonArray getArray(JsonObject jsonObject, String name) {
        JsonElement element = jsonObject.get(name);
        if (element instanceof JsonArray) {
            return (JsonArray) element;
        }
        return new JsonArray();
    }
}