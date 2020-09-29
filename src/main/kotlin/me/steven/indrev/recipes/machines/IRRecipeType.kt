package me.steven.indrev.recipes.machines

import me.steven.indrev.recipes.IRecipeGetter
import me.steven.indrev.utils.getAllOfType
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.RecipeType
import net.minecraft.server.world.ServerWorld

class IRRecipeType<T : IRRecipe> : IRecipeGetter<T>, RecipeType<T> {

    private val recipeCache: MutableMap<Item, Set<T>> = mutableMapOf()

    override fun getMatchingRecipe(world: ServerWorld, item: ItemStack): Set<T> {
        if (recipeCache.contains(item.item)) return recipeCache[item.item]!!
        val matches = world.recipeManager.getAllOfType(this).values
            .filter { recipe -> recipe.input.any { it.ingredient.test(item) } }.toHashSet()
        recipeCache[item.item] = matches
        return matches
    }
}