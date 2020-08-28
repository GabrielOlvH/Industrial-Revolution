package me.steven.indrev.compat.rei.plugins

import me.shedaniel.rei.api.EntryStack
import me.shedaniel.rei.utils.CollectionUtils
import me.steven.indrev.recipes.machines.FluidInfuserRecipe
import net.minecraft.util.Identifier

class FluidInfuserMachinePlugin(recipe: FluidInfuserRecipe, category: Identifier) : BaseMachinePlugin(recipe, category) {
    private val outputPreview: MutableList<EntryStack> =
        mutableListOf(EntryStack.create(recipe.output))
    private val output = outputPreview.toMutableList().also {
        it.add(EntryStack.create(recipe.outputFluid.rawFluid))
    }
    private val input: MutableList<MutableList<EntryStack>> =
        recipe.previewInputs.map { preview ->
            preview.matchingStacksClient.map { stack -> EntryStack.create(stack) }.toMutableList()
        }.toMutableList().also {
            it.add(mutableListOf(EntryStack.create(recipe.inputFluid.rawFluid)))
        }
    override fun getOutputEntries(): MutableList<EntryStack> = outputPreview

    override fun getResultingEntries(): MutableList<MutableList<EntryStack>> =
        CollectionUtils.map<EntryStack, MutableList<EntryStack>>(output) { o: EntryStack -> mutableListOf(o) }.toMutableList()

    override fun getInputEntries(): MutableList<MutableList<EntryStack>> = input
}