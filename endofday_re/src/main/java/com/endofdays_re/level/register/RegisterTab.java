package com.endofdays_re.level.register;

import com.endofdays_re.utils.ModUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

@SuppressWarnings("all")
public class RegisterTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModUtils.MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EOD_TABLE_CREATIVE = CREATIVE_MODE_TABS.register(ModUtils.KeyWraps("eod_table"), () -> CreativeModeTab.builder()
            .title(Component.translatable(ModUtils.MODID + ".eod_table"))
            .icon(() -> new ItemStack(Items.AIR))
            .displayItems(((itemDisplay, output) -> {
                List<Item> size = BuiltInRegistries.ITEM.stream().filter(item -> item.builtInRegistryHolder().key().location().getNamespace().equals(ModUtils.MODID)).toList();
                for (Item item : size) {
                    output.accept(item);
                }
            }))
            .build());

}
