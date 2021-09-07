package me.steven.indrev.recipes

import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import me.steven.indrev.utils.IRFluidAmount
import me.steven.indrev.utils.IRFluidTank
import me.steven.indrev.utils.asMutableList
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Recipe
import net.minecraft.server.world.ServerWorld

interface IRecipeGetter<T : Recipe<Inventory>>  {
    fun getMatchingRecipe(world: ServerWorld, itemStack: ItemStack): Collection<T>

    fun getMatchingRecipe(world: ServerWorld, fluidInput: FluidVariant): Collection<T>

    fun getMatchingRecipe(world: ServerWorld, stacks: List<ItemStack>, fluids: List<IRFluidTank>): Collection<T> {
        return when {
            stacks.isEmpty() && fluids.isEmpty() -> emptyList()
            stacks.isEmpty() -> fluids.flatMap { getMatchingRecipe(world, it.resource) }
            fluids.isEmpty() -> stacks.flatMap { getMatchingRecipe(world, it) }
            else -> stacks.flatMap { getMatchingRecipe(world, it) }.asMutableList().also { results ->
                results.addAll(fluids.flatMap { getMatchingRecipe(world, it.resource) })
            }
        }
    }
}