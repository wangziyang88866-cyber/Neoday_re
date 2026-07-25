package com.endofdays_re.datagen.gen;

import com.endofdays_re.utils.ModUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.SwordItem;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class ItemGenData extends ItemModelProvider {

    public ItemGenData(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ModUtils.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        BuiltInRegistries.ITEM.stream().forEach(item -> {//获取注册表里的所有物品
            ResourceLocation location = item.getDefaultInstance().getItemHolder().unwrapKey().orElseThrow().location();//获取注册表资源键名称
            if (location.getNamespace().equals(ModUtils.MODID)) {
                switch (location.getPath()) {
                    case "quicksand_bucket_block":
                        withExistingParent("quicksand_bucket_block", "minecraft:block/sand");
                        break;
                    case "be_zombie_spawn_egg":
                        break;
                    case "corpse_zombie":
                        break;
                    case "spike_block":
                        break;
                    case "barbed_wire_fence":
                        break;
                    default:
                        if (item instanceof SwordItem) {
                            withExistingParent(location.getPath(), ResourceLocation.parse("item/handheld")).texture("layer0", ResourceLocation.fromNamespaceAndPath(location.getNamespace(), "item/" + location.getPath()));
                        } else {
                            this.basicItem(item);
                        }

                }
            }
        });


    }
}
