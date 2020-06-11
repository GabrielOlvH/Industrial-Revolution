package me.steven.indrev.mixin;

import me.steven.indrev.utils.FakePlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class MixinLivingEntity {
    @Inject(method = "drop", at = @At("INVOKE"), cancellable = true)
    public void cancelDrop(DamageSource source, CallbackInfo ci) {
        if (source.getAttacker() instanceof FakePlayerEntity) {
            ci.cancel();
        }
    }
}
