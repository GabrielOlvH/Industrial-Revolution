package me.steven.indrev.compat.rei.plugins

import me.shedaniel.rei.api.EntryStack
import me.shedaniel.rei.api.TransferRecipeDisplay
import me.shedaniel.rei.server.ContainerInfo
import net.minecraft.recipe.Recipe
import net.minecraft.screen.ScreenHandler
import net.minecraft.util.Identifier
import java.util.*

open class BaseMachinePlugin(val recipe: Recipe<*>, private val category: Identifier) : TransferRecipeDisplay {

    private val output: MutableList<EntryStack> =
        mutableListOf(EntryStack.create(recipe.output))
    private val input: MutableList<MutableList<EntryStack>> =
        recipe.previewInputs.map { preview -> preview.matchingStacksClient.map { stack -> EntryStack.create(stack) }.toMutableList() }.toMutableList()

    override fun getRecipeCategory(): Identifier = category

    override fun getRecipeLocation(): Optional<Identifier> = Optional.ofNullable(recipe).map { it.id }

    override fun getRequiredEntries(): MutableList<MutableList<EntryStack>> = input

    override fun getOrganisedInputEntries(
        p0: ContainerInfo<ScreenHandler>?,
        p1: ScreenHandler?
    ): MutableList<MutableList<EntryStack>> = input

    override fun getInputEntries(): MutableList<MutableList<EntryStack>> = input

    override fun getOutputEntries(): MutableList<EntryStack> = output

    override fun getWidth(): Int = 1

    override fun getHeight(): Int = 1
}