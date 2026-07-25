package com.endofdays_re.client.render.entity.model;

import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.Monster;
import org.jetbrains.annotations.NotNull;

public abstract class EndAbstractZombieModel<T extends Monster> extends HumanoidModel<T> {
    //抽象模型层-HumanoidModel可以改成其他模型
    protected EndAbstractZombieModel(ModelPart pRoot) {
        super(pRoot);
    }

    public void setupAnim(@NotNull T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {
        super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
        AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, this.isAggressive(pEntity), this.attackTime, pAgeInTicks);
    }

    public abstract boolean isAggressive(T var1);


}
