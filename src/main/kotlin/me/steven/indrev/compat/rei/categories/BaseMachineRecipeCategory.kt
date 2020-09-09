package me.steven.indrev.compat.rei.categories

import it.unimi.dsi.fastutil.ints.IntList
import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.EntryStack
import me.shedaniel.rei.api.TransferRecipeCategory
import me.shedaniel.rei.api.widgets.Widgets
import me.shedaniel.rei.gui.entries.RecipeEntry
import me.shedaniel.rei.gui.entries.SimpleRecipeEntry
import me.shedaniel.rei.gui.widget.Widget
import me.steven.indrev.compat.rei.plugins.BaseMachinePlugin
import me.steven.indrev.recipes.machines.IRFluidRecipe
import me.steven.indrev.utils.createREIFluidWidget
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.resource.language.I18n
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier


open class BaseMachineRecipeCategory(
    private val identifier: Identifier,
    private val logo: EntryStack,
    private val categoryName: String
) : TransferRecipeCategory<BaseMachinePlugin> {

    override fun renderRedSlots(
        matrices: MatrixStack,
        widgets: List<Widget?>?,
        bounds: Rectangle,
        display: BaseMachinePlugin?,
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

    override fun setupDisplay(recipeDisplay: BaseMachinePlugin, bounds: Rectangle): MutableList<Widget> {
        val startPoint = Point(bounds.centerX - 41, bounds.centerY - 27)
        val widgets: MutableList<Widget> = mutableListOf(Widgets.createRecipeBase(bounds))
        widgets.add(Widgets.createArrow(Point(startPoint.x + 24, startPoint.y + 18)))
        val input = recipeDisplay.inputEntries
        widgets.add(Widgets.createSlot(Point(startPoint.x + 1, startPoint.y + 19)).entries(input[0]))
        if (input.size > 1)
            widgets.add(
                Widgets.createSlot(Point(startPoint.x - 17, startPoint.y + 19)).entries(input[1])
            )
        val recipe = recipeDisplay.recipe
        if (recipe is IRFluidRecipe) {
            if (recipe.fluidInput != null) {
                val inputFluidPoint = Point(startPoint.x - 20, startPoint.y)
                createREIFluidWidget(widgets, inputFluidPoint, recipe.fluidInput!!)
            }
            if (recipe.fluidOutput != null) {
                val outputFluidPoint = Point(startPoint.x + 80, startPoint.y)
                createREIFluidWidget(widgets, outputFluidPoint, recipe.fluidOutput!!)
            }
        }
        widgets.add(
            Widgets.createSlot(Point(startPoint.x + 61, startPoint.y + 19)).entries(recipeDisplay.outputEntries)
        )
        return widgets
    }

    override fun getSimpleRenderer(recipe: BaseMachinePlugin): RecipeEntry =
        SimpleRecipeEntry.create(listOf(recipe.inputEntries[0]), recipe.outputEntries)

    override fun getDisplayHeight(): Int = 49

    override fun getIdentifier(): Identifier? = identifier

    override fun getLogo(): EntryStack? = logo

    override fun getCategoryName(): String? = I18n.translate(categoryName)
}