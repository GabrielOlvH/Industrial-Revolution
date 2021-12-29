package me.steven.indrev.recipes.machines

import me.steven.indrev.recipes.machines.entries.InputEntry
import me.steven.indrev.recipes.machines.entries.OutputEntry
import me.steven.indrev.utils.IRFluidAmount
import me.steven.indrev.utils.identifier
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier

class SmelterRecipe(
    override val identifier: Identifier,
    override val input: Array<InputEntry>,
    override val outputs: Array<OutputEntry>,
    override val fluidOutput: Array<IRFluidAmount>,
    override val ticks: Int,
) : IRFluidRecipe() {
    override val fluidInput: Array<IRFluidAmount> = emptyArray()

    override fun getType(): IRRecipeType<*> = TYPE

    override fun fits(width: Int, height: Int): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    companion object {
        val IDENTIFIER = identifier("smelter")
        val TYPE = IRRecipeType<SmelterRecipe>(IDENTIFIER)
        val SERIALIZER = Serializer()

        class Serializer : IRFluidRecipeSerializer<SmelterRecipe>({ id, ingredients, output, _, fluidOutput, ticks -> SmelterRecipe(id, ingredients, output,
            fluidOutput, ticks) })
    }
}