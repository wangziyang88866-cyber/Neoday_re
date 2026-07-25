package com.endofdays_re.client.render.hud;

import com.endofdays_re.config.ConfigData;
import com.endofdays_re.utils.tools.TextureLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class TabTitle {
    private static Component currentTitle;
    private static Component currentSubtitle;
    private static int remainingTicks;
    private static int fadeInTicks, stayTicks, fadeOutTicks;
    // 特效变量
    private static boolean typewriterEffect = false;
    private static boolean shakeEffect = false;
    private static int typewriterProgress = 0;
    private static int typewriterSpeed = 2;
    private static int typewriterTimer = 0;
    private static float shakeIntensity = 2.0f;
    private static long shakeSeed = System.currentTimeMillis();
    // 偏移与纹理
    private static int componentOffsetX = 0;
    private static int componentOffsetY = 0;
    private static TextureLoader.SpriteInfo TextUres = null;
    private static boolean isAlpha;
    // 1.21.1 标准 Layer 接口
    public static final LayeredDraw.Layer OVERLAY = TabTitle::render;

    // ================== 初始化与设置方法 (你需要的入口) ==================

    /**
     * 基础显示
     */
    public static void ShowTitle(Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
        currentTitle = title;
        currentSubtitle = subtitle;
        fadeInTicks = fadeIn;
        stayTicks = stay;
        fadeOutTicks = fadeOut;
        remainingTicks = fadeIn + stay + fadeOut;
        resetTypewriterProgress();
        shakeSeed = System.currentTimeMillis();
    }

    /**
     * 带纹理显示
     */
    public static void SetTitle(Component title, Component subtitle, int fadeIn, int stay, int fadeOut, boolean isAlpha, TextureLoader.SpriteInfo textUres) {
        ShowTitle(title, subtitle, fadeIn, stay, fadeOut);
        TabTitle.isAlpha = isAlpha;
        TabTitle.TextUres = textUres;
    }

    /**
     * 带偏移显示 (SetTitleWithOffset)
     */
    public static void SetTitleWithOffset(Component title, Component subtitle, int fadeIn, int stay, int fadeOut, boolean isAlpha, TextureLoader.SpriteInfo textUres, int offsetX, int offsetY) {
        SetTitle(title, subtitle, fadeIn, stay, fadeOut, isAlpha, textUres);
        setComponentOffset(offsetX, offsetY);
    }

    /**
     * 带偏移和打字机开关
     */
    public static void SetTitleWithOffset(Component title, Component subtitle, int fadeIn, int stay, int fadeOut, boolean isAlpha, TextureLoader.SpriteInfo textUres, int offsetX, int offsetY, boolean typewriterEnabled) {
        SetTitleWithOffset(title, subtitle, fadeIn, stay, fadeOut, isAlpha, textUres, offsetX, offsetY);
        setTypewriterEffect(typewriterEnabled);
    }

    /**
     * 全特效显示 (SetTitleWithEffects)
     */
    public static void SetTitleWithEffects(Component title, Component subtitle, int fadeIn, int stay, int fadeOut,
                                           boolean isAlpha, TextureLoader.SpriteInfo textUres, int offsetX, int offsetY,
                                           boolean typewriterEnabled, boolean shakeEnabled, int typewriterSpeed, float shakeIntensity) {
        SetTitleWithOffset(title, subtitle, fadeIn, stay, fadeOut, isAlpha, textUres, offsetX, offsetY, typewriterEnabled);
        setShakeEffect(shakeEnabled);
        setTypewriterSpeed(typewriterSpeed);
        setShakeIntensity(shakeIntensity);
    }

    // ================== 逻辑更新 ==================

    private static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.options.hideGui) return;

        // 修正: 1.21.1 检查调试界面的方式
        boolean isDebug = mc.getDebugOverlay().showDebugScreen();

        if (!isDebug && currentTitle != null && remainingTicks > 0 && ConfigData.ScreenConfigData.isTitleShow && mc.screen == null) {
            int total = fadeInTicks + stayTicks + fadeOutTicks;
            int elapsed = total - remainingTicks;
            int alpha = calculateAlpha(elapsed, total, fadeInTicks, stayTicks, fadeOutTicks);

            if (alpha > 8) {
                int sw = guiGraphics.guiWidth();
                int sh = guiGraphics.guiHeight();
                renderTitle(guiGraphics, mc.font, alpha, sw, sh);
                if (currentSubtitle != null) {
                    renderSubtitle(guiGraphics, mc.font, alpha, sw, sh);
                }
            }

            if (!mc.isPaused()) {
                updateTypewriterProgress();
                remainingTicks--;
            }
        }
    }

    // --- 工具方法 ---
    public static void setTypewriterEffect(boolean e) {
        typewriterEffect = e;
        resetTypewriterProgress();
    }

    public static void setShakeEffect(boolean e) {
        shakeEffect = e;
    }

    public static void setTypewriterSpeed(int s) {
        typewriterSpeed = Math.max(1, s);
    }

    public static void setShakeIntensity(float i) {
        shakeIntensity = Math.max(0, i);
    }

    public static void setComponentOffset(int x, int y) {
        componentOffsetX = x;
        componentOffsetY = y;
    }

    private static void resetTypewriterProgress() {
        typewriterProgress = 0;
        typewriterTimer = 0;
    }

    private static void updateTypewriterProgress() {
        if (typewriterEffect && currentTitle != null) {
            typewriterTimer++;
            if (typewriterTimer >= typewriterSpeed) {
                typewriterTimer = 0;
                if (typewriterProgress < currentTitle.getString().length()) typewriterProgress++;
            }
        }
    }

    private static int calculateAlpha(int elapsed, int total, int fadeIn, int stay, int fadeOut) {
        if (elapsed < fadeIn) return (int) (255 * ((float) elapsed / fadeIn));
        if (elapsed < fadeIn + stay) return 255;
        int foElapsed = elapsed - (fadeIn + stay);
        return Math.max(0, (int) (255 * (1.0F - (float) foElapsed / fadeOut)));
    }

    private static Component getProcessedTitle() {
        if (!typewriterEffect || currentTitle == null) return currentTitle;
        String s = currentTitle.getString();
        return Component.literal(s.substring(0, Math.min(typewriterProgress, s.length()))).setStyle(currentTitle.getStyle());
    }

    private static void renderTitle(GuiGraphics graphics, Font font, int alpha, int sw, int sh) {
        graphics.pose().pushPose();
        float sx = 0, sy = 0;
        if (shakeEffect) {
            float t = (float) ((System.currentTimeMillis() - shakeSeed) / 1000.0);
            sx = Mth.sin(t * 20F) * shakeIntensity;
            sy = Mth.cos(t * 15F) * shakeIntensity;
        }
        graphics.pose().translate(sw / 2.0F + sx, sh / 2.0F + sy, 0);
        graphics.pose().scale(2.5F, 2.5F, 2.5F);

        Component title = getProcessedTitle();
        int color = 0xFFFFFF | (alpha << 24);
        int w = font.width(title);
        int tx = -w / 2 + componentOffsetX;
        int ty = -10 + componentOffsetY;

        drawBackdrop(graphics, tx, ty, w, font.lineHeight);
        graphics.drawString(font, title, tx, ty, color, true);
        graphics.pose().popPose();
    }

    private static void renderSubtitle(GuiGraphics graphics, Font font, int alpha, int sw, int sh) {
        graphics.pose().pushPose();
        graphics.pose().translate(sw / 2.0F, sh / 2.0F + 30, 0);
        graphics.pose().scale(1.2F, 1.2F, 1.2F);

        int color = 0xFFFFFF | (alpha << 24);
        int w = font.width(currentSubtitle);
        int tx = -w / 2 + componentOffsetX;
        int ty = componentOffsetY;

        drawBackdrop(graphics, tx, ty, w, font.lineHeight);
        graphics.drawString(font, currentSubtitle, tx, ty, color, true);
        graphics.pose().popPose();
    }

    private static void drawBackdrop(GuiGraphics g, int x, int y, int w, int h) {
        if (TextUres != null) {
            TextureLoader.render(g, TextUres, x - 2, y - 2, w + 4, h + 4);
        } else if (!isAlpha) {
            g.fill(x - 2, y - 2, x + w + 2, y + h + 2, 0x80000000);
        }
    }
}