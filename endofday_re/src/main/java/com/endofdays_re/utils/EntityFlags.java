package com.endofdays_re.utils;


import com.endofdays_re.mixin.mixinhelper.IEntityData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

@SuppressWarnings("removal")
public class EntityFlags {
    public static final String SERVER_ENTITY_TAG_ID = "ServerSideEntityID";
    public static final String TAG_ID = "IMFlags";
    public FlagType canFly = FlagType.TRUE;
    public ResourceLocation serverSideEntityID;

    public static EntityFlags get(Entity entity) {
        return ((IEntityData) entity).getFlags();
    }

    public void load(CompoundTag nbt) {
        this.canFly = FlagType.values()[nbt.getInt("CanFly")];
        if (nbt.contains(SERVER_ENTITY_TAG_ID))
            this.serverSideEntityID = ResourceLocation.parse(nbt.getString(SERVER_ENTITY_TAG_ID));
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("CanFly", this.canFly.ordinal());
        return tag;
    }

    public enum FlagType {
        UNDEF,
        TRUE,
        FALSE,
    }

}
