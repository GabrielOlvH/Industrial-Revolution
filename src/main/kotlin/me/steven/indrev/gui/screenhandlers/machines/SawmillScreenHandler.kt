package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.blockentities.crafters.SawmillBlockEntity
import me.steven.indrev.blockentities.crafters.SolidInfuserBlockEntity
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.screenhandlers.SAWMILL_HANDLER
import me.steven.indrev.gui.widgets.machines.WCustomBar
import me.steven.indrev.gui.widgets.machines.processBar
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.createProcessBar
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext

class SawmillScreenHandler(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiScreenHandler(
        SAWMILL_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {

    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.sawmill", ctx, playerInventory, blockInventory)

        val inputSlot = WItemSlot.of(blockInventory, 2)
        root.add(inputSlot, 3.0, 1.8)

        val processWidget = query<SawmillBlockEntity, WCustomBar> { be -> processBar(be, SawmillBlockEntity.CRAFTING_COMPONENT_ID) }
        root.add(processWidget, 4.15, 1.8)

        val outputSlots = WItemSlot.of(blockInventory, 3, 2, 2)
        outputSlots.isInsertingAllowed = false
        root.add(outputSlots, 5.5, 1.3)

        root.validate(this)
    }

    companion object {
        val SCREEN_ID = identifier("sawmill_screen")
    }
}