package com.endofdays_re.mixin;

import com.endofdays_re.event.helper.GoalHelper;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public abstract class ZombieAIMixin {
    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void onFinalizeSpawn(CallbackInfoReturnable<net.minecraft.world.entity.SpawnGroupData> cir) {
        Zombie zombie = (Zombie) (Object) this;

        if (zombie.level().isClientSide()) return;

        if (!com.endofdays_re.config.ConfigData.isDayEnable("enable") ||
                !com.endofdays_re.config.ConfigData.isModeEnable("goal_enable")) {
            return;
        }

        GoalHelper.initGoal(zombie.goalSelector, zombie);
    }
}
