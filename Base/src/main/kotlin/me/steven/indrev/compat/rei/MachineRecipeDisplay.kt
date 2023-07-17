package me.steven.indrev.compat.rei

import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.display.Display
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.util.EntryIngredients
import me.shedaniel.rei.api.common.util.EntryStacks
import me.steven.indrev.recipes.MachineRecipe
import net.minecraft.registry.Registries

class MachineRecipeDisplay(val recipe: MachineRecipe) : Display {
    override fun getInputEntries(): MutableList<EntryIngredient> {
        val list = mutableListOf<EntryIngredient>()
        recipe.itemInput.forEach { itemInput -> list.add(EntryIngredients.ofIngredient(itemInput.ingredient)) }
        recipe.fluidInput.forEach { itemInput -> list.add(EntryIngredients.of(itemInput.fluid)) }
        return list
    }


    override fun getOutputEntries(): MutableList<EntryIngredient> {
        val list = mutableListOf<EntryIngredient>()
        recipe.itemOutput.forEach { itemInput -> list.add(EntryIngredients.of(itemInput.item)) }
        recipe.fluidOutput.forEach { itemInput -> list.add(EntryIngredients.of(itemInput.fluid)) }
        return list
    }

    override fun getCategoryIdentifier(): CategoryIdentifier<MachineRecipeDisplay> {
        return CategoryIdentifier.of("indrev", Registries.RECIPE_TYPE.getId(recipe.type)!!.path)
    }
}