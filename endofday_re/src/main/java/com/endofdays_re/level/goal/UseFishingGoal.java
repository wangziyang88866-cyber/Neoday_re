package com.endofdays_re.level.goal;


import com.endofdays_re.level.register.entity.item.entity.FishingHook;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.Items;

public class UseFishingGoal extends Goal {
    //使用钓鱼竿
    private final Mob fisher;
    private final int FishingTime;
    FishingHook fishingHook;
    private LivingEntity target;
    private int cooldown = reducedTickDelay(60);
    private int inventoryHookCooldown = 0;
    private int reel;
    private int fishingHookLifetime = 0;

    public UseFishingGoal(Mob fisher, int time) {
        this.fisher = fisher;
        this.FishingTime = time;
    }

    public boolean canUse() {
        LivingEntity target = this.fisher.getTarget();
        if (target == null || !target.isAlive()) {
            return false;
        } else if (this.fisher.isUnderWater()) {
            return false;
        } else if (!(this.fisher.distanceToSqr(target) > 576.0) && !(this.fisher.distanceToSqr(target) <= 1.0) && this.fisher.getSensing().hasLineOfSight(target)) {
            if (--this.cooldown > 0) {
                return false;
            } else {
                return (this.fisher.getMainHandItem().getItem() == Items.FISHING_ROD || this.fisher.getOffhandItem().getItem() == Items.FISHING_ROD)
                        ;
            }
        } else {
            return false;
        }
    }

    public boolean canContinueToUse() {
        return this.fishingHook != null && this.fishingHook.isAlive();
    }

    public void start() {
        this.target = this.fisher.getTarget();
        this.fisher.setAggressive(true);
        this.fisher.level().playSound(null, this.fisher.getX(), this.fisher.getY(), this.fisher.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.HOSTILE, 1.0F, 0.4F / (this.fisher.getRandom().nextFloat() * 0.4F + 0.8F));
        this.fishingHook = new FishingHook(this.fisher, this.fisher.level());
        this.fishingHook.setPos(this.fisher.getEyePosition(1.0F).x, this.fisher.getEyePosition(1.0F).y + 0.1, this.fisher.getEyePosition(1.0F).z);
        double distance = this.fisher.distanceTo(this.target);
        double distanceY = this.target.getY() - this.fisher.getY();
        double dirX = this.target.getX() - this.fisher.getX();
        double dirZ = this.target.getZ() - this.fisher.getZ();
        double distanceXZ = Math.sqrt(dirX * dirX + dirZ * dirZ);
        double yPos = this.target.getY(0.0);
        yPos += (double) this.target.getEyeHeight() * 0.5 + distanceY / distanceXZ;
        double dirY = yPos - this.fishingHook.getY();
        this.fishingHook.shoot(dirX, dirY + distanceXZ * 0.17, dirZ, 1.1F + (float) distance / 32.0F + (float) Math.max(distanceY / 48.0, 0.0), 1.0F);
        this.fisher.level().addFreshEntity(this.fishingHook);
        this.reel = reducedTickDelay(FishingTime);
        this.fishingHookLifetime = reducedTickDelay(60);
    }

    public void tick() {
        this.fisher.getLookControl().setLookAt(this.target);
        if (this.fishingHook.getHookedIn() != null || --this.fishingHookLifetime <= 0) {
            --this.reel;
            if (--this.reel <= 0) {
                this.fishingHook.level().playSound(null, this.fisher.getX(), this.fisher.getY(), this.fisher.getZ(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.HOSTILE, 1.0F, 0.4F / (this.fisher.getRandom().nextFloat() * 0.4F + 0.8F));
                boolean isInventoryHooked = this.fisher.getRandom().nextDouble() < 0.4;
                this.fishingHook.retrieve(--this.inventoryHookCooldown <= 0 && isInventoryHooked);
                if (this.inventoryHookCooldown <= 0 && isInventoryHooked) {
                    this.inventoryHookCooldown = 4;
                }
            }
        }

    }

    public void stop() {
        this.target = null;
        this.fishingHook = null;
        this.cooldown = reducedTickDelay(60);
        this.fisher.setAggressive(false);
    }
}
