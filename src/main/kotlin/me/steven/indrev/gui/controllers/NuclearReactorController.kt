package me.steven.indrev.gui.controllers

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext

class NuclearReactorController(
    syncId: Int,
    playerInventory: PlayerInventory,
    screenHandlerContext: ScreenHandlerContext
) :
    SyncedGuiDescription(
        IndustrialRevolution.NUCLEAR_REACTOR_HANDLER,
        syncId,
        playerInventory,
        getBlockInventory(screenHandlerContext),
        getBlockPropertyDelegate(screenHandlerContext)
    ) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.nuclear_reactor", screenHandlerContext, blockInventory, propertyDelegate)

        root.add(WItemSlot.of(blockInventory, (blockInventory as DefaultSidedInventory).inputSlots.first(), 3, 3), 4, 1)

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    companion object {
        val SCREEN_ID = identifier("nuclear_reactor")
    }
}