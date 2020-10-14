package me.steven.indrev.gui.controllers.machines

import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.controllers.IRGuiController
import me.steven.indrev.gui.widgets.misc.WTooltipedItemSlot
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import me.steven.indrev.utils.setIcon
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.TranslatableText
import net.minecraft.util.Identifier

class FishingFarmController(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    IRGuiController(
        IndustrialRevolution.FISHING_FARM_HANDLER,
        syncId,
        playerInventory,
        ctx
    ), PatchouliEntryShortcut {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.fishing_farm", ctx, playerInventory, blockInventory, propertyDelegate)

        root.add(
            WItemSlot.of(blockInventory, (blockInventory as IRInventory).outputSlots.first(), 2, 2),
            3.95,
            0.7
        )

        val fishingRodSlot = WTooltipedItemSlot.of(blockInventory, 1, TranslatableText("gui.indrev.fishingrod"))
        fishingRodSlot.setIcon(ctx, blockInventory, 1, FISHING_ROD_ICON)
        root.add(fishingRodSlot, 4.45, 3.5)

        root.validate(this)
    }

    override fun getEntry(): Identifier = identifier("machines/fisher")

    override fun getPage(): Int = 0

    companion object {
        val SCREEN_ID = identifier("fishing_farm_screen")
        val FISHING_ROD_ICON = identifier("textures/gui/fishing_rod_icon.png")
    }
}