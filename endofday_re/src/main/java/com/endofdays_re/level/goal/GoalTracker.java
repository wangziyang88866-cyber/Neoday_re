package com.endofdays_re.level.goal;

import net.minecraft.world.entity.monster.Zombie;

import java.util.WeakHashMap;

public enum GoalTracker {
    ;
    // 使用 WeakHashMap 防止内存泄漏
    private static final WeakHashMap<Zombie, PathBuildingGoal> CACHE = new WeakHashMap<>();

    public static void register(Zombie zombie, PathBuildingGoal goal) {
        if (CACHE.containsValue(goal)) return;
        CACHE.put(zombie, goal);
    }

    public static PathBuildingGoal getPathGoalFromZombie(Zombie zombie) {
        return CACHE.get(zombie);
    }
}
