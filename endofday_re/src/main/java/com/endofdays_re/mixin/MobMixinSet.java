package com.endofdays_re.mixin;

import com.endofdays_re.event.data.AllSyncValue;
import com.endofdays_re.event.register.BrainInitEvent;
import com.endofdays_re.utils.ModUtils;
import com.endofdays_re.utils.type.EventPase;
import com.mojang.serialization.Dynamic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(value = Mob.class)
public abstract class MobMixinSet extends LivingEntity {
    @Unique
    private BrainInitEvent<Mob> endofdays_re$event;

    protected MobMixinSet(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Inject(method = "getTarget", at = @At("RETURN"), cancellable = true)
    private void onSetTarget(CallbackInfoReturnable<LivingEntity> cir) {
        LivingEntity target = cir.getReturnValue();
        if (target == null) return;

        Mob mob = (Mob) (Object) this;

        // 检查是否为同类
        String mobId = ModUtils.getEntityTypeID(mob.getType());
        String targetId = ModUtils.getEntityTypeID(target.getType());

        if (Objects.equals(mobId, targetId)) {
            cir.setReturnValue(null);
            return;
        }

        // 检查是否拥有禁止目标的标签
        if (mob.getPersistentData().contains(ModUtils.KeyWraps("no_target"))) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    public void onTick(CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (mob.level().isClientSide) return; // 确保是服务端
        if (mob.getServer() == null) return;

        // 增加非空检查
        if (this.endofdays_re$event != null && this.endofdays_re$event.getBrain() != null) {
            this.endofdays_re$event.getBrain().tick((ServerLevel) mob.level(), mob);
        }
    }


    @Override
    protected Brain.@NotNull Provider<Mob> brainProvider() {
        Mob mob = (Mob) (Object) this;
        endofdays_re$event = new BrainInitEvent<>(mob, AllSyncValue.Instance, EventPase.Pre);
        NeoForge.EVENT_BUS.post(endofdays_re$event);
        return endofdays_re$event.getBrainProvider() != null
                ? endofdays_re$event.getBrainProvider()
                : (Brain.Provider<Mob>) super.brainProvider();
    }

    @Override
    protected @NotNull Brain<?> makeBrain(@NotNull Dynamic<?> dynamic) {
        Mob mob = (Mob) (Object) this;
        // 注意：这里需要确保 brainProvider() 的逻辑正确
        Brain<Mob> brain = this.brainProvider().makeBrain(dynamic);
        endofdays_re$event = new BrainInitEvent<>(mob, () -> brain, AllSyncValue.Instance, EventPase.Post);
        NeoForge.EVENT_BUS.post(endofdays_re$event);
        return brain;
    }


}