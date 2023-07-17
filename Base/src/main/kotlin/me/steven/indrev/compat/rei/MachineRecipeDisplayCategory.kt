package me.steven.indrev.compat.rei

import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.Renderer
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.registry.display.DisplayCategory
import me.shedaniel.rei.api.common.category.CategoryIdentifier
import me.shedaniel.rei.api.common.util.EntryStacks
import me.steven.indrev.blockentities.crafting.PulverizerBlockEntity
import me.steven.indrev.blocks.PULVERIZER
import me.steven.indrev.recipes.MachineRecipeType
import me.steven.indrev.screens.machine.MachineScreenHandler
import me.steven.indrev.screens.widgets.WidgetBar
import me.steven.indrev.screens.widgets.WidgetSlot
import me.steven.indrev.utils.argb
import me.steven.indrev.utils.identifier
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.inventory.SimpleInventory
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos

class MachineRecipeDisplayCategory(val recipeType: MachineRecipeType, val setup: (MachineRecipeDisplay, Rectangle) -> MutableList<Widget>, val icon: Item) : DisplayCategory<MachineRecipeDisplay> {
    override fun getCategoryIdentifier(): CategoryIdentifier<MachineRecipeDisplay> {
        return CategoryIdentifier.of("indrev", Registries.RECIPE_TYPE.getId(recipeType.provider.type)!!.path)
    }

    override fun getTitle(): Text {
        return Text.translatable("indrev.rei.title.${Registries.RECIPE_TYPE.getId(recipeType.provider.type)!!.path}")
    }

    override fun getIcon(): Renderer {
        return EntryStacks.of(icon)
    }

    override fun getDisplayWidth(display: MachineRecipeDisplay?): Int = 150

    override fun getDisplayHeight(): Int = 86

    override fun setupDisplay(display: MachineRecipeDisplay, bounds: Rectangle): MutableList<Widget> {
        return setup(display, bounds)
    }
}