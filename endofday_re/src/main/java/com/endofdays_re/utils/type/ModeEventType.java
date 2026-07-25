package com.endofdays_re.utils.type;


import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * 模式事件枚举 - 1.21.1 适配版
 * 继承 StringRepresentable 以支持自动生成 Codec
 */
public enum ModeEventType implements StringRepresentable {
    BLOOD("blood"),
    NONE("none"),
    TIME("time"),
    MSG("msg");

    // 1. 定义 Codec (用于 JSON, 配置文件, DataFixers)
    // 使用 StringRepresentable.fromEnum 可以自动处理映射
    public static final Codec<ModeEventType> CODEC =
            StringRepresentable.fromEnum(ModeEventType::values);
    public static final StreamCodec<ByteBuf, ModeEventType> STREAM_CODEC =
            ByteBufCodecs.idMapper(
                    id -> ModeEventType.values()[id], // 将 ID 转回枚举
                    ModeEventType::ordinal            // 将枚举转为 ID
            );
    private final String name;

    ModeEventType(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }

    /**
     * 辅助方法：判断是否为血月相关模式
     */
    public boolean isBloodRelated() {
        return this == BLOOD;
    }
}
