package com.endofdays_re.mixin;

import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Button.class)
public interface IbuttonMixin {
    @Accessor("onPress")
    void setOnPress(Button.OnPress onPress);
}
