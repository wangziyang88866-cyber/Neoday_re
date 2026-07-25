package com.endofdays_re.client.config.factory;

import com.endofdays_re.client.config.data.InvasionBuild;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.MultiElementListEntry;
import me.shedaniel.clothconfig2.gui.entries.NestedListListEntry;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class InvasionConfigFactory {

    public static void build(ConfigBuilder builder, InvasionBuild config) {
        ConfigEntryBuilder eb = builder.entryBuilder();
        ConfigCategory category = builder.getOrCreateCategory(Component.translatable("config.endofdays_re.category.invasion"));

        // 0. 全局冷却配置
        category.addEntry(eb.startLongField(Component.translatable("config.endofdays_re.invasion.max_time"), config.max_time)
                .setSaveConsumer(v -> config.max_time = v).build());

        List<InvasionWrapper> iWrappers = new ArrayList<>();
        if (config.invasionSettings != null) {
            config.invasionSettings.forEach((k, v) -> iWrappers.add(new InvasionWrapper(k, v)));
        }

        // --- 一级嵌套：入侵事件列表 (修复 Key 同步问题) ---
        category.addEntry(new NestedListListEntry<>(
                Component.translatable("config.endofdays_re.invasion.list"), iWrappers, false, Optional::empty,
                newList -> {
                    config.invasionSettings.clear();
                    if (newList != null) {
                        for (InvasionWrapper w : newList) {
                            // 修复：确保标识符不为空且内外同步
                            String finalKey = (w.key == null || w.key.trim().isEmpty())
                                    ? "invasion_" + System.currentTimeMillis()
                                    : w.key;
                            w.data.key = finalKey; // 同步到对象内部
                            config.invasionSettings.put(finalKey, w.data); // 存入 Map
                        }
                    }
                },
                () -> List.of(new InvasionWrapper("new_invasion_" + System.currentTimeMillis(), new InvasionBuild.InvasionSettings())),
                eb.getResetButtonKey(), true, true,
                (invOrigin, n1) -> {
                    final AtomicReference<InvasionWrapper> invRef = new AtomicReference<>((invOrigin != null) ? invOrigin : new InvasionWrapper("unknown", new InvasionBuild.InvasionSettings()));
                    InvasionWrapper inv = invRef.get();
                    List<AbstractConfigListEntry<?>> invEntries = new ArrayList<>();

                    // 1. 基础识别与权重
                    invEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.common.key"), inv.key)
                            .setSaveConsumer(v -> invRef.get().key = v).build());
                    invEntries.add(eb.startIntField(Component.translatable("config.endofdays_re.invasion.weight"), inv.data.weight)
                            .setSaveConsumer(v -> invRef.get().data.weight = v).build());
                    invEntries.add(eb.startFloatField(Component.translatable("config.endofdays_re.common.probability"), inv.data.probability)
                            .setSaveConsumer(v -> invRef.get().data.probability = v).build());

                    // 2. 环境与维度限制
                    invEntries.add(eb.startIntSlider(Component.translatable("config.endofdays_re.invasion.max_entity"), inv.data.maxEntity, 0, 9999)
                            .setSaveConsumer(v -> invRef.get().data.maxEntity = v).build());
                    invEntries.add(eb.startIntField(Component.translatable("config.endofdays_re.invasion.pos_max"), inv.data.pos_max)
                            .setSaveConsumer(v -> invRef.get().data.pos_max = v).build());
                    invEntries.add(eb.startStrList(Component.translatable("config.endofdays_re.invasion.dim"), Arrays.asList(inv.data.dim))
                            .setSaveConsumer(v -> invRef.get().data.dim = v.toArray(new String[0]))
                            .build());

                    // 3. 范围配置补全 (对照 JSON 结构)
                    invEntries.add(eb.startIntField(Component.translatable("config.endofdays_re.invasion.pos_range.min"), inv.data.pos_range.min).setSaveConsumer(v -> inv.data.pos_range.min = v).build());
                    invEntries.add(eb.startIntField(Component.translatable("config.endofdays_re.invasion.pos_range.max"), inv.data.pos_range.max).setSaveConsumer(v -> inv.data.pos_range.max = v).build());
                    invEntries.add(eb.startIntField(Component.translatable("config.endofdays_re.invasion.time_range.min"), inv.data.time_range.min).setSaveConsumer(v -> inv.data.time_range.min = v).build());
                    invEntries.add(eb.startIntField(Component.translatable("config.endofdays_re.invasion.time_range.max"), inv.data.time_range.max).setSaveConsumer(v -> inv.data.time_range.max = v).build());
                    invEntries.add(eb.startIntField(Component.translatable("config.endofdays_re.common.min_waves"), inv.data.InvasionCount.min).setSaveConsumer(v -> inv.data.InvasionCount.min = v).build());
                    invEntries.add(eb.startIntField(Component.translatable("config.endofdays_re.common.max_waves"), inv.data.InvasionCount.max).setSaveConsumer(v -> inv.data.InvasionCount.max = v).build());

                    // 4. 生效日期
                    invEntries.add(eb.startIntField(Component.translatable("config.endofdays_re.day.start"), inv.data.day_).setSaveConsumer(v -> inv.data.day_ = v).build());
                    invEntries.add(eb.startIntField(Component.translatable("config.endofdays_re.day.end"), inv.data.end_day).setSaveConsumer(v -> inv.data.end_day = v).build());

                    // --- 二级嵌套：实体 (EntitySetting) ---
                    List<EntityWrapper> eWrappers = Arrays.stream(inv.data.entitySetting != null ? inv.data.entitySetting : new InvasionBuild.EntitySetting[0]).map(EntityWrapper::new).collect(Collectors.toList());
                    invEntries.add(new NestedListListEntry<>(
                            Component.translatable("config.endofdays_re.invasion.entities"), eWrappers, false, Optional::empty,
                            nEs -> invRef.get().data.entitySetting = nEs.stream().map(w -> w.data).toArray(InvasionBuild.EntitySetting[]::new),
                            () -> List.of(new EntityWrapper(new InvasionBuild.EntitySetting())),
                            eb.getResetButtonKey(), true, true,
                            (entOrigin, n2) -> {
                                final AtomicReference<EntityWrapper> entRef = new AtomicReference<>((entOrigin != null) ? entOrigin : new EntityWrapper(new InvasionBuild.EntitySetting()));
                                List<AbstractConfigListEntry<?>> entEntries = new ArrayList<>();
                                InvasionBuild.EntitySetting ent = entRef.get().data;

                                entEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.entity.id"), ent.id).setSaveConsumer(v -> ent.id = v).build());
                                entEntries.add(eb.startStrField(Component.translatable("config.endofdays_re.entity.tag"), ent.tag).setSaveConsumer(v -> ent.tag = v).build());
                                entEntries.add(eb.startFloatField(Component.translatable("config.endofdays_re.common.probability"), ent.probability).setSaveConsumer(v -> ent.probability = v).build());
                                // 补全 Entity max_count
                                entEntries.add(eb.startIntField(Component.translatable("config.endofdays_re.entity.count.min"), ent.max_count.min).setSaveConsumer(v -> ent.max_count.min = v).build());
                                entEntries.add(eb.startIntField(Component.translatable("config.endofdays_re.entity.count.max"), ent.max_count.max).setSaveConsumer(v -> ent.max_count.max = v).build());

                                // --- 三级嵌套：药水效果 (补全 Range) ---
                                List<EffectWrapper> effWs = Arrays.stream(ent.effectSetting != null ? ent.effectSetting : new InvasionBuild.EffectSetting[0]).map(EffectWrapper::new).collect(Collectors.toList());
                                entEntries.add(new NestedListListEntry<>(Component.translatable("config.endofdays_re.entity.effects"), effWs, false, Optional::empty,
                                        l -> entRef.get().data.effectSetting = l.stream().map(w -> w.data).toArray(InvasionBuild.EffectSetting[]::new),
                                        () -> List.of(new EffectWrapper(new InvasionBuild.EffectSetting())), eb.getResetButtonKey(), true, true,
                                        (effO, n3) -> {
                                            final AtomicReference<EffectWrapper> er = new AtomicReference<>((effO != null) ? effO : new EffectWrapper(new InvasionBuild.EffectSetting()));
                                            List<AbstractConfigListEntry<?>> f = new ArrayList<>();
                                            f.add(eb.startStrField(Component.translatable("config.endofdays_re.effect.id"), er.get().data.ef_id).setSaveConsumer(v -> er.get().data.ef_id = v).build());
                                            f.add(eb.startIntField(Component.translatable("config.endofdays_re.common.min_lv"), er.get().data.level.min).setSaveConsumer(v -> er.get().data.level.min = v).build());
                                            f.add(eb.startIntField(Component.translatable("config.endofdays_re.common.max_lv"), er.get().data.level.max).setSaveConsumer(v -> er.get().data.level.max = v).build());
                                            f.add(eb.startIntField(Component.translatable("config.endofdays_re.effect.time.min"), er.get().data.leftTime.min).setSaveConsumer(v -> er.get().data.leftTime.min = v).build());
                                            f.add(eb.startIntField(Component.translatable("config.endofdays_re.effect.time.max"), er.get().data.leftTime.max).setSaveConsumer(v -> er.get().data.leftTime.max = v).build());
                                            f.add(eb.startBooleanToggle(Component.translatable("config.endofdays_re.effect.show_particle"), er.get().data.show_ef).setSaveConsumer(v -> er.get().data.show_ef = v).build());
                                            f.add(eb.startFloatField(Component.translatable("config.endofdays_re.common.probability"), er.get().data.probability).setSaveConsumer(v -> er.get().data.probability = v).build());
                                            return new MultiElementListEntry<>(Component.translatable("config.endofdays_re.effect.header"), er.get(), f, true);
                                        }));

                                // --- 三级嵌套：属性 ---
                                List<AttributeWrapper> attWs = Arrays.stream(ent.attributeSetting != null ? ent.attributeSetting : new InvasionBuild.AttributeSetting[0]).map(AttributeWrapper::new).collect(Collectors.toList());
                                entEntries.add(new NestedListListEntry<>(Component.translatable("config.endofdays_re.entity.attributes"), attWs, false, Optional::empty,
                                        l -> entRef.get().data.attributeSetting = l.stream().map(w -> w.data).toArray(InvasionBuild.AttributeSetting[]::new),
                                        () -> List.of(new AttributeWrapper(new InvasionBuild.AttributeSetting())), eb.getResetButtonKey(), true, true,
                                        (attO, n4) -> {
                                            final AtomicReference<AttributeWrapper> ar = new AtomicReference<>((attO != null) ? attO : new AttributeWrapper(new InvasionBuild.AttributeSetting()));
                                            List<AbstractConfigListEntry<?>> f = new ArrayList<>();
                                            f.add(eb.startStrField(Component.translatable("config.endofdays_re.attribute.id"), ar.get().data.id).setSaveConsumer(v -> ar.get().data.id = v).build());
                                            f.add(eb.startStrField(Component.translatable("config.endofdays_re.attribute.evl"), ar.get().data.evl).setSaveConsumer(v -> ar.get().data.evl = v).build());
                                            return new MultiElementListEntry<>(Component.translatable("config.endofdays_re.attribute.header"), ar.get(), f, true);
                                        }));

                                // --- 三级嵌套：护甲 (注意拼写 arrmor) ---
                                List<ArmorWrapper> armWs = Arrays.stream(ent.arrmorSetting != null ? ent.arrmorSetting : new InvasionBuild.ArrmorSetting[0]).map(ArmorWrapper::new).collect(Collectors.toList());
                                entEntries.add(new NestedListListEntry<>(Component.translatable("config.endofdays_re.entity.armor"), armWs, false, Optional::empty,
                                        l -> entRef.get().data.arrmorSetting = l.stream().map(w -> w.data).toArray(InvasionBuild.ArrmorSetting[]::new),
                                        () -> List.of(new ArmorWrapper(new InvasionBuild.ArrmorSetting())), eb.getResetButtonKey(), true, true,
                                        (armO, n5) -> {
                                            final AtomicReference<ArmorWrapper> rr = new AtomicReference<>((armO != null) ? armO : new ArmorWrapper(new InvasionBuild.ArrmorSetting()));
                                            List<AbstractConfigListEntry<?>> f = new ArrayList<>();
                                            f.add(eb.startStrField(Component.translatable("config.endofdays_re.armor.id"), rr.get().data.arrmor_id).setSaveConsumer(v -> rr.get().data.arrmor_id = v).build());
                                            f.add(eb.startStrField(Component.translatable("config.endofdays_re.armor.slot"), rr.get().data.slot).setSaveConsumer(v -> rr.get().data.slot = v).build());
                                            f.add(eb.startStrField(Component.translatable("config.endofdays_re.armor.tag"), rr.get().data.tag).setSaveConsumer(v -> rr.get().data.tag = v).build());
                                            f.add(eb.startIntField(Component.translatable("config.endofdays_re.armor.durability"), rr.get().data.durability).setSaveConsumer(v -> rr.get().data.durability = v).build());
                                            f.add(eb.startBooleanToggle(Component.translatable("config.endofdays_re.armor.on_drop"), rr.get().data.on_drop).setSaveConsumer(v -> rr.get().data.on_drop = v).build());
                                            f.add(eb.startFloatField(Component.translatable("config.endofdays_re.common.probability"), rr.get().data.probability).setSaveConsumer(v -> rr.get().data.probability = v).build());
                                            return new MultiElementListEntry<>(Component.translatable("config.endofdays_re.armor.header"), rr.get(), f, true);
                                        }));

                                return new MultiElementListEntry<>(Component.literal("Entity: " + entRef.get().data.id), entRef.get(), entEntries, true);
                            }
                    ));
                    return new MultiElementListEntry<>(Component.literal("Invasion: " + invRef.get().key), invRef.get(), invEntries, true);
                }
        ));
    }

    // --- 包装类：保持逻辑一致性 ---
    public static class EntityWrapper {
        public InvasionBuild.EntitySetting data;

        public EntityWrapper(InvasionBuild.EntitySetting data) {
            this.data = (data != null) ? data : new InvasionBuild.EntitySetting();
        }
    }

    public static class ArmorWrapper {
        public InvasionBuild.ArrmorSetting data;

        public ArmorWrapper(InvasionBuild.ArrmorSetting data) {
            this.data = (data != null) ? data : new InvasionBuild.ArrmorSetting();
        }
    }

    public static class EffectWrapper {
        public InvasionBuild.EffectSetting data;

        public EffectWrapper(InvasionBuild.EffectSetting data) {
            this.data = (data != null) ? data : new InvasionBuild.EffectSetting();
        }
    }

    public static class AttributeWrapper {
        public InvasionBuild.AttributeSetting data;

        public AttributeWrapper(InvasionBuild.AttributeSetting data) {
            this.data = (data != null) ? data : new InvasionBuild.AttributeSetting();
        }
    }

    public static class InvasionWrapper {
        public String key;
        public InvasionBuild.InvasionSettings data;

        public InvasionWrapper(String key, InvasionBuild.InvasionSettings data) {
            this.key = (key != null) ? key : "";
            this.data = (data != null) ? data : new InvasionBuild.InvasionSettings();
        }
    }
}