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
import net.minecraft.util.Identifier

class IRRecipeType<T : IRRecipe>(val id: Identifier) : IRecipeGetter<T>, RecipeType<T> {

    private val recipeCache: Multimap<Item, T> = HashMultimap.create()
    private val fluidOnlyRecipeCache:  Multimap<FluidKey, T> = HashMultimap.create()

    override fun getMatchingRecipe(world: ServerWorld, itemStack: ItemStack): Collection<T> {
        if (itemStack.isEmpty) return emptyList()
        else if (recipeCache.containsKey(itemStack.item)) return recipeCache[itemStack.item]!!
        val matches = ImmutableList.copyOf(
            world.recipeManager.getAllOfType(this).values
                .filter { recipe -> recipe.input.any { it.ingredient.test(itemStack) } }.toList()
        )
        recipeCache.putAll(itemStack.item, matches)
        return matches
    }

    override fun getMatchingRecipe(world: ServerWorld, fluidInput: FluidKey): Collection<T> {
        if (fluidOnlyRecipeCache.containsKey(fluidInput)) return fluidOnlyRecipeCache[fluidInput]!!
        val matches = ImmutableList.copyOf(
            world.recipeManager.getAllOfType(this).values
                .filter { recipe -> recipe is IRFluidRecipe && recipe.fluidInput.any { it.fluidKey == fluidInput } }
        )
        fluidOnlyRecipeCache.putAll(fluidInput, matches)
        return matches
    }

    fun clearCache() {
        recipeCache.clear()
        fluidOnlyRecipeCache.clear()
    }
}