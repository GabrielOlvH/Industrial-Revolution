package me.steven.indrev.mixin;

import me.steven.indrev.api.CustomEnchantmentProvider;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class MixinEnchantmentHelper {
    @Inject(method = "getLevel", at = @At("HEAD"), cancellable = true)
    private static void indrev_customEnchantProvider(Enchantment enchantment, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.getItem() instanceof CustomEnchantmentProvider) {
            int level = ((CustomEnchantmentProvider) stack.getItem()).getLevel(enchantment, stack);
            if (level > -1)
                cir.setReturnValue(level);
        }
    }
}
