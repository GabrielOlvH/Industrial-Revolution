package me.steven.indrev.gui.controllers

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WItemSlot
import me.steven.indrev.IndustrialRevolution
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.widgets.machines.WEnergy
import me.steven.indrev.utils.add
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

    val shieldPainter = getSlotPainter(40, Identifier("minecraft", "textures/item/empty_armor_slot_shield.png"))
    val helmetPainter = getSlotPainter(39, Identifier("minecraft", "textures/item/empty_armor_slot_helmet.png"))
    val chestplatePainter = getSlotPainter(38, Identifier("minecraft", "textures/item/empty_armor_slot_chestplate.png"))
    val leggingsPainter = getSlotPainter(37, Identifier("minecraft", "textures/item/empty_armor_slot_leggings.png"))
    val bootsPainter = getSlotPainter(36, Identifier("minecraft", "textures/item/empty_armor_slot_boots.png"))

    init {
        val root = WGridPanel()
        setRootPanel(root)
        root.setSize(150, 120)

        root.add(WEnergy(ctx), 0, 0, 16, 64)

        root.add(WItemSlot.of(blockInventory, 0), 4, 2)

        root.add(createPlayerInventoryPanel(), 0.0, 4.2)

        val boots = WItemSlot.of(playerInventory, 36)
        boots.backgroundPainter = bootsPainter
        root.add(boots, 1, 3)

        val leggings = WItemSlot.of(playerInventory, 37)
        leggings.backgroundPainter = leggingsPainter
        root.add(leggings, 1, 2)

        val chestplate = WItemSlot.of(playerInventory, 38)
        chestplate.backgroundPainter = chestplatePainter
        root.add(chestplate, 1, 1)

        val helmet = WItemSlot.of(playerInventory, 39)
        helmet.backgroundPainter = helmetPainter
        root.add(helmet, 1, 0)

        val shield = WItemSlot.of(playerInventory, 40)
        shield.backgroundPainter = shieldPainter
        root.add(shield, 2.2, 3.0)

        addBookEntryShortcut(playerInventory, root, -1.4, -0.47)

        root.validate(this)
    }

    override fun canUse(player: PlayerEntity?): Boolean = true

    override fun getEntry(): Identifier = identifier("machines/batteries")

    override fun getPage(): Int = 0

    private fun getSlotPainter(slot: Int, identifier: Identifier) = BackgroundPainter { left, top, panel ->
        BackgroundPainter.SLOT.paintBackground(left, top, panel)
        if (playerInventory.getStack(slot).isEmpty)
            ScreenDrawing.texturedRect(left + 1, top + 1, 16, 16, identifier, -1)
    }

    companion object {
        val SCREEN_ID = identifier("battery_screen")
    }
}