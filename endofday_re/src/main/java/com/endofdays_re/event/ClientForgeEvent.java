package com.endofdays_re.event;

import com.endofdays_re.client.config.ConfigScreenBuild;
import com.endofdays_re.client.mapping.EodKeyMapping;
import com.endofdays_re.config.ConfigData;
import com.endofdays_re.utils.tools.Component;
import com.endofdays_re.utils.tools.MessageTool;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.lwjgl.glfw.GLFW;


@EventBusSubscriber(
        value = Dist.CLIENT
)
public enum ClientForgeEvent {
    ;

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (ConfigData.ScreenConfigData.isShowJoin) {
            event.getEntity().sendSystemMessage(Component.translatable("endofdays_re.join.key", new MessageTool.Variable<>("Player", event.getEntity().getDisplayName().getString())));
            event.getEntity().sendSystemMessage(Component.getComponent(""));
            event.getEntity().sendSystemMessage(Component.getComponent(""));
            event.getEntity().sendSystemMessage(Component.translatable("endofdays_re.join.key_buttom", new MessageTool.Variable<>("Player", event.getEntity().getDisplayName().getString())));
            event.getEntity().sendSystemMessage(Component.translatable("endofdays_re.join.key_buttom_1"));

        }
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // 1. 打开配置菜单 (本地 UI)
        if (event.getKey() == EodKeyMapping.KeyBindingScreen.getKey().getValue() && event.getAction() == GLFW.GLFW_PRESS) {
            if (mc.screen == null) {
                mc.setScreen(ConfigScreenBuild.getConfigBuilder().build());
            }
        }
    }

}
