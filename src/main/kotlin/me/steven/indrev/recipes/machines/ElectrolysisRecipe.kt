package me.steven.indrev.recipes.machines

import me.steven.indrev.recipes.machines.entries.InputEntry
import me.steven.indrev.recipes.machines.entries.OutputEntry
import me.steven.indrev.utils.IRFluidAmount
import me.steven.indrev.utils.identifier
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier

class ElectrolysisRecipe(
    override val identifier: Identifier,
    override val input: Array<InputEntry>,
    override val outputs: Array<OutputEntry>,
    override val fluidInput: Array<IRFluidAmount>,
    override val fluidOutput: Array<IRFluidAmount>,
    override val ticks: Int
) : IRFluidRecipe() {

    override fun getType(): IRRecipeType<*> = TYPE

    override fun fits(width: Int, height: Int): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    companion object {
        val IDENTIFIER = identifier("electrolysis")
        val TYPE = IRRecipeType<ElectrolysisRecipe>(IDENTIFIER)
        val SERIALIZER = Serializer()

        class Serializer : IRFluidRecipeSerializer<ElectrolysisRecipe>({ id, ingredients, output, fluidInput, fluidOutput, ticks -> ElectrolysisRecipe(id, ingredients, output, fluidInput, fluidOutput, ticks) })
    }
}