package me.steven.indrev.recipes.machines

import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import me.steven.indrev.recipes.machines.entries.InputEntry
import me.steven.indrev.recipes.machines.entries.OutputEntry
import me.steven.indrev.utils.identifier
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.util.Identifier

class FluidInfuserRecipe(
    override val identifier: Identifier,
    override val input: Array<InputEntry>,
    override val outputs: Array<OutputEntry>,
    override val fluidInput: FluidVolume,
    override val fluidOutput: FluidVolume,
    override val ticks: Int,
) : IRFluidRecipe() {

    override fun getType(): IRRecipeType<*> = TYPE

    override fun fits(width: Int, height: Int): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    companion object {
        val IDENTIFIER = identifier("fluid_infuse")
        val TYPE = IRRecipeType<FluidInfuserRecipe>(IDENTIFIER)
        val SERIALIZER = Serializer()

        class Serializer : IRFluidRecipeSerializer<FluidInfuserRecipe>({ id, ingredients, output, fluidInput, fluidOutput, ticks -> FluidInfuserRecipe(id, ingredients, output, fluidInput!!, fluidOutput!!, ticks) })
    }
}