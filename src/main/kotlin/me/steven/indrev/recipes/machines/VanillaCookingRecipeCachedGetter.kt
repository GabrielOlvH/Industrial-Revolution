package me.steven.indrev.recipes.machines

import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import me.steven.indrev.recipes.IRecipeGetter
import me.steven.indrev.utils.getAllOfType
import me.steven.indrev.utils.input
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.recipe.AbstractCookingRecipe
import net.minecraft.recipe.RecipeType
import net.minecraft.server.world.ServerWorld

class VanillaCookingRecipeCachedGetter<T : AbstractCookingRecipe>(private val type: RecipeType<T>) : IRecipeGetter<T> {

    private val recipeCache: Multimap<Item, T> = HashMultimap.create()

    override fun getMatchingRecipe(world: ServerWorld, itemStack: ItemStack): Collection<T> {
        if (recipeCache.containsKey(itemStack.item)) return recipeCache[itemStack.item]!!
        val matches = world.recipeManager.getAllOfType(type).values
            .filter { recipe -> recipe.input.test(itemStack) }
        recipeCache.putAll(itemStack.item, matches)
        return matches
    }

    override fun getMatchingRecipe(world: ServerWorld, fluidInput: FluidVariant): Collection<T> = emptyList()

    companion object {
        val SMELTING = VanillaCookingRecipeCachedGetter(RecipeType.SMELTING)
        val SMOKING = VanillaCookingRecipeCachedGetter(RecipeType.SMOKING)
        val BLASTING = VanillaCookingRecipeCachedGetter(RecipeType.BLASTING)
    }
}