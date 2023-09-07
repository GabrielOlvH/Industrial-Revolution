package me.steven.indrev.compat.rei

import me.shedaniel.math.Point
import me.shedaniel.math.Rectangle
import me.shedaniel.rei.api.client.gui.widgets.Slot
import me.shedaniel.rei.api.client.gui.widgets.Widget
import me.shedaniel.rei.api.client.gui.widgets.Widgets
import me.shedaniel.rei.api.client.plugins.REIClientPlugin
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry
import me.shedaniel.rei.api.common.util.EntryStacks
import me.shedaniel.rei.plugin.common.BuiltinPlugin
import me.steven.indrev.blocks.*
import me.steven.indrev.recipes.*
import me.steven.indrev.screens.machine.grid
import me.steven.indrev.screens.widgets.WidgetSlot
import me.steven.indrev.utils.*
import me.steven.indrev.utils.identifier
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element

class IndustrialRevolutionREIPlugin : REIClientPlugin {
    override fun registerCategories(registry: CategoryRegistry) {
        ELECTRIC_FURNACE.blockItems.forEach { item ->
            registry.addWorkstations(BuiltinPlugin.SMELTING, EntryStacks.of(item))
        }
        
        val pulverizerCategory = MachineRecipeDisplayCategory(PULVERIZER_RECIPE_TYPE, ::setupPulverizingDisplay, PULVERIZER.block.asItem())
        registry.add(pulverizerCategory)
        PULVERIZER.blockItems.forEach { item ->
            registry.addWorkstations(pulverizerCategory.categoryIdentifier, EntryStacks.of(item))
        }

        val alloySmelterCategory = MachineRecipeDisplayCategory(ALLOY_SMELTER_RECIPE_TYPE, ::setupAlloyDisplay, ALLOY_SMELTER.block.asItem())
        registry.add(alloySmelterCategory)
        ALLOY_SMELTER.blockItems.forEach { item ->
            registry.addWorkstations(alloySmelterCategory.categoryIdentifier, EntryStacks.of(item))
        }


        val chemicalInfuserCategory = MachineRecipeDisplayCategory(CHEMICAL_INFUSER_RECIPE_TYPE, ::setupBasicDisplay, CHEMICAL_INFUSER.block.asItem())
        registry.add(chemicalInfuserCategory)
        CHEMICAL_INFUSER.blockItems.forEach { item ->
            registry.addWorkstations(chemicalInfuserCategory.categoryIdentifier, EntryStacks.of(item))
        }

        val compressorCategory = MachineRecipeDisplayCategory(COMPRESSOR_RECIPE_TYPE, ::setupBasicDisplay, COMPRESSOR.block.asItem())
        registry.add(compressorCategory)
        COMPRESSOR.blockItems.forEach { item ->
            registry.addWorkstations(compressorCategory.categoryIdentifier, EntryStacks.of(item))
        }
    }

    override fun registerDisplays(registry: DisplayRegistry) {
        registry.registerRecipeFiller(MachineRecipe::class.java, PULVERIZER_RECIPE_TYPE, ::MachineRecipeDisplay)
        registry.registerRecipeFiller(MachineRecipe::class.java, COMPRESSOR_RECIPE_TYPE, ::MachineRecipeDisplay)
        registry.registerRecipeFiller(MachineRecipe::class.java, ALLOY_SMELTER_RECIPE_TYPE, ::MachineRecipeDisplay)
        registry.registerRecipeFiller(MachineRecipe::class.java, CHEMICAL_INFUSER_RECIPE_TYPE, ::MachineRecipeDisplay)
    }

    private fun setupBasicDisplay(display: MachineRecipeDisplay, bounds: Rectangle): MutableList<Widget> {
        val widgets = mutableListOf<Widget>(Widgets.createTexturedWidget(identifier("textures/gui/widgets/rei_background.png"), bounds, 0f, 0f, 150, 86))
        val recipe = display.recipe
        widgets.add(createArrow(bounds.offset(grid(3), grid(1)+9)))
        createSlot(widgets, bounds.offset(grid(2)-9, grid(1)+9), INPUT_COLOR, false).entries(recipe.itemInput[0].ingredient.matchingStacks.map { EntryStacks.of(it.item, recipe.itemInput[0].count) })
        createSlot(widgets, bounds.offset(grid(4)+9, grid(1)+9), OUTPUT_COLOR, true).entry(EntryStacks.of(recipe.itemOutput[0].item, recipe.itemOutput[0].count))
        return widgets
    }

    private fun setupPulverizingDisplay(display: MachineRecipeDisplay, bounds: Rectangle): MutableList<Widget> {
        val widgets = mutableListOf<Widget>(Widgets.createTexturedWidget(identifier("textures/gui/widgets/rei_background.png"), bounds, 0f, 0f, 150, 86))
        val recipe = display.recipe
        widgets.add(createArrow(bounds.offset(grid(3), grid(1))))
        createSlot(widgets, bounds.offset(grid(2)-9, grid(1)), INPUT_COLOR, false).entries(recipe.itemInput[0].ingredient.matchingStacks.map { EntryStacks.of(it.item, recipe.itemInput[0].count) })
        createSlot(widgets, bounds.offset(grid(4)+9, grid(1)), OUTPUT_COLOR, true).entry(EntryStacks.of(recipe.itemOutput[0].item, recipe.itemOutput[0].count))
        createSlot(widgets, bounds.offset(grid(4)+9, grid(2)+12), OUTPUT_COLOR, false).also {
            if (recipe.itemOutput.size > 1) {
                it.entry(EntryStacks.of(recipe.itemOutput[1].item, recipe.itemOutput[1].count))
            }
        }
        return widgets
    }

    private fun setupAlloyDisplay(display: MachineRecipeDisplay, bounds: Rectangle): MutableList<Widget> {
        val widgets = setupBasicDisplay(display, bounds)
        val recipe = display.recipe
        createSlot(widgets, bounds.offset(grid(1)-11, grid(1)+9), INPUT_COLOR, false).also {
            if (recipe.itemInput.size > 1) {
                it.entries(recipe.itemInput[1].ingredient.matchingStacks.map { EntryStacks.of(it.item, recipe.itemInput[1].count) })
            }
        }
        return widgets
    }

    private fun createArrow(p: Point): Widget {
        return Widgets.createTexturedWidget(identifier("textures/gui/widgets/widget_processing_full.png"), Rectangle(p.x, p.y, 18, 18), 0f, 0f, 18, 18)
    }

    private fun createSlot(widgets: MutableList<Widget>, point: Point, color: Int, big: Boolean): Slot  {
        val bg = object : Widget() {
            override fun children(): List<Element> {
                return emptyList()
            }

            override fun render(ctx: DrawContext, mouseX: Int, mouseY: Int, delta: Float) {
                val (alpha, red, green, blue) = color
                ctx.setShaderColor(red / 256f, green / 256f, blue / 256f, alpha / 256f)
                if (!big) {
                    ctx.drawTexture(WidgetSlot.MACHINE_SLOT_TEXTURE, point.x - (22 - 18) / 2, point.y - (22 - 18) / 2, 0f, 0f, 22, 22, 22, 22)
                } else {
                    ctx.drawTexture(WidgetSlot.MACHINE_BIG_SLOT_TEXTURE, point.x - 6, point.y - 6, 0f, 0f, 30, 30, 30, 30)
                }
            }

        }
        widgets.add(bg)
        val slot = Widgets.createSlot(Point(point.x+1, point.y+1)).backgroundEnabled(false)
        widgets.add(slot)
        return slot
    }

    fun Rectangle.offset(x: Int, y: Int) = Point(this.x + x, this.y + y)
}