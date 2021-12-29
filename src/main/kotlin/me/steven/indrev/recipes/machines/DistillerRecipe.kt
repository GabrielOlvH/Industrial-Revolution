package me.steven.indrev.recipes.machines

import me.steven.indrev.recipes.machines.entries.InputEntry
import me.steven.indrev.recipes.machines.entries.OutputEntry
import me.steven.indrev.utils.IRFluidAmount
import me.steven.indrev.utils.identifier
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier

class DistillerRecipe(
    override val identifier: Identifier,
    override val input: Array<InputEntry>,
    override val outputs: Array<OutputEntry>,
    override val fluidInput: Array<IRFluidAmount>,
    override val ticks: Int,
) : IRFluidRecipe() {
    override val fluidOutput: Array<IRFluidAmount> = emptyArray()

    override fun getType(): IRRecipeType<*> = TYPE

    override fun fits(width: Int, height: Int): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    companion object {
        val IDENTIFIER = identifier("distiller")
        val TYPE = IRRecipeType<DistillerRecipe>(IDENTIFIER)
        val SERIALIZER = Serializer()

        class Serializer : IRFluidRecipeSerializer<DistillerRecipe>({ id, ingredients, output, fluidInput, _, ticks -> DistillerRecipe(id, ingredients, output, fluidInput!!, ticks) })
    }
}