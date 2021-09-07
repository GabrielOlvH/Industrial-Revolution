package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.blockentities.solarpowerplant.DistillerBlockEntity
import me.steven.indrev.gui.screenhandlers.DISTILLER_HANDLER
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.widgets.machines.fluidTank
import me.steven.indrev.gui.widgets.machines.processBar
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext

class DistillerScreenHandler(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiScreenHandler(
        DISTILLER_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.distiller", ctx, playerInventory, blockInventory)

        withBlockEntity<DistillerBlockEntity> { be ->
            val fluid = fluidTank(be, DistillerBlockEntity.TANK_ID)
            root.add(fluid, 2.8, 0.7)

            val processWidget = processBar(be, DistillerBlockEntity.CRAFTING_COMPONENT_ID)
            root.add(processWidget, 4.0, 2.2)

        }

        val outputSlot = WItemSlot.outputOf(blockInventory, 2)
        root.add(outputSlot, 5.7, 2.2)

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    companion object {
        val SCREEN_ID = identifier("distiller")
    }
}