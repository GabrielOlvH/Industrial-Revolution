package me.steven.indrev.mixin;

import me.steven.indrev.api.IRPlayerEntityExtension;
import me.steven.indrev.tools.modular.ArmorModule;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow public abstract Iterable<ItemStack> getArmorItems();

    @Inject(method = "setAir", at = @At("INVOKE"), cancellable = true)
    private void indrev_breathingModule(CallbackInfo ci) {
        if (this instanceof IRPlayerEntityExtension && ((IRPlayerEntityExtension) this).isApplied(ArmorModule.BREATHING)) {
            ci.cancel();
        }
    }

    @Inject(method = "getJumpVelocityMultiplier", at = @At(value = "RETURN"), cancellable = true)
    private void indrev_jumpBoostModule(CallbackInfoReturnable<Float> cir) {
        if (this instanceof IRPlayerEntityExtension && ((IRPlayerEntityExtension) this).isApplied(ArmorModule.JUMP_BOOST)) {
            cir.setReturnValue((float) ((IRPlayerEntityExtension) this).getAppliedLevel(ArmorModule.JUMP_BOOST));
        }
    }
}
