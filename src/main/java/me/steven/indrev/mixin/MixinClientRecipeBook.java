package me.steven.indrev.mixin;

import net.minecraft.client.recipebook.ClientRecipeBook;
import net.minecraft.client.recipebook.RecipeBookGroup;
import net.minecraft.recipe.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientRecipeBook.class)
public class MixinClientRecipeBook {
    @Inject(method = "getGroupForRecipe", at = @At("HEAD"), cancellable = true)
    private static void indrev_suppressUnknownRecipeType(Recipe<?> recipe, CallbackInfoReturnable<RecipeBookGroup> cir) {
        if (recipe.getId().getNamespace().equals("indrev")) cir.setReturnValue(RecipeBookGroup.UNKNOWN);
    }
}
