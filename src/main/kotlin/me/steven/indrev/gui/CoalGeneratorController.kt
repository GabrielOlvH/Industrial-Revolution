package me.steven.indrev.gui

import io.github.cottonmc.cotton.gui.CottonCraftingController
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.gui.widgets.FuelWidget
import net.minecraft.container.BlockContext
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.recipe.RecipeType


class CoalGeneratorController(syncId: Int, playerInventory: PlayerInventory, blockContext: BlockContext) :
    CottonCraftingController(RecipeType.SMELTING, syncId, playerInventory, getBlockInventory(blockContext), getBlockPropertyDelegate(blockContext)) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        root.setSize(200, 100)

        val itemSlot = WItemSlot.of(blockInventory, 0)
        root.add(itemSlot, 4, 1)

        root.add(createPlayerInventoryPanel(), 0, 3)

        root.add(FuelWidget(propertyDelegate), 3, 1)

        root.validate(this)
    }
}