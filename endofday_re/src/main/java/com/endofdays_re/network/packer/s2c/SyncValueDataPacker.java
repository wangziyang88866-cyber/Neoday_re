package com.endofdays_re.network.packer.s2c;

import com.endofdays_re.client.render.hud.TabTitle;
import com.endofdays_re.config.ConfigData;
import com.endofdays_re.event.data.AllSyncValue;
import com.endofdays_re.utils.ModUtils;
import com.endofdays_re.utils.tools.Component;
import com.endofdays_re.utils.tools.MessageTool;
import com.endofdays_re.utils.tools.TextureLoader;
import com.endofdays_re.utils.type.ModeEventType;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * 全局数值同步包 (S2C) - 1.21.1 适配版
 */
public record SyncValueDataPacker(
        float temperature,
        Optional<String> lang,
        long day,
        int time,
        ModeEventType mode,
        boolean isDay,
        double bProbability,
        boolean isUpdateLevel,
        ModeEventType nextMode
) implements CustomPacketPayload {

    public static final Type<SyncValueDataPacker> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ModUtils.MODID, "sync_value_data"));

    // 使用 StreamCodec 代替 encoder/decoder
    // 在 SyncValueDataPacker 记录类中替换之前的 STREAM_CODEC
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncValueDataPacker> STREAM_CODEC = StreamCodec.of(
            (buffer, value) -> {
                // 1. 写入数据 (必须与读取顺序完全一致)
                buffer.writeFloat(value.temperature());
                // 处理 Optional<String> lang
                ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8)
                        .encode(buffer, value.lang());
                buffer.writeLong(value.day());
                buffer.writeInt(value.time());
                // 使用枚举的 STREAM_CODEC
                ModeEventType.STREAM_CODEC.encode(buffer, value.mode());
                buffer.writeBoolean(value.isDay());
                buffer.writeDouble(value.bProbability());
                buffer.writeBoolean(value.isUpdateLevel());
                // 使用枚举的 STREAM_CODEC
                ModeEventType.STREAM_CODEC.encode(buffer, value.nextMode());
            },
            (buffer) -> {
                // 2. 读取数据 (必须与写入顺序完全一致)
                float temperature = buffer.readFloat();
                Optional<String> lang = ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8)
                        .decode(buffer);
                long day = buffer.readLong();
                int time = buffer.readInt();
                ModeEventType mode = ModeEventType.STREAM_CODEC.decode(buffer);
                boolean isDay = buffer.readBoolean();
                double bProbability = buffer.readDouble();
                boolean isUpdateLevel = buffer.readBoolean();
                ModeEventType nextMode = ModeEventType.STREAM_CODEC.decode(buffer);
                // 3. 返回新实例
                return new SyncValueDataPacker(temperature, lang, day, time, mode, isDay, bProbability, isUpdateLevel, nextMode);
            }
    );

    /**
     * 处理器逻辑
     */
    public static void handle(final SyncValueDataPacker payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.player == null) return;

            // 1. 同步全局基础数据
            AllSyncValue.Instance.day = payload.day();
            AllSyncValue.Instance.time = payload.time();
            AllSyncValue.Instance.isDay = payload.isDay();
            AllSyncValue.Instance.BProbability = payload.bProbability();
            AllSyncValue.Instance.nextNightMode = payload.nextMode();
            AllSyncValue.Instance.temperature = payload.temperature();

            // 2. 处理模式切换逻辑
            if (payload.mode() == ModeEventType.BLOOD && payload.isDay()) {
                AllSyncValue.Instance.mode = ModeEventType.TIME;
            } else {
                AllSyncValue.Instance.mode = payload.mode();
            }

            // 3. 处理语言与消息显示
            payload.lang().ifPresent(langStr -> {
                if (langStr.isEmpty()) return;

                String formattedProb = String.format("%.2f%%", payload.bProbability() * 100);

                // --- MSG 模式下使用图集背景 ---
                if (payload.mode() == ModeEventType.MSG) {
                    TextureLoader.SpriteInfo bgSprite = ModUtils.getConfigTexture("title_bg_resized.png", "title_bg");

                    TabTitle.SetTitleWithOffset(
                            Component.translatable(langStr, new MessageTool.Variable<>("bp", formattedProb)),
                            null,
                            ConfigData.ScreenConfigData.joinTime,
                            ConfigData.ScreenConfigData.ShowTime * 2,
                            ConfigData.ScreenConfigData.OutTime * 2,
                            true,
                            bgSprite,
                            0,
                            -35
                    );
                    return;
                }

                // 聊天栏消息处理
                if (ConfigData.dimensionConfigData.chat_show || payload.mode() != ModeEventType.BLOOD) {
                    minecraft.player.sendSystemMessage(
                            Component.translatable(langStr, new MessageTool.Variable<>("bp", formattedProb))
                    );
                }

                // 标题显示逻辑判定
                boolean nightSkip = !ConfigData.ScreenConfigData.TitleNightShow && !payload.isDay() && (payload.mode() == ModeEventType.TIME || payload.mode() == ModeEventType.NONE);
                boolean eventSkip = (!payload.isDay() && payload.mode() == ModeEventType.TIME) || payload.mode() == ModeEventType.NONE;

                if (!nightSkip && !eventSkip) {
                    if (payload.nextMode() == ModeEventType.BLOOD && payload.isDay()) {
                        minecraft.player.sendSystemMessage(Component.translatable(ModUtils.MODID + ".event.next.moon.msg"));
                        TabTitle.SetTitleWithOffset(Component.translatable(ModUtils.MODID + ".event.next.moon.title"), null, ConfigData.ScreenConfigData.joinTime, ConfigData.ScreenConfigData.ShowTime, ConfigData.ScreenConfigData.OutTime, true, null, 0, 0);
                    } else {
                        TabTitle.SetTitleWithOffset(Component.translatable(langStr, new MessageTool.Variable<>("bp", formattedProb)), null, ConfigData.ScreenConfigData.joinTime, ConfigData.ScreenConfigData.ShowTime, ConfigData.ScreenConfigData.OutTime, true, null, 0, 0);
                    }
                }
            });
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}