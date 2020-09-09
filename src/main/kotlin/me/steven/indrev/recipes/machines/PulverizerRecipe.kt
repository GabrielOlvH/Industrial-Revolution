package me.steven.indrev.recipes.machines

import me.steven.indrev.recipes.machines.entries.InputEntry
import me.steven.indrev.recipes.machines.entries.OutputEntry
import me.steven.indrev.utils.identifier
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.util.Identifier

class PulverizerRecipe(
    override val identifier: Identifier,
    override val input: Array<InputEntry>,
    override val outputs: Array<OutputEntry>,
    override val ticks: Int
) : IRRecipe {

    override fun getType(): RecipeType<*> = TYPE

    override fun fits(width: Int, height: Int): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    companion object {
        val TYPE = object : RecipeType<PulverizerRecipe> {}
        val SERIALIZER = Serializer()
        val IDENTIFIER = identifier("pulverize")

        class Serializer : IRRecipe.IRRecipeSerializer<PulverizerRecipe>({ id, input, output, ticks -> PulverizerRecipe(id,  input, output, ticks) })
    }
}