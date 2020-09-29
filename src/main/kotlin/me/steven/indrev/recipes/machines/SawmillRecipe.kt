package me.steven.indrev.recipes.machines

import me.steven.indrev.recipes.machines.entries.InputEntry
import me.steven.indrev.recipes.machines.entries.OutputEntry
import me.steven.indrev.utils.identifier
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier

class SawmillRecipe(
    override val identifier: Identifier,
    override val input: Array<InputEntry>,
    override val outputs: Array<OutputEntry>,
    override val ticks: Int
) : IRRecipe {

    override fun fits(width: Int, height: Int): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    override fun getType(): IRRecipeType<*> = TYPE

    companion object {
        val IDENTIFIER = identifier("sawmill")
        val TYPE = IRRecipeType<SawmillRecipe>()
        val SERIALIZER = IRRecipe.IRRecipeSerializer(::SawmillRecipe)
    }
}