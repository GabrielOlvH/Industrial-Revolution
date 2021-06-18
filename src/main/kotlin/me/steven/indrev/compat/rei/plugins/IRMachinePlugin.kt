package me.steven.indrev.compat.rei.plugins

import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.display.Display
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.util.EntryIngredients
import me.steven.indrev.recipes.machines.IRFluidRecipe
import me.steven.indrev.recipes.machines.IRRecipe
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import java.util.*

open class IRMachinePlugin(val recipe: IRRecipe)
    : Display {


    override fun getInputEntries(): List<EntryIngredient> = getInputs(recipe)

    override fun getOutputEntries(): List<EntryIngredient> = getOutputs(recipe)

    override fun getCategoryIdentifier(): CategoryIdentifier<*> {
        return CategoryIdentifier.of<IRMachinePlugin>(recipe.type.id)
    }

    override fun getDisplayLocation(): Optional<Identifier> {
        return Optional.of(recipe.id)
    }

    companion object {
        fun getInputs(recipe: IRRecipe): List<EntryIngredient> {
            val list = mutableListOf<EntryIngredient>()
            if (recipe is IRFluidRecipe) {
                val fluidInput = recipe.fluidInput
                if (fluidInput != null)
                    list.addAll(mutableListOf(EntryIngredients.of(fluidInput.rawFluid, 81000)))
            }
            list.addAll(recipe.input.map { (ingredient, count) ->
                ingredient.matchingStacksClient.map { stack -> EntryIngredients.of(ItemStack(stack.item, count)) }.toMutableList()
            }.flatten())
            return list
        }

        fun getOutputs(recipe: IRRecipe): List<EntryIngredient> {
            val list = mutableListOf<EntryIngredient>()
            list.addAll(recipe.outputs.map { (stack, _) -> EntryIngredients.of(stack) }.toMutableList())
            if (recipe is IRFluidRecipe) {
                val fluidOutput = recipe.fluidOutput
                if (fluidOutput != null)
                    list.add(EntryIngredients.of(fluidOutput.rawFluid, 81000))
            }

            return list
        }
    }

}