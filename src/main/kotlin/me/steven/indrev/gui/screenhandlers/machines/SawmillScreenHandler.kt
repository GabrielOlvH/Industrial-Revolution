package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.createProcessBar
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext

class SawmillScreenHandler(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiScreenHandler(
        IndustrialRevolution.SAWMILL_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {

    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.sawmill", ctx, playerInventory, blockInventory)

        val inputSlot = WItemSlot.of(blockInventory, 2)
        root.add(inputSlot, 3.2, 2.0)

        val processWidget = createProcessBar()
        root.add(processWidget, 4.35, 2.0)

        val outputSlots = WItemSlot.of(blockInventory, 3, 2, 2)
        outputSlots.isInsertingAllowed = false
        root.add(outputSlots, 5.7, 1.5)

        root.validate(this)
    }

    companion object {
        val SCREEN_ID = identifier("sawmill_screen")
    }
}