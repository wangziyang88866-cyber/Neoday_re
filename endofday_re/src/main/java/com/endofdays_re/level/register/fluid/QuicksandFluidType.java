package com.endofdays_re.level.register.fluid;

import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.function.Consumer;

@SuppressWarnings("removal")
public class QuicksandFluidType extends FluidType {
    public QuicksandFluidType(Properties properties) {
        super(properties);
    }

    @Override
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            private static final ResourceLocation STILL_TEXTURE = ResourceLocation.parse("minecraft:block/sand");
            private static final ResourceLocation FLOWING_TEXTURE = ResourceLocation.parse("minecraft:block/sand");

            @Override
            public ResourceLocation getStillTexture() {
                return STILL_TEXTURE;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return FLOWING_TEXTURE;
            }

            public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance, float partialTick, float nearDistance, float farDistance, FogShape shape) {
                Entity entity = camera.getEntity();
                Level world = entity.level();
                RenderSystem.setShaderFogShape(FogShape.SPHERE);
                RenderSystem.setShaderFogStart(0f);
                RenderSystem.setShaderFogEnd(Math.min(1f, renderDistance));
            }

        });
    }

}
