package me.steven.indrev.mixin;

import me.steven.indrev.api.IRPlayerEntityExtension;
import me.steven.indrev.tools.modular.ArmorModule;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PiglinBrain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinBrain.class)
public class MixinPiglinBrain {
    @Inject(method = "wearsGoldArmor", at = @At("RETURN"), cancellable = true)
    private static void indrev_hasPiglinTrickerModule(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof IRPlayerEntityExtension && ((IRPlayerEntityExtension) entity).isApplied(ArmorModule.NIGHT_VISION)) {
            cir.setReturnValue(true);
        }
    }
}
