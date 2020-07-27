package me.steven.indrev.gui.controllers

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.gui.widgets.VerticalProcessWidget
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext

class ModularWorkbenchController(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    SyncedGuiDescription(
        IndustrialRevolution.MODULAR_WORKBENCH_HANDLER,
        syncId,
        playerInventory,
        getBlockInventory(ctx),
        getBlockPropertyDelegate(ctx)
    ) {

    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.modular_workbench", ctx, playerInventory, blockInventory, propertyDelegate)

        val armorSlot = WItemSlot.of(blockInventory, 2)
        root.add(armorSlot, 4.0, 3.5)

        val moduleSlot = WItemSlot.of(blockInventory, 1)
        root.add(moduleSlot, 4, 1)

        val process = VerticalProcessWidget(propertyDelegate)
        root.add(process, 4, 2)

        root.validate(this)
    }

    companion object {
        val SCREEN_ID = identifier("modular_workbench_screen")
    }
}