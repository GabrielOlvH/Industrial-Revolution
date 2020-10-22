package me.steven.indrev.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.item.EnchantmentPredicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EnchantmentPredicate.class)
public interface AccessorEnchantmentPredicate {
    @Accessor
    Enchantment getEnchantment();
    @Accessor
    NumberRange.IntRange getLevels();
}
