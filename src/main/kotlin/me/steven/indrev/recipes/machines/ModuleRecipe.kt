package me.steven.indrev.recipes.machines

import me.steven.indrev.recipes.machines.entries.InputEntry
import me.steven.indrev.recipes.machines.entries.OutputEntry
import me.steven.indrev.utils.identifier
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier

class ModuleRecipe(
    override val identifier: Identifier,
    override val input: Array<InputEntry>,
    override val outputs: Array<OutputEntry>,
    override val ticks: Int
) : IRRecipe {

    override fun getType(): IRRecipeType<*> = TYPE

    override fun fits(width: Int, height: Int): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    companion object {
        val TYPE = IRRecipeType<ModuleRecipe>()
        val SERIALIZER = Serializer()
        val IDENTIFIER = identifier("modules")

        class Serializer : IRRecipe.IRRecipeSerializer<ModuleRecipe>({ id, input, output, ticks -> ModuleRecipe(id,  input, output, ticks) })
    }
}