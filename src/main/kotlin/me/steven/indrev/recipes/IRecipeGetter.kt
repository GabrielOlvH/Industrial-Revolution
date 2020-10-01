package me.steven.indrev.recipes

import alexiil.mc.lib.attributes.fluid.volume.FluidKey
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.recipe.Recipe
import net.minecraft.server.world.ServerWorld

interface IRecipeGetter<T : Recipe<Inventory>>  {
    fun getMatchingRecipe(world: ServerWorld, item: ItemStack, fluidInput: FluidKey?): Set<T>
}