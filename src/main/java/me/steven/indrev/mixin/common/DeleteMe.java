package me.steven.indrev.mixin.common;

import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

//TODO delete this after libgui fixes https://github.com/CottonMC/LibGui/issues/113
@Mixin(Slot.class)
public class DeleteMe {
    @Shadow @Mutable @Final public int x;
    @Shadow @Mutable @Final public int y;
}
