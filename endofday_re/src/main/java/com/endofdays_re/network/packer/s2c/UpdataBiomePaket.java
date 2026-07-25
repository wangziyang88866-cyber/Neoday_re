package com.endofdays_re.network.packer.s2c;

import com.endofdays_re.utils.ModUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

/**
 * 区块群系刷新数据包 (S2C) - 1.21.1 适配版
 */
public record UpdataBiomePaket(int chunkX, int chunkZ) implements CustomPacketPayload {

    // 1. 定义 ID (注意：修正了原包名中的拼写错误 Paket -> Packet)
    public static final Type<UpdataBiomePaket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(ModUtils.MODID, "update_biome"));

    // 2. 只有两个字段，使用 composite 绰绰有余
    public static final StreamCodec<RegistryFriendlyByteBuf, UpdataBiomePaket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, UpdataBiomePaket::chunkX,
            ByteBufCodecs.VAR_INT, UpdataBiomePaket::chunkZ,
            UpdataBiomePaket::new
    );

    /**
     * 3. 客户端处理器
     */
    public static void handle(final UpdataBiomePaket payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null) {
                // 强制刷新指定区块的渲染和逻辑
                // 在 1.21.1 中，直接调用 setSectionDirty 会更精准
                int minSection = mc.level.getMinSection();
                int maxSection = mc.level.getMaxSection();

                for (int y = minSection; y < maxSection; y++) {
                    mc.level.setSectionDirtyWithNeighbors(payload.chunkX, y, payload.chunkZ);
                }

                // 如果是较大范围的群系改变，也可以使用原有的逻辑
                // ChunkPos pos = new ChunkPos(payload.chunkX, payload.chunkZ);
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}