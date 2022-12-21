package me.steven.indrev.recipes

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import me.steven.indrev.mixin.RecipeManagerAccessor
import net.minecraft.fluid.Fluid
import net.minecraft.inventory.Inventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.AbstractCookingRecipe
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeManager
import net.minecraft.recipe.RecipeType
import net.minecraft.recipe.SmeltingRecipe
import net.minecraft.util.Identifier
import java.util.Collections

class MachineRecipeProvider(val type: RecipeType<out Recipe<Inventory>>) {
    private val recipeCache: Multimap<Item, MachineRecipe> = HashMultimap.create()
    private val fluidOnlyRecipeCache: Multimap<Fluid, MachineRecipe> = HashMultimap.create()

    fun getMatchingRecipes(recipeManager: RecipeManager, stacks: List<ItemStack>, fluids: List<Fluid>): Collection<MachineRecipe> {
        val matches = mutableListOf<MachineRecipe>()
        stacks.forEach { stack -> matches.addAll(getMatchingRecipes(recipeManager, stack)) }
        fluids.forEach { fluid -> matches.addAll(getMatchingRecipes(recipeManager, fluid)) }
        return matches
    }

    fun getMatchingRecipes(recipeManager: RecipeManager, itemStack: ItemStack): Collection<MachineRecipe> {
        if (recipeCache.containsKey(itemStack.item)) return recipeCache.get(itemStack.item)

        val recipes = (recipeManager as RecipeManagerAccessor).recipes.getOrDefault(type, Collections.emptyMap()) as Map<Identifier, Recipe<Inventory>>

        val matches = ArrayList<MachineRecipe>(1)
        recipes.forEach { (_, recipe) ->
            when (recipe) {
                is MachineRecipe -> {
                    if (recipe.itemInput.any { it.ingredient.test(itemStack) })
                        matches.add(recipe)
                }
                is AbstractCookingRecipe -> {
                    if (recipe.ingredients.any { it.test(itemStack) }) {
                        matches.add(wrapRecipe(recipe))
                    }
                }
            }
        }

        recipeCache.putAll(itemStack.item, matches)

        return matches
    }

    fun getMatchingRecipes(recipeManager: RecipeManager, fluid: Fluid): Collection<MachineRecipe> {
        if (fluidOnlyRecipeCache.containsKey(fluid)) return fluidOnlyRecipeCache.get(fluid)

        val recipes = (recipeManager as RecipeManagerAccessor).recipes.getOrDefault(type, Collections.emptyMap()) as Map<Identifier, Recipe<Inventory>>

        val matches = ArrayList<MachineRecipe>(1)
        recipes.forEach { (_, recipe) ->
            if (recipe is MachineRecipe && recipe.fluidInput.any { it.fluid == fluid }) {
                matches.add(recipe)
            }
        }

        fluidOnlyRecipeCache.putAll(fluid, matches)

        return matches
    }

    fun clearCache() {
        recipeCache.clear()
        fluidOnlyRecipeCache.clear()
    }

    companion object {
        fun wrapRecipe(recipe: AbstractCookingRecipe): MachineRecipe {
            return MachineRecipe(
                recipe.id,
                recipe.type,
                recipe.ingredients.map { ing -> MachineRecipe.RecipeItemInput(ing, 1, 1.0) }.toTypedArray(),
                arrayOf(MachineRecipe.RecipeItemOutput(recipe.output.item, recipe.output.count, 1.0)),
                emptyArray(),
                emptyArray(),
                recipe.cookTime,
                1
            )
        }
    }
}