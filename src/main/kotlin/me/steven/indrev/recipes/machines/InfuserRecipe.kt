package me.steven.indrev.recipes.machines

import me.steven.indrev.recipes.machines.entries.InputEntry
import me.steven.indrev.recipes.machines.entries.OutputEntry
import me.steven.indrev.utils.identifier
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.util.Identifier

class InfuserRecipe(
    override val identifier: Identifier,
    override val input: Array<InputEntry>,
    override val outputs: Array<OutputEntry>,
    override val ticks: Int
) : IRRecipe {
    override fun getType(): RecipeType<*> = TYPE

    override fun fits(width: Int, height: Int): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    companion object {
        val TYPE = object : RecipeType<InfuserRecipe> {}
        val SERIALIZER = Serializer()
        val IDENTIFIER = identifier("infuse")

        class Serializer : IRRecipe.IRRecipeSerializer<InfuserRecipe>({ id, input, output, ticks -> InfuserRecipe(id, input, output, ticks) })
    }
}