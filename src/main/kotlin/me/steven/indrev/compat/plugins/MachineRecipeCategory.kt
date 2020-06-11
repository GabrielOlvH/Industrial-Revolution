package me.steven.indrev.compat.plugins

import it.unimi.dsi.fastutil.ints.IntList
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.EntryStack
import me.shedaniel.rei.api.TransferRecipeCategory
import me.shedaniel.rei.api.widgets.Widgets
import me.shedaniel.rei.gui.entries.RecipeEntry
import me.shedaniel.rei.gui.entries.SimpleRecipeEntry
import me.shedaniel.rei.gui.widget.Widget
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.resource.language.I18n
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier


class MachineRecipeCategory(
    private val identifier: Identifier?,
    private val logo: EntryStack?,
    private val categoryName: String?
) : TransferRecipeCategory<MachinePlugin> {

    override fun renderRedSlots(
        matrices: MatrixStack,
        widgets: List<Widget?>?,
        bounds: Rectangle,
        display: MachinePlugin?,
        redSlots: IntList
    ) {
        val startPoint = Point(bounds.centerX - 41, bounds.centerY - 27)
        matrices.push()
        matrices.translate(0.0, 0.0, 400.0)
        if (redSlots.contains(0)) {
            DrawableHelper.fill(
                matrices,
                startPoint.x + 1,
                startPoint.y + 1,
                startPoint.x + 1 + 16,
                startPoint.y + 1 + 16,
                1090453504
            )
        }
        matrices.pop()
    }

    override fun setupDisplay(recipeDisplay: MachinePlugin, bounds: Rectangle): MutableList<Widget> {
        val startPoint = Point(bounds.centerX - 41, bounds.centerY - 27)
        val widgets: MutableList<Widget> = mutableListOf(Widgets.createRecipeBase(bounds))
        widgets.add(Widgets.createArrow(Point(startPoint.x + 24, startPoint.y + 18)))
        val input: List<List<EntryStack>> = recipeDisplay.inputEntries
        widgets.add(Widgets.createSlot(Point(startPoint.x + 1, startPoint.y + 19)).entries(input[0]))
        if (input.size > 1) widgets.add(
            Widgets.createSlot(Point(startPoint.x - 17, startPoint.y + 19)).entries(input[1])
        )
        widgets.add(
            Widgets.createSlot(Point(startPoint.x + 61, startPoint.y + 19)).entries(recipeDisplay.outputEntries)
        )
        return widgets
    }

    override fun getSimpleRenderer(recipe: MachinePlugin): RecipeEntry {
        return SimpleRecipeEntry.create(listOf(recipe.inputEntries[0]), recipe.outputEntries)
    }

    override fun getDisplayHeight(): Int {
        return 49
    }

    override fun getIdentifier(): Identifier? {
        return identifier
    }

    override fun getLogo(): EntryStack? {
        return logo
    }

    override fun getCategoryName(): String? {
        return I18n.translate(categoryName)
    }
}