package me.steven.indrev.gui.controllers

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.gui.widgets.ProcessWidget
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext

class CompressorController(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    SyncedGuiDescription(
        IndustrialRevolution.COMPRESSOR_HANDLER,
        syncId,
        playerInventory,
        getBlockInventory(ctx),
        getBlockPropertyDelegate(ctx)
    ) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.compressor", ctx, blockInventory, propertyDelegate)

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
        val SCREEN_ID = identifier("compressor_screen")
    }
}