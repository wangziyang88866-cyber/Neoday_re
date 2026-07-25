package com.endofdays_re.level.goal;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class BarkerVehicle extends Goal {
    private final int maxRideTime = 80; // 4秒（20 ticks/秒 * 4 = 80 ticks）
    private final int attackInterval = 10; // 每10 ticks（0.5秒）攻击一次
    public Mob mob;
    private int rideTime = 0;

    public BarkerVehicle(Mob mob) {
        this.mob = mob;
    }

    @Override
    public void start() {
        rideTime = 0; // 重置计时器
    }

    @Override
    public void tick() {
        if (mob.getVehicle() == null) {
            rideTime = 0;
            return;
        }

        rideTime++;

        // 每隔一定时间攻击一次（摆动手臂 + 播放粒子）
        if (rideTime % attackInterval == 0) {
            // 摆动手臂（模拟攻击动作）
            mob.swing(InteractionHand.MAIN_HAND);
            mob.swing(InteractionHand.OFF_HAND);

            // 播放攻击粒子
            if (mob.getVehicle() instanceof Boat boat) {
                Vec3 boatPos = boat.position();
                ((ServerLevel) mob.level()).sendParticles(
                        ParticleTypes.CRIT, // 暴击粒子（模拟矿车被破坏）
                        boatPos.x, boatPos.y + 0.5, boatPos.z,
                        2, 0, 0, 0, 0
                );
            } else if (mob.getVehicle() instanceof AbstractMinecart minecart) {
                Vec3 minecartPos = minecart.position();
                ((ServerLevel) mob.level()).sendParticles(
                        ParticleTypes.CRIT, // 暴击粒子（模拟矿车被破坏）
                        minecartPos.x, minecartPos.y + 0.5, minecartPos.z,
                        2, 0, 0, 0, 0
                );
            }
        }

        // 超过4秒就下车
        if (rideTime >= maxRideTime) {
            if (mob.getVehicle() instanceof Boat boat) {
                ItemStack boatItem = new ItemStack(boat.getDropItem());
                boat.spawnAtLocation(boatItem);
                boat.discard();
                mob.stopRiding();
            } else if (mob.getVehicle() instanceof AbstractMinecart minecart) {
                ItemStack minecartItem = new ItemStack(Items.MINECART);
                minecart.spawnAtLocation(minecartItem);
                minecart.discard();
                mob.stopRiding();
            }
            rideTime = 0; // 重置计时器
        }
    }

    @Override
    public boolean canUse() {
        return mob.getVehicle() instanceof AbstractMinecart || mob.getVehicle() instanceof Boat;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse(); // 保持运行，直到下车
    }
}