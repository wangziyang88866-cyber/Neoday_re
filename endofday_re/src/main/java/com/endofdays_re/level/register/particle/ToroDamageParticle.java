package com.endofdays_re.level.register.particle;

import com.endofdays_re.utils.ModUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

public class ToroDamageParticle extends Particle {
    private final float damage;
    private final int color;
    private final boolean isCrit;
    private final boolean damageBold;
    private final String prefix;
    private final int prefixColor;
    private final boolean prefixBold;
    private final String suffix;
    private final int suffixColor;
    private final boolean suffixBold;

    protected ToroDamageParticle(ClientLevel level, double x, double y, double z, DamageParticleOptions options) {
        super(level, x, y, z);
        this.damage = options.damage();
        this.color = options.color();
        this.isCrit = options.isCrit();
        this.damageBold = options.damageBold();
        this.prefix = options.prefix();
        this.prefixColor = options.prefixColor();
        this.prefixBold = options.prefixBold();
        this.suffix = options.suffix();
        this.suffixColor = options.suffixColor();
        this.suffixBold = options.suffixBold();

        // 物理表现完全一致
        this.xd = (Math.random() - 0.5D) * 0.1D;
        this.yd = 0.3D;
        this.zd = (Math.random() - 0.5D) * 0.1D;

        this.gravity = 0.6f;
        this.hasPhysics = true;
        this.lifetime = 18 + ModUtils.random.nextInt(8);

    }

    @Override
    public void tick() {
        super.tick();
        if (this.onGround) {
            this.yd *= -0.2D;
            this.xd *= 0.7D;
            this.zd *= 0.7D;
        }
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTicks) {
        double lerpX = Mth.lerp(partialTicks, this.xo, this.x);
        double lerpY = Mth.lerp(partialTicks, this.yo, this.y);
        double lerpZ = Mth.lerp(partialTicks, this.zo, this.z);
        Vec3 camPos = camera.getPosition();

        float renderX = (float) (lerpX - camPos.x());
        float renderY = (float) (lerpY - camPos.y());
        float renderZ = (float) (lerpZ - camPos.z());

        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(renderX, renderY, renderZ);

        Quaternionf rotation = new Quaternionf(camera.rotation());
        poseStack.mulPose(rotation);

        float currentAge = (float) this.age + partialTicks;
        float totalLifetime = (float) this.lifetime;

        // 基础缩放保持一致
        float baseScale = 0.025F;
        float finalScale;

        float popInDuration = 4.0F;
        if (currentAge < popInDuration) {
            float t = currentAge / popInDuration;
            float easeOut = 1.0F - (1.0F - t) * (1.0F - t);
            finalScale = Mth.lerp(easeOut, 0.05F * baseScale, baseScale);
        } else {
            finalScale = baseScale;
        }

        // 注意：这里的负号是为了修正MC渲染空间的坐标朝向
        poseStack.scale(-finalScale, -finalScale, finalScale);

        float alpha = 1.0F;
        float fadeStart = 0.7F;
        float progress = currentAge / totalLifetime;

        if (progress > fadeStart) {
            alpha = 1.0F - (progress - fadeStart) / (1.0F - fadeStart);
        }

        int alphaInt = (int) (Mth.clamp(alpha, 0.0F, 1.0F) * 255.0F);
        if (alphaInt <= 0) {
            poseStack.popPose();
            return;
        }
        int alphaBits = alphaInt << 24;

        Font font = Minecraft.getInstance().font;
        String pText = (prefixBold ? "§l" : "") + prefix;
        String sText = (suffixBold ? "§l" : "") + suffix;
        String dText = (damageBold ? "§l" : "") + String.format("%.1f", damage);

        // --- 核心修复：计算总宽度和垂直中心偏移 ---
        float totalWidth = font.width(dText);
        if (!prefix.isEmpty()) totalWidth += font.width(pText + " ");
        if (!suffix.isEmpty()) totalWidth += font.width(" " + sText);

        float currentX = -totalWidth / 2.0F;
        float centerY = -4.5F; // 字体高度的一半，确保文本垂直居中于粒子坐标点

        MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();

        if (!prefix.isEmpty()) {
            drawTextPart(font, pText, currentX, centerY, prefixColor, alphaBits, poseStack, bufferSource);
            currentX += font.width(pText + " ");
        }
        drawTextPart(font, dText, currentX, centerY, color, alphaBits, poseStack, bufferSource);
        currentX += font.width(dText);
        if (!suffix.isEmpty()) {
            currentX += font.width(" ");
            drawTextPart(font, sText, currentX, centerY, suffixColor, alphaBits, poseStack, bufferSource);
        }

        bufferSource.endBatch();
        poseStack.popPose();
    }

    private void drawTextPart(Font font, String text, float x, float y, int color, int alphaBits, PoseStack ps, MultiBufferSource bs) {
        int finalColor = (color & 0x00FFFFFF) | alphaBits;
        // 使用传入的 y 偏移量绘制

        font.drawInBatch(text, x, y, finalColor, false, ps.last().pose(), bs, Font.DisplayMode.NORMAL, 0, 15728880);
    }

    @Override
    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.CUSTOM;
    }

    public static class Provider implements ParticleProvider<DamageParticleOptions> {
        @Override
        public Particle createParticle(@NotNull DamageParticleOptions options, @NotNull ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new ToroDamageParticle(level, x, y, z, options);
        }
    }
}