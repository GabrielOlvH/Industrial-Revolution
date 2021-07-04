package me.steven.indrev.recipes

import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import me.steven.indrev.utils.asMutableList
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Recipe
import net.minecraft.server.world.ServerWorld

interface IRecipeGetter<T : Recipe<Inventory>>  {
    fun getMatchingRecipe(world: ServerWorld, itemStack: ItemStack): Collection<T>

    fun getMatchingRecipe(world: ServerWorld, fluidInput: FluidKey): Collection<T>

    fun getMatchingRecipe(world: ServerWorld, stacks: List<ItemStack>, fluids: List<FluidVolume>): Collection<T> {
        return when {
            stacks.isEmpty() && fluids.isEmpty() -> emptyList()
            stacks.isEmpty() -> fluids.flatMap { getMatchingRecipe(world, it.fluidKey) }
            fluids.isEmpty() -> stacks.flatMap { getMatchingRecipe(world, it) }
            else -> stacks.flatMap { getMatchingRecipe(world, it) }.asMutableList().also { results ->
                results.addAll(fluids.flatMap { getMatchingRecipe(world, it.fluidKey) })
            }
        }
    }
}