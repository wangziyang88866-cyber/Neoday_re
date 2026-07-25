package com.endofdays_re.event.register;

import com.endofdays_re.event.data.AllSyncValue;
import com.endofdays_re.utils.ModUtils;
import com.endofdays_re.utils.type.EventPase;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.neoforged.bus.api.Event;

import java.util.function.Supplier;

public class BrainInitEvent<E extends LivingEntity> extends Event {
    private final E livingEntity;
    private final AllSyncValue syncValue;
    private final EventPase pase;
    private Brain.Provider<E> brainProvider;
    private Brain<E> brain;

    public BrainInitEvent(E livingEntity, Supplier<Brain<E>> brain, AllSyncValue instance, EventPase pase) {
        this.livingEntity = livingEntity;
        this.syncValue = instance;
        this.brain = brain.get();
        this.pase = pase;
    }

    public BrainInitEvent(E livingEntity, AllSyncValue instance, EventPase pase) {
        this.livingEntity = livingEntity;
        this.syncValue = instance;
        this.pase = pase;
    }

    public EventPase getPase() {
        return pase;
    }

    public E getEntity() {
        return livingEntity;
    }

    public AllSyncValue getSyncValue() {
        return syncValue;
    }

    public Brain.Provider<E> getBrainProvider() {
        if (ModUtils.IsShowDebug) {
            // ModUtils.debug("BrainInitEvent [{}] getBrainProvider called for {}: {}", pase, livingEntity, brainProvider);
        }
        return brainProvider;
    }

    public void setBrainProvider(Brain.Provider<E> brain) {
        this.brainProvider = brain;
    }

    public Brain<E> getBrain() {
        if (ModUtils.IsShowDebug) {
            // ModUtils.debug("BrainInitEvent [{}] getBrain called for {}: {}", pase, livingEntity, brain);
        }
        return brain;
    }

}
