package me.steven.indrev.recipes.machines

import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import me.steven.indrev.recipes.machines.entries.InputEntry
import me.steven.indrev.recipes.machines.entries.OutputEntry
import me.steven.indrev.utils.identifier
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.util.Identifier

class CondenserRecipe(
    override val identifier: Identifier,
    override val input: Array<InputEntry>,
    override val outputs: Array<OutputEntry>,
    override val fluidInput: FluidVolume,
    override val ticks: Int,
) : IRFluidRecipe() {
    override val fluidOutput: FluidVolume? = null

    override fun getType(): RecipeType<*> = TYPE

    override fun fits(width: Int, height: Int): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    companion object {
        val IDENTIFIER = identifier("condenser")
        val TYPE = object : RecipeType<CondenserRecipe> {}
        val SERIALIZER = Serializer()

        class Serializer : IRFluidRecipeSerializer<CondenserRecipe>({ id, ingredients, output, fluidInput, _, ticks -> CondenserRecipe(id, ingredients, output, fluidInput!!, ticks) })
    }
}