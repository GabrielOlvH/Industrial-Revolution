package me.steven.indrev.recipes.machines

import me.steven.indrev.recipes.machines.entries.InputEntry
import me.steven.indrev.recipes.machines.entries.OutputEntry
import me.steven.indrev.utils.identifier
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier

class LaserRecipe(
    override val identifier: Identifier,
    override val input: Array<InputEntry>,
    override val outputs: Array<OutputEntry>,
    override val ticks: Int
) : IRRecipe {

    override fun getType(): IRRecipeType<*> = TYPE

    override fun fits(width: Int, height: Int): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    companion object {
        val TYPE = IRRecipeType<LaserRecipe>()
        val SERIALIZER = Serializer()
        val IDENTIFIER = identifier("laser")

        class Serializer : IRRecipe.IRRecipeSerializer<LaserRecipe>({ id, input, output, ticks -> LaserRecipe(id,  input, output, ticks) })
    }
}