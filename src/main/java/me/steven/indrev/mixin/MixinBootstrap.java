package me.steven.indrev.mixin;

import me.steven.indrev.registry.IRRegistry;
import net.minecraft.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public class MixinBootstrap {
    @Inject(method = "initialize", at = @At(value = "INVOKE", target = "Lnet/minecraft/Bootstrap;setOutputStreams()V", shift = At.Shift.AFTER), require = 1, allow = 1)
    private static void indrev_register(CallbackInfo ci) {
        IRRegistry.INSTANCE.registerAll();
    }
}
