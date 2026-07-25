package com.endofdays_re.event.register;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.Event;


public class ChunkTickEvent extends Event { //区块TICK事件
    private final Level level;
    private final ChunkPos pos;
    private final LevelChunk chunk;
    private final int randomTickSpeed;

    public ChunkTickEvent(Level level, ChunkPos pos, LevelChunk pChunk, int pRandomTickSpeed) {
        this.level = level;
        this.pos = pos;
        this.chunk = pChunk;
        this.randomTickSpeed = pRandomTickSpeed;
    }

    public boolean isClient() {
        return level.isClientSide;
    }

    public ChunkPos getPos() {
        return pos;
    }

    public LevelChunk GetChunk() {
        return chunk;
    }

    public int getRandomTickSpeed() {
        return randomTickSpeed;
    }

    public Level getLevel() {
        return level;
    }

    public ServerLevel getServerLevel() {
        return level instanceof ServerLevel ? (ServerLevel) level : null;
    }


}
