package me.steven.indrev.utils

import io.github.cottonmc.cotton.gui.SyncedGuiDescription
import io.github.cottonmc.cotton.gui.client.BackgroundPainter
import io.github.cottonmc.cotton.gui.client.ScreenDrawing
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder
import io.github.cottonmc.cotton.gui.widget.WButton
import io.github.cottonmc.cotton.gui.widget.WGridPanel
import io.github.cottonmc.cotton.gui.widget.WWidget
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment
import io.github.cottonmc.cotton.gui.widget.icon.Icon
import me.steven.indrev.blockentities.MachineBlockEntity
import me.steven.indrev.blockentities.crafters.UpgradeProvider
import me.steven.indrev.blockentities.farms.AOEMachineBlockEntity
import me.steven.indrev.gui.PatchouliEntryShortcut
import me.steven.indrev.gui.widgets.machines.WEnergy
import me.steven.indrev.gui.widgets.machines.WTemperature
import me.steven.indrev.gui.widgets.misc.WBookEntryShortcut
import me.steven.indrev.gui.widgets.misc.WText
import me.steven.indrev.gui.widgets.misc.WTip
import me.steven.indrev.gui.widgets.misc.WTooltipedItemSlot
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.inventory.Inventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.screen.PropertyDelegate
import net.minecraft.screen.ScreenHandlerContext
import net.minecraft.text.TranslatableText
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry
import vazkii.patchouli.client.book.ClientBookRegistry

fun WGridPanel.add(w: WWidget, x: Double, y: Double, width: Double, height: Double) {
    this.add(w, x.toInt(), y.toInt(), width.toInt(), height.toInt())
    w.setLocation((x * 18).toInt(), (y * 18).toInt())
}

fun WGridPanel.add(w: WWidget, x: Double, y: Double) {
    this.add(w, x.toInt(), y.toInt())
    w.setLocation((x * 18).toInt(), (y * 18).toInt())
}

fun SyncedGuiDescription.configure(
    titleId: String,
    screenHandlerContext: ScreenHandlerContext,
    playerInventory: PlayerInventory,
    blockInventory: Inventory,
    propertyDelegate: PropertyDelegate
) {
    (rootPanel as WGridPanel).also { panel ->
        panel.setSize(150, 120)
        panel.add(createPlayerInventoryPanel(), 0, 5)
        panel.add(WText(TranslatableText(titleId), HorizontalAlignment.CENTER, 0x404040), 4.3, 0.0)

        val energyWidget = WEnergy(screenHandlerContext)
        panel.add(energyWidget, 0.1, 0.0)

        val batterySlot = WTooltipedItemSlot.of(blockInventory, 0, TranslatableText("gui.indrev.battery_slot_type"))

        screenHandlerContext.run { world, blockPos ->
            if (world.isClient)
                batterySlot.backgroundPainter = getEnergySlotPainter(blockInventory, 0)
            panel.add(batterySlot, 0.0, 3.7)
            panel.add(WTip(world.random), -1, -1)
            val blockEntity = world.getBlockEntity(blockPos)
            if (blockEntity is MachineBlockEntity<*> && blockEntity is UpgradeProvider) {
                for ((i, slot) in blockEntity.getUpgradeSlots().withIndex()) {
                    val s = WTooltipedItemSlot.of(blockInventory, slot, TranslatableText("gui.indrev.upgrade_slot_type"))
                    if (world.isClient)
                        s.backgroundPainter = if (blockEntity.isLocked(slot, blockEntity.tier)) getLockedSlotPainter(blockInventory, slot) else getUpgradeSlotPainter(blockInventory, slot)
                    panel.add(s, 8, i)
                }
            }
            if (blockEntity is MachineBlockEntity<*> && blockEntity.temperatureComponent != null) {
                val controller = blockEntity.temperatureComponent!!
                panel.add(WTemperature(propertyDelegate, controller), 1.1, 0.0)
                val coolerSlot = WTooltipedItemSlot.of(blockInventory, 1, TranslatableText("gui.indrev.cooler_slot_type"))
                if (world.isClient)
                    coolerSlot.backgroundPainter = getCoolerSlotPainter(blockInventory, 1)
                panel.add(coolerSlot, 1.0, 3.7)
            }
            if (blockEntity is AOEMachineBlockEntity<*>) {
                val button = object : WButton() {
                    override fun addTooltip(information: TooltipBuilder?) {
                        information?.add(TranslatableText("block.indrev.aoe.toggle.${blockEntity.renderWorkingArea}"))
                    }
                }
                button.setSize(20, 20)
                button.setOnClick {
                    blockEntity.renderWorkingArea = !blockEntity.renderWorkingArea
                }
                button.icon = Icon { _, x, y, _ ->
                    ScreenDrawing.texturedRect(x + 1, y + 1, 16, 16, identifier("textures/gui/range_icon.png"), -1)
                }
                panel.add(button, 7.95, 4.2)
                button.setSize(20, 20)
            }
        }
        if (this is PatchouliEntryShortcut) {
            addBookEntryShortcut(playerInventory, panel, -1.4, -0.47)
        }
    }
}

fun PatchouliEntryShortcut.addBookEntryShortcut(playerInventory: PlayerInventory, panel: WGridPanel, x: Double, y: Double): WButton {
    val containsBook =
        playerInventory.contains(ItemStack(Registry.ITEM[Identifier("patchouli:guide_book")]).also { stack ->
            stack.tag = CompoundTag().also { it.putString("patchouli:book", "indrev:indrev") }
        })
    val button = object : WBookEntryShortcut() {
        override fun addTooltip(tooltip: TooltipBuilder?) {
            if (containsBook)
                tooltip?.add(
                    TranslatableText("gui.indrev.guide_book_shortcut.contains").formatted(
                        Formatting.BLUE,
                        Formatting.ITALIC
                    )
                )
            else
                tooltip?.add(
                    TranslatableText("gui.indrev.guide_book_shortcut.missing").formatted(
                        Formatting.RED,
                        Formatting.ITALIC
                    )
                )
        }

        override fun isWithinBounds(x: Int, y: Int): Boolean =
            x < this.width && y < this.height
    }
    if (containsBook) {
        button.setOnClick {
            ClientBookRegistry.INSTANCE.displayBookGui(
                Identifier("indrev:indrev"),
                this.getEntry(),
                this.getPage()
            )
        }
    }
    panel.add(button, x, y)
    button.setSize(24, 24)
    return button
}

val POWER_ICON_ID = identifier("textures/gui/power_icon.png")

fun getEnergySlotPainter(inventory: Inventory, slot: Int) = BackgroundPainter { left, top, widget ->
    BackgroundPainter.SLOT.paintBackground(left, top, widget)
    if (inventory.getStack(slot).isEmpty)
        ScreenDrawing.texturedRect(left, top, 18, 18, POWER_ICON_ID, -1)
}

val VENT_ICON_ID = identifier("textures/gui/vent_icon.png")

fun getCoolerSlotPainter(inventory: Inventory, slot: Int) = BackgroundPainter { left, top, widget ->
    BackgroundPainter.SLOT.paintBackground(left, top, widget)
    if (inventory.getStack(slot).isEmpty)
        ScreenDrawing.texturedRect(left, top, 18, 18, VENT_ICON_ID, -1)
}

val UPGRADE_ICON_ID = identifier("textures/gui/upgrade_icon.png")

fun getUpgradeSlotPainter(inventory: Inventory, slot: Int) = BackgroundPainter { left, top, widget ->
    BackgroundPainter.SLOT.paintBackground(left, top, widget)
    if (inventory.getStack(slot).isEmpty)
        ScreenDrawing.texturedRect(left, top, 18, 18, UPGRADE_ICON_ID, -1)
}

val LOCKED_ICON_ID = identifier("textures/gui/locked_icon.png")

fun getLockedSlotPainter(inventory: Inventory, slot: Int) = BackgroundPainter { left, top, widget ->
    BackgroundPainter.SLOT.paintBackground(left, top, widget)
    if (inventory.getStack(slot).isEmpty)
        ScreenDrawing.texturedRect(left, top, 18, 18, LOCKED_ICON_ID, -1)
}