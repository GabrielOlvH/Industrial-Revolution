package me.steven.indrev.recipes.machines

import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import com.google.common.collect.HashMultimap
import com.google.common.collect.ImmutableList
import com.google.common.collect.Multimap
import me.steven.indrev.recipes.IRecipeGetter
import me.steven.indrev.utils.getAllOfType
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeType
import net.minecraft.server.world.ServerWorld

class IRRecipeType<T : IRRecipe> : IRecipeGetter<T>, RecipeType<T> {

    private val recipeCache: Multimap<Item, T> = HashMultimap.create()
    private val fluidOnlyRecipeCache:  Multimap<FluidKey, T> = HashMultimap.create()

    override fun getMatchingRecipe(world: ServerWorld, itemStack: ItemStack, fluidInput: FluidKey?): Collection<T> {
        if (recipeCache.containsKey(itemStack.item)) return recipeCache[itemStack.item]!!
        else if (itemStack.isEmpty && fluidInput != null) {
            if (fluidOnlyRecipeCache.containsKey(fluidInput)) return fluidOnlyRecipeCache[fluidInput]!!
            val matches = ImmutableList.copyOf(
                world.recipeManager.getAllOfType(this).values
                    .filter { recipe -> recipe is IRFluidRecipe && recipe.fluidInput != null && recipe.fluidInput!!.fluidKey == fluidInput }
            )
            fluidOnlyRecipeCache.putAll(fluidInput, matches)
            return matches
        }
        if (itemStack.isEmpty) return emptyList()
        val matches = ImmutableList.copyOf(
            world.recipeManager.getAllOfType(this).values
                .filter { recipe -> recipe.input.any { it.ingredient.test(itemStack) } }.toList()
        )
        recipeCache.putAll(itemStack.item, matches)
        return matches
    }

    fun clearCache() {
        recipeCache.clear()
        fluidOnlyRecipeCache.clear()
    }
}