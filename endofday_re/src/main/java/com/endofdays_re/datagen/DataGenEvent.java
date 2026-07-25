package com.endofdays_re.datagen;

import com.endofdays_re.datagen.gen.EBlockStateProvider;
import com.endofdays_re.datagen.gen.ItemGenData;
import com.endofdays_re.datagen.gen.RecipeProviderData;
import com.endofdays_re.datagen.gen.WorldGenConfigs;
import com.endofdays_re.datagen.gen.lang.LangDataCN;
import com.endofdays_re.datagen.gen.lang.LangDataEN;
import com.endofdays_re.utils.ModUtils;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.DatapackBuiltinEntriesProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Set;

@EventBusSubscriber
public class DataGenEvent {
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        var existingFileHelper = event.getExistingFileHelper();
        PackOutput packOutput = event.getGenerator().getPackOutput();
        var lp = event.getLookupProvider();

        event.getGenerator().addProvider(event.includeClient(), new EBlockStateProvider(packOutput, existingFileHelper));
        event.getGenerator().addProvider(event.includeClient(), new ItemGenData(packOutput, existingFileHelper));
        event.getGenerator().addProvider(event.includeClient(), new LangDataCN(packOutput, "zh_cn"));
        event.getGenerator().addProvider(event.includeClient(), new LangDataEN(packOutput, "en_us"));
        event.getGenerator().addProvider(event.includeServer(), new RecipeProviderData(packOutput, lp));
        event.getGenerator().addProvider(event.includeServer(), new DatapackBuiltinEntriesProvider(packOutput, lp, WorldGenConfigs.BUILDER, Set.of(ModUtils.MODID)));
    }
}

