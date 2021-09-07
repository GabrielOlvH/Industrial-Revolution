package me.steven.indrev.compat.rei.plugins

import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.display.Display
import me.shedaniel.rei.api.common.entry.EntryIngredient
import me.shedaniel.rei.api.common.entry.EntryStack
import me.shedaniel.rei.api.common.util.EntryIngredients
import me.shedaniel.rei.api.common.util.EntryStacks
import me.steven.indrev.recipes.machines.IRFluidRecipe
import me.steven.indrev.recipes.machines.IRRecipe
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import java.util.*

open class IRMachinePlugin(val recipe: IRRecipe) : Display {

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
                //TODO properly support multiple inputs
                if (fluidInput.isNotEmpty())
                    list.addAll(mutableListOf(EntryIngredients.of(fluidInput[0].resource.fluid, 81000)))
            }
            list.addAll(recipe.input.map { (ingredient, count) ->
                val builder = EntryIngredient.builder()
                ingredient.matchingStacks.map { stack -> builder.add(EntryStacks.of(ItemStack(stack.item, count))) }
                builder.build()
            })
            return list
        }

        fun getOutputs(recipe: IRRecipe): List<EntryIngredient> {
            val list = mutableListOf<EntryIngredient>()
            list.addAll(recipe.outputs.map { (stack, _) -> EntryIngredients.of(stack) }.toMutableList())
            if (recipe is IRFluidRecipe) {
                val fluidOutput = recipe.fluidOutput

                //TODO properly support multiple outputs
                if (fluidOutput.isNotEmpty())
                    list.add(EntryIngredients.of(fluidOutput[0].resource.fluid, 81000))
            }

            return list
        }
    }

}