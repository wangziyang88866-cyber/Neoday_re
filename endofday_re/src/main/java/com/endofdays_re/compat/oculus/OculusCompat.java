package com.endofdays_re.compat.oculus;

import com.endofdays_re.compat.oculus.shader.ModShaders;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

public class OculusCompat {
    private static final Logger LOGGER = LogUtils.getLogger();

    // 注意：这个 RenderType 目前在 OverWorldRender 里并没有被实际使用
    // （天空是用 Tesselator 走 immediate 模式绘制的），保留它是为了以后
    // 如果要接一个真正的 MultiBufferSource 管线时可以直接用。
    // 如果你确认用不上，可以直接删掉这段，避免误导后来的人以为它已经生效了。
    public static final RenderType BLOOD_SKY_RENDER_TYPE = RenderType.create(
            "blood_sky",
            DefaultVertexFormat.POSITION_TEX_COLOR, // 确保与你的 Shader 顶点格式一致
            VertexFormat.Mode.QUADS,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(ModShaders::getBloodSkyShader))
                    .setDepthTestState(new RenderStateShard.DepthTestStateShard("<=", 515))
                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
                    .setTransparencyState(new RenderStateShard.TransparencyStateShard("blood_transparency", () -> {
                        RenderSystem.enableBlend();
                        RenderSystem.blendFuncSeparate(
                                GlStateManager.SourceFactor.SRC_ALPHA,
                                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                                GlStateManager.SourceFactor.ONE,
                                GlStateManager.DestFactor.ZERO
                        );
                    }, () -> {
                        RenderSystem.disableBlend();
                        RenderSystem.defaultBlendFunc();
                    }))
                    .createCompositeState(false)
    );

    public static boolean isLoadOculus() {
        return ModList.get().isLoaded("oculus");
    }

    /**
     * 检测当前是否真的有 Iris/Oculus 光影包在运行（不是只装了模组，而是玩家启用了某个 shaderpack）。
     * 用软依赖 + 反射的方式调用 Iris API，这样即使编译期没有把 Iris API 加进依赖，
     * 也不会导致整个 mod 崩掉——只是这个方法会静默返回 false。
     *
     * 如果你的 build.gradle 里已经把 Iris API 加成 compileOnly 依赖了，
     * 可以把下面这段换成直接 `import net.irisshaders.iris.api.v0.IrisApi;`
     * 然后 `return IrisApi.getInstance().isShaderPackInUse();`，更简洁也更不容易反射失败。
     */
    public static boolean isShaderPackActive() {
        if (!isLoadOculus()) return false;
        try {
            Class<?> irisApiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            Object instance = irisApiClass.getMethod("getInstance").invoke(null);
            Object result = irisApiClass.getMethod("isShaderPackInUse").invoke(instance);
            return result instanceof Boolean b && b;
        } catch (Throwable t) {
            // Iris API 不存在 / 版本不匹配 / 反射失败，都当作"没有光影包在跑"处理，
            // 不要因为这个检测挂掉而影响正常渲染。
            LOGGER.debug("[EndOfDays] 检测 Iris 光影包状态失败，按未启用处理", t);
            return false;
        }
    }
}