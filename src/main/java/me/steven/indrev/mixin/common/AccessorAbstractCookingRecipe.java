package me.steven.indrev.mixin.common;

import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.Ingredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractCookingRecipe.class)
public interface AccessorAbstractCookingRecipe {
    @Accessor
    Ingredient getInput();
}
