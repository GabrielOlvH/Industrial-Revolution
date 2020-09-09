package me.steven.indrev.compat.rei.plugins

import me.steven.indrev.recipes.machines.CondenserRecipe
import net.minecraft.util.Identifier

class CondenserMachinePlugin(recipe: CondenserRecipe, category: Identifier) : BaseMachinePlugin(recipe, category) {
    /*private val outputPreview: MutableList<EntryStack> =
        mutableListOf(EntryStack.create(recipe.outputs))

    private val input: MutableList<MutableList<EntryStack>> =
        recipe.previewInputs.map { preview ->
            preview.matchingStacksClient.map { stack -> EntryStack.create(stack) }.toMutableList()
        }.toMutableList().also {
            it.add(mutableListOf(EntryStack.create(recipe.fluid.rawFluid)))
        }
    override fun getOutputEntries(): MutableList<EntryStack> = outputPreview

    override fun getInputEntries(): MutableList<MutableList<EntryStack>> = input*/
    //TODO
}