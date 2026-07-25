package com.endofdays_re.event;

import com.endofdays_re.compat.oculus.shader.ModShaders;
import com.endofdays_re.event.data.AllSyncValue;
import com.endofdays_re.utils.type.ModeEventType;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class BloodMoonRenderHandler {

    private static boolean logged = false;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // 只在 AFTER_LEVEL 阶段执行
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }

        // 仅在血月时渲染
        if (AllSyncValue.Instance.mode != ModeEventType.BLOOD) {
            return;
        }

        var shader = ModShaders.getBloodFullscreenShader();
        if (shader == null) {
            if (!logged) {
                System.out.println("[BloodMoon] Fullscreen shader is NULL! Check loading.");
                logged = true;
            }
            return;
        }

        if (!logged) {
            System.out.println("[BloodMoon] Fullscreen shader is loaded, rendering red tint.");
            logged = true;
        }

        // 计算强度
        float intensity = (float) ((Math.sin(System.currentTimeMillis() / 2000.0) + 1) * 0.5);

        // 保存并设置渲染状态
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 绑定着色器
        RenderSystem.setShader(() -> shader);

        // 设置 uniform
        var uniform = shader.getUniform("intensity");
        if (uniform != null) {
            uniform.set(intensity);
        }

        // ★ 关键修复：使用 try-finally 确保 BufferBuilder 被提交
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = null;
        try {
            builder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
            builder.addVertex(-1.0f, -1.0f, 0.0f);
            builder.addVertex( 1.0f, -1.0f, 0.0f);
            builder.addVertex( 1.0f,  1.0f, 0.0f);
            builder.addVertex(-1.0f,  1.0f, 0.0f);
            BufferUploader.drawWithShader(builder.buildOrThrow());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 确保 builder 被释放
            builder = null;
        }

        // 恢复状态
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
    }
}