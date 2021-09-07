package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.blockentities.crafters.CondenserBlockEntity
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.screenhandlers.CONDENSER_HANDLER
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.widgets.machines.fluidTank
import me.steven.indrev.gui.widgets.machines.processBar
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.Identifier

class CondenserScreenHandler(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiScreenHandler(
        CONDENSER_HANDLER,
        syncId,
        playerInventory,
        ctx
    ), PatchouliEntryShortcut {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.condenser", ctx, playerInventory, blockInventory)

        withBlockEntity<CondenserBlockEntity> { be ->
            val fluid = fluidTank(be, CondenserBlockEntity.INPUT_TANK_ID)
            root.add(fluid, 2.8, 1.0)

            val processWidget = processBar(be, CondenserBlockEntity.CRAFTING_COMPONENT_ID)
            root.add(processWidget, 4.0, 1.8)
        }

        val outputSlot = WItemSlot.outputOf(blockInventory, 2)
        root.add(outputSlot, 5.7, 1.8)

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    override fun getEntry(): Identifier = identifier("machines/basic_machines")

    override fun getPage(): Int = 6

    companion object {
        val SCREEN_ID = identifier("condenser")
    }
}