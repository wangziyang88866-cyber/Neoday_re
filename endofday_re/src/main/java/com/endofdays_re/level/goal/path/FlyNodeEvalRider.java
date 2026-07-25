package com.endofdays_re.level.goal.path;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.FlyNodeEvaluator;
import org.jetbrains.annotations.NotNull;

/**
 * A FlyNodeEvaluator made for the riding ai
 */
public class FlyNodeEvalRider extends FlyNodeEvaluator {

    @Override
    public void prepare(@NotNull PathNavigationRegion level, @NotNull Mob mob) {
        super.prepare(level, mob);

        // 1. 获取 Phantom 的高度
        double phantomHeight = EntityType.PHANTOM.getDimensions().height();
        double ridingOffset = mob.getVehicleAttachmentPoint(mob).y;
        double heightInc = phantomHeight * 0.35 + ridingOffset;
        this.entityHeight += (int) Math.ceil(heightInc);
    }
}
