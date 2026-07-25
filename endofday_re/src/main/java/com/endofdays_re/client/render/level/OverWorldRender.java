package com.endofdays_re.client.render.level;

import com.endofdays_re.compat.oculus.OculusCompat;
import com.endofdays_re.compat.oculus.shader.ModShaders;
import com.endofdays_re.event.data.AllSyncValue;
import com.endofdays_re.utils.type.ModeEventType;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.slf4j.Logger;

public class OverWorldRender extends DimensionSpecialEffects {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ResourceLocation
            MOON_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/moon_phases.png");
    private static final ResourceLocation SUN_LOCATION = ResourceLocation.withDefaultNamespace("textures/environment/sun.png");
    private final Minecraft minecraft = Minecraft.getInstance();

    // ---- 天空穹顶几何参数 ----
    // 半径不能设太大，否则在低渲染距离下可能被摄像机的远裁剪面裁掉；
    // 250 在大多数常见渲染距离（>=12 区块）下都是安全的。
    // 如果你的服务器/整合包默认渲染距离很低（比如 <=6 区块），可以适当调小这个值。
    private static final float SKY_DOME_RADIUS = 250.0F;
    private static final int SKY_DOME_LAT_SEGMENTS = 16; // 纬度分段：地平线->天顶
    private static final int SKY_DOME_LON_SEGMENTS = 32; // 经度分段：绕一圈
    // 略微向下延伸到地平线以下，避免视角略微下压时露出穹顶边缘的缝隙
    private static final float SKY_DOME_MIN_PITCH_DEG = -10.0F;

    // 记录是否已经打印过“着色器为空”的警告，避免每帧刷屏日志
    private boolean loggedNullShaderWarning = false;

    public OverWorldRender(float pCloudLevel, boolean pHasGround, SkyType pSkyType, boolean pForceBrightLightmap, boolean pConstantAmbientLight) {
        super(pCloudLevel, pHasGround, pSkyType, pForceBrightLightmap, pConstantAmbientLight);
    }

    /**
     * 计算当前血月强度（0~1），仅在夜晚区间有效，且为平滑正弦曲线
     */
    private float getBloodIntensity(ClientLevel level, float partialTick) {
        if (AllSyncValue.Instance.mode != ModeEventType.BLOOD) return 0.0f;

        long dayTime = level.getDayTime() % 24000;
        float timeOfDay = dayTime + partialTick;

        float nightStart = 13000;
        float nightEnd = 23000;
        float nightMid = (nightStart + nightEnd) / 2;

        if (timeOfDay >= nightStart && timeOfDay <= nightEnd) {
            float nightProgress;
            if (timeOfDay <= nightMid) {
                nightProgress = (timeOfDay - nightStart) / (nightMid - nightStart);
            } else {
                nightProgress = 1.0f - (timeOfDay - nightMid) / (nightEnd - nightMid);
            }
            return (float) Math.sin(nightProgress * Math.PI);
        }
        return 0.0f;
    }

    @Override
    public @NotNull Vec3 getBrightnessDependentFogColor(@NotNull Vec3 baseColor, float t) {
        if (AllSyncValue.Instance.mode == ModeEventType.BLOOD) {
            float intensity = getBloodIntensity(Minecraft.getInstance().level, 0);
            double brightness = Math.max(0.2, t * 0.6 + 0.2);
            return new Vec3(0.4 * brightness * intensity,
                    0.03 * brightness * intensity,
                    0.03 * brightness * intensity);
        }
        return baseColor.multiply(t * 0.94F + 0.06F, t * 0.94F + 0.06F, t * 0.91F + 0.09F);
    }

    @Override
    public boolean renderSky(ClientLevel level, int ticks, float partialTick, Matrix4f modelViewMatrix, Camera camera, Matrix4f projectionMatrix, boolean isFoggy, Runnable setupFog) {
        if (AllSyncValue.Instance.mode == ModeEventType.BLOOD) {
            PoseStack poseStack = new PoseStack();
            poseStack.last().pose().set(modelViewMatrix);
            this.renderBloodSky(level, poseStack, projectionMatrix, partialTick, camera, isFoggy, setupFog);
            return true;
        }
        return false;
    }

    public void renderBloodSky(ClientLevel level, PoseStack pPoseStack, Matrix4f pProjectionMatrix, float pPartialTick, Camera pCamera, boolean pIsFoggy, Runnable pSkyFogSetup) {
        pSkyFogSetup.run();
        if (pIsFoggy) return;

        FogType fogtype = pCamera.getFluidInCamera();
        if (fogtype == FogType.POWDER_SNOW || fogtype == FogType.LAVA || doesMobEffectBlockSky(pCamera)) return;

        float intensity = getBloodIntensity(level, pPartialTick);

        // 1. 绘制自定义着色天空背景（半球穹顶渐变 + 星星）
        renderCustomSkyShader(pPoseStack, level, intensity);

        // 2. 绘制太阳和月亮（叠加层，颜色随 intensity 变化）
        RenderSystem.enableBlend();
        RenderSystem.depthMask(false);

        pPoseStack.pushPose();
        pPoseStack.scale(1.5f, 1.5f, 1.5f);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        pPoseStack.mulPose(Axis.XP.rotationDegrees(level.getTimeOfDay(pPartialTick) * 360.0F));

        Matrix4f matrix4f = pPoseStack.last().pose();
        Tesselator tesselator = Tesselator.getInstance();

        // 太阳
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        float rSun = 0.6f + 0.4f * intensity;
        float gSun = 0.3f * intensity;
        float bSun = 0.05f * intensity;
        RenderSystem.setShaderColor(rSun, gSun, bSun, 1.0F);
        RenderSystem.setShaderTexture(0, SUN_LOCATION);

        float sunSize = 30.0F;
        BufferBuilder sunBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        sunBuilder.addVertex(matrix4f, -sunSize, 100.0F, -sunSize).setUv(0.0F, 0.0F);
        sunBuilder.addVertex(matrix4f, sunSize, 100.0F, -sunSize).setUv(1.0F, 0.0F);
        sunBuilder.addVertex(matrix4f, sunSize, 100.0F, sunSize).setUv(1.0F, 1.0F);
        sunBuilder.addVertex(matrix4f, -sunSize, 100.0F, sunSize).setUv(0.0F, 1.0F);
        BufferUploader.drawWithShader(sunBuilder.buildOrThrow());

        // 月亮
        RenderSystem.setShaderTexture(0, MOON_LOCATION);
        float rMoon = 0.5f + 0.3f * intensity;
        float gMoon = 0.2f * intensity;
        float bMoon = 0.05f * intensity;
        RenderSystem.setShaderColor(rMoon, gMoon, bMoon, 1.0F);

        int phase = level.getMoonPhase();
        float u1 = (float) (phase % 4) / 4.0F;
        float v1 = (float) (phase / 4 % 2) / 2.0F;
        float u2 = (float) (phase % 4 + 1) / 4.0F;
        float v2 = (float) (phase / 4 % 2 + 1) / 2.0F;

        float moonSize = 20.0F;
        BufferBuilder moonBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        moonBuilder.addVertex(matrix4f, -moonSize, -100.0F, moonSize).setUv(u2, v2);
        moonBuilder.addVertex(matrix4f, moonSize, -100.0F, moonSize).setUv(u1, v2);
        moonBuilder.addVertex(matrix4f, moonSize, -100.0F, -moonSize).setUv(u1, v1);
        moonBuilder.addVertex(matrix4f, -moonSize, -100.0F, -moonSize).setUv(u2, v1);
        BufferUploader.drawWithShader(moonBuilder.buildOrThrow());

        pPoseStack.popPose();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    /**
     * 使用自定义着色器渲染程序化血月天空背景（半球穹顶 + 渐变 + 星星）。
     * 着色器基于视图空间位置计算仰角，产生从地平线到天顶的渐变颜色，并包含星星。
     */
    private void renderCustomSkyShader(PoseStack pPoseStack, ClientLevel level, float intensity) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        // 穹顶几何体是从摄像机内部看向内壁的，必须关闭背面剔除，否则整个天空会消失不见
        RenderSystem.disableCull();

        pPoseStack.pushPose();
        Matrix4f matrix = pPoseStack.last().pose();

        ShaderInstance shader = ModShaders.getBloodSkyShader();
        if (shader == null) {
            if (!loggedNullShaderWarning) {
                LOGGER.warn("[EndOfDays] blood_sky 着色器为空，已降级为纯色天空。" +
                        "请检查 ModShaders.getBloodSkyShader() 是否正确注册、" +
                        "blood_sky.json/.vsh/.fsh 资源路径是否正确，以及游戏日志中是否有 GLSL 编译报错。");
                loggedNullShaderWarning = true;
            }
            renderFallbackSky(matrix, intensity);
            pPoseStack.popPose();
            RenderSystem.enableCull();
            RenderSystem.depthMask(true);
            return;
        }
        loggedNullShaderWarning = false;

        // 如果检测到 Iris/Oculus 光影包正在运行，光影包很可能会接管天空的雾效/色调，
        // 我们自己的强辉光叠加在光影包处理之上容易出现颜色叠加错误（过曝、发灰等）。
        // 这里做一个轻量降级：仍然渲染我们的穹顶和渐变，但不再叠加额外的辉光带，
        // 交给光影包自己的雾效/天空后处理去融合。真正意义上让第三方光影包完美适配
        // 自定义 GLSL 天空是做不到"通用兼容"的（光影包不认识 blood_sky 这个程序名），
        // 这里只能尽量减少和光影包互相打架的概率。
        boolean shaderPackActive = OculusCompat.isShaderPackActive();

        RenderSystem.setShader(() -> shader);
        var intensityUniform = shader.getUniform("bloodIntensity");
        if (intensityUniform != null) {
            intensityUniform.set(shaderPackActive ? intensity * 0.7f : intensity);
        } else {
            LOGGER.warn("[EndOfDays] blood_sky 着色器缺少 bloodIntensity uniform，天空将不会随血月强度变化。");
        }

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder builder = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        buildSkyDome(builder, matrix, SKY_DOME_RADIUS);
        BufferUploader.drawWithShader(builder.buildOrThrow());

        pPoseStack.popPose();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
    }

    /**
     * 着色器为 null 时的降级方案：绘制一个纯色平面（不再是超大平面，缩小尺寸避免裁剪问题）
     */
    private void renderFallbackSky(Matrix4f matrix, float intensity) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tess = Tesselator.getInstance();
        BufferBuilder builder = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        float y = 80.0F, size = 200.0F;
        float r = 0.35f + 0.35f * intensity;
        float g = 0.02f * intensity;
        float b = 0.01f * intensity;
        builder.addVertex(matrix, -size, y, -size).setColor(r, g, b, 1.0F);
        builder.addVertex(matrix, -size, y, size).setColor(r, g, b, 1.0F);
        builder.addVertex(matrix, size, y, size).setColor(r, g, b, 1.0F);
        builder.addVertex(matrix, size, y, -size).setColor(r, g, b, 1.0F);
        BufferUploader.drawWithShader(builder.buildOrThrow());
    }

    /**
     * 构建一个以摄像机为中心的半球穹顶几何体（略微延伸到地平线以下）。
     * 用真实的球面坐标代替原来的单张平面，这样片元着色器里根据
     * normalize(viewPos).y 算出的"仰角"才能正确覆盖从地平线到天顶的整个范围，
     * 避免原来平面几何在低角度时穿帮、露出默认雾色的问题。
     */
    private void buildSkyDome(BufferBuilder builder, Matrix4f matrix, float radius) {
        float minPitch = (float) Math.toRadians(SKY_DOME_MIN_PITCH_DEG);
        float maxPitch = (float) Math.toRadians(90.0);

        for (int lat = 0; lat < SKY_DOME_LAT_SEGMENTS; lat++) {
            float pitch0 = minPitch + (maxPitch - minPitch) * lat / SKY_DOME_LAT_SEGMENTS;
            float pitch1 = minPitch + (maxPitch - minPitch) * (lat + 1) / SKY_DOME_LAT_SEGMENTS;

            for (int lon = 0; lon < SKY_DOME_LON_SEGMENTS; lon++) {
                float yaw0 = (float) (2 * Math.PI * lon / SKY_DOME_LON_SEGMENTS);
                float yaw1 = (float) (2 * Math.PI * (lon + 1) / SKY_DOME_LON_SEGMENTS);

                Vec3 p00 = spherePoint(pitch0, yaw0, radius);
                Vec3 p01 = spherePoint(pitch0, yaw1, radius);
                Vec3 p11 = spherePoint(pitch1, yaw1, radius);
                Vec3 p10 = spherePoint(pitch1, yaw0, radius);

                // Color 通道原本没在用（一直传纯白色），这里借用它把"未经相机旋转的本地方向"
                // （即这个点在真实天空中的世界方位）编码进去，-1~1 映射到 0~1。
                // 顶点着色器里会解码出来喂给星星噪声，让星星图案钉死在世界方向上，
                // 而不是钉死在屏幕坐标系里（那样转头星星就会跟着转）。
                float c00r = (float) (p00.x / radius) * 0.5F + 0.5F;
                float c00g = (float) (p00.y / radius) * 0.5F + 0.5F;
                float c00b = (float) (p00.z / radius) * 0.5F + 0.5F;
                float c01r = (float) (p01.x / radius) * 0.5F + 0.5F;
                float c01g = (float) (p01.y / radius) * 0.5F + 0.5F;
                float c01b = (float) (p01.z / radius) * 0.5F + 0.5F;
                float c11r = (float) (p11.x / radius) * 0.5F + 0.5F;
                float c11g = (float) (p11.y / radius) * 0.5F + 0.5F;
                float c11b = (float) (p11.z / radius) * 0.5F + 0.5F;
                float c10r = (float) (p10.x / radius) * 0.5F + 0.5F;
                float c10g = (float) (p10.y / radius) * 0.5F + 0.5F;
                float c10b = (float) (p10.z / radius) * 0.5F + 0.5F;

                builder.addVertex(matrix, (float) p00.x, (float) p00.y, (float) p00.z).setUv(0.0F, 0.0F).setColor(c00r, c00g, c00b, 1.0F);
                builder.addVertex(matrix, (float) p01.x, (float) p01.y, (float) p01.z).setUv(1.0F, 0.0F).setColor(c01r, c01g, c01b, 1.0F);
                builder.addVertex(matrix, (float) p11.x, (float) p11.y, (float) p11.z).setUv(1.0F, 1.0F).setColor(c11r, c11g, c11b, 1.0F);
                builder.addVertex(matrix, (float) p10.x, (float) p10.y, (float) p10.z).setUv(0.0F, 1.0F).setColor(c10r, c10g, c10b, 1.0F);
            }
        }
    }

    private Vec3 spherePoint(float pitch, float yaw, float radius) {
        double y = Math.sin(pitch) * radius;
        double horizontal = Math.cos(pitch) * radius;
        double x = Math.cos(yaw) * horizontal;
        double z = Math.sin(yaw) * horizontal;
        return new Vec3(x, y, z);
    }

    @Override
    public float[] getSunriseColor(float pTimeOfDay, float pPartialTick) {
        if (AllSyncValue.Instance.mode == ModeEventType.BLOOD) {
            float intensity = getBloodIntensity(Minecraft.getInstance().level, pPartialTick);
            float r = 0.4f + 0.5f * intensity;
            float g = 0.1f * intensity;
            float b = 0.02f * intensity;
            return new float[]{r, g, b, 1.0f};
        }
        return super.getSunriseColor(pTimeOfDay, pPartialTick);
    }

    private boolean doesMobEffectBlockSky(Camera pCamera) {
        Entity entity = pCamera.getEntity();
        return entity instanceof LivingEntity living && (living.hasEffect(MobEffects.BLINDNESS) || living.hasEffect(MobEffects.DARKNESS));
    }

    @Override
    public boolean isFoggyAt(int pX, int pY) {
        return false;
    }
}