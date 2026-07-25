package com.endofdays_re.level.goal;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.EnumSet;
import java.util.Objects;

public class TNTGoal extends Goal {
    private static final int STUCK_THRESHOLD = 60; // 3秒
    private final Mob zombie;
    private int stuckTicks = 0;

    public TNTGoal(Mob zombie) {
        this.zombie = zombie;
        // 设置 Flag 确保不会与正常的移动 AI 冲突导致逻辑混乱
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        LivingEntity target = this.zombie.getTarget();

        // 1. 基础条件检查（只读）
        if (target == null || !this.zombie.isAlive()) return false;

        ItemStack headItem = this.zombie.getItemBySlot(EquipmentSlot.HEAD);
        if (!headItem.is(Items.TNT)) {
            stuckTicks = 0;
            return false;
        }

        double distanceSqr = this.zombie.distanceToSqr(target);
        double yDiff = Math.abs(this.zombie.getY() - target.getY());

        // 2. 距离触发逻辑
        if (yDiff <= 2.0D && distanceSqr < 9.0D) {
            return true;
        }

        // 3. 阻塞触发逻辑（注意：计数操作移到了 tick 中，这里只做判断）
        return stuckTicks >= STUCK_THRESHOLD;
    }

    @Override
    public void tick() {
        // 在 tick 中处理计数器，比在 canUse 中安全得多
        LivingEntity target = this.zombie.getTarget();
        if (target != null && (this.zombie.horizontalCollision || this.zombie.getNavigation().isDone())) {
            stuckTicks++;
        } else {
            stuckTicks = Math.max(0, stuckTicks - 1);
        }

        // 如果在执行过程中达到了条件，手动触发 start (部分版本需要)
        if (canUse()) {
            this.start();
        }
    }

    @Override
    public void start() {
        // 关键修复：绝对不要在 Goal 内部直接 kill 实体，这会破坏正在进行的 Goal 列表迭代
        // 使用 server().execute 确保在下一帧或当前帧安全时刻执行
        if (this.zombie.level().getServer() != null) {
            Objects.requireNonNull(this.zombie.level().getServer()).execute(() -> {
                if (this.zombie.isAlive() && this.zombie.getItemBySlot(EquipmentSlot.HEAD).is(Items.TNT)) {
                    this.executeExplosionLogic();
                }
            });
        }
        stuckTicks = 0;
    }

    private void executeExplosionLogic() {
        Level level = this.zombie.level();
        if (level.isClientSide) return;

        // 1. 先移除头盔，防止重复触发
        this.zombie.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);

        // 2. 判定爆炸类型
        if (this.zombie.getRandom().nextFloat() < 0.5F) {
            // 直接原地爆炸并移除实体
            level.explode(this.zombie, this.zombie.getX(), this.zombie.getY(), this.zombie.getZ(), 3.0F, Level.ExplosionInteraction.MOB);
            this.zombie.discard(); // 使用 discard 比 kill 更干净，不会触发掉落物
        } else {
            // 产生一个点燃的 TNT 实体
            this.zombie.playSound(SoundEvents.TNT_PRIMED, 1.0F, 1.0F);
            PrimedTnt tnt = new PrimedTnt(level, this.zombie.getX(), this.zombie.getY(), this.zombie.getZ(), this.zombie);
            tnt.setFuse(40);
            level.addFreshEntity(tnt);
            // 这里也可以选择是否杀死僵尸，或者让它继续存在
            // this.zombie.kill();
        }
    }

    @Override
    public boolean canContinueToUse() {
        // 一旦进入 start 逻辑，爆炸就是不可逆的，所以返回 false 结束该 Goal 的生命周期
        return false;
    }
}