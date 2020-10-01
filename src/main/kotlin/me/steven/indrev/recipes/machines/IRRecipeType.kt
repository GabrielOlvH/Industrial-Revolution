package me.steven.indrev.recipes.machines

import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import me.steven.indrev.recipes.IRecipeGetter
import me.steven.indrev.utils.getAllOfType
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeType
import net.minecraft.server.world.ServerWorld

class IRRecipeType<T : IRRecipe> : IRecipeGetter<T>, RecipeType<T> {

    private val recipeCache: MutableMap<Item, Set<T>> = mutableMapOf()
    private val fluidOnlyRecipeCache: MutableMap<FluidKey, Set<T>> = mutableMapOf()

    override fun getMatchingRecipe(world: ServerWorld, item: ItemStack, fluidInput: FluidKey?): Set<T> {
        if (recipeCache.contains(item.item)) return recipeCache[item.item]!!
        else if (item.isEmpty && fluidInput != null) {
            if (fluidOnlyRecipeCache.containsKey(fluidInput)) return fluidOnlyRecipeCache[fluidInput]!!
            val matches = world.recipeManager.getAllOfType(this).values
                .filter { recipe -> recipe is IRFluidRecipe && recipe.fluidInput != null && recipe.fluidInput!!.fluidKey == fluidInput }.toHashSet()
            fluidOnlyRecipeCache[fluidInput] = matches
            return matches
        }
        val matches = world.recipeManager.getAllOfType(this).values
            .filter { recipe -> recipe.input.any { it.ingredient.test(item) } }.toHashSet()
        recipeCache[item.item] = matches
        return matches
    }
}