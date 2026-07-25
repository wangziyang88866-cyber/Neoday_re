package com.endofdays_re.client.render.entity.render;

import com.endofdays_re.client.render.entity.model.CorpseZombieModel;
import com.endofdays_re.level.register.entity.block.CorpseZombieBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class CorpseZombieRenderer implements BlockEntityRenderer<CorpseZombieBlockEntity> {
    private static final ResourceLocation ZOMBIE_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/zombie/zombie.png");
    private final CorpseZombieModel<LivingEntity> model;
    private final ItemRenderer itemRenderer;
    private final HumanoidModel<LivingEntity> innerArmor;
    private final HumanoidModel<LivingEntity> outerArmor;

    public CorpseZombieRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new CorpseZombieModel<>(context.bakeLayer(CorpseZombieModel.LAYER_LOCATION));
        this.itemRenderer = context.getItemRenderer();
        this.innerArmor = new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR));
        this.outerArmor = new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR));
    }

    @Override
    public void render(CorpseZombieBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        if (be.getLevel() == null) return;

        poseStack.pushPose();
        try {
            poseStack.translate(0.5D, 0.0D, 0.5D);

            if (be.getBlockState().hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                float f = be.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).toYRot();
                poseStack.mulPose(Axis.YP.rotationDegrees(-f));
            }

            poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            poseStack.translate(0.0D, -1.5D, 0.0D);

            applyPoseTransform(poseStack, be.getPoseType(), be);
            this.model.setupAnimFromBlockEntity(be.getPoseType(), be.getBlockPos());

            // --- 1. 渲染僵尸本体 (使用 1.21.1 颜色整数格式) ---
            int color = be.isBurning() ? 0x4DFFFFFF : 0xFFFFFFFF; // 1.21 使用 ARGB 格式 int
            VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(ZOMBIE_LOCATION));
            this.model.renderToBuffer(poseStack, vertexConsumer, combinedLight, combinedOverlay, color);

            // --- 2. 渲染盔甲 ---
            renderArmor(be, poseStack, buffer, combinedLight, combinedOverlay);

            // --- 3. 渲染手持物品 ---
            renderHeldItems(be, poseStack, buffer, combinedLight);

        } finally {
            poseStack.popPose();
        }
    }

    private void renderArmor(CorpseZombieBlockEntity be, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        renderArmorSlot(be, EquipmentSlot.HEAD, be.getItem(CorpseZombieBlockEntity.SLOT_HEAD), poseStack, buffer, light, overlay);
        renderArmorSlot(be, EquipmentSlot.CHEST, be.getItem(CorpseZombieBlockEntity.SLOT_CHEST), poseStack, buffer, light, overlay);
        renderArmorSlot(be, EquipmentSlot.LEGS, be.getItem(CorpseZombieBlockEntity.SLOT_LEGS), poseStack, buffer, light, overlay);
        renderArmorSlot(be, EquipmentSlot.FEET, be.getItem(CorpseZombieBlockEntity.SLOT_FEET), poseStack, buffer, light, overlay);
    }

    private void renderArmorSlot(CorpseZombieBlockEntity be, EquipmentSlot slot, ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int light, int overlay) {
        if (!(stack.getItem() instanceof ArmorItem armorItem)) return;

        // 1. 获取模型
        HumanoidModel<LivingEntity> baseModel = (slot == EquipmentSlot.LEGS) ? innerArmor : outerArmor;
        Model rawModel = net.neoforged.neoforge.client.ClientHooks.getArmorModel(null, stack, slot, baseModel);

        if (rawModel instanceof HumanoidModel<?> activeModel) {
            // 同步姿态
            this.model.copyPropertiesTo((HumanoidModel<LivingEntity>) activeModel);
            setPartVisibility((HumanoidModel<LivingEntity>) activeModel, slot);

            // 2. 获取颜色 (1.21.1 新组件系统)
            int color = net.minecraft.world.item.component.DyedItemColor.getOrDefault(stack, 0xFFFFFFFF);
            if (be.isBurning()) {
                color = (color & 0xFF000000) | (((color & 0x00FFFFFF) >> 1) & 0x007F7F7F);
            }

            // 3. 获取材质资源路径 (1.21.1 的 ArmorMaterial 现在是 Holder 类型)
            // 这是一个通用的材质获取方式
            ResourceLocation texture = getArmorLocation(armorItem, slot == EquipmentSlot.LEGS);

            // 4. 渲染基础层
            VertexConsumer vc = buffer.getBuffer(RenderType.armorCutoutNoCull(texture));
            activeModel.renderToBuffer(poseStack, vc, light, overlay, color);

            // 5. 渲染附魔光泽 (Glint)
            if (stack.isEnchanted()) {
                VertexConsumer glintVc = buffer.getBuffer(RenderType.armorEntityGlint());
                activeModel.renderToBuffer(poseStack, glintVc, light, overlay, color);
            }

            // 注意：1.21.1 的 Armor Trim (装饰) 渲染极其复杂，通常在 BER 中跳过。
            // 如果必须支持 Trim，需要调用 armorItem.getComponents().get(DataComponents.ARMOR_TRIM)
        }
    }

    private ResourceLocation getArmorLocation(ArmorItem item, boolean isInner) {
        // 1. 获取 ArmorMaterial 的 Holder
        var materialHolder = item.getMaterial();

        // 2. 获取该材质的唯一标识符 (例如 "minecraft:iron")
        // 使用 unwrapKey() 是 1.21 获取注册名最稳妥的方式
        ResourceLocation materialLoc = materialHolder.unwrapKey()
                .map(net.minecraft.resources.ResourceKey::location)
                .orElse(ResourceLocation.withDefaultNamespace("leather")); // 找不到则回退到皮革

        // 3. 拼接材质纹理路径
        // 结果类似：minecraft:textures/models/armor/iron_layer_1.png
        return ResourceLocation.fromNamespaceAndPath(
                materialLoc.getNamespace(),
                "textures/models/armor/" + materialLoc.getPath() + "_layer_" + (isInner ? 2 : 1) + ".png"
        );
    }

    private void renderHeldItems(CorpseZombieBlockEntity be, PoseStack poseStack, MultiBufferSource buffer, int light) {
        ItemStack mainHand = be.getItem(CorpseZombieBlockEntity.SLOT_MAINHAND);
        if (mainHand.isEmpty()) return;

        poseStack.pushPose();
        try {
            this.model.rightArm.translateAndRotate(poseStack);
            poseStack.translate(-0.0625D, 0.4375D, 0.0625D);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            this.itemRenderer.renderStatic(mainHand, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, light, OverlayTexture.NO_OVERLAY, poseStack, buffer, be.getLevel(), 0);
        } finally {
            poseStack.popPose();
        }
    }

    private void setPartVisibility(HumanoidModel<LivingEntity> m, EquipmentSlot s) {
        m.setAllVisible(false);
        switch (s) {
            case HEAD -> {
                m.head.visible = true;
                m.hat.visible = true;
            }
            case CHEST -> {
                m.body.visible = true;
                m.rightArm.visible = true;
                m.leftArm.visible = true;
            }
            case LEGS -> {
                m.body.visible = true;
                m.rightLeg.visible = true;
                m.leftLeg.visible = true;
            }
            case FEET -> {
                m.rightLeg.visible = true;
                m.leftLeg.visible = true;
            }
        }
    }

    private void applyPoseTransform(PoseStack poseStack, int poseType, CorpseZombieBlockEntity be) {
        if (poseType == 0) {
            poseStack.translate(0.0, 0.65, 0.0);
        } else {
            poseStack.translate(0.0, 1.4, 0.0);
            if (poseType == 1) {
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
                poseStack.translate(0.0, 0.0, 0.1);
            } else {
                poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                poseStack.translate(0.0, 0.0, -0.1);
            }
            long posSeed = be.getBlockPos().asLong();
            float randomYaw = ((float) ((posSeed ^ (posSeed >>> 32)) * 31 & 0xFFFF) / 65535.0F - 0.5F) * 30.0F;
            poseStack.mulPose(Axis.YP.rotationDegrees(randomYaw));
        }
    }
}