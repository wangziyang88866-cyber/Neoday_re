package com.endofdays_re.client.render.entity.model;

import com.endofdays_re.utils.ModUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

public class CorpseZombieModel<T extends LivingEntity> extends HumanoidModel<T> {
    // 1.21.1 资源定位符写法
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(ModUtils.MODID, "corpse_zombie"), "main");

    public CorpseZombieModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createBodyLayer() {
        // HumanoidModel.createMesh 是一个通用的基础人体网格生成器
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    /**
     * 1.21.1 参数名修正：pPackedLight -> pLight, pPackedOverlay -> pOverlay
     */
    @Override
    public void renderToBuffer(@NotNull PoseStack pPoseStack, @NotNull VertexConsumer pBuffer, int pLight, int pOverlay, int pColor) {
        this.young = false; // 强制成人比例
        super.renderToBuffer(pPoseStack, pBuffer, pLight, pOverlay, pColor);
    }

    /**
     * 为方块实体渲染提供的特殊姿态设置
     */
    public void setupAnimFromBlockEntity(int poseType, BlockPos pos) {
        // 1. 彻底重置所有部件的旋转和偏移 (1.21+ 必须确保初始态干净)
        this.head.setRotation(0, 0, 0);
        this.body.setRotation(0, 0, 0);
        this.rightArm.setRotation(0, 0, 0);
        this.leftArm.setRotation(0, 0, 0);
        this.rightLeg.setRotation(0, 0, 0);
        this.leftLeg.setRotation(0, 0, 0);

        if (poseType == 0) {
            // 坐姿
            this.body.xRot = 0.2F;
            this.head.xRot = 0.52F;
            this.rightLeg.xRot = -1.35F;
            this.rightLeg.yRot = 0.35F;
            this.leftLeg.xRot = -1.35F;
            this.leftLeg.yRot = -0.35F;
            this.rightArm.zRot = 0.25F;
            this.leftArm.zRot = -0.25F;
        } else {
            // 基于坐标的随机偏移 (保证同一个坐标位置的尸体姿态永远一致)
            long seed = pos.asLong();

            float rand1 = getHash(seed, 1);
            float rand2 = getHash(seed, 2);
            float rand3 = getHash(seed, 3);

            this.head.yRot = (rand1 - 0.5F) * 1.5F;
            this.rightArm.zRot = 0.4F + (rand2 * 0.8F);
            this.leftArm.zRot = -(0.4F + (rand3 * 0.8F));

            this.rightLeg.yRot = (rand1 - 0.5F);
            this.leftLeg.yRot = (rand2 - 0.5F);
        }
    }

    private float getHash(long seed, int index) {
        long n = seed * 3123611L ^ (long) index * 4322131L;
        n = n * n * 42317861L + n * 11L;
        return (float) ((n >> 16) & 0xFFFF) / 65535.0F;
    }

    @Override
    public void setupAnim(@NotNull T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        // 覆盖默认行走动画，除非该模型也用于实体
    }
}