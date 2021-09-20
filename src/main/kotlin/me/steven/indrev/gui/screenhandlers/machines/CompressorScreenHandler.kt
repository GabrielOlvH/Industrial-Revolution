package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.blockentities.crafters.CompressorBlockEntity
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.screenhandlers.COMPRESSOR_HANDLER
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.widgets.machines.WCustomBar
import me.steven.indrev.gui.widgets.machines.processBar
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.Identifier

class CompressorScreenHandler(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiScreenHandler(
        COMPRESSOR_HANDLER,
        syncId,
        playerInventory,
        ctx
    ), PatchouliEntryShortcut {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.compressor", ctx, playerInventory, blockInventory)

        val inputSlot = WItemSlot.of(blockInventory, 2)
        root.add(inputSlot, 3.3, 1.8)

        val processWidget = query<CompressorBlockEntity, WCustomBar> { be -> processBar(be, CompressorBlockEntity.CRAFTING_COMPONENT_ID) }
        root.add(processWidget, 4.45, 1.8)

        val outputSlot = WItemSlot.outputOf(blockInventory, 3)
        outputSlot.isInsertingAllowed = false
        root.add(outputSlot, 5.94, 1.8)

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    override fun getEntry(): Identifier = identifier("machines/basic_machines")

    override fun getPage(): Int = 3

    companion object {
        val SCREEN_ID = identifier("compressor_screen")
    }
}