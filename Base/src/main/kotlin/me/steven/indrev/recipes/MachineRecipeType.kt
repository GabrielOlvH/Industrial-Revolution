package me.steven.indrev.recipes

import net.minecraft.inventory.Inventory
import net.minecraft.recipe.Recipe
import net.minecraft.recipe.RecipeType

class MachineRecipeType : RecipeType<Recipe<Inventory>> {
    val provider = MachineRecipeProvider(this)
}