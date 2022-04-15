package me.steven.indrev.recipes.machines

import com.google.common.collect.HashMultimap
import com.google.common.collect.ImmutableList
import com.google.common.collect.Multimap
import me.steven.indrev.recipes.IRecipeGetter
import me.steven.indrev.utils.getRecipes
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeType
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Identifier

class IRRecipeType<T : IRRecipe>(val id: Identifier) : IRecipeGetter<T>, RecipeType<T> {

    private val recipeCache: Multimap<Item, T> = HashMultimap.create()
    private val fluidOnlyRecipeCache:  Multimap<FluidVariant, T> = HashMultimap.create()

    override fun getMatchingRecipe(world: ServerWorld, itemStack: ItemStack): Collection<T> {
        if (itemStack.isEmpty) return emptyList()
        else if (recipeCache.containsKey(itemStack.item)) return recipeCache[itemStack.item]
        val matches = ImmutableList.copyOf(
            world.recipeManager.getRecipes(this).values
                .filter { recipe -> recipe.input.any { it.ingredient.test(itemStack) } }
        )
        recipeCache.putAll(itemStack.item, matches)
        return matches
    }

    override fun getMatchingRecipe(world: ServerWorld, fluidInput: FluidVariant): Collection<T> {
        if (fluidOnlyRecipeCache.containsKey(fluidInput)) return fluidOnlyRecipeCache[fluidInput]
        val matches = ImmutableList.copyOf(
            world.recipeManager.getRecipes(this).values
                .filter { recipe -> recipe is IRFluidRecipe && recipe.fluidInput.any { it.resource == fluidInput } }
        )
        fluidOnlyRecipeCache.putAll(fluidInput, matches)
        return matches
    }

    fun clearCache() {
        recipeCache.clear()
        fluidOnlyRecipeCache.clear()
    }
}