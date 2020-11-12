package me.steven.indrev.mixin;

import me.steven.indrev.items.armor.IRModularArmor;
import me.steven.indrev.tools.modular.ArmorModule;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntityClient {
    @Shadow public abstract Iterable<ItemStack> getArmorItems();

    @Inject(method = "hasStatusEffect", at = @At("INVOKE"), cancellable = true)
    private void indrev_nightVision(StatusEffect effect, CallbackInfoReturnable<Boolean> cir) {
        if (effect == StatusEffects.NIGHT_VISION) {
            for (ItemStack stack : getArmorItems()) {
                if (stack.getItem() instanceof IRModularArmor && ArmorModule.NIGHT_VISION.getLevel(stack) > 0) {
                    cir.setReturnValue(true);
                }
            }
        }
    }
}
