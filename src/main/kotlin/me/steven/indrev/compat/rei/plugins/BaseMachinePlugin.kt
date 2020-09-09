package me.steven.indrev.compat.rei.plugins

import me.shedaniel.rei.api.EntryStack
import me.shedaniel.rei.api.TransferRecipeDisplay
import me.shedaniel.rei.server.ContainerInfo
import me.steven.indrev.recipes.machines.IRRecipe
import net.minecraft.item.ItemStack
import net.minecraft.screen.ScreenHandler
import net.minecraft.util.Identifier
import java.util.*

open class BaseMachinePlugin(val recipe: IRRecipe, private val category: Identifier) : TransferRecipeDisplay {

    private val output: MutableList<EntryStack> =
        recipe.outputs.map { (stack, _) -> EntryStack.create(stack) }.toMutableList()
    private val input: MutableList<MutableList<EntryStack>> =
            recipe.input.map { (ingredient, count) -> ingredient.matchingStacksClient.map { stack -> EntryStack.create(ItemStack(stack.item, count)) }.toMutableList() }.toMutableList()


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