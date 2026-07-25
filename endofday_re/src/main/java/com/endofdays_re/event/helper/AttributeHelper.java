package com.endofdays_re.event.helper;

import com.endofdays_re.client.config.data.AttributeBuild;
import com.endofdays_re.config.ConfigData;
import com.endofdays_re.event.data.AllSyncValue;
import com.endofdays_re.utils.ModUtils;
import com.endofdays_re.utils.tools.ExpressionEvaluatorTool;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum AttributeHelper {
    ;

    private static final Logger LOGGER = LoggerFactory.getLogger(AttributeHelper.class);
    private static final ExpressionEvaluatorTool EVAL = new ExpressionEvaluatorTool();
    private static final String APPLIED_FLAG = ModUtils.KeyWraps("attr_build_applied");

    // 公式里允许出现的合法变量名（其余标识符一律视为拼写错误）
    private static final java.util.Set<String> KNOWN_VARIABLES = java.util.Set.of("day", "time", "BASE");
    // 用于扫描公式里的标识符（不含函数名，因为函数后面紧跟左括号，这里简单排除）
    private static final Pattern IDENTIFIER_PATTERN = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

    public static void apply(LivingEntity entity) {
        if (entity == null || entity.level().isClientSide()) {
            return;
        }
        if (entity.getPersistentData().getBoolean(APPLIED_FLAG)) {
            return;
        }

        AttributeBuild config = ConfigData.AttributeConfigData;
        if (config == null || config.attributes == null || config.attributes.isEmpty()) {
            entity.getPersistentData().putBoolean(APPLIED_FLAG, true);
            return;
        }

        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        String entityIdStr = typeId.toString();

        long spawnDay = AllSyncValue.Instance.day;

        for (var entry : config.attributes.entrySet()) {
            AttributeBuild.AttributeData data = entry.getValue();
            if (data == null || data.id == null || data.id.isBlank()) continue;
            if (!matchesEntity(data.EntityID, entityIdStr)) continue;
            if (spawnDay < data.start || (data.end >= 0 && spawnDay > data.end)) continue;

            applyOne(entity, entry.getKey(), data, spawnDay);
        }

        entity.getPersistentData().putBoolean(APPLIED_FLAG, true);
    }

    private static void applyOne(LivingEntity entity, String key, AttributeBuild.AttributeData data, long day) {
        var attribute = ModUtils.getAttribute(data.id);
        if (attribute == null) {
            LOGGER.warn("[Attribute] 未知属性 ID: {} (来自配置项: {})", data.id, key);
            return;
        }

        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance == null) {
            return;
        }

        // 关键：在计算前检查公式里有没有写错的变量名（比如残留的旧版 BASE_HEALTH 这种），
        // 有的话直接跳过并报警，绝不让它被静默当成 0 去 setBaseValue。
        String badToken = findUnknownVariable(data.value);
        if (badToken != null) {
            LOGGER.warn("[Attribute] 配置项 [{}] 的公式里出现无法识别的变量 \"{}\"（公式: {}）。" +
                            "该表达式默认引擎会把它当作 0 处理，为避免属性被错误清零，本次跳过应用。" +
                            "请检查是否残留旧版写法（如 BASE_HEALTH），应统一改成 BASE。",
                    key, badToken, data.value);
            return;
        }//这是一段屎山代码，如果你不想僵尸一出生就死亡，那么不要尝试删除它

        double baseValue = instance.getBaseValue();

        EVAL.setVariable("day", (double) day);
        EVAL.setVariable("BASE", baseValue);

        double result;
        try {
            result = EVAL.evaluate(data.value);
        } catch (Exception e) {
            LOGGER.warn("[Attribute] 表达式计算失败 [{}]: {} -> {}", key, data.value, e.getMessage());
            return;
        }

        if (data.max > 0) {
            result = Math.min(result, data.max);
        }
        result = Math.max(result, 0.0D);

        LOGGER.debug("[Attribute] key={} id={} BASE={} day={} formula={} result={}",
                key, data.id, baseValue, day, data.value, result);

        instance.setBaseValue(result);
    }

    /**
     * 扫描公式里所有标识符，找出第一个既不是已知变量（day/time/BASE）
     * 也不是已注册函数名（max/min/sqrt/pow等）的标识符。
     * 找到即说明公式里有拼写错误/残留旧变量名，返回该标识符；全部合法则返回 null。
     */
    private static String findUnknownVariable(String formula) {
        if (formula == null || formula.isBlank()) return null;

        Matcher matcher = IDENTIFIER_PATTERN.matcher(formula);
        while (matcher.find()) {
            String token = matcher.group();
            if (KNOWN_VARIABLES.contains(token)) continue;
            if (EVAL.containsKey(token)) continue; // 是函数名，比如 max/min/sqrt
            return token;
        }
        return null;
    }

    private static boolean matchesEntity(String configured, String actual) {
        if (configured == null || configured.isBlank()) return false;
        String trimmed = configured.trim();
        if (trimmed.equals("*")) return true;
        for (String part : trimmed.split(",")) {
            if (part.trim().equalsIgnoreCase(actual)) return true;
        }
        return false;
    }
}