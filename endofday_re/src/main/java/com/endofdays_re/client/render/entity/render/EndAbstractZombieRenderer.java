package com.endofdays_re.client.render.entity.render;


import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("removal")
public class EndAbstractZombieRenderer<T extends Zombie, M extends EndZombie<T>> extends HumanoidMobRenderer<T, M> {
    //抽象渲染层
    private static final ResourceLocation ZOMBIE_LOCATION = ResourceLocation.parse("textures/entity/zombie/zombie.png");

    protected EndAbstractZombieRenderer(EntityRendererProvider.Context pContext, M pModel, M pInnerModel, M pOuterModel) {
        super(pContext, pModel, 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this, pInnerModel, pOuterModel, pContext.getModelManager()));
    }


    protected boolean isShaking(@NotNull T pEntity) {
        return super.isShaking(pEntity) || pEntity.isUnderWaterConverting();
    }

    @Override
    public boolean shouldRender(@NotNull T pLivingEntity, @NotNull Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
        return true;
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull T t) {
        return ZOMBIE_LOCATION;
    }


}
