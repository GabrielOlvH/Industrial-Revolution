package me.steven.indrev.compat.rei.categories

import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.EntryStack
import me.shedaniel.rei.api.widgets.Widgets
import me.shedaniel.rei.gui.widget.Widget
import me.steven.indrev.compat.rei.plugins.IRMachinePlugin
import net.minecraft.util.Identifier

class IRModuleCraftingRecipeCategory(
    identifier: Identifier,
    logo: EntryStack,
    categoryName: String
) : IRMachineRecipeCategory(identifier, logo, categoryName) {

    private val slotLayout = hashMapOf<Int, Array<Point>>()

    init {

        slotLayout[1] = arrayOf(Point(2 * 18, 0))
        slotLayout[2] = arrayOf(
            Point(0, 2 * 18),
            Point(4 * 18, 2 * 18)
        )
        slotLayout[3] = arrayOf(
            Point(2 * 18, 0),
            Point(0, 4 * 17),
            Point(4 * 18, 4 * 17)
        )
        slotLayout[4] = arrayOf(
            Point(2 * 18, 0),
            Point(0, 2 * 18),
            Point(4 * 18, 2 * 18),
            Point(2 * 18, 4 * 18)
        )
        slotLayout[5] = arrayOf(
            Point(2 * 18, 0),
            Point(0, 2 * 18),
            Point(4 * 18, 2 * 18),
            Point(1 * 14, 4 * 18),
            Point(3 * 20, 4 * 18)
        )
        slotLayout[6] = arrayOf(
            Point(2 * 18, 0),
            Point(0 * 18, 1 * 18),
            Point(4 * 18, 1 * 18),
            Point(0 * 14, 3 * 18),
            Point(4 * 18, 3 * 18),
            Point(2 * 18, 4 * 18)
        )
    }

    override fun setupDisplay(recipeDisplay: IRMachinePlugin, bounds: Rectangle): MutableList<Widget> {
        val recipe = recipeDisplay.recipe
        val startPoint = Point(bounds.centerX - 43, bounds.centerY - 45)
        val widgets: MutableList<Widget> = listOf(Widgets.createCategoryBase(bounds)).toMutableList()
        val input = recipeDisplay.inputEntries
        val outputs = recipe.outputs.map { EntryStack.create(it.stack) }
        slotLayout[input.size]?.forEachIndexed { index, point ->
            widgets.add(Widgets.createSlot(startPoint + point).entries(input[index]))
        }
        widgets.add(Widgets.createResultSlotBackground(startPoint + Point(2 * 18, 2 * 18)))
        widgets.add(Widgets.createSlot(startPoint + Point(2 * 18, 2 * 18)).entry(outputs[0]).disableBackground().markOutput())
        return widgets
    }

    private operator fun Point.plus(other: Point) = Point(x + other.x, y + other.y)

    override fun getDisplayHeight(): Int = 120

}