package com.endofdays_re.client.render.entity.render;


import com.endofdays_re.level.register.entity.item.entity.ThrownTNTEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public class ThrownTNTRenderer extends EntityRenderer<ThrownTNTEntity> {
    public ThrownTNTRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ThrownTNTEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // 渲染 TNT 实体
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(ThrownTNTEntity entity) {
        // 返回 TNT 的纹理
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
