package me.steven.indrev.compat.plugins

import com.mojang.blaze3d.systems.RenderSystem
import it.unimi.dsi.fastutil.ints.IntList
import me.shedaniel.math.api.Point
import me.shedaniel.math.api.Rectangle
import me.shedaniel.rei.api.EntryStack
import me.shedaniel.rei.api.TransferRecipeCategory
import me.shedaniel.rei.gui.entries.RecipeEntry
import me.shedaniel.rei.gui.entries.SimpleRecipeEntry
import me.shedaniel.rei.gui.widget.EntryWidget
import me.shedaniel.rei.gui.widget.RecipeArrowWidget
import me.shedaniel.rei.gui.widget.RecipeBaseWidget
import me.shedaniel.rei.gui.widget.Widget
import net.minecraft.client.gui.DrawableHelper
import net.minecraft.client.resource.language.I18n
import net.minecraft.util.Identifier
import java.util.function.Supplier


class MachineRecipeCategory(
    private val identifier: Identifier?,
    private val logo: EntryStack?,
    private val categoryName: String?
) : TransferRecipeCategory<MachinePlugin> {

    override fun renderRedSlots(widgets: MutableList<Widget>?, bounds: Rectangle, p2: MachinePlugin?, redSlots: IntList) {
        val startPoint = Point(bounds.centerX - 41, bounds.centerY - 27)
        RenderSystem.translatef(0f, 0f, 400f)
        if (redSlots.contains(0)) {
            DrawableHelper.fill(startPoint.x + 1, startPoint.y + 1, startPoint.x + 1 + 16, startPoint.y + 1 + 16, 1090453504)
        }
        RenderSystem.translatef(0f, 0f, -400f)
    }

    override fun setupDisplay(recipeDisplaySupplier: Supplier<MachinePlugin>, bounds: Rectangle): MutableList<Widget> {
        val startPoint = Point(bounds.centerX - 41, bounds.centerY - 27)
        val widgets: MutableList<Widget> = mutableListOf(RecipeBaseWidget(bounds))
        widgets.add(RecipeArrowWidget(startPoint.x + 24, startPoint.y + 18, true))
        val input: List<List<EntryStack>> = recipeDisplaySupplier.get().inputEntries
        widgets.add(EntryWidget.create(startPoint.x + 1, startPoint.y + 19).entries(input[0]))
        if (input.size > 1) widgets.add(EntryWidget.create(startPoint.x - 17, startPoint.y + 19).entries(input[1]))
        widgets.add(EntryWidget.create(startPoint.x + 61, startPoint.y + 19).entries(recipeDisplaySupplier.get().outputEntries))
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