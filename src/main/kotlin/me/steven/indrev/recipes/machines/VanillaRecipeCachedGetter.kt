package me.steven.indrev.recipes.machines

import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import me.steven.indrev.recipes.IRecipeGetter
import me.steven.indrev.utils.getAllOfType
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeType
import net.minecraft.server.world.ServerWorld

class VanillaRecipeCachedGetter<T : Recipe<Inventory>>(private val type: RecipeType<T>) : IRecipeGetter<T> {

    private val recipeCache: MutableMap<Item, Set<T>> = hashMapOf()

    override fun getMatchingRecipe(world: ServerWorld, itemStack: ItemStack, fluidInput: FluidKey?): Set<T> {
        if (recipeCache.contains(itemStack.item)) return recipeCache[itemStack.item]!!
        val matches = world.recipeManager.getAllOfType(type).values
            .filter { recipe -> recipe.matches(SimpleInventory(itemStack), world) }.toSet()
        recipeCache[itemStack.item] = matches
        return matches
    }

    companion object {
        val SMELTING = VanillaRecipeCachedGetter(RecipeType.SMELTING)
        val SMOKING = VanillaRecipeCachedGetter(RecipeType.SMOKING)
        val BLASTING = VanillaRecipeCachedGetter(RecipeType.BLASTING)
    }
}