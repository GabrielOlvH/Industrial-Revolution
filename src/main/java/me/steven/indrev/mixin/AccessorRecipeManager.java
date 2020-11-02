package me.steven.indrev.mixin;

import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(RecipeManager.class)
public interface AccessorRecipeManager {
    @Invoker("getAllOfType")
    <C extends Inventory, T extends Recipe<C>> Map<Identifier, Recipe<C>> indrev_getAllOfType(RecipeType<T> type);
    @Accessor
    Map<RecipeType<?>, Map<Identifier, Recipe<?>>> getRecipes();
}
