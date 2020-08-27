package me.steven.indrev.mixin;

import alexiil.mc.lib.attributes.fluid.volume.FluidKeys;
import me.steven.indrev.IndustrialRevolution;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Thanks Gimpansor for the fix
@Mixin(value = FluidKeys.class, remap = false)
public class FluidKeysMixin {

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void registerFluids(CallbackInfo ci) {
        IndustrialRevolution.INSTANCE.registerFluids();
    }

}