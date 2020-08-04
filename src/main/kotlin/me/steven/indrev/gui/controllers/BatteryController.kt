package me.steven.indrev.gui.controllers

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.widgets.machines.EnergyWidget
import me.steven.indrev.utils.addBookEntryShortcut
import me.steven.indrev.utils.identifier
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.util.Identifier

class BatteryController(syncId: Int, playerInventory: PlayerInventory, ctx: ScreenHandlerContext) :
    SyncedGuiDescription(
        IndustrialRevolution.BATTERY_HANDLER,
        syncId,
        playerInventory,
        getBlockInventory(ctx),
        getBlockPropertyDelegate(ctx)
    ), PatchouliEntryShortcut {
    init {
        val root = WGridPanel()
        setRootPanel(root)
        root.setSize(150, 120)

        root.add(EnergyWidget(ctx), 0, 0, 16, 64)

        root.add(WItemSlot.of(blockInventory, 0), 4, 2)

        root.add(createPlayerInventoryPanel(), 0, 4)

        addBookEntryShortcut(playerInventory, root, 7, 0)

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    override fun getEntry(): Identifier = identifier("machines/batteries")

    override fun getPage(): Int = 0

    companion object {
        val SCREEN_ID = identifier("battery_screen")
    }
}