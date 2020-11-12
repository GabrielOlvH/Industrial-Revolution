package me.steven.indrev.mixin;

import me.steven.indrev.items.armor.IRModularArmor;
import me.steven.indrev.tools.modular.ArmorModule;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Inject(method = "getNightVisionStrength", at = @At("INVOKE"), cancellable = true)
    private static void indrev_nightVisionStrength(LivingEntity livingEntity, float f, CallbackInfoReturnable<Float> cir) {
        for (ItemStack stack : livingEntity.getArmorItems()) {
            if (stack.getItem() instanceof IRModularArmor && ArmorModule.NIGHT_VISION.getLevel(stack) > 0) {
                cir.setReturnValue(1.0f);
            }
        }
    }
}
