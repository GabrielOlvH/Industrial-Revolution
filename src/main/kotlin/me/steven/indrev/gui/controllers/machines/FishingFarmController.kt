package me.steven.indrev.gui.controllers.machines

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.gui.controllers.IRGuiController
import me.steven.indrev.gui.widgets.misc.WTooltipedItemSlot
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.TranslatableText

class FishingFarmController(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiController(
        IndustrialRevolution.FISHING_FARM_HANDLER,
        syncId,
        playerInventory,
        ctx
    ) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.fishing_farm", ctx, playerInventory, blockInventory, propertyDelegate)

        root.add(
            WItemSlot.of(blockInventory, (blockInventory as IRInventory).outputSlots.first(), 2, 2),
            3.5,
            0.7
        )

        root.add(WTooltipedItemSlot.of(blockInventory, 1, TranslatableText("gui.indrev.fishingrod")), 4.0, 3.0)

        root.validate(this)
    }

    companion object {
        val SCREEN_ID = identifier("fishing_farm_screen")
    }
}