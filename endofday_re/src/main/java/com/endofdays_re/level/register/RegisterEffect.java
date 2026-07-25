package com.endofdays_re.level.register;

import com.endofdays_re.level.register.effect.*;
import com.endofdays_re.utils.ModUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


public enum RegisterEffect {
    ;
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, ModUtils.MODID);
    // 1. 撕裂 Buff
    public static final DeferredHolder<MobEffect, MobEffect> LACERATE = EFFECTS.register("lacerate",
            LacerateEffect::new);

    // 2. 流血 Buff
    public static final DeferredHolder<MobEffect, MobEffect> BLEEDING = EFFECTS.register("bleeding",
            BleedingEffect::new);

    // 3. 击晕 Buff
    public static final DeferredHolder<MobEffect, MobEffect> STUN = EFFECTS.register("stun",
            StunEffect::new);

    // 4. 骨折 Buff
    public static final DeferredHolder<MobEffect, MobEffect> FRACTURE = EFFECTS.register("fracture",
            FractureEffect::new);
    // 5. 重伤 Buff (新添加)
    public static final DeferredHolder<MobEffect, MobEffect> HEAVY_INJURY = EFFECTS.register("heavy_injury",
            HeavyInjuryEffect::new);

    public static final DeferredHolder<MobEffect, MobEffect> INFECTION = EFFECTS.register("infection",
            InfectionEffect::new);


}
