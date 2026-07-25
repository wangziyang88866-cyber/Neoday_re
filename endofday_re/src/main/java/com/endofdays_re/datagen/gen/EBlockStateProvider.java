package com.endofdays_re.datagen.gen;

import com.endofdays_re.utils.ModUtils;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public class EBlockStateProvider extends BlockStateProvider {
    public EBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, ModUtils.MODID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {

    }

}