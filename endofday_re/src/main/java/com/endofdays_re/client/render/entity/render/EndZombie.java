package com.endofdays_re.client.render.entity.render;

import com.endofdays_re.client.render.entity.model.EndAbstractZombieModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public class EndZombie<T extends Zombie> extends EndAbstractZombieModel<T> {

    public EndZombie(ModelPart pRoot) {
        super(pRoot);
    }

    @Override
    public boolean isAggressive(T pEntity) {
        return pEntity.isAggressive();
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack pPoseStack, @NotNull VertexConsumer pBuffer, int pPackedLight, int pPackedOverlay, int pColor) {
        super.renderToBuffer(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pColor);
    }

    @Override
    public void setupAnim(@NotNull T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        // 1. 先调用父类基础动画（行走等）
        super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);

        // 2. TACZ 枪械动画适配 —— 改为可选依赖，使用反射
        if (ModList.get().isLoaded("tacz")) {
            try {
                // 动态加载 InnerThirdPersonManager 类
                Class<?> managerClass = Class.forName("com.tacz.guns.client.animation.third.InnerThirdPersonManager");
                // 获取 LivingEntity 的 Class 对象（用于方法参数匹配）
                Class<?> livingEntityClass = Class.forName("net.minecraft.world.entity.LivingEntity");
                // 获取静态方法 setRotationAnglesHead
                Method method = managerClass.getMethod("setRotationAnglesHead",
                        livingEntityClass,   // 第一个参数：LivingEntity
                        ModelPart.class,     // rightArm
                        ModelPart.class,     // leftArm
                        ModelPart.class,     // body
                        ModelPart.class,     // head
                        float.class);        // limbSwingAmount
                // 调用静态方法，第一个参数为 null
                method.invoke(null, pEntity, this.rightArm, this.leftArm, this.body, this.head, pLimbSwingAmount);
            } catch (Exception e) {
                // 若反射失败（如版本不兼容、方法签名变化），静默忽略，不影响游戏
                // 可选择性打印日志，但非必须
            }
        }

        // 3. 特殊逻辑：如果头部戴着发射器，隐藏原有的头部模型（用于实现特殊的怪物视觉效果）
        Item headItem = pEntity.getItemBySlot(EquipmentSlot.HEAD).getItem();
        this.head.visible = (headItem != Items.DISPENSER);
        // 通常也要隐藏 hat 层（头盔层）以防止穿模
        this.hat.visible = this.head.visible;
    }
}