package me.steven.indrev.recipes

import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Recipe
import net.minecraft.server.world.ServerWorld

interface IRecipeGetter<T : Recipe<Inventory>>  {
    fun getMatchingRecipe(world: ServerWorld, itemStack: ItemStack): Collection<T>

    fun getMatchingRecipe(world: ServerWorld, fluidInput: FluidKey): Collection<T>

    fun getMatchingRecipe(world: ServerWorld, stacks: List<ItemStack>, fluids: List<FluidVolume>): Collection<T> {
        if (stacks.isEmpty() && fluids.isEmpty()) return emptyList()
        else if (stacks.isEmpty()) return fluids.flatMap { getMatchingRecipe(world, it.fluidKey) }
        else if (fluids.isEmpty()) return stacks.flatMap { getMatchingRecipe(world, it) }
        else return listOf(fluids.flatMap { getMatchingRecipe(world, it.fluidKey) }, stacks.flatMap { getMatchingRecipe(world, it) }).flatten()
    }
}