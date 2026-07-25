package com.endofdays_re.mixin;

import com.endofdays_re.event.register.EntityGoalRegisterEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobGoalMixin {
    @Shadow
    @Final
    public GoalSelector goalSelector;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(EntityType<? extends Mob> type, Level level, CallbackInfo ci) {
        Mob mob = (Mob) (Object) this;
        if (!level.isClientSide) {
            NeoForge.EVENT_BUS.post(new EntityGoalRegisterEvent(this.goalSelector, mob));
        }
    }
}