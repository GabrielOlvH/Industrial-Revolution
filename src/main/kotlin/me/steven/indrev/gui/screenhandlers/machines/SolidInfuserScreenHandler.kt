package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.blockentities.crafters.SolidInfuserBlockEntity
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.screenhandlers.SOLID_INFUSER_HANDLER
import me.steven.indrev.gui.widgets.machines.WCustomBar
import me.steven.indrev.gui.widgets.machines.processBar
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext

class SolidInfuserScreenHandler(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiScreenHandler(
        SOLID_INFUSER_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.solid_infuser", ctx, playerInventory, blockInventory)

        val firstInput = WItemSlot.of(blockInventory, 2)
        root.add(firstInput, 2.9, 1.8)

        val secondInput = WItemSlot.of(blockInventory, 3)
        root.add(secondInput, 4.0, 1.8)

        val processWidget = query<SolidInfuserBlockEntity, WCustomBar> { be -> processBar(be, SolidInfuserBlockEntity.CRAFTING_COMPONENT_ID) }
        root.add(processWidget, 5.15, 1.8)

        val outputSlot = WItemSlot.outputOf(blockInventory, 4)
        outputSlot.isInsertingAllowed = false
        root.add(outputSlot, 6.6, 1.8)

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    companion object {
        val SCREEN_ID = identifier("infuser")
    }
}