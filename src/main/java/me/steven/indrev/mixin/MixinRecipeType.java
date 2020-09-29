package me.steven.indrev.mixin;

import me.steven.indrev.recipes.IRecipeGetter;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import java.util.HashSet;
import java.util.Set;

@Mixin(RecipeType.class)
public interface MixinRecipeType<T extends Recipe<Inventory>> extends IRecipeGetter<T> {
    @NotNull
    @Override
    default Set<T> getMatchingRecipe(@NotNull ServerWorld world, @NotNull ItemStack item) {
        return new HashSet(((AccessorRecipeManager)world.getRecipeManager()).indrev_getAllOfType((RecipeType<T>)this).values());
    }
}