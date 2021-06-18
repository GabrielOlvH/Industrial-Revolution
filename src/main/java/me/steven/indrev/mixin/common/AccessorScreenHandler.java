package me.steven.indrev.mixin.common;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ScreenHandler.class)
public interface AccessorScreenHandler {
    @Accessor
    List<Property> getProperties();

    @Accessor
    IntList getTrackedPropertyValues();
}
