package me.steven.indrev.compat.rei.plugins

import me.shedaniel.rei.api.EntryStack
import me.shedaniel.rei.api.TransferRecipeDisplay
import me.shedaniel.rei.server.ContainerInfo
import me.steven.indrev.recipes.machines.IRFluidRecipe
import me.steven.indrev.recipes.machines.IRRecipe
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.util.Identifier
import java.util.*

open class IRMachinePlugin(val recipe: IRRecipe, private val category: Identifier) : TransferRecipeDisplay {

    private val outputPreview: MutableList<EntryStack> = mutableListOf()
    private val output: MutableList<EntryStack> = mutableListOf()
    private val input: MutableList<MutableList<EntryStack>> = mutableListOf()

    init {
        input.addAll(recipe.input.map { (ingredient, count) ->
            ingredient.matchingStacksClient.map { stack -> EntryStack.create(ItemStack(stack.item, count)) }.toMutableList()
        })
        outputPreview.addAll(recipe.outputs.map { (stack, _) -> EntryStack.create(stack) }.toMutableList())
        if (recipe is IRFluidRecipe) {
            val fluidInput = recipe.fluidInput
            if (fluidInput != null)
                input.addAll(mutableListOf(mutableListOf(EntryStack.create(fluidInput.rawFluid, fluidInput.amount().asInt(1000)))))
            val fluidOutput = recipe.fluidOutput
            if (fluidOutput != null)
                output.add(EntryStack.create(fluidOutput.rawFluid, fluidOutput.amount().asInt(1000)))
        }
        output.addAll(outputPreview)
    }

    override fun getRecipeCategory(): Identifier = category

    override fun getRecipeLocation(): Optional<Identifier> = Optional.ofNullable(recipe).map { it.id }

    override fun getRequiredEntries(): MutableList<MutableList<EntryStack>> = input

    override fun getOrganisedInputEntries(
        p0: ContainerInfo<ScreenHandler>?,
        p1: ScreenHandler?
    ): MutableList<MutableList<EntryStack>> = input

    override fun getInputEntries(): MutableList<MutableList<EntryStack>> = input

    override fun getOutputEntries(): MutableList<EntryStack> = outputPreview

    override fun getResultingEntries(): MutableList<MutableList<EntryStack>> = output.map { mutableListOf(it) }.toMutableList()

    override fun getWidth(): Int = 1

    override fun getHeight(): Int = 1
}