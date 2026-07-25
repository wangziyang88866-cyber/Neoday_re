package com.endofdays_re.event.register;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.neoforged.bus.api.Event;


public class EntityGoalRegisterEvent extends Event {


    public LivingEntity livingEntity;
    public GoalSelector goalSelector;

    public EntityGoalRegisterEvent(GoalSelector goalSelector, LivingEntity mob) {
        this.goalSelector = goalSelector;
        this.livingEntity = mob;
    }
}
