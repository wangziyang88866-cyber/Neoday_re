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
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum AttributeHelper {
    ;

    private static final Logger LOGGER = LoggerFactory.getLogger(AttributeHelper.class);

    private static final String APPLIED_FLAG =
            ModUtils.KeyWraps("attr_build_applied");

    private static final ExpressionEvaluatorTool FUNCTION_CHECKER =
            new ExpressionEvaluatorTool();

    private static final Set<String> KNOWN_VARIABLES =
            Set.of("day", "time", "BASE");

    private static final Pattern IDENTIFIER_PATTERN =
            Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

    /**
     * 兼容旧版配置中的属性专用基础值变量。
     * 它们的含义都是“当前正在计算的这个属性的原始基础值”。
     */
    private static final Pattern LEGACY_BASE_VARIABLE_PATTERN = Pattern.compile(
            "\\bBASE_(?:HEALTH|BUILD_SPEED|BREAKER_SPEED|ARMOR|MOVE_SPEED|ATTACK_DAMAGE)\\b"
    );

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

        ResourceLocation typeId =
                BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());

        if (typeId == null) {
            LOGGER.warn("[Attribute] 无法取得实体类型: {}", entity.getClass().getName());
            return;
        }

        String entityId = typeId.toString();
        long day = AllSyncValue.Instance.day;
        long time = entity.level().getGameTime();

        for (var entry : config.attributes.entrySet()) {
            String key = entry.getKey();
            AttributeBuild.AttributeData data = entry.getValue();

            if (data == null || data.id == null || data.id.isBlank()) {
                continue;
            }

            if (!matchesEntity(data.EntityID, entityId)) {
                continue;
            }

            if (day < data.start || (data.end >= 0 && day > data.end)) {
                continue;
            }

            applyOne(entity, entityId, key, data, day, time);
        }

        entity.getPersistentData().putBoolean(APPLIED_FLAG, true);
    }

    private static void applyOne(
            LivingEntity entity,
            String entityId,
            String key,
            AttributeBuild.AttributeData data,
            long day,
            long time
    ) {
        /*
         * 1.21.1 NeoForge：这里是 Holder.Reference<Attribute>，
         * 必须保留 var，不能写成 Attribute。
         */
        var attribute = ModUtils.getAttribute(data.id);

        if (attribute == null) {
            LOGGER.warn(
                    "[Attribute] 未知属性 ID，跳过。key={}, entity={}, attribute={}",
                    key, entityId, data.id
            );
            return;
        }

        AttributeInstance instance = entity.getAttribute(attribute);

        if (instance == null) {
            LOGGER.debug(
                    "[Attribute] 实体不支持该属性，跳过。key={}, entity={}, attribute={}",
                    key, entityId, data.id
            );
            return;
        }

        String originalFormula = data.value;
        String formula = normalizeLegacyBaseVariables(originalFormula);

        if (!formula.equals(originalFormula)) {
            LOGGER.debug(
                    "[Attribute] 已兼容转换旧公式变量。key={}, oldFormula={}, formula={}",
                    key, originalFormula, formula
            );
        }

        String badToken = findUnknownVariable(formula);
        if (badToken != null) {
            LOGGER.warn(
                    "[Attribute] 公式有未知变量，跳过。key={}, entity={}, variable={}, formula={}",
                    key, entityId, badToken, originalFormula
            );
            return;
        }

        double baseValue = instance.getBaseValue();

        /*
         * 每次独立创建，避免共享 evaluator 留下上一只实体的变量。
         */
        ExpressionEvaluatorTool evaluator = new ExpressionEvaluatorTool();
        evaluator.setVariable("day", (double) day);
        evaluator.setVariable("time", (double) time);
        evaluator.setVariable("BASE", baseValue);

        final double evaluatedValue;
        try {
            evaluatedValue = evaluator.evaluate(formula);
        } catch (Exception e) {
            LOGGER.warn(
                    "[Attribute] 表达式计算失败。key={}, entity={}, attribute={}, BASE={}, day={}, formula={}, error={}",
                    key, entityId, data.id, baseValue, day, formula, e.getMessage()
            );
            return;
        }

        if (!Double.isFinite(evaluatedValue)) {
            LOGGER.error(
                    "[Attribute] 表达式结果非法，拒绝写入。key={}, entity={}, attribute={}, BASE={}, day={}, formula={}, result={}",
                    key, entityId, data.id, baseValue, day, formula, evaluatedValue
            );
            return;
        }

        double result = evaluatedValue;

        if (data.max > 0) {
            result = Math.min(result, data.max);
        }

        result = Math.max(result, 0.0D);

        boolean isMaxHealth =
                attribute.value() == Attributes.MAX_HEALTH.value();

        if (isMaxHealth && result <= 0.0D) {
            LOGGER.error(
                    "[Attribute] 最大生命结果 <= 0，拒绝写入以防实体死亡。key={}, entity={}, BASE={}, day={}, formula={}, result={}",
                    key, entityId, baseValue, day, formula, result
            );
            return;
        }

        LOGGER.debug(
                "[Attribute] 应用成功。key={}, entity={}, attribute={}, BASE={}, day={}, formula={}, result={}",
                key, entityId, data.id, baseValue, day, formula, result
        );

        instance.setBaseValue(result);
    }

    private static String normalizeLegacyBaseVariables(String formula) {
        if (formula == null || formula.isBlank()) {
            return formula;
        }

        return LEGACY_BASE_VARIABLE_PATTERN
                .matcher(formula)
                .replaceAll("BASE");
    }

    private static String findUnknownVariable(String formula) {
        if (formula == null || formula.isBlank()) {
            return null;
        }

        Matcher matcher = IDENTIFIER_PATTERN.matcher(formula);

        while (matcher.find()) {
            String token = matcher.group();

            if (KNOWN_VARIABLES.contains(token)) {
                continue;
            }

            if (FUNCTION_CHECKER.containsKey(token)) {
                continue;
            }

            return token;
        }

        return null;
    }

    private static boolean matchesEntity(String configured, String actual) {
        if (configured == null || configured.isBlank()) {
            return false;
        }

        String trimmed = configured.trim();

        if ("*".equals(trimmed)) {
            return true;
        }

        for (String part : trimmed.split(",")) {
            if (part.trim().equalsIgnoreCase(actual)) {
                return true;
            }
        }

        return false;
    }
}