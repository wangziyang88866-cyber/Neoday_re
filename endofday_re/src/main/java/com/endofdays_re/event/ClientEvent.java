package com.endofdays_re.event;

import com.endofdays_re.client.config.ConfigScreenBuild;
import com.endofdays_re.client.mapping.EodKeyMapping;
import com.endofdays_re.client.render.entity.render.FishingHookRenderer;
import com.endofdays_re.client.render.entity.render.ThrownTNTRenderer;
import com.endofdays_re.client.render.entity.render.ZombieRenderer;
import com.endofdays_re.client.render.hud.ScreenLabelHud;
import com.endofdays_re.client.render.hud.TabTitle;
import com.endofdays_re.client.render.level.OverWorldRender;
import com.endofdays_re.compat.oculus.shader.ModShaders;
import com.endofdays_re.config.ConfigData;
import com.endofdays_re.event.data.AllSyncValue;
import com.endofdays_re.level.register.RegisterEntity;
import com.endofdays_re.level.register.RegistryParticles;
import com.endofdays_re.level.register.particle.ToroDamageParticle;
import com.endofdays_re.utils.ModUtils;
import com.endofdays_re.utils.tools.TextureLoader;
import com.endofdays_re.utils.type.ModeEventType;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.serialization.Codec;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public enum ClientEvent {
    ;

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onLoadComplete(FMLClientSetupEvent event) {
        ConfigScreenBuild.loadGui();

        if (ModUtils.isloadMod("tacz")) {
            event.enqueueWork(() -> {
                try {
                    Class<?> clazz = Class.forName("com.tacz.guns.api.client.other.ThirdPersonManager");
                    clazz.getMethod("registerDefault").invoke(null);
                } catch (ReflectiveOperationException exception) {
                    ModUtils.warn("Failed to register TACZ third-person animations", exception);
                }
            });
            try {
                NeoForge.EVENT_BUS.register(Class.forName("com.endofdays_re.event.GunHurtEvent"));
            } catch (ClassNotFoundException exception) {
                ModUtils.warn("TACZ client event class is missing", exception);
            }
        }
    }

    @SubscribeEvent
    public static void registerParticleFactories(RegisterParticleProvidersEvent event) {
        event.registerSpecial(RegistryParticles.DAMAGE_NUMBER.get(), new ToroDamageParticle.Provider());
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void onRegisterGuiOverlays(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.TITLE,
                ResourceLocation.fromNamespaceAndPath("endofdays_re", "tab_title"),
                TabTitle.OVERLAY
        );
        event.registerAbove(
                VanillaGuiLayers.TITLE,
                ResourceLocation.fromNamespaceAndPath("endofdays_re", "screen_label_hud"),
                ScreenLabelHud.OVERLAY
        );
    }

    @SubscribeEvent
    public static void init(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(RegisterEntity.FISHING_HOOK.get(), FishingHookRenderer::new);
        event.registerEntityRenderer(RegisterEntity.THROWN_TNT.get(), ThrownTNTRenderer::new);
        event.registerEntityRenderer(EntityType.ZOMBIE, ZombieRenderer::new);
        // 已移除 CorpseZombieRenderer 注册，因为其物品/方块已被删除
        // event.registerBlockEntityRenderer(RegisterBlockEntityTypes.CORPSE_ZOMBIE_BE.get(), CorpseZombieRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // 已移除 CorpseZombieModel 层定义，因为其方块实体已被删除
        // event.registerLayerDefinition(CorpseZombieModel.LAYER_LOCATION, CorpseZombieModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterMemoryModules(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.MEMORY_MODULE_TYPE)) {
            event.register(Registries.MEMORY_MODULE_TYPE,
                    ResourceLocation.fromNamespaceAndPath(ModUtils.MODID, "destination_blocks"),
                    () -> new MemoryModuleType<>(Optional.of(Codec.list(BlockPos.CODEC))));
        }
    }

    @SubscribeEvent
    public static void registerDimensionEffects(RegisterDimensionSpecialEffectsEvent event) {
        // 替换主世界维度特效（自定义天空渲染）
        event.register(BuiltinDimensionTypes.OVERWORLD_EFFECTS,
                new OverWorldRender(192.0F, true, DimensionSpecialEffects.SkyType.NORMAL, false, false));
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        EodKeyMapping.KeyBindingScreen = new KeyMapping(ModUtils.MODID + ".key.screen",
                InputConstants.KEY_J, ModUtils.MODID + ".screen.title");
        event.register(EodKeyMapping.KeyBindingScreen);
    }

    @SubscribeEvent
    public static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected @NotNull Void prepare(@NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
                return null;
            }

            @Override
            protected void apply(@NotNull Void object, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
                // 资源重载时清理动态纹理
                TextureLoader.cleanup();
            }
        });
    }

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        try {
            // 注册天空血月着色器（原有）
            event.registerShader(
                    new ShaderInstance(
                            event.getResourceProvider(),
                            ResourceLocation.fromNamespaceAndPath(ModUtils.MODID, "blood_sky"),
                            DefaultVertexFormat.POSITION_TEX_COLOR
                    ),
                    ModShaders::setBloodSkyShader
            );

            // 注册全屏血月着色器（新增，用于后处理覆盖整个画面）
            event.registerShader(
                    new ShaderInstance(
                            event.getResourceProvider(),
                            ResourceLocation.fromNamespaceAndPath(ModUtils.MODID, "blood_fullscreen"),
                            DefaultVertexFormat.POSITION
                    ),
                    ModShaders::setBloodFullscreenShader
            );
        } catch (IOException e) {
            throw new RuntimeException("无法加载血月 Shader!", e);
        }
    }

    // ==================== 新增：全屏红色滤镜（GUI 层） ====================

    /**
     * 在 HUD 层绘制全屏血月红色滤镜。
     * 这个滤镜会覆盖在光影后处理之上，确保血月氛围不被光影冲淡。
     * 同时使用 try-finally 确保 BufferBuilder 被正确提交，消除 "Clearing BufferBuilder" 警告。
     */
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        // 只在血月时渲染
        if (AllSyncValue.Instance.mode != ModeEventType.BLOOD) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui) return;

        // 获取屏幕尺寸
        var window = mc.getWindow();
        float width = window.getGuiScaledWidth();
        float height = window.getGuiScaledHeight();

        // 计算强度
        float intensity = getCurrentBloodIntensity(mc);

        // 颜色（调暗 20%）
        float r = (0.6f + 0.3f * intensity) * 0.8f;
        float g = (0.08f - 0.05f * intensity) * 0.8f;
        float b = (0.04f - 0.03f * intensity) * 0.8f;
        float a = 0.25f + 0.55f * intensity;

        // 设置渲染状态
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        // 获取姿势矩阵（使用事件提供的 PoseStack）
        var matrix = event.getGuiGraphics().pose().last().pose();

        Tesselator tess = Tesselator.getInstance();
        BufferBuilder builder;
        try {
            builder = tess.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
            builder.addVertex(matrix, 0, 0, 0).setColor(r, g, b, a);
            builder.addVertex(matrix, width, 0, 0).setColor(r, g, b, a);
            builder.addVertex(matrix, width, height, 0).setColor(r, g, b, a);
            builder.addVertex(matrix, 0, height, 0).setColor(r, g, b, a);

            BufferUploader.drawWithShader(builder.buildOrThrow());
        } catch (Exception e) {
            e.printStackTrace();
        }

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private static float getCurrentBloodIntensity(Minecraft mc) {
        var level = mc.level;
        if (level == null) return 0.0f;
        if (AllSyncValue.Instance.mode != ModeEventType.BLOOD) return 0.0f;

        float timeOfDay = level.getDayTime() % 24000;
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
}
