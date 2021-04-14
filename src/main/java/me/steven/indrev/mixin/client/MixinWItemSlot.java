package me.steven.indrev.mixin.client;

import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.widget.WItemSlot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WItemSlot.class, remap = false)
public class MixinWItemSlot {
    @Shadow @Nullable
    private BackgroundPainter backgroundPainter;

    @Inject(method = "addPainters", at = @At("HEAD"), cancellable = true, remap = false)
    private void indrev_dontOverridePainters(CallbackInfo ci) {
        if (backgroundPainter != null) ci.cancel();
    }
}
