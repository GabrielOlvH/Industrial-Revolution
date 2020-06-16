package me.steven.indrev.gui.controllers

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.gui.widgets.ProcessWidget
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext

class ElectricFurnaceController(
    syncId: Int,
    playerInventory: PlayerInventory,
    screenHandlerContext: ScreenHandlerContext
) :
    SyncedGuiDescription(
        syncId,
        playerInventory,
        getBlockInventory(screenHandlerContext),
        getBlockPropertyDelegate(screenHandlerContext)
    ) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.electric_furnace", screenHandlerContext, blockInventory, propertyDelegate)

        val batterySlot = WItemSlot.of(blockInventory, 0)
        root.add(batterySlot, 0.0, 3.7)

        val inputSlot = WItemSlot.of(blockInventory, 2)
        root.add(inputSlot, 2.3, 1.5)

        val processWidget = ProcessWidget(propertyDelegate)
        root.add(processWidget, 3.5, 1.5)

        val outputSlot = WItemSlot.outputOf(blockInventory, 3)
        outputSlot.isInsertingAllowed = false
        root.add(outputSlot, 5.5, 1.5)

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    companion object {
        val SCREEN_ID = identifier("electric_furnace_screen")
    }
}