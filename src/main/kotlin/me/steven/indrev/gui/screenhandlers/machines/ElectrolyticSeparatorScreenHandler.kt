package me.steven.indrev.gui.screenhandlers.machines

import io.github.cottonmc.cotton.gui.widget.WBar
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.screenhandlers.ELECTROLYTIC_SEPARATOR_HANDLER
import me.steven.indrev.gui.screenhandlers.IRGuiScreenHandler
import me.steven.indrev.gui.widgets.machines.WFluid
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.createProcessBar
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.Identifier

class ElectrolyticSeparatorScreenHandler(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiScreenHandler(
        ELECTROLYTIC_SEPARATOR_HANDLER,
        syncId,
        playerInventory,
        ctx
    ), PatchouliEntryShortcut {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.electrolytic_separator", ctx, playerInventory, blockInventory)

        val fluid = WFluid(ctx, 0)
        root.add(fluid, 5.0, 1.0)

        val processWidget = createProcessBar()
        root.add(processWidget, 6.2, 1.8)

        val leftProcessWidget = createProcessBar(WBar.Direction.LEFT, EMPTY_BAR, FULL_BAR)
        root.add(leftProcessWidget, 3.7, 1.8)

        val firstOutputFluid = WFluid(ctx, 1)
        root.add(firstOutputFluid, 2.5, 1.0)

        val secondOutputFluid = WFluid(ctx, 2)
        root.add(secondOutputFluid, 7.5, 1.0)

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    override fun getEntry(): Identifier = identifier("machines/basic_machines")

    override fun getPage(): Int = 5

    companion object {
        val SCREEN_ID = identifier("electrolytic_separator")
        val EMPTY_BAR = identifier("textures/gui/widget_processing_empty_left.png")
        val FULL_BAR = identifier("textures/gui/widget_processing_full_left.png")
    }
}