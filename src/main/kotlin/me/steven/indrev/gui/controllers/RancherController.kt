package me.steven.indrev.gui.controllers

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext

class RancherController(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    SyncedGuiDescription(
        IndustrialRevolution.RANCHER_HANDLER,
        syncId,
        playerInventory,
        getBlockInventory(ctx),
        getBlockPropertyDelegate(ctx)
    ) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.rancher", ctx, blockInventory, propertyDelegate)

        root.add(
            WItemSlot.of(blockInventory, (blockInventory as IRInventory).outputSlots.first(), 3, 3),
            4.8,
            1.0
        )
        root.add(
            WItemSlot.of(blockInventory, (blockInventory as IRInventory).inputSlots.first(), 2, 2),
            2.4,
            1.5
        )

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    companion object {
        val SCREEN_ID = identifier("rancher_screen")
    }
}