package com.endofdays_re.mixin;

import com.endofdays_re.event.register.ChunkTickEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class LevelMixin {

    @Inject(
            method = "tickChunk",
            at = @At("TAIL"),
            remap = true
    )
    public void onTickChunk(LevelChunk pChunk, int pRandomTickSpeed, CallbackInfo ci) {
        // 强制检查当前是否在 ServerThread 运行
        if (pRandomTickSpeed > 0 && pChunk.getLevel() != null && !pChunk.getLevel().isClientSide) {
            ServerLevel serverLevel = (ServerLevel) pChunk.getLevel();

            // 关键点：如果是多线程环境，则不发送事件，或者在代码里避开共享资源
            NeoForge.EVENT_BUS.post(new ChunkTickEvent(serverLevel, pChunk.getPos(), pChunk, pRandomTickSpeed));
        }
    }
}