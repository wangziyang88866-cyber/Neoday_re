package com.endofdays_re.client.render.entity.render;

import com.endofdays_re.level.register.entity.item.entity.FishingHook;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

@OnlyIn(Dist.CLIENT)
public class FishingHookRenderer extends EntityRenderer<FishingHook> {
    // 1.21.1 资源路径解析修正
    private static final ResourceLocation TEXTURE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/fishing_hook.png");
    private static final RenderType RENDER_TYPE = RenderType.entityCutout(TEXTURE_LOCATION);

    public FishingHookRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    private static float fraction(int k) {
        return (float) k / 16.0F;
    }

    // 1.21.1 顶点构建修正
    private static void vertex(VertexConsumer buffer, Matrix4f pose, Matrix3f normalMatrix, int light, float u, int vInt, int uInt, int v) {
        // 1.21.1 中 setNormal 必须传入计算后的 float。由于 (0,1,0) 经过 normalMatrix 变换后通常仍是其对应的列
        // 我们使用标准的 transform 方法来保证兼容性
        org.joml.Vector3f transformedNormal = normalMatrix.transform(0.0F, 1.0F, 0.0F, new org.joml.Vector3f());

        buffer.addVertex(pose, u - 0.5F, (float) vInt - 0.5F, 0.0F)
                .setColor(255, 255, 255, 255)
                .setUv((float) uInt, (float) v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(transformedNormal.x, transformedNormal.y, transformedNormal.z);
    }

    // 1.21.1 线条顶点构建修正
    private static void stringVertex(float dx, float dy, float dz, VertexConsumer buffer, PoseStack.Pose pose, float currentFrac, float nextFrac) {
        float x = dx * currentFrac;
        float y = dy * (currentFrac * currentFrac + currentFrac) * 0.5F + 0.25F;
        float z = dz * currentFrac;

        float nx = dx * nextFrac - x;
        float ny = dy * (nextFrac * nextFrac + nextFrac) * 0.5F + 0.25F - y;
        float nz = dz * nextFrac - z;
        float len = Mth.sqrt(nx * nx + ny * ny + nz * nz);

        // 计算归一化方向
        float invLen = 1.0F / (len == 0 ? 1.0F : len);
        float rawNx = nx * invLen;
        float rawNy = ny * invLen;
        float rawNz = nz * invLen;

        // 关键修复：使用 Pose 中的 Matrix3f 变换法线向量
        org.joml.Vector3f transformedNormal = pose.normal().transform(rawNx, rawNy, rawNz, new org.joml.Vector3f());

        buffer.addVertex(pose.pose(), x, y, z)
                .setColor(0, 0, 0, 255)
                .setNormal(transformedNormal.x, transformedNormal.y, transformedNormal.z);
    }

    @Override
    public void render(FishingHook fishingHook, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        Entity owner = fishingHook.getOwner();
        if (owner instanceof Mob mob) {
            poseStack.pushPose();

            // --- 渲染浮标 (Hook) ---
            poseStack.pushPose();
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

            PoseStack.Pose lastPose = poseStack.last();
            Matrix4f poseMatrix = lastPose.pose();
            Matrix3f normalMatrix = lastPose.normal();
            VertexConsumer buffer = bufferSource.getBuffer(RENDER_TYPE);

            // 1.21.1 顶点坐标修正
            vertex(buffer, poseMatrix, normalMatrix, packedLight, 0.0F, 0, 0, 1);
            vertex(buffer, poseMatrix, normalMatrix, packedLight, 1.0F, 0, 1, 1);
            vertex(buffer, poseMatrix, normalMatrix, packedLight, 1.0F, 1, 1, 0);
            vertex(buffer, poseMatrix, normalMatrix, packedLight, 0.0F, 1, 0, 0);
            poseStack.popPose();

            // --- 渲染钓鱼线 (String) ---
            int armDirection = mob.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
            ItemStack itemstack = mob.getMainHandItem();
            if (!itemstack.is(Items.FISHING_ROD)) {
                armDirection = -armDirection;
            }

            // 计算鱼竿尖端位置
            float bodyRot = Mth.lerp(partialTicks, mob.yBodyRotO, mob.yBodyRot) * (Mth.PI / 180F);
            double sinRot = Mth.sin(bodyRot);
            double cosRot = Mth.cos(bodyRot);
            double armOffset = (double) armDirection * 0.35D;

            double tipX = Mth.lerp(partialTicks, owner.xo, owner.getX()) - cosRot * armOffset - sinRot * 0.8D;
            double tipY = owner.yo + (double) owner.getEyeHeight() + (owner.getY() - owner.yo) * (double) partialTicks - 0.45D;
            double tipZ = Mth.lerp(partialTicks, owner.zo, owner.getZ()) - sinRot * armOffset + cosRot * 0.8D;
            float crouchOffset = owner.isCrouching() ? -0.1875F : 0.0F;

            double hookX = Mth.lerp(partialTicks, fishingHook.xo, fishingHook.getX());
            double hookY = Mth.lerp(partialTicks, fishingHook.yo, fishingHook.getY());
            double hookZ = Mth.lerp(partialTicks, fishingHook.zo, fishingHook.getZ());

            float dx = (float) (tipX - hookX);
            float dy = (float) (tipY - hookY) + crouchOffset;
            float dz = (float) (tipZ - hookZ);

            // 1.21.1 线条渲染建议使用 RenderType.lineStrip() 或 RenderType.lines()
            VertexConsumer lineBuffer = bufferSource.getBuffer(RenderType.lineStrip());
            PoseStack.Pose linePose = poseStack.last();

            for (int k = 0; k <= 16; ++k) {
                stringVertex(dx, dy, dz, lineBuffer, linePose, fraction(k), fraction(k + 1));
            }

            poseStack.popPose();
            super.render(fishingHook, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        }
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull FishingHook fishingHook) {
        return TEXTURE_LOCATION;
    }
}