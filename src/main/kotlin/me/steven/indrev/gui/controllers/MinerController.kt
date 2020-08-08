package me.steven.indrev.gui.controllers

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.gui.widgets.misc.WTooltipedItemSlot
import me.steven.indrev.inventories.IRInventory
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

class MinerController(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    SyncedGuiDescription(
        IndustrialRevolution.MINER_HANDLER,
        syncId,
        playerInventory,
        getBlockInventory(ctx),
        getBlockPropertyDelegate(ctx)
    ), PatchouliEntryShortcut {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.miner", ctx, playerInventory, blockInventory, propertyDelegate)

        root.add(
            WTooltipedItemSlot.of(
                blockInventory, (blockInventory as IRInventory).outputSlots.first(), 3, 3, mutableListOf(
                TranslatableText("gui.indrev.output_slot_type").formatted(
                    Formatting.BLUE, Formatting.ITALIC
                )
            )
            ).also { it.isInsertingAllowed = false },
            3.0,
            0.7
        )

        root.add(WText({
            TranslatableText("block.indrev.miner.mined", "${propertyDelegate[3]}%")
        }, HorizontalAlignment.CENTER), 4.0, 3.9)
        root.add(
            WTooltipedItemSlot.of(
                blockInventory,
                14,
                mutableListOf(
                    TranslatableText("gui.indrev.scan_output_slot_type").formatted(
                        Formatting.BLUE,
                        Formatting.ITALIC
                    )
                )
            ), 4.0, 4.4
        )

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    override fun getEntry(): Identifier = identifier("machines/miner")

    override fun getPage(): Int = 0

    companion object {
        val SCREEN_ID = identifier("miner")
    }
}