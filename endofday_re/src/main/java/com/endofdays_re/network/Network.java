package com.endofdays_re.network;


import com.endofdays_re.network.packer.s2c.SyncValueDataPacker;
import com.endofdays_re.network.packer.s2c.UpdataBiomePaket;
import com.endofdays_re.utils.ModUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class Network {

    /**
     * 在 1.21.1 中，注册不再是手动调用 register()，
     * 而是订阅 RegisterPayloadHandlersEvent 事件。
     */
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        // 设置协议版本（可选）
        final PayloadRegistrar registrar = event.registrar(ModUtils.MODID)
                .versioned("1.0");

        // 注册 S2C (服务端到客户端)
        registrar.playToClient(
                SyncValueDataPacker.TYPE,
                SyncValueDataPacker.STREAM_CODEC,
                SyncValueDataPacker::handle
        );
        registrar.playToClient(
                UpdataBiomePaket.TYPE,
                UpdataBiomePaket.STREAM_CODEC,
                UpdataBiomePaket::handle
        );


    }

    // --- 发送工具方法 ---

    public static <MSG extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> void sendToPlayer(ServerPlayer player, MSG msg) {
        PacketDistributor.sendToPlayer(player, msg);
    }

    public static <MSG extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> void sendToALLClient(MSG msg) {
        PacketDistributor.sendToAllPlayers(msg);
    }

    public static <MSG extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> void sendToClientTrackingEntity(Entity entity, MSG msg) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, msg);
    }

    public static <MSG extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> void sendToServer(MSG msg) {
        PacketDistributor.sendToServer(msg);
    }
}