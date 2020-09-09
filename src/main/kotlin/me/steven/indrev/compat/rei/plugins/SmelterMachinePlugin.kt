package me.steven.indrev.compat.rei.plugins

import me.steven.indrev.recipes.machines.SmelterRecipe
import net.minecraft.util.Identifier

class SmelterMachinePlugin(recipe: SmelterRecipe, category: Identifier) : BaseMachinePlugin(recipe, category) {
    /*private val outputPreview: MutableList<EntryStack> =
        mutableListOf(EntryStack.create(recipe.outputs))

    private val output = outputPreview.also {
        it.add(EntryStack.create(recipe.fluid.rawFluid))
    }

    private val input: MutableList<MutableList<EntryStack>> =
        recipe.previewInputs.map { preview ->
            preview.matchingStacksClient.map { stack -> EntryStack.create(stack) }.toMutableList()
        }.toMutableList()

    override fun getOutputEntries(): MutableList<EntryStack> = outputPreview

    override fun getResultingEntries(): MutableList<MutableList<EntryStack>> =
        CollectionUtils.map<EntryStack, MutableList<EntryStack>>(output) { o: EntryStack -> mutableListOf(o) }.toMutableList()

    override fun getInputEntries(): MutableList<MutableList<EntryStack>> = input*/
    //TODO
}
