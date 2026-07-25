package com.endofdays_re.client.render.entity.render;


import com.endofdays_re.utils.ModUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.monster.Zombie;
import org.jetbrains.annotations.NotNull;

public class ZombieRenderer extends EndAbstractZombieRenderer<Zombie, EndZombie<Zombie>> { //僵尸渲染
    public ZombieRenderer(EntityRendererProvider.Context context) {
        this(context, ModelLayers.ZOMBIE, ModelLayers.ZOMBIE_INNER_ARMOR, ModelLayers.ZOMBIE_OUTER_ARMOR);

    }

    public ZombieRenderer(EntityRendererProvider.Context pContext, ModelLayerLocation pZombieLayer, ModelLayerLocation pInnerArmor, ModelLayerLocation pOuterArmor) {
        super(pContext, new EndZombie<>(pContext.bakeLayer(pZombieLayer)), new EndZombie<>(pContext.bakeLayer(pInnerArmor)), new EndZombie<>(pContext.bakeLayer(pOuterArmor)));
    }

    @Override
    public void render(@NotNull Zombie zombie, float yaw, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        if (zombie.getPersistentData().contains(ModUtils.KeyWraps("mod_scale"))) {
            float sc = zombie.getPersistentData().getFloat(ModUtils.KeyWraps("mod_scale"));
            poseStack.scale(sc, sc, sc);
        }


        // 渲染原模型
        super.render(zombie, yaw, partialTicks, poseStack, buffer, packedLight);
        // 恢复原变换
        poseStack.popPose();

    }


}
