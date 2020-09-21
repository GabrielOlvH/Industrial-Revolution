package me.steven.indrev.mixin;

import me.steven.indrev.items.miningdrill.DrillModule;
import me.steven.indrev.items.miningdrill.IRMiningDrill;
import me.steven.indrev.tools.Module;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnchantmentHelper.class)
public class MixinEnchantmentHelper {
    @Inject(method = "getLevel", at = @At("HEAD"), cancellable = true)
    private static void indrev_modularToolEnchant(Enchantment enchantment, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.getItem() instanceof IRMiningDrill) {
            Module module;
            if (Enchantments.FORTUNE.equals(enchantment))
                module = DrillModule.FORTUNE;
            else if (Enchantments.SILK_TOUCH.equals(enchantment))
                module = DrillModule.SILK_TOUCH;
            else return;
            int level = module.getLevel(stack);
            cir.setReturnValue(level);
        }
    }
}
