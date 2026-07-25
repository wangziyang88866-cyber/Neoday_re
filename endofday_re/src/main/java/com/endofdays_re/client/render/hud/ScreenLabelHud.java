package com.endofdays_re.client.render.hud;

import com.endofdays_re.config.ConfigData;
import com.endofdays_re.event.data.AllSyncValue;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class ScreenLabelHud {
    // 1.21.1 使用 LayeredDraw.Layer 接口
    public static final LayeredDraw.Layer OVERLAY = ScreenLabelHud::render;

    private static void render(GuiGraphics graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        // 1.21.1 检查 GUI 是否隐藏或是否有开启的屏幕
        if (mc.options.hideGui || mc.screen != null || !ConfigData.ScreenConfigData.showHud) return;

        var font = mc.font;
        // 使用渲染帧时间来实现平滑动画，而不是 System.currentTimeMillis
        float guiTicks = mc.gui.getGuiTicks() + deltaTracker.getGameTimeDeltaPartialTick(false);

        float sine = (Mth.sin(guiTicks / 15.0F) + 1.0F) / 2.0F;
        float flash = (Mth.sin(guiTicks / 5.0F) + 1.0F) / 2.0F;

        int startX = 10;
        int startY = 10;
        int spacing = 12;

        // --- 1. 渲染生存天数 ---
        int dayAlpha = (int) (200 + (55 * sine));
        int dayColor = (dayAlpha << 24) | 0xE6CF00;

        Component dayText = Component.translatable("endofdays_re.hud.day", AllSyncValue.Instance.day);
        graphics.drawString(font, dayText, startX, startY, dayColor, true);

        // --- 2. 装饰线 ---
        int lineWidth = (int) (30 + 10 * sine);
        graphics.fill(startX, startY + spacing + 2, startX + lineWidth, startY + spacing + 3, 0x88FFFFFF);
    }
}