package me.steven.indrev.compat.plugins

import me.shedaniel.rei.api.EntryStack
import me.shedaniel.rei.api.TransferRecipeDisplay
import me.shedaniel.rei.server.ContainerInfo
import me.steven.indrev.compat.REIPlugin
import net.minecraft.container.Container
import net.minecraft.recipe.Recipe
import net.minecraft.util.Identifier
import java.util.*

class MachinePlugin(private val recipe: Recipe<*>) : TransferRecipeDisplay {

    private val output: MutableList<EntryStack> =
        mutableListOf(EntryStack.create(recipe.output))
    private val input: MutableList<MutableList<EntryStack>> =
        recipe.previewInputs.map { preview -> preview.matchingStacksClient.map { stack -> EntryStack.create(stack) }.toMutableList() }.toMutableList()

    override fun getRecipeCategory(): Identifier = REIPlugin.PULVERIZING

    override fun getRecipeLocation(): Optional<Identifier> = Optional.ofNullable(recipe).map { it.id }

    override fun getRequiredEntries(): MutableList<MutableList<EntryStack>> = input

    override fun getOrganisedInputEntries(p0: ContainerInfo<Container>?, p1: Container?): MutableList<MutableList<EntryStack>> = input

    override fun getInputEntries(): MutableList<MutableList<EntryStack>> = input

    override fun getOutputEntries(): MutableList<EntryStack> = output

    override fun getWidth(): Int = 1

    override fun getHeight(): Int = 1
}