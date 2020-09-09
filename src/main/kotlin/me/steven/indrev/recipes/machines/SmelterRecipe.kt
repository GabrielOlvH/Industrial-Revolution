package me.steven.indrev.recipes.machines

import alexiil.mc.lib.attributes.fluid.volume.FluidVolume
import me.steven.indrev.recipes.machines.entries.InputEntry
import me.steven.indrev.recipes.machines.entries.OutputEntry
import me.steven.indrev.utils.identifier
import net.minecraft.recipe.RecipeSerializer
import net.minecraft.recipe.RecipeType
import net.minecraft.util.Identifier

class SmelterRecipe(
    override val identifier: Identifier,
    override val input: Array<InputEntry>,
    override val outputs: Array<OutputEntry>,
    override val fluidOutput: FluidVolume,
    override val ticks: Int,
) : IRFluidRecipe() {
    override val fluidInput: FluidVolume? = null

    override fun getType(): RecipeType<*> = TYPE

    override fun fits(width: Int, height: Int): Boolean = true

    override fun getSerializer(): RecipeSerializer<*> = SERIALIZER

    companion object {
        val IDENTIFIER = identifier("smelter")
        val TYPE = object : RecipeType<SmelterRecipe> {}
        val SERIALIZER = Serializer()

        class Serializer : IRFluidRecipeSerializer<SmelterRecipe>({ id, ingredients, output, _, fluidOutput, ticks -> SmelterRecipe(id, ingredients, output, fluidOutput!!, ticks) })
    }
}