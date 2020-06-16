package me.steven.indrev.gui.controllers

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import io.github.cottonmc.cotton.gui.widget.data.Alignment
import me.steven.indrev.gui.widgets.StringWidget
import me.steven.indrev.inventories.DefaultSidedInventory
import me.steven.indrev.utils.add
import me.steven.indrev.utils.configure
import me.steven.indrev.utils.identifier
import me.steven.indrev.world.chunkveins.ChunkVeinType
import net.minecraft.client.resource.language.I18n
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext

class MinerController(syncId: Int, playerInventory: PlayerInventory, screenHandlerContext: ScreenHandlerContext) :
    SyncedGuiDescription(
        syncId,
        playerInventory,
        getBlockInventory(screenHandlerContext),
        getBlockPropertyDelegate(screenHandlerContext)
    ) {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        configure("block.indrev.miner", screenHandlerContext, blockInventory, propertyDelegate)

        root.add(
            WItemSlot.of(blockInventory, (blockInventory as DefaultSidedInventory).outputSlots.first(), 3, 3),
            3,
            1
        )

        root.add(StringWidget({
            val typeId = propertyDelegate[3]
            val type = if (typeId >= 0) ChunkVeinType.values()[typeId] else null
            I18n.translate("block.indrev.miner.gui1", type)
        }, titleColor, Alignment.LEFT), 3.0, 4.2)
        root.add(StringWidget({
            I18n.translate("block.indrev.miner.gui2", "${propertyDelegate[4]}%")
        }, titleColor, Alignment.LEFT), 3.0, 4.7)

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    companion object {
        val SCREEN_ID = identifier("miner")
    }
}