package me.steven.indrev.gui.pulverizer

import io.github.cottonmc.cotton.gui.CottonCraftingController
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.gui.widgets.EnergyWidget
import me.steven.indrev.gui.widgets.ProcessWidget
import me.steven.indrev.recipes.PulverizerRecipe
import net.minecraft.container.BlockContext
import net.minecraft.entity.player.PlayerInventory

class PulverizerController(syncId: Int, playerInventory: PlayerInventory, blockContext: BlockContext) :
        CottonCraftingController(PulverizerRecipe.TYPE, syncId, playerInventory, getBlockInventory(blockContext), getBlockPropertyDelegate(blockContext)) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        root.setSize(150, 120)

        root.add(createPlayerInventoryPanel(), 0, 4)

        root.add(EnergyWidget(propertyDelegate), 0, 0, 16, 64)

        val inputSlot = WItemSlot.of(blockInventory, 0)
        root.add(inputSlot, 3, 2)
        inputSlot.setLocation((2.3 * 18).toInt(), (1.5 * 18).toInt())

        val processWidget = ProcessWidget(propertyDelegate)
        root.add(processWidget, 4, 2)
        processWidget.setLocation((3.5 * 18).toInt(), (1.5 * 18).toInt())

        val outputSlot = WItemSlot.outputOf(blockInventory, 1)
        outputSlot.isInsertingAllowed = false
        root.add(outputSlot, 6, 2)
        outputSlot.setLocation((5.5 * 18).toInt(), (1.5 * 18).toInt())

        val extraOutputSlot = WItemSlot.of(blockInventory, 2)
        extraOutputSlot.isInsertingAllowed = false
        root.add(extraOutputSlot, 6, 3)
        extraOutputSlot.setLocation((5.5 * 18).toInt(), (2.5 * 18).toInt())

        root.validate(this)
    }
}