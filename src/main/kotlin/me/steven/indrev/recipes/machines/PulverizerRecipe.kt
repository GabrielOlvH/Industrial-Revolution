package me.steven.indrev.recipes.machines

import me.steven.indrev.recipes.machines.entries.InputEntry
import me.steven.indrev.recipes.machines.entries.OutputEntry
import me.steven.indrev.utils.identifier
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier

class PulverizerRecipe(
    override val identifier: Identifier,
    override val input: Array<InputEntry>,
    override val outputs: Array<OutputEntry>,
    override val ticks: Int
) : IRRecipe {

    override fun getType(): IRRecipeType<*> = TYPE

    override fun fits(width: Int, height: Int): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    companion object {
        val IDENTIFIER = identifier("pulverize")
        val TYPE = IRRecipeType<PulverizerRecipe>(IDENTIFIER)
        val SERIALIZER = Serializer()

        class Serializer : IRRecipe.IRRecipeSerializer<PulverizerRecipe>({ id, input, output, ticks -> PulverizerRecipe(id,  input, output, ticks) })
    }
}